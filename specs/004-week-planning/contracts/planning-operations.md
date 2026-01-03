# Planning Operations Contract (Feature 004)

**Date**: 2026-01-03
**Type**: Internal (no REST API - local operations only)

## Overview

Feature 004 is offline-first with no remote API calls. All operations are local SQLDelight queries and DataStore updates. This contract defines the internal operation signatures.

---

## Repository Operations

### TaskRepository Extensions

#### observeIncompleteTasksForWeek
```kotlin
/**
 * Observe incomplete tasks from a specific week for rollover review.
 *
 * @param weekId ISO 8601 week ID (e.g., "2025-W52")
 * @param userId Current user's ID
 * @return Flow of tasks with status = PENDING
 */
fun observeIncompleteTasksForWeek(weekId: String, userId: String): Flow<List<Task>>
```

**SQLDelight Query**:
```sql
getIncompleteTasksByWeekId:
SELECT * FROM Task
WHERE week_id = :weekId
  AND owner_id = :userId
  AND status = 'PENDING'
ORDER BY created_at ASC;
```

---

#### observeTasksByStatus
```kotlin
/**
 * Observe tasks by status for current user.
 *
 * @param status Task status to filter (typically PENDING_ACCEPTANCE)
 * @param userId Current user's ID
 * @return Flow of matching tasks
 */
fun observeTasksByStatus(status: TaskStatus, userId: String): Flow<List<Task>>
```

**SQLDelight Query**:
```sql
getTasksByStatusAndOwnerId:
SELECT * FROM Task
WHERE status = :status
  AND owner_id = :userId
ORDER BY created_at DESC;
```

---

### WeekRepository Extensions

#### getPreviousWeekId
```kotlin
/**
 * Calculate previous ISO 8601 week ID.
 * Handles year boundary (W01 → W52/W53 of previous year).
 *
 * @param currentWeekId Current week ID (e.g., "2026-W01")
 * @return Previous week ID (e.g., "2025-W52")
 * @throws IllegalArgumentException if weekId format invalid
 */
fun getPreviousWeekId(currentWeekId: String): String
```

**Implementation**:
- If weekNumber > 1: Return same year, weekNumber - 1
- If weekNumber == 1: Return previous year's last week (W52 or W53)

---

## ViewModel Operations

### PlanningViewModel

#### init
```kotlin
// Initialization sequence (CRITICAL ORDER)
viewModelScope.launch {
    // 1. Wait for authentication
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Ensure current week exists
    val currentWeek = weekRepository.getOrCreateCurrentWeek(userId)

    // 3. Check for saved progress
    val savedProgress = planningProgress.planningProgress.first()
    if (savedProgress.weekId != currentWeek.id) {
        planningProgress.clearProgress()
    }

    // 4. Calculate previous week ID
    val previousWeekId = weekRepository.getPreviousWeekId(currentWeek.id)

    // 5. Query rollover candidates
    val rolloverTasks = taskRepository
        .observeIncompleteTasksForWeek(previousWeekId, userId)
        .first()

    // 6. Query partner requests
    val partnerRequests = taskRepository
        .observeTasksByStatus(TaskStatus.PENDING_ACCEPTANCE, userId)
        .first()

    // 7. Initialize UI state
    _uiState.update {
        it.copy(
            currentWeek = currentWeek,
            rolloverTasks = rolloverTasks.map { task -> TaskUiModel.fromTask(task, userId, null) },
            partnerRequests = partnerRequests.map { task -> TaskUiModel.fromTask(task, userId, null) },
            isLoading = false
        )
    }
}
```

---

#### handleRolloverTaskAdded
```kotlin
/**
 * Roll over a task from previous week to current week.
 * Creates new task with rolledFromWeekId reference.
 * Original task remains unchanged.
 */
private fun handleRolloverTaskAdded(taskId: String) {
    viewModelScope.launch {
        val userId = currentUserId ?: return@launch
        val currentWeekId = weekRepository.getCurrentWeekId()
        val originalTask = taskRepository.getTaskById(taskId) ?: return@launch

        val newTask = Task(
            id = "",
            title = originalTask.title,
            notes = originalTask.notes,
            ownerId = userId,
            ownerType = OwnerType.SELF,
            weekId = currentWeekId,
            status = TaskStatus.PENDING,
            createdBy = userId,
            repeatTarget = null,
            repeatCompleted = 0,
            linkedGoalId = originalTask.linkedGoalId,
            reviewNote = null,
            rolledFromWeekId = originalTask.weekId,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST
        )

        val created = taskRepository.createTask(newTask)

        _uiState.update { state ->
            state.copy(
                currentRolloverIndex = state.currentRolloverIndex + 1,
                processedRolloverCount = state.processedRolloverCount + 1,
                addedTasks = state.addedTasks + TaskUiModel.fromTask(created, userId, null),
                rolloverTasksAdded = state.rolloverTasksAdded + 1
            )
        }

        saveProgress()
        _sideEffects.send(PlanningSideEffect.TriggerHapticFeedback)
    }
}
```

---

#### handleRolloverTaskSkipped
```kotlin
/**
 * Skip a rollover task (do not carry forward).
 * No database changes - just advance to next card.
 */
private fun handleRolloverTaskSkipped(taskId: String) {
    viewModelScope.launch {
        _uiState.update { state ->
            state.copy(
                currentRolloverIndex = state.currentRolloverIndex + 1,
                processedRolloverCount = state.processedRolloverCount + 1
            )
        }

        saveProgress()
    }
}
```

