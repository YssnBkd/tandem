# Data Model: Week Planning (Feature 004)

**Date**: 2026-01-03
**Status**: Complete

## Overview

Feature 004 primarily uses existing entities from Features 002-003. This document specifies:
1. Existing entities used (no changes needed)
2. New queries required
3. New DataStore model for planning progress

---

## Existing Entities (No Changes)

### Task
> Defined in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Task.kt`

| Field | Type | Usage in Planning |
|-------|------|-------------------|
| id | String | Primary key |
| title | String | Display in rollover cards |
| notes | String? | Display in rollover cards |
| ownerId | String | User who owns the task |
| ownerType | OwnerType | SELF, PARTNER, SHARED |
| weekId | String | Week association (ISO 8601) |
| status | TaskStatus | PENDING, PENDING_ACCEPTANCE, COMPLETED, TRIED, SKIPPED, DECLINED |
| createdBy | String | Who created the task |
| rolledFromWeekId | String? | **Key field** - tracks rollover origin |
| createdAt | Instant | Timestamp |
| updatedAt | Instant | Timestamp |

**Planning Usage**:
- Rollover Step: Query tasks where `weekId = previousWeek` AND `status IN (PENDING)`
- Partner Requests: Query tasks where `status = PENDING_ACCEPTANCE` AND `ownerId = currentUser`
- New Task Creation: Create with `rolledFromWeekId` set when rolling over

### Week
> Defined in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Week.kt`

| Field | Type | Usage in Planning |
|-------|------|-------------------|
| id | String | ISO 8601 week ID (e.g., "2026-W01") |
| startDate | LocalDate | Week boundaries |
| endDate | LocalDate | Week boundaries |
| userId | String | User who owns the week |
| planningCompletedAt | Instant? | **Key field** - set when planning finishes |
| overallRating | Int? | Not used in planning |
| reviewNote | String? | Not used in planning |
| reviewedAt | Instant? | Not used in planning |

**Planning Usage**:
- Banner visibility: Show if `planningCompletedAt == null` AND Sunday >= 6pm
- Completion: Call `markPlanningCompleted()` at wizard finish

### TaskStatus Enum
> Defined in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/TaskStatus.kt`

| Value | Planning Role |
|-------|---------------|
| PENDING | Rollover candidates, newly added tasks |
| PENDING_ACCEPTANCE | Partner requests to review |
| COMPLETED | Excluded from rollover |
| TRIED | Excluded from rollover |
| SKIPPED | Excluded from rollover |
| DECLINED | Excluded from partner requests |

---

## New Data Model: Planning Progress (DataStore)

### PlanningProgressState
> New file: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/preferences/PlanningProgress.kt`

```kotlin
data class PlanningProgressState(
    val currentStep: Int = 0,
    val processedRolloverTaskIds: Set<String> = emptySet(),
    val addedTaskIds: Set<String> = emptySet(),
    val acceptedRequestIds: Set<String> = emptySet(),
    val isInProgress: Boolean = false,
    val weekId: String? = null
)
```

| Field | Type | Purpose |
|-------|------|---------|
| currentStep | Int | 0-3 for steps ROLLOVER, ADD_TASKS, PARTNER_REQUESTS, CONFIRMATION |
| processedRolloverTaskIds | Set<String> | Tasks already swiped (Add or Skip) in Step 1 |
| addedTaskIds | Set<String> | Task IDs created during this planning session (for summary) |
| acceptedRequestIds | Set<String> | Partner request IDs accepted in Step 3 |
| isInProgress | Boolean | Distinguishes "step 0" from "not yet started" |
| weekId | String? | Validates progress is for current week (stale = discard) |

**Stale Progress Handling**:
```kotlin
// On resume, check if progress is for current week
if (savedProgress.weekId != getCurrentWeekId()) {
    clearProgress()  // Discard stale progress
}
```

---

## New Queries (SQLDelight)

### Task.sq Additions

```sql
-- Get incomplete tasks for rollover
getIncompleteTasksByWeekId:
SELECT * FROM Task
WHERE week_id = ?
  AND owner_id = ?
  AND status = 'PENDING'
ORDER BY created_at ASC;

-- Get tasks by status (for partner requests)
getTasksByStatusAndOwnerId:
SELECT * FROM Task
WHERE status = ?
  AND owner_id = ?
ORDER BY created_at DESC;
```

---

## Repository Interface Additions

### TaskRepository Additions

```kotlin
/**
 * Observe incomplete tasks from a specific week (for rollover).
 * Returns tasks with status = PENDING only.
 */
fun observeIncompleteTasksForWeek(weekId: String, userId: String): Flow<List<Task>>

/**
 * Observe tasks by status for current user.
 * Used for PENDING_ACCEPTANCE (partner requests).
 */
fun observeTasksByStatus(status: TaskStatus, userId: String): Flow<List<Task>>
```

