package org.epoque.tandem.presentation.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.domain.usecase.review.CalculateStreakUseCase
import org.epoque.tandem.domain.usecase.review.IsReviewWindowOpenUseCase
import org.epoque.tandem.presentation.week.model.Segment
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.presentation.week.model.WeekInfo
import org.epoque.tandem.presentation.week.preferences.SegmentPreferences

/**
 * ViewModel for the Week View screen.
 *
 * Follows Android best practices:
 * - Screen-level state holder (not reusable)
 * - StateFlow for reactive UI state
 * - Channel for one-time side effects
 * - viewModelScope for lifecycle-aware coroutines
 * - Unidirectional data flow (UDF) pattern
 * - No references to Context or lifecycle-aware APIs
 */
class WeekViewModel(
    private val taskRepository: TaskRepository,
    private val weekRepository: WeekRepository,
    private val segmentPreferences: SegmentPreferences,
    private val authRepository: AuthRepository,
    private val isReviewWindowOpenUseCase: IsReviewWindowOpenUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeekUiState())
    val uiState: StateFlow<WeekUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<WeekSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<WeekSideEffect> = _sideEffects.receiveAsFlow()

    init {
        loadInitialData()
        observeSegmentPreference()
        observeTasks()
    }

    /**
     * Handle user events from the UI.
     * Following UDF pattern: events go UP to ViewModel.
     */
    fun onEvent(event: WeekEvent) {
        when (event) {
            is WeekEvent.SegmentSelected -> handleSegmentSelected(event.segment)
            is WeekEvent.TaskCheckboxTapped -> handleTaskCheckboxTapped(event.taskId)
            is WeekEvent.QuickAddTextChanged -> handleQuickAddTextChanged(event.text)
            is WeekEvent.QuickAddSubmitted -> handleQuickAddSubmitted()
            is WeekEvent.TaskTapped -> handleTaskTapped(event.taskId)
            is WeekEvent.DetailSheetDismissed -> handleDetailSheetDismissed()
            is WeekEvent.TaskTitleChanged -> handleTaskTitleChanged(event.title)
            is WeekEvent.TaskNotesChanged -> handleTaskNotesChanged(event.notes)
            is WeekEvent.TaskSaveRequested -> handleTaskSaveRequested()
            is WeekEvent.TaskDeleteRequested -> handleTaskDeleteRequested()
            is WeekEvent.TaskDeleteConfirmed -> handleTaskDeleteConfirmed()
            is WeekEvent.TaskMarkCompleteRequested -> handleTaskMarkCompleteRequested()
            is WeekEvent.AddTaskSheetRequested -> handleAddTaskSheetRequested()
            is WeekEvent.AddTaskSheetDismissed -> handleAddTaskSheetDismissed()
            is WeekEvent.AddTaskSubmitted -> handleAddTaskSubmitted(event.title, event.notes, event.ownerType)
            is WeekEvent.RefreshRequested -> handleRefreshRequested()
            is WeekEvent.RequestTaskFromPartnerTapped -> handleRequestTaskFromPartner()
            is WeekEvent.InvitePartnerTapped -> handleInvitePartner()
        }
    }

    /**
     * Load initial week data.
     * Waits for authentication, then ensures the current week exists before observing.
     * Also checks review window status and calculates streak.
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            // Wait for authentication before creating/observing week
            val userId = authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .first()
                .let { authState ->
                    val userId = authState.user.id
                    // This creates the week if it doesn't exist
                    weekRepository.getOrCreateCurrentWeek(userId)
                    userId
                }

            // Check review window status (runs synchronously, no IO)
            val isReviewWindowOpen = isReviewWindowOpenUseCase()

            // Calculate current streak
            val streak = calculateStreakUseCase(userId)

            // Update review state
            _uiState.update { state ->
                state.copy(
                    isReviewWindowOpen = isReviewWindowOpen,
                    currentStreak = streak
                )
            }

            // Now observe the week (guaranteed to exist)
            val currentWeekId = weekRepository.getCurrentWeekId()
            weekRepository.observeWeek(currentWeekId).collect { week ->
                week?.let {
                    val weekInfo = WeekInfo.fromWeek(it, currentWeekId)
                    _uiState.update { state ->
                        state.copy(
                            weekInfo = weekInfo,
                            isPlanningComplete = it.planningCompletedAt != null,
                            isWeekReviewed = it.isReviewed
                        )
                    }
                }
            }
        }
    }

    /**
     * Observe segment preference and update state reactively.
     */
    private fun observeSegmentPreference() {
        viewModelScope.launch {
            segmentPreferences.selectedSegment.collect { segment ->
                _uiState.update { it.copy(selectedSegment = segment) }
            }
        }
    }

    /**
     * Observe tasks based on selected segment, week, and auth state.
     * Uses combined flow to react to auth, segment, and week changes.
     * Waits for authentication before observing tasks.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeTasks() {
        viewModelScope.launch {
            combine(
                authRepository.authState,
                segmentPreferences.selectedSegment,
                weekRepository.observeWeek(weekRepository.getCurrentWeekId())
            ) { authState, segment, week ->
                Triple(authState, segment, week)
            }.flatMapLatest { (authState, segment, week) ->
                // Only observe if authenticated
                val userId = (authState as? AuthState.Authenticated)?.user?.id
                    ?: return@flatMapLatest flowOf(emptyList())

                if (week == null) {
                    flowOf(emptyList())
                } else {
                    taskRepository.observeTasksByWeekAndOwnerType(
                        weekId = week.id,
                        ownerType = segment.toOwnerType(),
                        userId = userId
                    )
                }
            }.collect { tasks ->
                updateTasksInState(tasks)
            }
        }
    }

    /**
     * Update UI state with new task list.
     * Separates completed from incomplete and calculates progress.
     */
    private fun updateTasksInState(tasks: List<Task>) {
        val userId = currentUserId ?: return
        val partnerName = _uiState.value.partnerName

        val uiModels = tasks.map { task ->
            TaskUiModel.fromTask(task, userId, partnerName)
        }

        val incomplete = uiModels.filter { !it.isCompleted }
        val completed = uiModels.filter { it.isCompleted }

        _uiState.update {
            it.copy(
                tasks = uiModels,
                incompleteTasks = incomplete,
                completedTasks = completed,
                completedCount = completed.size,
                totalCount = uiModels.size,
                progressText = "${completed.size}/${uiModels.size}",
                isLoading = false
            )
        }
    }

    // Event Handlers

    private fun handleSegmentSelected(segment: Segment) {
        viewModelScope.launch {
            segmentPreferences.setSelectedSegment(segment)
            // State updates reactively via observeSegmentPreference()
        }
    }

    private fun handleTaskCheckboxTapped(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch

            if (task.isRepeating) {
                val newCount = task.repeatCompleted + 1
                taskRepository.incrementRepeatCount(taskId)

                if (newCount >= (task.repeatTarget ?: 0)) {
                    taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                }
            } else {
                val newStatus = if (task.status == TaskStatus.COMPLETED) {
                    TaskStatus.PENDING
                } else {
                    TaskStatus.COMPLETED
                }
                taskRepository.updateTaskStatus(taskId, newStatus)
            }

            _sideEffects.send(WeekSideEffect.TriggerHapticFeedback)
        }
    }

    private fun handleQuickAddTextChanged(text: String) {
        _uiState.update { it.copy(quickAddText = text, quickAddError = null) }
    }

    /**
     * Handle quick add task submission.
     * Following Android best practices:
     * - Try-catch with specific exception types
     * - Never consume CancellationException
     * - Notify view of errors via snackbar
     * @see https://developer.android.com/kotlin/coroutines/coroutines-best-practices
     */
    private fun handleQuickAddSubmitted() {
        val title = _uiState.value.quickAddText.trim()

        if (title.isEmpty()) {
            _uiState.update { it.copy(quickAddError = "Task title cannot be empty") }
            return
        }

        viewModelScope.launch {
            // Show feedback if not authenticated yet
            val userId = currentUserId
            if (userId == null) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Please wait, signing in..."))
                return@launch
            }

            val weekId = weekRepository.getCurrentWeekId()

            val ownerType = when (_uiState.value.selectedSegment) {
                Segment.YOU -> OwnerType.SELF
                Segment.SHARED -> OwnerType.SHARED
                Segment.PARTNER -> return@launch // Should not happen
            }

            val task = Task(
                id = "",  // Generated by repository
                title = title,
                notes = null,
                ownerId = userId,
                ownerType = ownerType,
                weekId = weekId,
                status = TaskStatus.PENDING,
                createdBy = userId,
                repeatTarget = null,
                repeatCompleted = 0,
                linkedGoalId = null,
                reviewNote = null,
                rolledFromWeekId = null,
                createdAt = Instant.DISTANT_PAST,  // Set by repository
                updatedAt = Instant.DISTANT_PAST   // Set by repository
            )

            try {
                taskRepository.createTask(task)
                _uiState.update { it.copy(quickAddText = "", quickAddError = null) }
                _sideEffects.send(WeekSideEffect.ClearFocus)
            } catch (e: CancellationException) {
                throw e  // Never consume CancellationException (Android best practice)
            } catch (e: IllegalArgumentException) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Invalid task: ${e.message}"))
            } catch (e: Exception) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Failed to create task"))
            }
        }
    }

    private fun handleTaskTapped(taskId: String) {
        val task = _uiState.value.tasks.find { it.id == taskId }
        _uiState.update {
            it.copy(
                selectedTaskId = taskId,
                showDetailSheet = true,
                editedTaskTitle = task?.title ?: "",
                editedTaskNotes = task?.notes ?: ""
            )
        }
    }

    private fun handleDetailSheetDismissed() {
        _uiState.update {
            it.copy(
                selectedTaskId = null,
                showDetailSheet = false,
                editedTaskTitle = "",
                editedTaskNotes = ""
            )
        }
    }

    private fun handleTaskTitleChanged(title: String) {
        _uiState.update { it.copy(editedTaskTitle = title) }
    }

    private fun handleTaskNotesChanged(notes: String) {
        _uiState.update { it.copy(editedTaskNotes = notes) }
    }

    private fun handleTaskSaveRequested() {
        viewModelScope.launch {
            val taskId = _uiState.value.selectedTaskId ?: return@launch
            val title = _uiState.value.editedTaskTitle.trim()
            val notes = _uiState.value.editedTaskNotes.trim().takeIf { it.isNotEmpty() }

            if (title.isBlank()) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Title cannot be empty"))
                return@launch
            }

            // Get current task to preserve status
            val currentTask = taskRepository.getTaskById(taskId) ?: return@launch
            taskRepository.updateTask(taskId, title, notes, currentTask.status)
            _uiState.update {
                it.copy(
                    showDetailSheet = false,
                    selectedTaskId = null,
                    editedTaskTitle = "",
                    editedTaskNotes = ""
                )
            }
            _sideEffects.send(WeekSideEffect.ShowSnackbar("Task updated"))
        }
    }

    private fun handleTaskDeleteRequested() {
        // Show confirmation dialog (UI handles this)
    }

    private fun handleTaskDeleteConfirmed() {
        val taskId = _uiState.value.selectedTaskId ?: return

        viewModelScope.launch {
            taskRepository.deleteTask(taskId)

            _uiState.update {
                it.copy(
                    selectedTaskId = null,
                    showDetailSheet = false,
                    editedTaskTitle = "",
                    editedTaskNotes = ""
                )
            }
            _sideEffects.send(WeekSideEffect.ShowSnackbar("Task deleted"))
        }
    }

    private fun handleTaskMarkCompleteRequested() {
        val taskId = _uiState.value.selectedTaskId ?: return

        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
            _uiState.update {
                it.copy(
                    showDetailSheet = false,
                    selectedTaskId = null,
                    editedTaskTitle = "",
                    editedTaskNotes = ""
                )
            }
            _sideEffects.send(WeekSideEffect.TriggerHapticFeedback)
        }
    }

    private fun handleAddTaskSheetRequested() {
        _uiState.update { it.copy(showAddTaskSheet = true) }
    }

    private fun handleAddTaskSheetDismissed() {
        _uiState.update { it.copy(showAddTaskSheet = false) }
    }

    /**
     * Handle add task submission from the Add Task sheet.
     * Following Android best practices:
     * - Try-catch with specific exception types
     * - Never consume CancellationException
     * - Notify view of errors via snackbar
     * @see https://developer.android.com/kotlin/coroutines/coroutines-best-practices
     */
    private fun handleAddTaskSubmitted(title: String, notes: String?, ownerType: OwnerType) {
        if (title.isBlank()) {
            return
        }

        viewModelScope.launch {
            // Show feedback if not authenticated yet
            val userId = currentUserId
            if (userId == null) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Please wait, signing in..."))
                return@launch
            }

            val weekId = weekRepository.getCurrentWeekId()

            val task = Task(
                id = "",
                title = title.trim(),
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                ownerId = userId,
                ownerType = ownerType,
                weekId = weekId,
                status = TaskStatus.PENDING,
                createdBy = userId,
                repeatTarget = null,
                repeatCompleted = 0,
                linkedGoalId = null,
                reviewNote = null,
                rolledFromWeekId = null,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST
            )

            try {
                taskRepository.createTask(task)
                _uiState.update { it.copy(showAddTaskSheet = false) }
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Task added"))
            } catch (e: CancellationException) {
                throw e  // Never consume CancellationException (Android best practice)
            } catch (e: IllegalArgumentException) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Invalid task: ${e.message}"))
            } catch (e: Exception) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Failed to create task"))
            }
        }
    }

    private fun handleRefreshRequested() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            // Data refreshes automatically via flows
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun handleRequestTaskFromPartner() {
        viewModelScope.launch {
            // v1.0 placeholder: Feature 006
            _sideEffects.send(WeekSideEffect.ShowSnackbar("Request a Task feature coming in Partner System update"))
        }
    }

    private fun handleInvitePartner() {
        viewModelScope.launch {
            _sideEffects.send(WeekSideEffect.NavigateToPartnerInvite)
        }
    }

    /**
     * Get current user ID from auth repository.
     * Returns null if not authenticated.
     */
    private val currentUserId: String?
        get() = authRepository.currentUser?.id
}