---

#### handleNewTaskSubmitted
```kotlin
/**
 * Create a new task during planning.
 */
private fun handleNewTaskSubmitted() {
    val title = _uiState.value.newTaskText.trim()

    if (title.isEmpty()) {
        _uiState.update { it.copy(newTaskError = "Task title cannot be empty") }
        return
    }

    viewModelScope.launch {
        val userId = currentUserId ?: return@launch
        val currentWeekId = weekRepository.getCurrentWeekId()

        val task = Task(
            id = "",
            title = title,
            notes = null,
            ownerId = userId,
            ownerType = OwnerType.SELF,
            weekId = currentWeekId,
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
            val created = taskRepository.createTask(task)
            _uiState.update { state ->
                state.copy(
                    newTaskText = "",
                    newTaskError = null,
                    addedTasks = state.addedTasks + TaskUiModel.fromTask(created, userId, null),
                    newTasksCreated = state.newTasksCreated + 1
                )
            }
            saveProgress()
            _sideEffects.send(PlanningSideEffect.ClearFocus)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _sideEffects.send(PlanningSideEffect.ShowSnackbar("Failed to create task"))
        }
    }
}
```

---

#### handlePartnerRequestAccepted
```kotlin
/**
 * Accept a partner request.
 * Changes task status from PENDING_ACCEPTANCE to PENDING.
 */
private fun handlePartnerRequestAccepted(taskId: String) {
    viewModelScope.launch {
        taskRepository.updateTaskStatus(taskId, TaskStatus.PENDING)

        _uiState.update { state ->
            state.copy(
                currentRequestIndex = state.currentRequestIndex + 1,
                processedRequestCount = state.processedRequestCount + 1,
                partnerRequestsAccepted = state.partnerRequestsAccepted + 1
            )
        }

        saveProgress()
        _sideEffects.send(PlanningSideEffect.TriggerHapticFeedback)
    }
}
```

---

#### handlePartnerRequestDiscussed
```kotlin
/**
 * Mark partner request for discussion (v1.0 placeholder).
 * Shows "Coming soon" snackbar, advances to next card.
 */
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
```

---

#### handlePlanningCompleted
```kotlin
/**
 * Complete planning and mark week as planned.
 */
private fun handlePlanningCompleted() {
    viewModelScope.launch {
        val currentWeek = _uiState.value.currentWeek ?: return@launch

        // Mark planning complete in database
        weekRepository.markPlanningCompleted(currentWeek.id)

        // Clear progress from DataStore
        planningProgress.clearProgress()

        // Calculate summary
        val totalPlanned = _uiState.value.rolloverTasksAdded +
                           _uiState.value.newTasksCreated +
                           _uiState.value.partnerRequestsAccepted

        _uiState.update { it.copy(totalTasksPlanned = totalPlanned) }
        _sideEffects.send(PlanningSideEffect.ExitPlanning)
    }
}
```

---

## DataStore Operations

### PlanningProgress

#### saveProgress
```kotlin
suspend fun saveProgress(state: PlanningProgressState) {
    dataStore.edit { prefs ->
        prefs[Keys.CURRENT_STEP] = state.currentStep
        prefs[Keys.ROLLOVER_TASKS_PROCESSED] = state.processedRolloverTaskIds
        prefs[Keys.ADDED_TASK_IDS] = state.addedTaskIds
        prefs[Keys.ACCEPTED_REQUESTS] = state.acceptedRequestIds
        prefs[Keys.IS_IN_PROGRESS] = state.isInProgress
        state.weekId?.let { prefs[Keys.WEEK_ID] = it }
    }
}
```

#### clearProgress
```kotlin
suspend fun clearProgress() {
    dataStore.edit { it.clear() }
}
```

---

## Step Skipping Logic

```kotlin
/**
 * Determine which step to navigate to next, skipping empty steps.
 */
private fun getNextStep(currentStep: PlanningStep): PlanningStep {
    return when (currentStep) {
        PlanningStep.ROLLOVER -> {
            if (_uiState.value.partnerRequests.isEmpty()) {
                PlanningStep.ADD_TASKS
            } else {
                PlanningStep.ADD_TASKS  // Always show Add Tasks
            }
        }
        PlanningStep.ADD_TASKS -> {
            if (_uiState.value.partnerRequests.isEmpty()) {
                PlanningStep.CONFIRMATION
            } else {
                PlanningStep.PARTNER_REQUESTS
            }
        }
        PlanningStep.PARTNER_REQUESTS -> PlanningStep.CONFIRMATION
        PlanningStep.CONFIRMATION -> PlanningStep.CONFIRMATION  // Terminal
    }
}

/**
 * Determine starting step based on available data.
 */
private fun getInitialStep(): PlanningStep {
    return when {
        _uiState.value.rolloverTasks.isNotEmpty() -> PlanningStep.ROLLOVER
        else -> PlanningStep.ADD_TASKS
    }
}
```

---

## Error Handling

| Operation | Error Type | Handling |
|-----------|------------|----------|
| Task creation | IllegalArgumentException | Show snackbar with validation message |
| Task creation | CancellationException | Re-throw (Android best practice) |
| Task creation | Exception | Show generic "Failed to create task" snackbar |
| Status update | null return | Log warning, continue flow |
| DataStore write | IOException | Log error, continue (progress lost but data safe) |