### WeekRepository Additions

```kotlin
/**
 * Calculate the previous week ID (ISO 8601).
 * Handles year boundary (W01 → previous year's W52 or W53).
 */
fun getPreviousWeekId(currentWeekId: String): String
```

---

## UI State Models

### PlanningStep Enum
```kotlin
enum class PlanningStep {
    ROLLOVER,           // Step 1: Review incomplete tasks
    ADD_TASKS,          // Step 2: Add new tasks
    PARTNER_REQUESTS,   // Step 3: Accept/discuss partner requests
    CONFIRMATION        // Step 4: Summary and completion
}
```

### PlanningUiState
```kotlin
data class PlanningUiState(
    val currentStep: PlanningStep = PlanningStep.ROLLOVER,
    val currentWeek: Week? = null,

    // Rollover Step
    val rolloverTasks: List<TaskUiModel> = emptyList(),
    val currentRolloverIndex: Int = 0,
    val processedRolloverCount: Int = 0,

    // Add Tasks Step
    val newTaskText: String = "",
    val newTaskError: String? = null,
    val addedTasks: List<TaskUiModel> = emptyList(),

    // Partner Requests Step
    val partnerRequests: List<TaskUiModel> = emptyList(),
    val currentRequestIndex: Int = 0,
    val processedRequestCount: Int = 0,

    // General
    val isLoading: Boolean = true,
    val error: String? = null,

    // Summary (computed)
    val totalTasksPlanned: Int = 0,
    val rolloverTasksAdded: Int = 0,
    val newTasksCreated: Int = 0,
    val partnerRequestsAccepted: Int = 0
)
```

### PlanningEvent (Sealed Class)
```kotlin
sealed class PlanningEvent {
    // Rollover Step
    data class RolloverTaskAdded(val taskId: String) : PlanningEvent()
    data class RolloverTaskSkipped(val taskId: String) : PlanningEvent()
    data object RolloverStepComplete : PlanningEvent()

    // Add Tasks Step
    data class NewTaskTextChanged(val text: String) : PlanningEvent()
    data object NewTaskSubmitted : PlanningEvent()
    data object DoneAddingTasks : PlanningEvent()

    // Partner Requests Step
    data class PartnerRequestAccepted(val taskId: String) : PlanningEvent()
    data class PartnerRequestDiscussed(val taskId: String) : PlanningEvent()
    data object PartnerRequestsStepComplete : PlanningEvent()

    // Navigation
    data object BackPressed : PlanningEvent()
    data object ExitRequested : PlanningEvent()
    data object PlanningCompleted : PlanningEvent()
}
```

### PlanningSideEffect
```kotlin
sealed class PlanningSideEffect {
    data class ShowSnackbar(val message: String) : PlanningSideEffect()
    data class NavigateToStep(val step: PlanningStep) : PlanningSideEffect()
    data object NavigateBack : PlanningSideEffect()
    data object ExitPlanning : PlanningSideEffect()
    data object TriggerHapticFeedback : PlanningSideEffect()
    data object ClearFocus : PlanningSideEffect()
}
```

---

## Rollover Task Creation Logic

When user taps "Add to This Week" on a rollover task:

```kotlin
// Create new task in current week with rollover reference
val newTask = Task(
    id = "",  // Auto-generated
    title = originalTask.title,
    notes = originalTask.notes,
    ownerId = currentUserId,
    ownerType = OwnerType.SELF,
    weekId = currentWeekId,
    status = TaskStatus.PENDING,
    createdBy = currentUserId,
    repeatTarget = null,  // Rolled tasks lose repeat status
    repeatCompleted = 0,
    linkedGoalId = originalTask.linkedGoalId,
    reviewNote = null,
    rolledFromWeekId = originalTask.weekId,  // KEY: Reference to original week
    createdAt = Instant.DISTANT_PAST,  // Set by repository
    updatedAt = Instant.DISTANT_PAST   // Set by repository
)
```

**Important**: Original task in previous week remains unchanged (status stays PENDING).

---

## Entity Relationships

```
Week (current)
  └── Tasks (created during planning)
        └── rolledFromWeekId → Week (previous)

Week (previous)
  └── Tasks (incomplete, shown in rollover step)
        └── (remain unchanged after rollover)

Task (partner request)
  └── status = PENDING_ACCEPTANCE
  └── ownerId = current user (recipient)
  └── createdBy = partner (sender)
```

---

## Validation Rules

| Entity | Field | Rule |
|--------|-------|------|
| Task | title | Non-blank, trimmed |
| Task | weekId | Must match pattern `YYYY-Www` |
| Task | rolledFromWeekId | Must be valid week ID if present |
| Week | id | Must match pattern `YYYY-Www` |
| Week | startDate | Must be Monday |
| PlanningProgressState | weekId | Must match current week or discard |
