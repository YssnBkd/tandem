# Contracts: Review Operations

**Feature**: 005-week-review | **Date**: 2026-01-03

This document defines the ViewModel operations for the Week Review feature. Use this when implementing event handlers in `ReviewViewModel`.

## Table of Contents

1. [ViewModel Initialization](#1-viewmodel-initialization)
2. [Mode Selection Operations](#2-mode-selection-operations)
3. [Rating Operations](#3-rating-operations)
4. [Task Review Operations](#4-task-review-operations)
5. [Quick Finish Operation](#5-quick-finish-operation)
6. [Complete Review Operation](#6-complete-review-operation)
7. [Progress Persistence Operations](#7-progress-persistence-operations)
8. [Together Mode Operations](#8-together-mode-operations)

---

## 1. ViewModel Initialization

### init Block

**CRITICAL**: Follow the exact initialization sequence from Features 003/004.

```kotlin
// ReviewViewModel.kt
class ReviewViewModel(
    private val authRepository: AuthRepository,
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val isReviewWindowOpenUseCase: IsReviewWindowOpenUseCase,
    private val getReviewStatsUseCase: GetReviewStatsUseCase,
    private val reviewProgressDataStore: ReviewProgressDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ReviewSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<ReviewSideEffect> = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            // 1. Wait for authentication (NEVER skip this)
            val userId = authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .first()
                .user.id

            // 2. Check if review window is open
            val isWindowOpen = isReviewWindowOpenUseCase()

            // 3. Ensure current week exists
            val currentWeek = weekRepository.getOrCreateCurrentWeek(userId)

            // 4. Check for incomplete progress
            val savedProgress = reviewProgressDataStore.progress.first()
            val hasIncomplete = savedProgress != null &&
                                savedProgress.weekId == currentWeek.id

            // 5. Load tasks for the week
            val tasks = taskRepository
                .observeTasksForWeek(currentWeek.id, userId)
                .first()

            // 6. Prepare tasks for review (pending first, completed last)
            val tasksForReview = prepareTasksForReview(tasks)

            // 7. Calculate current streak
            val streak = calculateStreakUseCase(userId)

            // 8. Initialize UI state
            _uiState.update {
                it.copy(
                    currentWeek = currentWeek,
                    isReviewWindowOpen = isWindowOpen,
                    tasksToReview = tasksForReview,
                    currentStreak = streak,
                    hasIncompleteProgress = hasIncomplete,
                    isLoading = false
                )
            }
        }
    }

    private fun prepareTasksForReview(tasks: List<Task>): List<Task> {
        val (completed, pending) = tasks.partition {
            it.status == TaskStatus.COMPLETED
        }
        return pending.sortedBy { it.createdAt } +
               completed.sortedBy { it.createdAt }
    }
}
```

---

## 2. Mode Selection Operations

### SelectMode

```kotlin
fun onSelectMode(mode: ReviewMode) {
    _uiState.update { it.copy(reviewMode = mode) }

    viewModelScope.launch {
        saveProgress()
        _sideEffect.send(ReviewSideEffect.NavigateToRating)
    }
}
```

---

## 3. Rating Operations

### SelectRating

```kotlin
fun onSelectRating(rating: Int) {
    require(rating in 1..5) { "Rating must be 1-5" }

    _uiState.update {
        it.copy(overallRating = rating)
    }

    viewModelScope.launch {
        saveProgress()
    }
}
```

### UpdateRatingNote

```kotlin
fun onUpdateRatingNote(note: String) {
    _uiState.update {
        it.copy(overallNote = note)
    }

    // Debounce save for note changes
    viewModelScope.launch {
        delay(500) // Debounce
        saveProgress()
    }
}
```

### ContinueToTasks

```kotlin
fun onContinueToTasks() {
    val state = _uiState.value

    if (state.overallRating == null) {
        viewModelScope.launch {
            _sideEffect.send(ReviewSideEffect.ShowError("Please select a rating"))
        }
        return
    }

    _uiState.update {
        it.copy(currentStep = ReviewStep.TASK_REVIEW)
    }

    viewModelScope.launch {
        // Persist the week rating immediately
        weekRepository.updateWeekReview(
            weekId = state.currentWeek!!.id,
            overallRating = state.overallRating,
            reviewNote = state.overallNote.takeIf { it.isNotBlank() }
        )

        saveProgress()

        if (state.tasksToReview.isEmpty()) {
            _sideEffect.send(ReviewSideEffect.NavigateToSummary)
        } else {
            _sideEffect.send(ReviewSideEffect.NavigateToTask(0))
        }
    }
}
```

---

## 4. Task Review Operations

### SelectTaskOutcome

```kotlin
fun onSelectTaskOutcome(taskId: String, status: TaskStatus) {
    require(status in listOf(TaskStatus.COMPLETED, TaskStatus.TRIED, TaskStatus.SKIPPED)) {
        "Invalid review status: $status"
    }

    _uiState.update { state ->
        state.copy(
            taskOutcomes = state.taskOutcomes + (taskId to status)
        )
    }

    viewModelScope.launch {
        // Persist immediately
        taskRepository.updateTaskStatus(taskId, status)
        saveProgress()
    }
}
```

### UpdateTaskNote

```kotlin
fun onUpdateTaskNote(taskId: String, note: String) {
    _uiState.update { state ->
        state.copy(
            taskNotes = state.taskNotes + (taskId to note)
        )
    }

    viewModelScope.launch {
        delay(500) // Debounce
        taskRepository.updateTaskReviewNote(taskId, note.takeIf { it.isNotBlank() })
        saveProgress()
    }
}
```

### NextTask

```kotlin
fun onNextTask() {
    val state = _uiState.value
    val nextIndex = state.currentTaskIndex + 1

    if (nextIndex >= state.tasksToReview.size) {
        // All tasks reviewed, go to summary
        _uiState.update { it.copy(currentStep = ReviewStep.SUMMARY) }
        viewModelScope.launch {
            calculateAndSetStats()
            saveProgress()
            _sideEffect.send(ReviewSideEffect.NavigateToSummary)
        }
    } else {
        _uiState.update { it.copy(currentTaskIndex = nextIndex) }
        viewModelScope.launch {
            saveProgress()
            _sideEffect.send(ReviewSideEffect.NavigateToTask(nextIndex))
        }
    }
}
```

### PreviousTask

```kotlin
fun onPreviousTask() {
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
            _sideEffect.send(ReviewSideEffect.NavigateToRating)
        }
    } else {
        _uiState.update { it.copy(currentTaskIndex = prevIndex) }
        viewModelScope.launch {
            _sideEffect.send(ReviewSideEffect.NavigateToTask(prevIndex))
        }
    }
}
```

---

## 5. Quick Finish Operation

### QuickFinish

Marks all remaining (unreviewed) tasks as SKIPPED and completes the review.

```kotlin
fun onQuickFinish() {
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

            _sideEffect.send(ReviewSideEffect.NavigateToSummary)
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
```

---

## 6. Complete Review Operation

### CompleteReview

Called when user finishes reviewing all tasks or clicks "Done" on summary.

```kotlin
private suspend fun completeReview() {
    val state = _uiState.value
    val week = state.currentWeek ?: return

    // Final save of week review (sets reviewedAt)
    weekRepository.updateWeekReview(
        weekId = week.id,
        overallRating = state.overallRating,
        reviewNote = state.overallNote.takeIf { it.isNotBlank() }
    )

    // Clear progress from DataStore
    reviewProgressDataStore.clearProgress()

    // Recalculate streak
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id
    val newStreak = calculateStreakUseCase(userId)

    _uiState.update {
        it.copy(
            currentStreak = newStreak,
            hasIncompleteProgress = false
        )
    }
}

private fun calculateAndSetStats() {
    val state = _uiState.value
    val stats = getReviewStatsUseCase(state.taskOutcomes)

    _uiState.update {
        it.copy(completionPercentage = stats.completionPercentage)
    }
}
```

### OnDone

```kotlin
fun onDone() {
    viewModelScope.launch {
        completeReview()
        _sideEffect.send(ReviewSideEffect.CloseReview)
    }
}
```

### OnStartNextWeek

```kotlin
fun onStartNextWeek() {
    viewModelScope.launch {
        completeReview()
        _sideEffect.send(ReviewSideEffect.NavigateToPlanning)
    }
}
```

---

## 7. Progress Persistence Operations

### saveProgress

Called after each state change to persist progress.

```kotlin
private suspend fun saveProgress() {
    val state = _uiState.value
    val week = state.currentWeek ?: return

    val progress = ReviewProgress(
        weekId = week.id,
        reviewMode = state.reviewMode.name,
        currentStep = state.currentStep.name,
        overallRating = state.overallRating,
        overallNote = state.overallNote,
        currentTaskIndex = state.currentTaskIndex,
        taskOutcomes = state.taskOutcomes.mapValues { it.value.name },
        taskNotes = state.taskNotes,
        lastUpdatedAt = Clock.System.now().toEpochMilliseconds()
    )

    reviewProgressDataStore.saveProgress(progress)
}
```

### ResumeProgress

```kotlin
fun onResumeProgress() {
    viewModelScope.launch {
        val progress = reviewProgressDataStore.progress.first() ?: return@launch

        val (mode, step) = progress.toUiState()

        _uiState.update { state ->
            state.copy(
                reviewMode = mode,
                currentStep = step,
                overallRating = progress.overallRating,
                overallNote = progress.overallNote,
                currentTaskIndex = progress.currentTaskIndex,
                taskOutcomes = progress.getTaskOutcomesAsStatus(),
                taskNotes = progress.taskNotes,
                hasIncompleteProgress = false
            )
        }

        // Navigate to correct step
        when (step) {
            ReviewStep.MODE_SELECT -> { /* Stay on mode select */ }
            ReviewStep.RATING -> _sideEffect.send(ReviewSideEffect.NavigateToRating)
            ReviewStep.TASK_REVIEW -> _sideEffect.send(
                ReviewSideEffect.NavigateToTask(progress.currentTaskIndex)
            )
            ReviewStep.SUMMARY -> _sideEffect.send(ReviewSideEffect.NavigateToSummary)
        }
    }
}
```

### DiscardProgress

```kotlin
fun onDiscardProgress() {
    viewModelScope.launch {
        reviewProgressDataStore.clearProgress()

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
```

---

## 8. Together Mode Operations (P5 - Deferred)

### PassToPartner

```kotlin
fun onPassToPartner() {
    // Together mode is P5, basic implementation
    viewModelScope.launch {
        _sideEffect.send(ReviewSideEffect.ShowPassToPartnerDialog)
        // Full implementation deferred to v1.1
    }
}
```

### AddReaction

```kotlin
fun onAddReaction(taskId: String, emoji: String) {
    require(emoji in listOf("👏", "❤️", "💪")) { "Invalid reaction emoji" }

    // TODO: Store reaction on task entity
    // Deferred to v1.1 - requires schema update for reactions
    viewModelScope.launch {
        // For now, just show feedback
        // taskRepository.addReaction(taskId, emoji, userId)
    }
}
```

---

## Event Handler Dispatch

```kotlin
// In ReviewViewModel
fun onEvent(event: ReviewEvent) {
    when (event) {
        is ReviewEvent.SelectMode -> onSelectMode(event.mode)
        is ReviewEvent.SelectRating -> onSelectRating(event.rating)
        is ReviewEvent.UpdateRatingNote -> onUpdateRatingNote(event.note)
        ReviewEvent.ContinueToTasks -> onContinueToTasks()
        ReviewEvent.QuickFinish -> onQuickFinish()
        is ReviewEvent.SelectTaskOutcome -> onSelectTaskOutcome(event.taskId, event.status)
        is ReviewEvent.UpdateTaskNote -> onUpdateTaskNote(event.taskId, event.note)
        ReviewEvent.NextTask -> onNextTask()
        ReviewEvent.PreviousTask -> onPreviousTask()
        ReviewEvent.CompleteReview -> viewModelScope.launch { completeReview() }
        ReviewEvent.StartNextWeek -> onStartNextWeek()
        ReviewEvent.Done -> onDone()
        ReviewEvent.ResumeProgress -> onResumeProgress()
        ReviewEvent.DiscardProgress -> onDiscardProgress()
        ReviewEvent.PassToPartner -> onPassToPartner()
        is ReviewEvent.AddReaction -> onAddReaction(event.taskId, event.emoji)
        ReviewEvent.DismissError -> _uiState.update { it.copy(error = null) }
        ReviewEvent.Retry -> { /* Retry last failed operation */ }
    }
}
```

---

## Summary

| Operation Category | Key Methods | Persistence |
|-------------------|-------------|-------------|
| Initialization | `init` block | Load from DataStore |
| Mode Selection | `onSelectMode` | DataStore |
| Rating | `onSelectRating`, `onUpdateRatingNote`, `onContinueToTasks` | DataStore + SQLDelight (week) |
| Task Review | `onSelectTaskOutcome`, `onUpdateTaskNote`, `onNextTask` | DataStore + SQLDelight (task) |
| Quick Finish | `onQuickFinish` | SQLDelight (all tasks) |
| Complete | `completeReview`, `onDone`, `onStartNextWeek` | Clear DataStore, finalize SQLDelight |
| Progress | `saveProgress`, `onResumeProgress`, `onDiscardProgress` | DataStore |
| Together Mode | `onPassToPartner`, `onAddReaction` | Deferred to v1.1 |
