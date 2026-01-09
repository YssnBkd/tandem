package org.epoque.tandem.presentation.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.GoalRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.presentation.planning.preferences.PlanningProgress
import org.epoque.tandem.presentation.planning.preferences.PlanningProgressState
import org.epoque.tandem.presentation.week.model.TaskUiModel
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel for the Weekly Planning wizard.
 *
 * Follows Android best practices:
 * - Screen-level state holder
 * - StateFlow for reactive UI state
 * - Channel for one-time side effects
 * - viewModelScope for lifecycle-aware coroutines
 * - Unidirectional data flow (UDF) pattern
 */
class PlanningViewModel(
    private val taskRepository: TaskRepository,
    private val weekRepository: WeekRepository,
    private val authRepository: AuthRepository,
    private val goalRepository: GoalRepository,
    private val planningProgress: PlanningProgress
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanningUiState())
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<PlanningSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<PlanningSideEffect> = _sideEffects.receiveAsFlow()

    // Stored properties from init for event handlers
    private var currentUserId: String? = null
    private var currentWeek: Week? = null

    init {
        loadInitialData()
    }

    /**
     * Handle user events from the UI.
     * Following UDF pattern: events go UP to ViewModel.
     */
    fun onEvent(event: PlanningEvent) {
        when (event) {
            is PlanningEvent.RolloverTaskAdded -> handleRolloverTaskAdded(event.taskId)
            is PlanningEvent.RolloverTaskSkipped -> handleRolloverTaskSkipped(event.taskId)
            is PlanningEvent.RolloverStepComplete -> handleRolloverStepComplete()
            is PlanningEvent.NewTaskTextChanged -> handleNewTaskTextChanged(event.text)
            is PlanningEvent.NewTaskSubmitted -> handleNewTaskSubmitted()
            is PlanningEvent.DoneAddingTasks -> handleDoneAddingTasks()
            is PlanningEvent.GoalSuggestionSelected -> handleGoalSuggestionSelected(event.goalId)
            is PlanningEvent.ClearSelectedGoal -> handleClearSelectedGoal()
            is PlanningEvent.PartnerRequestAccepted -> handlePartnerRequestAccepted(event.taskId)
            is PlanningEvent.PartnerRequestDiscussed -> handlePartnerRequestDiscussed(event.taskId)
            is PlanningEvent.PartnerRequestsStepComplete -> handlePartnerRequestsStepComplete()
            is PlanningEvent.BackPressed -> handleBackPressed()
            is PlanningEvent.ExitRequested -> handleExitRequested()
            is PlanningEvent.PlanningCompleted -> handlePlanningCompleted()
        }
    }

    /**
     * Load initial planning data.
     * CRITICAL: Follows exact order specified in contracts/planning-operations.md
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // 1. Wait for authentication before any repository calls
                val authState = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                val userId = authState.user.id
                currentUserId = userId

                // 2. Ensure current week exists and store as ViewModel property
                val week = weekRepository.getOrCreateCurrentWeek(userId)
                currentWeek = week

                // 3. Check for saved progress - discard if stale
                val savedProgress = planningProgress.planningProgress.first()
                if (savedProgress.weekId != week.id) {
                    planningProgress.clearProgress()
                }

                // 4. Calculate previous week ID for rollover candidates
                val previousWeekId = weekRepository.getPreviousWeekId(week.id)

                // 5. Query rollover candidates from previous week
                val rolloverTasks = taskRepository
                    .observeIncompleteTasksForWeek(previousWeekId, userId)
                    .first()

                // 6. Query partner requests (PENDING_ACCEPTANCE status)
                val partnerRequests = taskRepository
                    .observeTasksByStatus(TaskStatus.PENDING_ACCEPTANCE, userId)
                    .first()

                // 7. Load goal suggestions for Add Tasks step (Feature 007)
                val goalSuggestions = goalRepository.getActiveGoalsForSuggestions(userId)

                // 8. Determine initial step (skip steps with no data)
                val initialStep = getInitialStep(rolloverTasks.isNotEmpty(), partnerRequests.isNotEmpty())

                // 9. Initialize UI state with all collected data
                _uiState.update {
                    it.copy(
                        currentStep = initialStep,
                        currentWeek = week,
                        rolloverTasks = rolloverTasks.map { task ->
                            TaskUiModel.fromTask(task, userId, null)
                        },
                        partnerRequests = partnerRequests.map { task ->
                            TaskUiModel.fromTask(task, userId, null)
                        },
                        goalSuggestions = goalSuggestions,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load planning data: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STEP SKIPPING LOGIC
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Determine initial step based on available data.
     * Skip ROLLOVER if no rollover tasks, skip PARTNER_REQUESTS if no requests.
     */
    private fun getInitialStep(hasRolloverTasks: Boolean, hasPartnerRequests: Boolean): PlanningStep {
        return when {
            hasRolloverTasks -> PlanningStep.ROLLOVER
            else -> PlanningStep.ADD_TASKS
        }
    }

    /**
     * Determine next step from current step, skipping empty steps.
     */
    private fun getNextStep(currentStep: PlanningStep): PlanningStep {
        val hasPartnerRequests = _uiState.value.partnerRequests.isNotEmpty()

        return when (currentStep) {
            PlanningStep.ROLLOVER -> PlanningStep.ADD_TASKS
            PlanningStep.ADD_TASKS -> {
                if (hasPartnerRequests) {
                    PlanningStep.PARTNER_REQUESTS
                } else {
                    PlanningStep.CONFIRMATION
                }
            }
            PlanningStep.PARTNER_REQUESTS -> PlanningStep.CONFIRMATION
            PlanningStep.CONFIRMATION -> PlanningStep.CONFIRMATION // Already at end
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ROLLOVER STEP EVENT HANDLERS (Phase 4 - US2)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleRolloverTaskAdded(taskId: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                val currentWeekId = weekRepository.getCurrentWeekId()
                val originalTask = taskRepository.getTaskById(taskId) ?: return@launch

                // Create new task with rollover reference
                val newTask = Task(
                    id = "",
                    title = originalTask.title,
                    notes = originalTask.notes,
                    ownerId = userId,
                    ownerType = OwnerType.SELF,
                    weekId = currentWeekId,
                    status = TaskStatus.PENDING,
                    createdBy = userId,
                    requestNote = null,
                    repeatTarget = null,
                    repeatCompleted = 0,
                    linkedGoalId = originalTask.linkedGoalId,
                    reviewNote = null,
                    rolledFromWeekId = originalTask.weekId,
                    priority = originalTask.priority,
                    scheduledDate = null,
                    scheduledTime = null,
                    deadline = null,
                    parentTaskId = null,
                    labels = originalTask.labels,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )

                val createdTask = taskRepository.createTask(newTask)

                // Update state
                _uiState.update { state ->
                    state.copy(
                        currentRolloverIndex = state.currentRolloverIndex + 1,
                        rolloverTasksAdded = state.rolloverTasksAdded + 1,
                        addedTasks = state.addedTasks + TaskUiModel.fromTask(createdTask, userId, null),
                        totalTasksPlanned = state.totalTasksPlanned + 1
                    )
                }

                // Save progress
                saveProgress()

                // Trigger haptic feedback
                _sideEffects.send(PlanningSideEffect.TriggerHapticFeedback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(PlanningSideEffect.ShowSnackbar("Failed to add task: ${e.message}"))
            }
        }
    }

    private fun handleRolloverTaskSkipped(taskId: String) {
        _uiState.update { state ->
            state.copy(
                currentRolloverIndex = state.currentRolloverIndex + 1,
                processedRolloverCount = state.processedRolloverCount + 1
            )
        }
        viewModelScope.launch {
            saveProgress()
        }
    }

    private fun handleRolloverStepComplete() {
        val nextStep = getNextStep(PlanningStep.ROLLOVER)
        _uiState.update { it.copy(currentStep = nextStep) }
        viewModelScope.launch {
            saveProgress()
            _sideEffects.send(PlanningSideEffect.NavigateToStep(nextStep))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADD TASKS STEP EVENT HANDLERS (Phase 5 - US3)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleNewTaskTextChanged(text: String) {
        _uiState.update { it.copy(newTaskText = text, newTaskError = null) }
    }

    private fun handleNewTaskSubmitted() {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                val currentWeekId = weekRepository.getCurrentWeekId()
                val state = _uiState.value
                val title = state.newTaskText.trim()
                val selectedGoal = state.selectedGoalForNewTask

                if (title.isEmpty()) {
                    _uiState.update { it.copy(newTaskError = "Task title cannot be empty") }
                    return@launch
                }

                val newTask = Task(
                    id = "",
                    title = title,
                    notes = null,
                    ownerId = userId,
                    ownerType = OwnerType.SELF,
                    weekId = currentWeekId,
                    status = TaskStatus.PENDING,
                    createdBy = userId,
                    requestNote = null,
                    repeatTarget = null,
                    repeatCompleted = 0,
                    linkedGoalId = selectedGoal?.id,
                    reviewNote = null,
                    rolledFromWeekId = null,
                    priority = TaskPriority.P4,
                    scheduledDate = null,
                    scheduledTime = null,
                    deadline = null,
                    parentTaskId = null,
                    labels = emptyList(),
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )

                val createdTask = taskRepository.createTask(newTask)

                _uiState.update { uiState ->
                    uiState.copy(
                        newTaskText = "",
                        newTaskError = null,
                        selectedGoalForNewTask = null,
                        addedTasks = uiState.addedTasks + TaskUiModel.fromTask(createdTask, userId, null),
                        newTasksCreated = uiState.newTasksCreated + 1,
                        totalTasksPlanned = uiState.totalTasksPlanned + 1
                    )
                }

                saveProgress()
                _sideEffects.send(PlanningSideEffect.ClearFocus)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(PlanningSideEffect.ShowSnackbar("Failed to create task: ${e.message}"))
            }
        }
    }

    private fun handleDoneAddingTasks() {
        val nextStep = getNextStep(PlanningStep.ADD_TASKS)
        _uiState.update { it.copy(currentStep = nextStep) }
        viewModelScope.launch {
            saveProgress()
            _sideEffects.send(PlanningSideEffect.NavigateToStep(nextStep))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GOAL SUGGESTION EVENT HANDLERS (Feature 007: Goals System)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleGoalSuggestionSelected(goalId: String) {
        val goal = _uiState.value.goalSuggestions.find { it.id == goalId }
        if (goal != null) {
            _uiState.update { it.copy(selectedGoalForNewTask = goal) }
        }
    }

    private fun handleClearSelectedGoal() {
        _uiState.update { it.copy(selectedGoalForNewTask = null) }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PARTNER REQUESTS STEP EVENT HANDLERS (Phase 6 - US4)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handlePartnerRequestAccepted(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskStatus(taskId, TaskStatus.PENDING)

                _uiState.update { state ->
                    state.copy(
                        currentRequestIndex = state.currentRequestIndex + 1,
                        partnerRequestsAccepted = state.partnerRequestsAccepted + 1,
                        totalTasksPlanned = state.totalTasksPlanned + 1
                    )
                }

                saveProgress()
                _sideEffects.send(PlanningSideEffect.TriggerHapticFeedback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(PlanningSideEffect.ShowSnackbar("Failed to accept request: ${e.message}"))
            }
        }
    }

    private fun handlePartnerRequestDiscussed(taskId: String) {
        viewModelScope.launch {
            _sideEffects.send(PlanningSideEffect.ShowSnackbar("Discuss feature coming soon"))

            _uiState.update { state ->
                state.copy(
                    currentRequestIndex = state.currentRequestIndex + 1,
                    processedRequestCount = state.processedRequestCount + 1
                )
            }

            saveProgress()
        }
    }

    private fun handlePartnerRequestsStepComplete() {
        _uiState.update { it.copy(currentStep = PlanningStep.CONFIRMATION) }
        viewModelScope.launch {
            saveProgress()
            _sideEffects.send(PlanningSideEffect.NavigateToStep(PlanningStep.CONFIRMATION))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NAVIGATION EVENT HANDLERS (Phase 3 - US1)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleBackPressed() {
        viewModelScope.launch {
            _sideEffects.send(PlanningSideEffect.NavigateBack)
        }
    }

    private fun handleExitRequested() {
        viewModelScope.launch {
            saveProgress()
            _sideEffects.send(PlanningSideEffect.ExitPlanning)
        }
    }

    private fun handlePlanningCompleted() {
        viewModelScope.launch {
            try {
                val weekId = currentWeek?.id ?: return@launch

                // Mark planning as completed
                weekRepository.markPlanningCompleted(weekId)

                // Clear saved progress
                planningProgress.clearProgress()

                // Exit planning
                _sideEffects.send(PlanningSideEffect.ExitPlanning)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(PlanningSideEffect.ShowSnackbar("Failed to complete planning: ${e.message}"))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROGRESS PERSISTENCE
    // ═══════════════════════════════════════════════════════════════════════════

    private suspend fun saveProgress() {
        val state = _uiState.value
        val progressState = PlanningProgressState(
            currentStep = state.currentStep.ordinal,
            processedRolloverTaskIds = state.rolloverTasks.take(state.currentRolloverIndex).map { it.id }.toSet(),
            addedTaskIds = state.addedTasks.map { it.id }.toSet(),
            acceptedRequestIds = state.partnerRequests.take(state.currentRequestIndex).map { it.id }.toSet(),
            isInProgress = true,
            weekId = currentWeek?.id
        )
        planningProgress.saveProgress(progressState)
    }
}
