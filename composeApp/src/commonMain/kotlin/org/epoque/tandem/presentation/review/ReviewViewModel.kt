package org.epoque.tandem.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.domain.usecase.review.CalculateStreakUseCase
import org.epoque.tandem.domain.usecase.review.CompleteReviewUseCase
import org.epoque.tandem.domain.usecase.review.GetReviewStatsUseCase
import org.epoque.tandem.domain.usecase.review.IsReviewWindowOpenUseCase
import org.epoque.tandem.presentation.review.preferences.ReviewProgress
import org.epoque.tandem.presentation.review.preferences.ReviewProgressState
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel for the Week Review wizard.
 *
 * Follows Android best practices:
 * - Screen-level state holder
 * - StateFlow for reactive UI state
 * - Channel for one-time side effects
 * - viewModelScope for lifecycle-aware coroutines
 * - Unidirectional data flow (UDF) pattern
 */
class ReviewViewModel(
    private val authRepository: AuthRepository,
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val isReviewWindowOpenUseCase: IsReviewWindowOpenUseCase,
    private val getReviewStatsUseCase: GetReviewStatsUseCase,
    private val completeReviewUseCase: CompleteReviewUseCase,
    private val reviewProgress: ReviewProgress
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ReviewSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<ReviewSideEffect> = _sideEffects.receiveAsFlow()

    // Stored properties from init for event handlers
    private var currentUserId: String? = null
    private var currentWeek: Week? = null

    // Debounce jobs for text field updates
    private var ratingNoteDebounceJob: Job? = null
    private var taskNoteDebounceJob: Job? = null

    init {
        loadInitialData()
    }

    /**
     * Handle user events from the UI.
     * Following UDF pattern: events go UP to ViewModel.
     */
    fun onEvent(event: ReviewEvent) {
        when (event) {
            // Mode selection
            is ReviewEvent.SelectMode -> onSelectMode(event.mode)

            // Rating step
            is ReviewEvent.SelectRating -> onSelectRating(event.rating)
            is ReviewEvent.UpdateRatingNote -> onUpdateRatingNote(event.note)
            ReviewEvent.ContinueToTasks -> onContinueToTasks()
            ReviewEvent.QuickFinish -> onQuickFinish()

            // Task review step
            is ReviewEvent.SelectTaskOutcome -> onSelectTaskOutcome(event.taskId, event.status)
            is ReviewEvent.UpdateTaskNote -> onUpdateTaskNote(event.taskId, event.note)
            ReviewEvent.NextTask -> onNextTask()
            ReviewEvent.PreviousTask -> onPreviousTask()

            // Summary
            ReviewEvent.CompleteReview -> viewModelScope.launch { completeReview() }
            ReviewEvent.StartNextWeek -> onStartNextWeek()
            ReviewEvent.Done -> onDone()

            // Progress
            ReviewEvent.ResumeProgress -> onResumeProgress()
            ReviewEvent.DiscardProgress -> onDiscardProgress()

            // Together mode (P5 - deferred)
            ReviewEvent.PassToPartner -> onPassToPartner()
            is ReviewEvent.AddReaction -> onAddReaction(event.taskId, event.emoji)

            // Error handling
            ReviewEvent.DismissError -> _uiState.update { it.copy(error = null) }
            ReviewEvent.Retry -> loadInitialData()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Load initial review data.
     * CRITICAL: Follows exact order specified in contracts/review-operations.md
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // 1. Wait for authentication (NEVER skip this)
                val authState = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                val userId = authState.user.id
                currentUserId = userId

                // 2. Check if review window is open
                val isWindowOpen = isReviewWindowOpenUseCase()

                // 3. Ensure current week exists
                val week = weekRepository.getOrCreateCurrentWeek(userId)
                currentWeek = week

                // 4. Check for incomplete progress
                val savedProgress = reviewProgress.reviewProgress.first()
                val hasIncomplete = savedProgress.isInProgress &&
                        savedProgress.weekId == week.id

                // 5. Load tasks for the week
                val tasks = taskRepository
                    .observeTasksForWeek(week.id, userId)
                    .first()

                // 6. Prepare tasks for review (pending first, completed last)
                val tasksForReview = prepareTasksForReview(tasks)

                // 7. Calculate current streak
                val streak = calculateStreakUseCase(userId)

                // 8. Initialize UI state
                _uiState.update {
                    it.copy(
                        currentWeek = week,
                        isReviewWindowOpen = isWindowOpen,
                        tasksToReview = tasksForReview,
                        currentStreak = streak,
                        hasIncompleteProgress = hasIncomplete,
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
                        error = "Failed to load review data: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Prepare tasks for review: pending first, completed last.
     * This order lets users focus on tasks needing attention first.
     */
    private fun prepareTasksForReview(tasks: List<Task>): List<Task> {
        val (completed, pending) = tasks.partition {
            it.status == TaskStatus.COMPLETED
        }
        return pending.sortedBy { it.createdAt } +
                completed.sortedBy { it.createdAt }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODE SELECTION
    // ═══════════════════════════════════════════════════════════════════════════

    private fun onSelectMode(mode: ReviewMode) {
        _uiState.update { it.copy(reviewMode = mode) }

        viewModelScope.launch {
            saveProgress()
            _sideEffects.send(ReviewSideEffect.NavigateToRating)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RATING OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun onSelectRating(rating: Int) {
        require(rating in 1..5) { "Rating must be 1-5" }

        _uiState.update { it.copy(overallRating = rating) }

        viewModelScope.launch {
            saveProgress()
        }
    }

    private fun onUpdateRatingNote(note: String) {
        _uiState.update { it.copy(overallNote = note) }

        // Debounce save for note changes
        ratingNoteDebounceJob?.cancel()
        ratingNoteDebounceJob = viewModelScope.launch {
            delay(500)
            saveProgress()
        }
    }

    private fun onContinueToTasks() {
        val state = _uiState.value

        if (state.overallRating == null) {
            viewModelScope.launch {
                _sideEffects.send(ReviewSideEffect.ShowError("Please select a rating"))
            }
            return
        }

        _uiState.update { it.copy(currentStep = ReviewStep.TASK_REVIEW) }

        viewModelScope.launch {
            try {
                // Persist the week rating immediately
                weekRepository.updateWeekReview(
                    weekId = state.currentWeek!!.id,
                    overallRating = state.overallRating,
                    reviewNote = state.overallNote.takeIf { it.isNotBlank() }
                )

                saveProgress()

                if (state.tasksToReview.isEmpty()) {
                    _sideEffects.send(ReviewSideEffect.NavigateToSummary)
                } else {
                    _sideEffects.send(ReviewSideEffect.NavigateToTask(0))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(ReviewSideEffect.ShowError("Failed to save rating: ${e.message}"))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TASK REVIEW OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun onSelectTaskOutcome(taskId: String, status: TaskStatus) {
        require(status in listOf(TaskStatus.COMPLETED, TaskStatus.TRIED, TaskStatus.SKIPPED)) {
            "Invalid review status: $status"
        }

        _uiState.update { state ->
            state.copy(
                taskOutcomes = state.taskOutcomes + (taskId to status)
            )
        }

        viewModelScope.launch {
            try {
                // Persist immediately
                taskRepository.updateTaskStatus(taskId, status)
                saveProgress()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(ReviewSideEffect.ShowError("Failed to save outcome: ${e.message}"))
            }
        }
    }

    private fun onUpdateTaskNote(taskId: String, note: String) {
        _uiState.update { state ->
            state.copy(
                taskNotes = state.taskNotes + (taskId to note)
            )
        }

        // Debounce save for note changes
        taskNoteDebounceJob?.cancel()
        taskNoteDebounceJob = viewModelScope.launch {
            delay(500)
            try {
                taskRepository.updateTaskReviewNote(taskId, note.takeIf { it.isNotBlank() })
                saveProgress()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Silent fail for notes - not critical
            }
        }
    }

    private fun onNextTask() {
        val state = _uiState.value
        val nextIndex = state.currentTaskIndex + 1

        if (nextIndex >= state.tasksToReview.size) {
            // All tasks reviewed, go to summary
            _uiState.update { it.copy(currentStep = ReviewStep.SUMMARY) }
            viewModelScope.launch {
                calculateAndSetStats()
                saveProgress()
                _sideEffects.send(ReviewSideEffect.NavigateToSummary)
            }
        } else {
            _uiState.update { it.copy(currentTaskIndex = nextIndex) }
            viewModelScope.launch {
                saveProgress()
                _sideEffects.send(ReviewSideEffect.NavigateToTask(nextIndex))
            }
        }
    }

    private fun onPreviousTask() {
        val state = _uiState.value
        val prevIndex = state.currentTaskIndex - 1

        if (prevIndex < 0) {
            // Go back to rating
            _uiState.update {
                it.copy(
                    currentStep = ReviewStep.RATING,
                    currentTaskIndex = 0
                )
            }
            viewModelScope.launch {
                _sideEffects.send(ReviewSideEffect.NavigateToRating)
            }
        } else {
            _uiState.update { it.copy(currentTaskIndex = prevIndex) }
            viewModelScope.launch {
                _sideEffects.send(ReviewSideEffect.NavigateToTask(prevIndex))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // QUICK FINISH OPERATION
    // ═══════════════════════════════════════════════════════════════════════════

    private fun onQuickFinish() {
        val state = _uiState.value

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                // Get all tasks not yet reviewed
                val unreviewed = state.tasksToReview
                    .filter { task -> task.id !in state.taskOutcomes }

                // Mark all as SKIPPED
                val newOutcomes = state.taskOutcomes.toMutableMap()
                unreviewed.forEach { task ->
                    taskRepository.updateTaskStatus(task.id, TaskStatus.SKIPPED)
                    newOutcomes[task.id] = TaskStatus.SKIPPED
                }

                _uiState.update {
                    it.copy(
                        taskOutcomes = newOutcomes,
                        currentStep = ReviewStep.SUMMARY,
                        isSaving = false
                    )
                }

                calculateAndSetStats()
                completeReview()

                _sideEffects.send(ReviewSideEffect.NavigateToSummary)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to complete review: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPLETE REVIEW OPERATION
    // ═══════════════════════════════════════════════════════════════════════════

    private suspend fun completeReview() {
        val state = _uiState.value
        val week = state.currentWeek ?: return
        val userId = currentUserId ?: return

        try {
            // Complete review and create feed item
            completeReviewUseCase(
                weekId = week.id,
                userId = userId,
                overallRating = state.overallRating,
                reviewNote = state.overallNote.takeIf { it.isNotBlank() }
            )

            // Clear progress from DataStore
            reviewProgress.clearProgress()

            // Recalculate streak
            val newStreak = calculateStreakUseCase(userId)

            _uiState.update {
                it.copy(
                    currentStreak = newStreak,
                    hasIncompleteProgress = false
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _sideEffects.send(ReviewSideEffect.ShowError("Failed to complete review: ${e.message}"))
        }
    }

    private fun calculateAndSetStats() {
        val state = _uiState.value
        val stats = getReviewStatsUseCase(state.taskOutcomes)

        _uiState.update {
            it.copy(completionPercentage = stats.completionPercentage)
        }
    }

    private fun onDone() {
        viewModelScope.launch {
            completeReview()
            _sideEffects.send(ReviewSideEffect.CloseReview)
        }
    }

    private fun onStartNextWeek() {
        viewModelScope.launch {
            completeReview()
            _sideEffects.send(ReviewSideEffect.NavigateToPlanning)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROGRESS PERSISTENCE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private suspend fun saveProgress() {
        val state = _uiState.value
        val week = state.currentWeek ?: return

        val progressState = ReviewProgressState(
            weekId = week.id,
            reviewMode = state.reviewMode,
            currentStep = state.currentStep,
            overallRating = state.overallRating,
            overallNote = state.overallNote,
            currentTaskIndex = state.currentTaskIndex,
            taskOutcomes = state.taskOutcomes,
            taskNotes = state.taskNotes,
            isInProgress = true,
            lastUpdatedAt = Clock.System.now().toEpochMilliseconds()
        )

        reviewProgress.saveProgress(progressState)
    }

    private fun onResumeProgress() {
        viewModelScope.launch {
            val progress = reviewProgress.reviewProgress.first()

            if (!progress.isInProgress) return@launch

            _uiState.update { state ->
                state.copy(
                    reviewMode = progress.reviewMode,
                    currentStep = progress.currentStep,
                    overallRating = progress.overallRating,
                    overallNote = progress.overallNote,
                    currentTaskIndex = progress.currentTaskIndex,
                    taskOutcomes = progress.taskOutcomes,
                    taskNotes = progress.taskNotes,
                    hasIncompleteProgress = false
                )
            }

            // Navigate to correct step
            when (progress.currentStep) {
                ReviewStep.MODE_SELECT -> { /* Stay on mode select */ }
                ReviewStep.RATING -> _sideEffects.send(ReviewSideEffect.NavigateToRating)
                ReviewStep.TASK_REVIEW -> _sideEffects.send(
                    ReviewSideEffect.NavigateToTask(progress.currentTaskIndex)
                )
                ReviewStep.SUMMARY -> _sideEffects.send(ReviewSideEffect.NavigateToSummary)
            }
        }
    }

    private fun onDiscardProgress() {
        viewModelScope.launch {
            reviewProgress.clearProgress()

            _uiState.update {
                it.copy(
                    hasIncompleteProgress = false,
                    overallRating = null,
                    overallNote = "",
                    currentTaskIndex = 0,
                    taskOutcomes = emptyMap(),
                    taskNotes = emptyMap(),
                    currentStep = ReviewStep.MODE_SELECT
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TOGETHER MODE OPERATIONS (P5 - Deferred to v1.1)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun onPassToPartner() {
        viewModelScope.launch {
            _sideEffects.send(ReviewSideEffect.ShowPassToPartnerDialog)
            // Full implementation deferred to v1.1
        }
    }

    private fun onAddReaction(taskId: String, emoji: String) {
        require(emoji in listOf("👏", "❤️", "💪")) { "Invalid reaction emoji" }

        // TODO: Store reaction on task entity
        // Deferred to v1.1 - requires schema update for reactions
        viewModelScope.launch {
            // For now, just provide feedback via haptic or similar
        }
    }
}
