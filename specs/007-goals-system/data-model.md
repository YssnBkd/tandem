# Data Model: Goals System

**Feature**: 007-goals-system
**Date**: 2026-01-04

## Entity Overview

```
┌─────────────┐       ┌─────────────┐
│    User     │───────│    Goal     │
│  (owner_id) │  1:N  │             │
└─────────────┘       └─────────────┘
                            │
                            │ 1:N
                            ▼
                      ┌─────────────┐
                      │ GoalProgress│
                      │  (weekly)   │
                      └─────────────┘

┌─────────────┐       ┌─────────────┐
│    Task     │───────│    Goal     │
│(linkedGoalId│  N:1  │             │
└─────────────┘       └─────────────┘

Partner visibility: Users can view their partner's goals (read-only) via Supabase sync.
```

## Entities

### Goal

Represents a user's long-term objective spanning multiple weeks. Each goal belongs exclusively to one user.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | String (UUID) | No | Primary key |
| `name` | String | No | Goal name (1-100 chars) |
| `icon` | String | No | Emoji icon (single emoji) |
| `type` | GoalType | No | WEEKLY_HABIT, RECURRING_TASK, or TARGET_AMOUNT |
| `target_per_week` | Int | Yes | For WEEKLY_HABIT: weekly target count |
| `target_total` | Int | Yes | For TARGET_AMOUNT: cumulative target |
| `duration_weeks` | Int | Yes | 4, 8, 12, or null (ongoing) |
| `start_week_id` | String | No | ISO week when goal started (e.g., "2026-W01") |
| `owner_id` | String (UUID) | No | User who created and owns the goal (FK to auth.users) |
| `current_progress` | Int | No | Current progress count (reset weekly for WEEKLY_HABIT) |
| `current_week_id` | String | No | Week ID of current progress |
| `status` | GoalStatus | No | ACTIVE, COMPLETED, or EXPIRED |
| `created_at` | Instant | No | When goal was created |
| `updated_at` | Instant | No | Last update timestamp |

**GoalType Enum**:
- `WEEKLY_HABIT` - Track frequency per week (e.g., exercise 3x/week)
- `RECURRING_TASK` - Binary completion each week (e.g., weekly review)
- `TARGET_AMOUNT` - Cumulative progress to total (e.g., read 50 books)

**GoalStatus Enum**:
- `ACTIVE` - Goal is in progress
- `COMPLETED` - Goal target was met (duration ended or target reached)
- `EXPIRED` - Goal duration ended without meeting target

**Constraints**:
- `name` must be 1-100 characters
- `icon` must be a valid emoji
- `target_per_week` required when type = WEEKLY_HABIT
- `target_total` required when type = TARGET_AMOUNT
- `duration_weeks` must be 4, 8, 12, or null (ongoing)
- User can have max 10 active goals

**Indexes**:
- `idx_goals_owner` on `owner_id`
- `idx_goals_status` on `status`

---

### GoalProgress

Weekly snapshot of goal progress for history tracking.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | String (UUID) | No | Primary key |
| `goal_id` | String (UUID) | No | FK to Goal |
| `week_id` | String | No | ISO week ID (e.g., "2026-W01") |
| `progress_value` | Int | No | Progress achieved that week |
| `target_value` | Int | No | Target for that week (snapshot) |
| `created_at` | Instant | No | When record was created |

**Constraints**:
- Unique constraint on `(goal_id, week_id)`
- `week_id` format: `YYYY-Www` (ISO 8601)

**Indexes**:
- `idx_goal_progress_goal` on `goal_id`
- `idx_goal_progress_week` on `week_id`

---

### Task (Extended)

Extends existing Task entity with goal linking (field already exists).

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| ... | ... | ... | (existing Task fields) |
| `linked_goal_id` | String (UUID) | Yes | FK to Goal (already exists in schema) |

**Business Rules**:
- When task is completed: If `linked_goal_id` is set, increment goal's `current_progress`
- When task is deleted: Goal progress is NOT decremented (spec requirement)
- When goal is deleted: `linked_goal_id` becomes orphaned (task remains, link broken)
- Tasks can only be linked to the user's own goals (not partner's goals)

---

## State Transitions

### Goal Lifecycle

```
[Created] ──────────────────────────────> [ACTIVE]
                                             │
                   ┌─────────────────────────┼─────────────────────────┐
                   │                         │                         │
                   ▼                         ▼                         ▼
            (target met)           (duration expired,          (user deletes)
                   │                target not met)                   │
                   ▼                         │                         ▼
             [COMPLETED]                     ▼                    [DELETED]
                                        [EXPIRED]
```

### Weekly Progress Reset (WEEKLY_HABIT only)

