# Research: Goals System

**Feature**: 007-goals-system
**Date**: 2026-01-04
**Status**: Complete

## Research Areas

### 1. Goal Type Implementation

**Decision**: Three distinct goal types with different progress calculation strategies

**Rationale**:
- Weekly Habit: Frequency-based tracking resets each week (e.g., "Exercise 3x/week")
- Recurring Task: Binary completion each week (e.g., "Weekly grocery shopping")
- Target Amount: Cumulative progress toward a total (e.g., "Read 50 books this year")

**Alternatives Considered**:
- Single generic goal type: Too abstract, harder to provide specific UI feedback
- More granular types: Violates Intentional Simplicity principle
- Daily goals: Conflicts with Weekly Rhythm principle

**Implementation Pattern**:
```kotlin
sealed class GoalType {
    data class WeeklyHabit(val targetPerWeek: Int) : GoalType()
    data object RecurringTask : GoalType()
    data class TargetAmount(val targetTotal: Int) : GoalType()
}

fun calculateProgress(goal: Goal, completions: Int): Float = when (goal.type) {
    is GoalType.WeeklyHabit -> completions.toFloat() / goal.type.targetPerWeek
    is GoalType.RecurringTask -> if (completions > 0) 1f else 0f
    is GoalType.TargetAmount -> completions.toFloat() / goal.type.targetTotal
}
```

---

### 2. Week Boundary Handling

**Decision**: Use ISO 8601 week IDs and kotlinx.datetime for week calculations

**Rationale**:
- ISO 8601 provides standardized week numbering ("2026-W01")
- kotlinx.datetime is already used in Features 002-006
- Consistent with existing `Week` model and week calculation logic

**Alternatives Considered**:
- Custom week calculation: Inconsistent with existing codebase
- Calendar-based weeks: Platform-dependent, timezone issues

**Implementation**:
```kotlin
import kotlinx.datetime.*

fun getWeekId(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String {
    // ISO week number calculation
    val dayOfYear = date.dayOfYear
    val dayOfWeek = date.dayOfWeek.isoDayNumber
    val weekNumber = (dayOfYear - dayOfWeek + 10) / 7
    return "${date.year}-W${weekNumber.toString().padStart(2, '0')}"
}
```

**Weekly Reset Logic**:
- On app launch/resume: Check if current week differs from goal's `lastProgressWeekId`
- If new week: Archive current progress to `GoalProgress` table, reset `currentWeekProgress` to 0
- Recurring Task goals reset `isCompletedThisWeek` flag

---

### 3. Task-Goal Linking

**Decision**: Use existing `Task.linkedGoalId` field with event-based progress updates

**Rationale**:
- `linkedGoalId` field already exists in Task model (Feature 002)
- Completing a linked task automatically increments goal progress
- No additional database schema changes needed

**Alternatives Considered**:
- Separate linking table: Over-engineering for 1:N relationship
- Manual progress entry only: Poor UX, defeats automation purpose

**Implementation**:
```kotlin
// In TaskRepository/ViewModel when task is completed
suspend fun completeTask(taskId: String) {
    val task = getTaskById(taskId) ?: return
    updateTaskStatus(taskId, TaskStatus.COMPLETED)

    task.linkedGoalId?.let { goalId ->
        goalRepository.incrementProgress(goalId, 1)
    }
}

// In AddGoalSheet - goal picker for task editing
@Composable
fun GoalPicker(
    goals: List<Goal>,
    selectedGoalId: String?,
    onGoalSelected: (String?) -> Unit
)
```

---

### 4. Shared Goals Synchronization

**Decision**: Leverage existing Partner System (Feature 006) real-time infrastructure

**Rationale**:
- Supabase Realtime already set up for partner task sync
- Goals can use same channel pattern with `goals` table filter
- Consistent architecture with Feature 006

**Alternatives Considered**:
- Separate sync mechanism: Duplicates infrastructure
- Polling: Doesn't meet <5s sync requirement

**Implementation Pattern**:
```kotlin
// Extend existing partner sync to include goals
private fun setupRealtimeSync(partnerId: String) {
    // Existing task sync...

    // Add goal sync channel
    val goalChannel = supabase.channel("partner-goals-$partnerId")
    val goalChanges = goalChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
        table = "goals"
        filter = "is_shared=eq.true"
    }

    goalChanges.onEach { action ->
        when (action) {
            is PostgresAction.Insert -> handleGoalCreated(action.record)
            is PostgresAction.Update -> handleGoalUpdated(action.record)
            is PostgresAction.Delete -> handleGoalDeleted(action.oldRecord)
        }
    }.launchIn(viewModelScope)
}
```

