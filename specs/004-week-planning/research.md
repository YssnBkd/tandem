# Research: Week Planning (Feature 004)

**Date**: 2026-01-03
**Status**: Complete

## Research Areas

### 1. Jetpack Navigation Compose - Nested Wizard Flow

**Decision**: Use nested NavHost pattern with step parameter routes

**Rationale**:
- Matches existing type-safe serialization routes (`Routes.Auth`, `Routes.Main`)
- Each step is a separate composable for testability
- Back navigation works naturally via `popBackStack()`
- Step index parameter enables resume from any point

**Pattern**:
```kotlin
sealed interface Routes {
    sealed interface Planning : Routes {
        @Serializable
        data object Start : Planning

        @Serializable
        data class Wizard(val stepIndex: Int = 0) : Planning
    }
}

// Navigation: each step advances stepIndex, back pops stack
navController.navigate(Routes.Planning.Wizard(newStep)) {
    popUpTo(Routes.Planning.Wizard(currentStep)) { inclusive = false }
}
```

**Alternatives Considered**:
- Single screen with internal step state: Rejected - harder to test, no deep linking
- Separate routes per step (Rollover, AddTasks, etc.): Rejected - verbose, no resume parameter

---

### 2. ISO 8601 Week ID Calculation - Previous Week

**Decision**: Extend `WeekRepositoryImpl` with `getPreviousWeekId()` function

**Rationale**:
- Existing `getCurrentWeekId()` already handles ISO 8601 week calculation
- Reuses existing validation logic (`weekIdPattern`)
- Handles year boundary (W01 → previous year's W52 or W53)

**Implementation**:
```kotlin
fun getPreviousWeekId(currentWeekId: String): String {
    validateWeekId(currentWeekId)
    val parts = currentWeekId.split("-W")
    val year = parts[0].toInt()
    val weekNumber = parts[1].toInt()

    return when {
        weekNumber > 1 -> "$year-W${(weekNumber - 1).toString().padStart(2, '0')}"
        else -> {
            val prevYear = year - 1
            val lastWeek = calculateLastWeekOfYear(prevYear)
            "$prevYear-W${lastWeek.toString().padStart(2, '0')}"
        }
    }
}
```

**Year Boundary Handling**:
- ISO 8601: December 28 is always in the last week of the year
- Last week can be W52 or W53 depending on how the year starts

---

### 3. DataStore for Planning Progress Persistence

**Decision**: Model after existing `SegmentPreferences` with structured `PlanningProgressState`

**Rationale**:
- Follows established project pattern
- Flow-based reactive reads for resume capability
- Type-safe keys prevent runtime errors
- Atomic updates via `dataStore.edit {}`

**Data Structure**:
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

**Keys**:
- `CURRENT_STEP`: Int - which wizard step user is on
- `ROLLOVER_TASKS_PROCESSED`: Set<String> - task IDs already swiped through
- `ADDED_TASK_IDS`: Set<String> - tasks created in this session (for rollback if incomplete)
- `ACCEPTED_REQUESTS`: Set<String> - partner requests accepted
- `IS_IN_PROGRESS`: Boolean - distinguishes "step 0" from "not started"
- `WEEK_ID`: String - validates progress is for current week (stale progress discarded)

**Alternatives Considered**:
- Room/SQLDelight for progress: Rejected - overkill for transient wizard state
- ViewModel SavedStateHandle: Rejected - doesn't survive process death
- Proto DataStore: Rejected - adds complexity, Preferences sufficient for flat data

---

### 4. Material Design 3 Full-Screen Cards

**Decision**: Hybrid approach - draggable gesture + visible action buttons

**Rationale**:
- FR-010 requires visible "Add" button (not keyboard-only)
- FR-023 requires 48dp+ touch targets
- Swipe gesture provides satisfying UX for power users
- Buttons provide clear, accessible fallback

**Implementation Pattern**:
```kotlin
Surface(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clip(RoundedCornerShape(16.dp))
        .draggable(...)  // Optional swipe
) {
    Column(modifier = Modifier.padding(24.dp)) {
        // Progress dots
        // Task content
        // Action buttons (48dp height)
    }
}
```

**Swipe Behavior**:
- Right swipe (>100dp) = Add to This Week
- Left swipe (<-100dp) = Skip
- Release within threshold = snap back

**Alternatives Considered**:
- Swipe only (Tinder-style): Rejected - poor discoverability, accessibility concerns
- Buttons only: Acceptable but less engaging
- Pager with horizontal scroll: Rejected - doesn't communicate accept/skip semantics

---

## Technology Validation

| Requirement | Existing Support | Gap |
|-------------|------------------|-----|
| Task creation | `TaskRepository.createTask()` | None |
| Status updates | `TaskRepository.updateTaskStatus()` | None |
| Incomplete tasks query | None | Add `observeIncompleteTasksForWeek()` |
| Partner requests query | None | Add `observeTasksByStatus(PENDING_ACCEPTANCE)` |
| Mark planning complete | `WeekRepository.markPlanningCompleted()` | None |
| Previous week calculation | None | Add `getPreviousWeekId()` |
| Progress persistence | DataStore pattern exists | Create `PlanningProgress` |
| Navigation | Type-safe routes exist | Add `Routes.Planning` sealed class |

---

## Dependencies (from existing codebase)

### Already Available
- `TaskRepository`: All CRUD operations
- `WeekRepository`: `getOrCreateCurrentWeek()`, `markPlanningCompleted()`
- `AuthRepository`: `authState` Flow with `AuthState.Authenticated`
- `Week.planningCompletedAt`: Field exists in domain model
- `Task.rolledFromWeekId`: Field exists in domain model
- `Task.status`: Includes `PENDING_ACCEPTANCE`

### Needs Implementation
1. **TaskRepository**
   - `observeIncompleteTasksForWeek(weekId, userId): Flow<List<Task>>`
   - `observeTasksByStatus(status, userId): Flow<List<Task>>`

2. **WeekRepository**
   - `getPreviousWeekId(currentWeekId): String`

3. **SQLDelight (Task.sq)**
   - `getIncompleteTasksByWeekId` query
   - `getTasksByStatus` query

4. **DataStore**
   - `PlanningProgress` class following `SegmentPreferences` pattern

---

## Files to Reference

| Purpose | File Path |
|---------|-----------|
| Navigation pattern | `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/AuthNavGraph.kt` |
| ISO 8601 existing | `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/WeekRepositoryImpl.kt:83-134` |
| DataStore template | `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/preferences/SegmentPreferences.kt` |
| M3 Surface patterns | `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/TaskDetailSheet.kt` |
| ViewModel patterns | `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekViewModel.kt` |