```
[Week N Progress]
        │
        │ (new week detected)
        ▼
[Archive Progress to GoalProgress table]
        │
        │
        ▼
[Reset current_progress to 0]
        │
        │
        ▼
[Update current_week_id to new week]
```

### Task Completion → Goal Progress

```
[Task Completed]
        │
        │ (has linked_goal_id?)
        ▼
[Increment Goal.current_progress]
        │
        │ (TARGET_AMOUNT: check if target_total reached)
        ▼
[Update Goal.status if COMPLETED]
```

---

## Validation Rules

### Goal Creation

1. **Name**: 1-100 characters, non-empty after trim
2. **Icon**: Single valid emoji (Unicode emoji detection)
3. **Type Validation**:
   - WEEKLY_HABIT: `target_per_week` required (1-99)
   - RECURRING_TASK: No target fields needed
   - TARGET_AMOUNT: `target_total` required (1-9999)
4. **Duration**: Must be 4, 8, 12, or null (ongoing)
5. **Goal Limit**: User cannot exceed 10 active goals

### Progress Recording

1. **Weekly Reset**: Only for WEEKLY_HABIT goals
2. **Progress Increment**: Always positive (no decrements)
3. **Target Checking**: Automatic status transition when target met

---

## SQLDelight Schema

```sql
-- Goal table
CREATE TABLE Goal (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    icon TEXT NOT NULL,
    type TEXT NOT NULL,
    target_per_week INTEGER,
    target_total INTEGER,
    duration_weeks INTEGER,
    start_week_id TEXT NOT NULL,
    owner_id TEXT NOT NULL,
    current_progress INTEGER NOT NULL DEFAULT 0,
    current_week_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_goals_owner ON Goal(owner_id);
CREATE INDEX idx_goals_status ON Goal(status);

-- GoalProgress table (weekly history)
CREATE TABLE GoalProgress (
    id TEXT NOT NULL PRIMARY KEY,
    goal_id TEXT NOT NULL REFERENCES Goal(id) ON DELETE CASCADE,
    week_id TEXT NOT NULL,
    progress_value INTEGER NOT NULL,
    target_value INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    UNIQUE(goal_id, week_id)
);

CREATE INDEX idx_goal_progress_goal ON GoalProgress(goal_id);
CREATE INDEX idx_goal_progress_week ON GoalProgress(week_id);
```

---

## Calculated Properties

### Goal

```kotlin
data class Goal(
    val id: String,
    val name: String,
    val icon: String,
    val type: GoalType,
    val durationWeeks: Int?,
    val startWeekId: String,
    val ownerId: String,
    val currentProgress: Int,
    val currentWeekId: String,
    val status: GoalStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /** Target based on goal type */
    val target: Int get() = when (type) {
        is GoalType.WeeklyHabit -> type.targetPerWeek
        is GoalType.RecurringTask -> 1
        is GoalType.TargetAmount -> type.targetTotal
    }

    /** Progress fraction (0.0 to 1.0+) */
    val progressFraction: Float get() = currentProgress.toFloat() / target

    /** Progress display text (e.g., "3/5" or "75/100") */
    val progressText: String get() = "$currentProgress/$target"

    /** Whether goal is still active */
    val isActive: Boolean get() = status == GoalStatus.ACTIVE

    /** Whether goal has met its target */
    val hasMetTarget: Boolean get() = currentProgress >= target

    /** End week ID if has duration */
    val endWeekId: String? get() = durationWeeks?.let {
        calculateEndWeekId(startWeekId, it)
    }

    /** Whether this is a weekly reset goal */
    val resetsWeekly: Boolean get() = type is GoalType.WeeklyHabit
}
```

---

## Relationships Summary

| From | To | Cardinality | Description |
|------|-----|-------------|-------------|
| Goal | User (owner) | N:1 | Goal is owned exclusively by one user |
| Goal | GoalProgress | 1:N | Goal has weekly progress history |
| Task | Goal | N:1 | Tasks can be linked to user's own goals |

**Partner Visibility**: Partners can view each other's goals via Supabase sync, but goals are NOT shared - each goal has exactly one owner who can edit/delete it.

---

## Edge Cases

### Goal Deletion
- Tasks with `linked_goal_id` pointing to deleted goal: Link becomes orphaned
- GoalProgress records: CASCADE deleted with goal

### Partner Disconnection
- User loses visibility to partner's goals in the "Partner's" segment
- Own goals remain unaffected

### Week Boundary at Midnight
- Use device timezone for consistency
- Week changes at midnight Sunday → Monday (ISO 8601)
- Check week boundary on app launch and resume

### Partner Goal Visibility
- Partner goals are synced via Supabase for read-only viewing
- Partner goals are cached locally for offline access
- When viewing partner's goals offline, show "Last updated" indicator