---

### 5. Progress Persistence Strategy

**Decision**: Dual-storage with current progress in `Goal` table and historical progress in `GoalProgress` table

**Rationale**:
- Quick reads for current progress (single query)
- Historical data preserved for week-by-week view
- Efficient storage model

**Alternatives Considered**:
- All progress in separate table: More queries for common operations
- Current progress only: Loses historical tracking capability

**Schema Design**:
```sql
-- Goal table stores current state
CREATE TABLE Goal (
    id TEXT PRIMARY KEY,
    current_progress INTEGER NOT NULL DEFAULT 0,
    current_week_id TEXT NOT NULL,
    -- ... other fields
);

-- GoalProgress stores historical weekly snapshots
CREATE TABLE GoalProgress (
    id TEXT PRIMARY KEY,
    goal_id TEXT NOT NULL REFERENCES Goal(id),
    week_id TEXT NOT NULL,
    progress_value INTEGER NOT NULL,
    target_value INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    UNIQUE(goal_id, week_id)
);
```

---

### 6. Goal Limit Enforcement

**Decision**: Client-side validation with max 10 active goals per user

**Rationale**:
- Spec clarification (Session 2026-01-04): Limit to 10 active goals
- Active goals = not completed or expired
- Client-side check before creation dialog

**Implementation**:
```kotlin
// In GoalsViewModel
fun onAddGoalTapped() {
    val activeCount = _uiState.value.personalGoals.count { it.isActive } +
                     _uiState.value.sharedGoals.count { it.isActive }

    if (activeCount >= 10) {
        viewModelScope.launch {
            _sideEffects.send(
                GoalsSideEffect.ShowSnackbar("You can have up to 10 active goals. Complete or delete a goal to add a new one.")
            )
        }
        return
    }

    _uiState.update { it.copy(showAddGoalSheet = true) }
}
```

---

### 7. Goal Expiration Handling

**Decision**: Automatic status transition based on duration and target achievement

**Rationale**:
- Goals with duration track end date: `startWeekId + durationWeeks`
- At expiration: COMPLETED if target met, EXPIRED otherwise
- No shame language: "Expired" not "Failed"

**Status Transitions**:
```
[ACTIVE] ──(target met)──────────> [COMPLETED]
    │
    └──(duration passed,
        target not met)──────────> [EXPIRED]
```

**Implementation**:
```kotlin
enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED
}

fun checkGoalExpiration(goal: Goal): GoalStatus {
    if (!goal.isActive) return goal.status

    // Ongoing goals never expire
    if (goal.durationWeeks == null) return GoalStatus.ACTIVE

    val endWeekId = calculateEndWeekId(goal.startWeekId, goal.durationWeeks)
    val currentWeekId = getWeekId()

    if (currentWeekId > endWeekId) {
        return if (goal.hasMetTarget) GoalStatus.COMPLETED else GoalStatus.EXPIRED
    }

    return GoalStatus.ACTIVE
}
```

---

## Key Decisions Summary

| Area | Decision | Key Benefit |
|------|----------|-------------|
| Goal Types | Three types (Weekly Habit, Recurring Task, Target Amount) | Cover common use cases with simple model |
| Week Calculation | ISO 8601 with kotlinx.datetime | Consistent with existing Features 002-006 |
| Task Linking | Use existing `linkedGoalId` field | No schema changes, automatic progress |
| Shared Goals | Leverage Feature 006 Supabase Realtime | Consistent architecture, <5s sync |
| Progress Storage | Current in Goal, history in GoalProgress | Fast reads + historical tracking |
| Goal Limit | 10 active goals per user | Spec requirement, client-side validation |
| Expiration | COMPLETED/EXPIRED based on target | Positive framing (no "Failed") |

## Dependencies

```kotlin
// Already in project - no new dependencies needed
kotlinx.datetime
io.github.jan-tennert.supabase:realtime-kt
```

## Database Schema Changes

1. **New Tables**: `Goal`, `GoalProgress`
2. **No Modified Tables**: `Task.linkedGoalId` already exists
3. **New Indexes**: For goal lookup by `owner_id`, `is_shared`, `status`

## Next Steps

1. Define detailed data model in `data-model.md`
2. Create SQLDelight schema contracts in `contracts/`
3. Generate quickstart guide in `quickstart.md`
