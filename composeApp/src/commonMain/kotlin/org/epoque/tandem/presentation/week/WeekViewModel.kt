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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.GoalRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.domain.usecase.review.CalculateStreakUseCase
import org.epoque.tandem.domain.usecase.review.IsReviewWindowOpenUseCase
import org.epoque.tandem.presentation.week.model.AddTaskFormState
import org.epoque.tandem.presentation.week.model.CalendarDay
import org.epoque.tandem.presentation.week.model.Segment
import org.epoque.tandem.presentation.week.model.SubtaskUiModel
import org.epoque.tandem.presentation.week.model.TaskDetailState
import org.epoque.tandem.presentation.week.model.TaskSection
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
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeekUiState())
    val uiState: StateFlow<WeekUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<WeekSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<WeekSideEffect> = _sideEffects.receiveAsFlow()

    // Goal cache for looking up goal details
    private var goalMap: Map<String, Goal> = emptyMap()

    // Edited goal ID for task detail sheet
    private var editedGoalId: String? = null

    // Current week offset for calendar navigation (0 = current week, -1 = previous, +1 = next)
    private var weekOffset: Int = 0

    // Today's date for sectioning calculations
    private val today: LocalDate
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    init {
        loadInitialData()
        observeSegmentPreference()
        observeTasks()
        observeGoals()
        loadCalendarDays()
    }

    /**
     * Handle user events from the UI.
     * Following UDF pattern: events go UP to ViewModel.
     */
    fun onEvent(event: WeekEvent) {
        when (event) {
            // Existing events
            is WeekEvent.SegmentSelected -> handleSegmentSelected(event.segment)
            is WeekEvent.TaskCheckboxTapped -> handleTaskCheckboxTapped(event.taskId)
            is WeekEvent.QuickAddTextChanged -> handleQuickAddTextChanged(event.text)
            is WeekEvent.QuickAddSubmitted -> handleQuickAddSubmitted()
            is WeekEvent.TaskTapped -> handleTaskTapped(event.taskId)
            is WeekEvent.DetailSheetDismissed -> handleDetailSheetDismissed()
            is WeekEvent.TaskTitleChanged -> handleTaskTitleChanged(event.title)
            is WeekEvent.TaskNotesChanged -> handleTaskNotesChanged(event.notes)
            is WeekEvent.TaskGoalChanged -> handleTaskGoalChanged(event.goalId)
            is WeekEvent.TaskSaveRequested -> handleTaskSaveRequested()
            is WeekEvent.TaskDeleteRequested -> handleTaskDeleteRequested()
            is WeekEvent.TaskDeleteConfirmed -> handleTaskDeleteConfirmed()
            is WeekEvent.TaskMarkCompleteRequested -> handleTaskMarkCompleteRequested()
            is WeekEvent.AddTaskSheetRequested -> handleAddTaskSheetRequested()
            is WeekEvent.AddTaskSheetDismissed -> handleAddTaskSheetDismissed()
            is WeekEvent.AddTaskSubmittedLegacy -> handleAddTaskSubmitted(
                event.title, event.notes, event.ownerType,
                event.priority, event.scheduledDate, event.linkedGoalId
            )
            is WeekEvent.RefreshRequested -> handleRefreshRequested()
            is WeekEvent.RequestTaskFromPartnerTapped -> handleRequestTaskFromPartner()
            is WeekEvent.InvitePartnerTapped -> handleInvitePartner()

            // Calendar strip events
            is WeekEvent.PreviousWeekTapped -> handlePreviousWeekTapped()
            is WeekEvent.NextWeekTapped -> handleNextWeekTapped()
            is WeekEvent.CalendarDateTapped -> handleCalendarDateTapped(event.date)
            is WeekEvent.CalendarFilterCleared -> handleCalendarFilterCleared()

            // Completed section events (TODO: Implement in Conversation 3)
            is WeekEvent.CompletedSectionToggled -> handleCompletedSectionToggled()

            // Add task modal events (TODO: Implement in Conversation 3)
            is WeekEvent.AddTaskTitleChanged -> handleAddTaskFormTitleChanged(event.title)
            is WeekEvent.AddTaskDescriptionChanged -> handleAddTaskFormDescriptionChanged(event.description)
            is WeekEvent.AddTaskOwnerChanged -> handleAddTaskFormOwnerChanged(event.ownerType)
            is WeekEvent.AddTaskDateChanged -> handleAddTaskFormDateChanged(event.date)
            is WeekEvent.AddTaskTimeChanged -> handleAddTaskFormTimeChanged(event.time)
            is WeekEvent.AddTaskPriorityChanged -> handleAddTaskFormPriorityChanged(event.priority)
            is WeekEvent.AddTaskLabelsChanged -> handleAddTaskFormLabelsChanged(event.labels)
            is WeekEvent.AddTaskGoalChanged -> handleAddTaskFormGoalChanged(event.goalId)
            is WeekEvent.AddTaskDeadlineChanged -> handleAddTaskFormDeadlineChanged(event.deadline)
            is WeekEvent.AddTaskSubmitted -> handleAddTaskFormSubmitted()

            // Task detail modal events (TODO: Implement in Conversation 3)
            is WeekEvent.TaskDescriptionChanged -> handleTaskDescriptionChanged(event.description)
            is WeekEvent.TaskOwnerChanged -> handleTaskOwnerChanged(event.ownerType)
            is WeekEvent.TaskDateChanged -> handleTaskDateChanged(event.date)
            is WeekEvent.TaskTimeChanged -> handleTaskTimeChanged(event.time)
            is WeekEvent.TaskPriorityChanged -> handleTaskPriorityChanged(event.priority)
            is WeekEvent.TaskLabelsChanged -> handleTaskLabelsChanged(event.labels)
            is WeekEvent.TaskDeadlineChanged -> handleTaskDeadlineChanged(event.deadline)
            is WeekEvent.TaskCompleteRequested -> handleTaskCompleteRequested()
            is WeekEvent.TaskSkipRequested -> handleTaskSkipRequested()

            // Dismiss flow events (hybrid save strategy)
            is WeekEvent.DetailSheetDismissRequested -> handleDetailSheetDismissRequested()
            is WeekEvent.DiscardChangesConfirmed -> handleDiscardChangesConfirmed()
            is WeekEvent.DiscardChangesCancelled -> handleDiscardChangesCancelled()
            is WeekEvent.SaveTextChangesRequested -> handleSaveTextChangesRequested()

            // Subtask events (TODO: Implement in Conversation 3)
            is WeekEvent.SubtaskCheckboxTapped -> handleSubtaskCheckboxTapped(event.subtaskId)
            is WeekEvent.NewSubtaskTitleChanged -> handleNewSubtaskTitleChanged(event.title)
            is WeekEvent.AddSubtaskSubmitted -> handleAddSubtaskSubmitted()
            is WeekEvent.SubtaskDeleted -> handleSubtaskDeleted(event.subtaskId)

            // Comment events (TODO: Implement in Conversation 3)
            is WeekEvent.CommentTextChanged -> handleCommentTextChanged(event.text)
            is WeekEvent.CommentSubmitted -> handleCommentSubmitted()
        }
    }

    /**
     * Load initial week data.
     * Waits for authentication, then ensures the current week exists.
     * Sets initial weekInfo once (navigation handlers control it after init).
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

            // Get current week info (one-shot, not continuous observation)
            val currentWeekId = weekRepository.getCurrentWeekId()
            val week = weekRepository.getWeekById(currentWeekId)

            // Set initial state once - navigation handlers control weekInfo after init
            val weekInfo = if (week != null) {
                WeekInfo.fromWeek(week, currentWeekId)
            } else {
                WeekInfo.fromWeekId(currentWeekId, currentWeekId)
            }

            _uiState.update { state ->
                state.copy(
                    weekInfo = weekInfo,
                    isPlanningComplete = week?.planningCompletedAt != null,
                    isWeekReviewed = week?.isReviewed == true,
                    isReviewWindowOpen = isReviewWindowOpen,
                    currentStreak = streak
                )
            }
        }
    }

    /**
     * Load calendar days for the current week with task indicators.
     * Uses weekId from state to support week navigation.
     */
    private fun loadCalendarDays() {
        viewModelScope.launch {
            // Use weekId from state (set by navigation) or fall back to current week
            val weekId = _uiState.value.weekInfo?.weekId ?: weekRepository.getCurrentWeekId()
            val taskCounts = taskRepository.getTaskCountsByDate(weekId)
            val weekStart = _uiState.value.weekInfo?.startDate ?: calculateWeekStart(today)
            val selectedDate = _uiState.value.selectedCalendarDate

            val calendarDays = generateCalendarDays(weekStart, today, taskCounts, selectedDate)
            _uiState.update { it.copy(calendarDays = calendarDays) }
        }
    }

    /**
     * Generate list of CalendarDay for a week.
     */
    private fun generateCalendarDays(
        weekStart: LocalDate,
        today: LocalDate,
        taskCounts: Map<LocalDate, Int>,
        selectedDate: LocalDate?
    ): List<CalendarDay> {
        val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        return (0..6).map { dayOffset ->
            val date = weekStart.plus(DatePeriod(days = dayOffset))
            CalendarDay(
                date = date,
                dayOfWeekLabel = dayNames[date.dayOfWeek.ordinal.let { (it + 1) % 7 }], // Convert Monday=0 to Sunday=0
                dayNumber = date.dayOfMonth,
                isToday = date == today,
                hasTasks = (taskCounts[date] ?: 0) > 0,
                isSelected = date == selectedDate
            )
        }
    }

    /**
     * Calculate the start of the week (Monday) for a given date.
     */
    private fun calculateWeekStart(date: LocalDate): LocalDate {
        val daysFromMonday = (date.dayOfWeek.ordinal)
        return date.plus(DatePeriod(days = -daysFromMonday))
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
                // Observe weekId from state so navigation triggers task reload
                _uiState.map { it.weekInfo?.weekId }.filterNotNull().distinctUntilChanged()
            ) { authState, segment, weekId ->
                Triple(authState, segment, weekId)
            }.flatMapLatest { (authState, segment, weekId) ->
                // Only observe if authenticated
                val userId = (authState as? AuthState.Authenticated)?.user?.id
                    ?: return@flatMapLatest flowOf(emptyList())

                taskRepository.observeTasksByWeekAndOwnerType(
                    weekId = weekId,
                    ownerType = segment.toOwnerType(),
                    userId = userId
                )
            }.collect { tasks ->
                updateTasksInState(tasks)
            }
        }
    }

    /**
     * Observe user's active goals for the goal picker.
     * Updates availableGoals in state and maintains goalMap for lookups.
     */
    private fun observeGoals() {
        viewModelScope.launch {
            val userId = authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .first()
                .user.id

            goalRepository.observeMyActiveGoals(userId).collect { goals ->
                goalMap = goals.associateBy { it.id }
                _uiState.update { it.copy(availableGoals = goals) }
                // Re-update tasks to include goal info
                val tasks = _uiState.value.tasks
                if (tasks.isNotEmpty()) {
                    // Trigger task UI update with new goal info
                    updateTasksWithGoalInfo()
                }
            }
        }
    }

    /**
     * Re-map tasks with goal information when goals are updated.
     */
    private fun updateTasksWithGoalInfo() {
        val currentTasks = _uiState.value.tasks
        val updatedTasks = currentTasks.map { taskUi ->
            val goal = taskUi.linkedGoalId?.let { goalMap[it] }
            taskUi.copy(
                linkedGoalName = goal?.name,
                linkedGoalIcon = goal?.icon
            )
        }
        val incomplete = updatedTasks.filter { !it.isCompleted }
        val completed = updatedTasks.filter { it.isCompleted }
        _uiState.update {
            it.copy(
                tasks = updatedTasks,
                incompleteTasks = incomplete,
                completedTasks = completed
            )
        }
    }

    /**
     * Update UI state with new task list.
     * Separates completed from incomplete and groups by section.
     */
    private fun updateTasksInState(tasks: List<Task>) {
        val userId = currentUserId ?: return
        val partnerName = _uiState.value.partnerName
        val currentDate = today
        val tomorrow = currentDate.plus(DatePeriod(days = 1))
        val weekEnd = _uiState.value.weekInfo?.endDate ?: calculateWeekStart(currentDate).plus(DatePeriod(days = 6))

        val uiModels = tasks.map { task ->
            val goal = task.linkedGoalId?.let { goalMap[it] }
            TaskUiModel.fromTask(
                task = task,
                currentUserId = userId,
                partnerName = partnerName,
                goalName = goal?.name,
                goalIcon = goal?.icon
            )
        }

        // Group tasks by section based on scheduled date
        val completed = uiModels.filter { it.isCompleted }
        val incomplete = uiModels.filter { !it.isCompleted }

        val overdue = incomplete.filter { task ->
            task.scheduledDate != null && task.scheduledDate < currentDate
        }.sortedWith(compareBy({ it.scheduledDate }, { it.scheduledTime }))

        val todayTasks = incomplete.filter { task ->
            task.scheduledDate == currentDate
        }.sortedWith(compareBy({ it.scheduledTime == null }, { it.scheduledTime }))

        val tomorrowTasks = incomplete.filter { task ->
            task.scheduledDate == tomorrow
        }.sortedWith(compareBy({ it.scheduledTime == null }, { it.scheduledTime }))

        val laterThisWeek = incomplete.filter { task ->
            task.scheduledDate != null &&
            task.scheduledDate > tomorrow &&
            task.scheduledDate <= weekEnd
        }.sortedWith(compareBy({ it.scheduledDate }, { it.scheduledTime == null }, { it.scheduledTime }))

        // Tasks without a scheduled date
        val unscheduled = incomplete.filter { task ->
            task.scheduledDate == null
        }.sortedBy { it.title }

        // Update state with sectioned tasks
        _uiState.update {
            it.copy(
                tasks = uiModels,
                incompleteTasks = incomplete,
                completedTasks = completed,
                overdueTasks = overdue,
                todayTasks = todayTasks,
                tomorrowTasks = tomorrowTasks,
                laterThisWeekTasks = laterThisWeek,
                unscheduledTasks = unscheduled,
                completedCount = completed.size,
                totalCount = uiModels.size,
                progressText = "${completed.size}/${uiModels.size}",
                isLoading = false
            )
        }

        // Reload calendar days to update task indicators
        loadCalendarDays()
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
            val wasCompleted = task.status == TaskStatus.COMPLETED ||
                (task.isRepeating && task.repeatCompleted >= (task.repeatTarget ?: 0))

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

            // Increment goal progress when task is completed (not uncompleted)
            val isNowCompleted = !wasCompleted
            val goalId = task.linkedGoalId
            if (isNowCompleted && goalId != null) {
                goalRepository.incrementProgress(goalId, 1)
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
                requestNote = null,
                repeatTarget = null,
                repeatCompleted = 0,
                linkedGoalId = null,
                reviewNote = null,
                rolledFromWeekId = null,
                priority = TaskPriority.P4,
                scheduledDate = null,
                scheduledTime = null,
                deadline = null,
                parentTaskId = null,
                labels = emptyList(),
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
        val task = _uiState.value.tasks.find { it.id == taskId } ?: return
        editedGoalId = task.linkedGoalId
        val goal = task.linkedGoalId?.let { goalMap[it] }

        // Set initial state with loading for subtasks
        _uiState.update {
            it.copy(
                selectedTaskId = taskId,
                showDetailSheet = true,
                editedTaskTitle = task.title,
                editedTaskNotes = task.notes ?: "",
                taskDetailState = TaskDetailState(
                    taskId = task.id,
                    title = task.title,
                    description = task.notes ?: "",
                    originalTitle = task.title,  // Store original for change detection
                    originalDescription = task.notes ?: "",  // Store original for change detection
                    ownerType = task.ownerType,
                    scheduledDate = task.scheduledDate,
                    scheduledTime = task.scheduledTime,
                    priority = task.priority,
                    labels = task.labels,
                    linkedGoalId = task.linkedGoalId,
                    linkedGoalName = goal?.name,
                    linkedGoalIcon = goal?.icon,
                    linkedGoalProgress = goal?.progressText,
                    linkedGoalProgressFraction = goal?.progressFraction ?: 0f,
                    deadline = task.deadline,
                    subtasks = emptyList(),
                    isLoading = true
                )
            )
        }

        // Load subtasks asynchronously
        viewModelScope.launch {
            try {
                val subtasks = taskRepository.observeSubtasks(taskId).first()
                val subtaskUiModels = subtasks.map { subtask ->
                    SubtaskUiModel(
                        id = subtask.id,
                        title = subtask.title,
                        isCompleted = subtask.status == TaskStatus.COMPLETED
                    )
                }
                _uiState.update { state ->
                    state.copy(
                        taskDetailState = state.taskDetailState?.copy(
                            subtasks = subtaskUiModels,
                            isLoading = false
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        taskDetailState = state.taskDetailState?.copy(isLoading = false)
                    )
                }
            }
        }
    }

    private fun handleDetailSheetDismissed() {
        editedGoalId = null
        _uiState.update {
            it.copy(
                selectedTaskId = null,
                showDetailSheet = false,
                editedTaskTitle = "",
                editedTaskNotes = "",
                taskDetailState = null  // Clear task detail state
            )
        }
    }

    private fun handleTaskTitleChanged(title: String) {
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    title = title,
                    hasUnsavedChanges = true
                )
            )
        }
    }

    private fun handleTaskNotesChanged(notes: String) {
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    description = notes,
                    hasUnsavedChanges = true
                )
            )
        }
    }

    // ============================================
    // DISMISS FLOW HANDLERS (Hybrid Save Strategy)
    // ============================================

    /**
     * Called when user attempts to dismiss the detail sheet.
     * Shows discard dialog if there are unsaved text changes.
     */
    private fun handleDetailSheetDismissRequested() {
        val state = _uiState.value.taskDetailState ?: return

        if (state.hasUnsavedTextChanges) {
            // Show discard changes dialog
            _uiState.update { it.copy(showDiscardChangesDialog = true) }
        } else {
            // No unsaved text changes, dismiss directly
            handleDetailSheetDismissed()
        }
    }

    /**
     * Called when user confirms discarding unsaved text changes.
     */
    private fun handleDiscardChangesConfirmed() {
        _uiState.update {
            it.copy(
                showDiscardChangesDialog = false,
                taskDetailState = null,
                selectedTaskId = null,
                showDetailSheet = false,
                editedTaskTitle = "",
                editedTaskNotes = ""
            )
        }
    }

    /**
     * Called when user cancels discard dialog (returns to sheet).
     */
    private fun handleDiscardChangesCancelled() {
        _uiState.update { it.copy(showDiscardChangesDialog = false) }
    }

    /**
     * Called when user taps save button to persist text changes (title/description).
     */
    private fun handleSaveTextChangesRequested() {
        val state = _uiState.value.taskDetailState ?: return
        val taskId = state.taskId

        viewModelScope.launch {
            // Save title and description
            taskRepository.updateTaskTitleAndNotes(
                taskId = taskId,
                title = state.title.trim(),
                notes = state.description.trim().takeIf { it.isNotEmpty() }
            )

            // Update original values to match current (no longer unsaved)
            _uiState.update { uiState ->
                uiState.copy(
                    taskDetailState = uiState.taskDetailState?.copy(
                        originalTitle = state.title,
                        originalDescription = state.description,
                        hasUnsavedChanges = false
                    )
                )
            }

            _sideEffects.send(WeekSideEffect.ShowSnackbar("Changes saved"))
        }
    }

    private fun handleTaskGoalChanged(goalId: String?) {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return
        val goal = goalId?.let { goalMap[it] }

        // Update UI state immediately
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    linkedGoalId = goalId,
                    linkedGoalName = goal?.name,
                    linkedGoalIcon = goal?.icon,
                    linkedGoalProgress = goal?.progressText,
                    linkedGoalProgressFraction = goal?.progressFraction ?: 0f
                )
            )
        }

        // Auto-save to repository
        viewModelScope.launch {
            if (goalId != null) {
                taskRepository.linkTaskToGoal(taskId, goalId)
            } else {
                taskRepository.unlinkTaskFromGoal(taskId)
            }
        }
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

            // Update goal linking if changed
            val newGoalId = editedGoalId
            val oldGoalId = currentTask.linkedGoalId
            if (newGoalId != oldGoalId) {
                if (newGoalId != null) {
                    taskRepository.linkTaskToGoal(taskId, newGoalId)
                } else {
                    taskRepository.unlinkTaskFromGoal(taskId)
                }
            }

            editedGoalId = null
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
    private fun handleAddTaskSubmitted(
        title: String,
        notes: String?,
        ownerType: OwnerType,
        priority: TaskPriority,
        scheduledDate: LocalDate?,
        linkedGoalId: String?
    ) {
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
                requestNote = null,
                repeatTarget = null,
                repeatCompleted = 0,
                linkedGoalId = linkedGoalId,
                reviewNote = null,
                rolledFromWeekId = null,
                priority = priority,
                scheduledDate = scheduledDate,
                scheduledTime = null,
                deadline = null,
                parentTaskId = null,
                labels = emptyList(),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST
            )

            try {
                taskRepository.createTask(task)
                // Keep modal open for chaining - form reset is handled by caller
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

    /**
     * Get the edited goal ID for task detail sheet.
     * Returns the goal ID being edited for the current selected task.
     */
    fun getEditedGoalId(): String? = editedGoalId

    // ============================================
    // CALENDAR NAVIGATION HANDLERS
    // ============================================

    private fun handlePreviousWeekTapped() {
        viewModelScope.launch {
            weekOffset--
            val newWeekId = calculateWeekIdWithOffset(weekOffset)
            val currentWeekId = weekRepository.getCurrentWeekId()
            val week = weekRepository.getWeekById(newWeekId)

            // Create WeekInfo from week if exists, otherwise from weekId
            val weekInfo = if (week != null) {
                WeekInfo.fromWeek(week, currentWeekId)
            } else {
                WeekInfo.fromWeekId(newWeekId, currentWeekId)
            }

            _uiState.update { state ->
                state.copy(
                    weekInfo = weekInfo,
                    selectedCalendarDate = null  // Clear filter when navigating
                )
            }

            // Reload calendar days and tasks for new week
            loadCalendarDays()
        }
    }

    private fun handleNextWeekTapped() {
        viewModelScope.launch {
            weekOffset++
            val newWeekId = calculateWeekIdWithOffset(weekOffset)
            val currentWeekId = weekRepository.getCurrentWeekId()
            val week = weekRepository.getWeekById(newWeekId)

            // Create WeekInfo from week if exists, otherwise from weekId
            val weekInfo = if (week != null) {
                WeekInfo.fromWeek(week, currentWeekId)
            } else {
                WeekInfo.fromWeekId(newWeekId, currentWeekId)
            }

            _uiState.update { state ->
                state.copy(
                    weekInfo = weekInfo,
                    selectedCalendarDate = null  // Clear filter when navigating
                )
            }

            // Reload calendar days and tasks for new week
            loadCalendarDays()
        }
    }

    private fun handleCalendarDateTapped(date: LocalDate) {
        val currentSelected = _uiState.value.selectedCalendarDate

        // Toggle selection: if already selected, clear it
        val newSelected = if (date == currentSelected) null else date

        _uiState.update { state ->
            state.copy(selectedCalendarDate = newSelected)
        }

        // Refresh calendar days to update selection state
        loadCalendarDays()
    }

    private fun handleCalendarFilterCleared() {
        _uiState.update { state ->
            state.copy(selectedCalendarDate = null)
        }
        loadCalendarDays()
    }

    /**
     * Calculate week ID for a given offset from current week.
     */
    private fun calculateWeekIdWithOffset(offset: Int): String {
        val currentWeekId = weekRepository.getCurrentWeekId()
        if (offset == 0) return currentWeekId

        // Parse current week ID (format: "2026-W02")
        val parts = currentWeekId.split("-W")
        val year = parts[0].toInt()
        val week = parts[1].toInt()

        // Calculate new week
        var newWeek = week + offset
        var newYear = year

        while (newWeek < 1) {
            newYear--
            newWeek += 52  // Simplified - actual implementation should check year
        }
        while (newWeek > 52) {
            newYear++
            newWeek -= 52
        }

        return "$newYear-W${newWeek.toString().padStart(2, '0')}"
    }

    // ============================================
    // TASK DETAIL & SUBTASK HANDLERS
    // ============================================

    private fun handleCompletedSectionToggled() {
        _uiState.update { it.copy(isCompletedSectionExpanded = !it.isCompletedSectionExpanded) }
    }

    // Add Task Form handlers
    private fun handleAddTaskFormTitleChanged(title: String) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(title = title, titleError = null))
        }
    }

    private fun handleAddTaskFormDescriptionChanged(description: String) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(description = description))
        }
    }

    private fun handleAddTaskFormOwnerChanged(ownerType: OwnerType) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(ownerType = ownerType))
        }
    }

    private fun handleAddTaskFormDateChanged(date: LocalDate?) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(scheduledDate = date))
        }
    }

    private fun handleAddTaskFormTimeChanged(time: kotlinx.datetime.LocalTime?) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(scheduledTime = time))
        }
    }

    private fun handleAddTaskFormPriorityChanged(priority: TaskPriority) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(priority = priority))
        }
    }

    private fun handleAddTaskFormLabelsChanged(labels: List<String>) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(labels = labels))
        }
    }

    private fun handleAddTaskFormGoalChanged(goalId: String?) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(linkedGoalId = goalId))
        }
    }

    private fun handleAddTaskFormDeadlineChanged(deadline: Instant?) {
        _uiState.update { state ->
            state.copy(addTaskForm = state.addTaskForm.copy(deadline = deadline))
        }
    }

    private fun handleAddTaskFormSubmitted() {
        val form = _uiState.value.addTaskForm
        if (!form.isValid) {
            _uiState.update { state ->
                state.copy(addTaskForm = state.addTaskForm.copy(titleError = "Title cannot be empty"))
            }
            return
        }

        // Delegate to existing handler with form values
        handleAddTaskSubmitted(
            title = form.title,
            notes = form.description.takeIf { it.isNotBlank() },
            ownerType = form.ownerType,
            priority = form.priority,
            scheduledDate = form.scheduledDate,
            linkedGoalId = form.linkedGoalId
        )

        // Reset form
        _uiState.update { state ->
            state.copy(addTaskForm = AddTaskFormState())
        }
    }

    // Task Detail handlers
    private fun handleTaskDescriptionChanged(description: String) {
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    description = description,
                    hasUnsavedChanges = true
                )
            )
        }
    }

    private fun handleTaskOwnerChanged(ownerType: OwnerType) {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return

        // Update UI state immediately
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    ownerType = ownerType
                )
            )
        }

        // Auto-save to repository
        viewModelScope.launch {
            taskRepository.updateTaskOwner(taskId, ownerType)
        }
    }

    private fun handleTaskDateChanged(date: LocalDate?) {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return
        val currentTime = _uiState.value.taskDetailState?.scheduledTime

        // Update UI state immediately
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    scheduledDate = date
                )
            )
        }

        // Auto-save to repository
        viewModelScope.launch {
            taskRepository.updateTaskSchedule(taskId, date, currentTime)
        }
    }

    private fun handleTaskTimeChanged(time: kotlinx.datetime.LocalTime?) {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return
        val currentDate = _uiState.value.taskDetailState?.scheduledDate

        // Update UI state immediately
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    scheduledTime = time
                )
            )
        }

        // Auto-save to repository
        viewModelScope.launch {
            taskRepository.updateTaskSchedule(taskId, currentDate, time)
        }
    }

    private fun handleTaskPriorityChanged(priority: TaskPriority) {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return

        // Update UI state immediately
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    priority = priority
                )
            )
        }

        // Auto-save to repository
        viewModelScope.launch {
            taskRepository.updateTaskPriority(taskId, priority)
        }
    }

    private fun handleTaskLabelsChanged(labels: List<String>) {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return

        // Update UI state immediately
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    labels = labels
                )
            )
        }

        // Auto-save to repository
        viewModelScope.launch {
            taskRepository.updateTaskLabels(taskId, labels)
        }
    }

    private fun handleTaskDeadlineChanged(deadline: Instant?) {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return

        // Update UI state immediately
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(
                    deadline = deadline
                )
            )
        }

        // Auto-save to repository
        viewModelScope.launch {
            taskRepository.updateTaskDeadline(taskId, deadline)
        }
    }

    private fun handleTaskCompleteRequested() {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
            _uiState.update { it.copy(taskDetailState = null) }
            _sideEffects.send(WeekSideEffect.TriggerHapticFeedback)
        }
    }

    private fun handleTaskSkipRequested() {
        val taskId = _uiState.value.taskDetailState?.taskId ?: return
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, TaskStatus.SKIPPED)
            _uiState.update { it.copy(taskDetailState = null) }
        }
    }

    // Subtask handlers
    private fun handleSubtaskCheckboxTapped(subtaskId: String) {
        viewModelScope.launch {
            val subtask = taskRepository.getTaskById(subtaskId) ?: return@launch
            val newStatus = if (subtask.status == TaskStatus.COMPLETED) {
                TaskStatus.PENDING
            } else {
                TaskStatus.COMPLETED
            }
            taskRepository.updateTaskStatus(subtaskId, newStatus)

            // Update subtask in state
            _uiState.update { state ->
                val updatedSubtasks = state.taskDetailState?.subtasks?.map { s ->
                    if (s.id == subtaskId) s.copy(isCompleted = newStatus == TaskStatus.COMPLETED)
                    else s
                } ?: emptyList()
                state.copy(
                    taskDetailState = state.taskDetailState?.copy(subtasks = updatedSubtasks)
                )
            }

            _sideEffects.send(WeekSideEffect.TriggerHapticFeedback)
        }
    }

    private fun handleNewSubtaskTitleChanged(title: String) {
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(newSubtaskTitle = title)
            )
        }
    }

    private fun handleAddSubtaskSubmitted() {
        val detailState = _uiState.value.taskDetailState ?: return
        val title = detailState.newSubtaskTitle.trim()
        if (title.isEmpty()) return

        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            val weekId = weekRepository.getCurrentWeekId()

            val subtask = Task(
                id = "",
                title = title,
                notes = null,
                ownerId = userId,
                ownerType = detailState.ownerType,
                weekId = weekId,
                status = TaskStatus.PENDING,
                createdBy = userId,
                requestNote = null,
                repeatTarget = null,
                repeatCompleted = 0,
                linkedGoalId = null,
                reviewNote = null,
                rolledFromWeekId = null,
                priority = TaskPriority.P4,
                scheduledDate = null,
                scheduledTime = null,
                deadline = null,
                parentTaskId = detailState.taskId,
                labels = emptyList(),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST
            )

            try {
                val created = taskRepository.createSubtask(detailState.taskId, subtask)

                // Add to state
                val newSubtaskUi = SubtaskUiModel(
                    id = created.id,
                    title = created.title,
                    isCompleted = false
                )
                _uiState.update { state ->
                    val updatedSubtasks = (state.taskDetailState?.subtasks ?: emptyList()) + newSubtaskUi
                    state.copy(
                        taskDetailState = state.taskDetailState?.copy(
                            subtasks = updatedSubtasks,
                            newSubtaskTitle = ""
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(WeekSideEffect.ShowSnackbar("Failed to create subtask"))
            }
        }
    }

    private fun handleSubtaskDeleted(subtaskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(subtaskId)

            // Remove from state
            _uiState.update { state ->
                val updatedSubtasks = state.taskDetailState?.subtasks?.filter { it.id != subtaskId }
                    ?: emptyList()
                state.copy(
                    taskDetailState = state.taskDetailState?.copy(subtasks = updatedSubtasks)
                )
            }
        }
    }

    // Comment handlers
    private fun handleCommentTextChanged(text: String) {
        _uiState.update { state ->
            state.copy(
                taskDetailState = state.taskDetailState?.copy(commentText = text)
            )
        }
    }

    private fun handleCommentSubmitted() {
        // Comment system is not yet implemented in the data layer
        viewModelScope.launch {
            _sideEffects.send(WeekSideEffect.ShowSnackbar("Comments coming soon"))
        }
    }
}
