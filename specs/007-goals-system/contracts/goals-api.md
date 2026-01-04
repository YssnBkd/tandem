# API Contracts: Goals System

**Feature**: 007-goals-system
**Date**: 2026-01-04

## SQLDelight Schema

### Goal.sq

```sql
import kotlinx.datetime.Instant;
import org.epoque.tandem.domain.model.GoalType;
import org.epoque.tandem.domain.model.GoalStatus;

CREATE TABLE Goal (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    icon TEXT NOT NULL,
    type TEXT AS GoalType NOT NULL,
    target_per_week INTEGER,
    target_total INTEGER,
    duration_weeks INTEGER,
    start_week_id TEXT NOT NULL,
    owner_id TEXT NOT NULL,
    is_shared INTEGER NOT NULL DEFAULT 0,
    current_progress INTEGER NOT NULL DEFAULT 0,
    current_week_id TEXT NOT NULL,
    status TEXT AS GoalStatus NOT NULL DEFAULT 'ACTIVE',
    created_at INTEGER AS Instant NOT NULL,
    updated_at INTEGER AS Instant NOT NULL
);

CREATE INDEX idx_goals_owner ON Goal(owner_id);
CREATE INDEX idx_goals_status ON Goal(status);
CREATE INDEX idx_goals_shared ON Goal(is_shared) WHERE is_shared = 1;

-- Insert or replace a goal
upsertGoal:
INSERT OR REPLACE INTO Goal (
    id, name, icon, type, target_per_week, target_total, duration_weeks,
    start_week_id, owner_id, is_shared, current_progress, current_week_id,
    status, created_at, updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- Get all goals for user (personal + shared where partner)
getAllGoalsForUser:
SELECT * FROM Goal
WHERE owner_id = :userId
   OR (is_shared = 1 AND owner_id = :partnerId)
ORDER BY created_at DESC;

-- Get active goals for user
getActiveGoalsForUser:
SELECT * FROM Goal
WHERE (owner_id = :userId OR (is_shared = 1 AND owner_id = :partnerId))
  AND status = 'ACTIVE'
ORDER BY created_at DESC;

-- Get goal by ID
getGoalById:
SELECT * FROM Goal WHERE id = ?;

-- Get goals by owner
getGoalsByOwner:
SELECT * FROM Goal WHERE owner_id = ? ORDER BY created_at DESC;

-- Get shared goals (visible to both partners)
getSharedGoals:
SELECT * FROM Goal
WHERE is_shared = 1
  AND (owner_id = :userId OR owner_id = :partnerId)
ORDER BY created_at DESC;

-- Count active goals for user (for limit check)
countActiveGoalsForUser:
SELECT COUNT(*) FROM Goal WHERE owner_id = ? AND status = 'ACTIVE';

-- Update goal progress
updateProgress:
UPDATE Goal SET
    current_progress = ?,
    current_week_id = ?,
    updated_at = ?
WHERE id = ?;

-- Update goal status
updateStatus:
UPDATE Goal SET
    status = ?,
    updated_at = ?
WHERE id = ?;

-- Reset weekly progress (for WEEKLY_HABIT goals)
resetWeeklyProgress:
UPDATE Goal SET
    current_progress = 0,
    current_week_id = ?,
    updated_at = ?
WHERE id = ?;

-- Delete goal by ID
deleteGoalById:
DELETE FROM Goal WHERE id = ?;

-- Get goals needing weekly reset
getGoalsNeedingWeeklyReset:
SELECT * FROM Goal
WHERE type = 'WEEKLY_HABIT'
  AND status = 'ACTIVE'
  AND current_week_id != ?;

-- Get expired goals (for status update check)
getExpiredGoals:
SELECT * FROM Goal
WHERE status = 'ACTIVE'
  AND duration_weeks IS NOT NULL;
```

---

### GoalProgress.sq

```sql
import kotlinx.datetime.Instant;

CREATE TABLE GoalProgress (
    id TEXT NOT NULL PRIMARY KEY,
    goal_id TEXT NOT NULL REFERENCES Goal(id) ON DELETE CASCADE,
    week_id TEXT NOT NULL,
    progress_value INTEGER NOT NULL,
    target_value INTEGER NOT NULL,
    created_at INTEGER AS Instant NOT NULL,
    UNIQUE(goal_id, week_id)
);

CREATE INDEX idx_goal_progress_goal ON GoalProgress(goal_id);
CREATE INDEX idx_goal_progress_week ON GoalProgress(week_id);

-- Insert weekly progress snapshot
insertProgress:
INSERT OR REPLACE INTO GoalProgress (
    id, goal_id, week_id, progress_value, target_value, created_at
) VALUES (?, ?, ?, ?, ?, ?);

-- Get progress history for goal
getProgressForGoal:
SELECT * FROM GoalProgress
WHERE goal_id = ?
ORDER BY week_id DESC;

-- Get progress for specific week
getProgressForWeek:
SELECT * FROM GoalProgress
WHERE goal_id = ? AND week_id = ?;

-- Get recent progress (last N weeks)
getRecentProgress:
SELECT * FROM GoalProgress
WHERE goal_id = ?
ORDER BY week_id DESC
LIMIT ?;

-- Delete all progress for goal
deleteProgressForGoal:
DELETE FROM GoalProgress WHERE goal_id = ?;
```

---

## Repository Interface Contracts

### GoalRepository

```kotlin
package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalStatus
import org.epoque.tandem.domain.model.GoalType

/**
 * Repository for managing goals and progress tracking.
 */
interface GoalRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe all goals for user (personal + shared with partner).
     * @param userId Current user's ID
     * @param partnerId Partner's ID (null if no partner)
     * @return Flow of all accessible goals
     */
    fun observeGoals(userId: String, partnerId: String?): Flow<List<Goal>>

    /**
     * Observe active goals only.
     * @param userId Current user's ID
     * @param partnerId Partner's ID (null if no partner)
     * @return Flow of active goals
     */
    fun observeActiveGoals(userId: String, partnerId: String?): Flow<List<Goal>>

    /**
     * Observe a single goal by ID.
     * @param goalId Goal ID
     * @return Flow of goal (null if not found or deleted)
     */
    fun observeGoal(goalId: String): Flow<Goal?>

    /**
     * Observe progress history for a goal.
     * @param goalId Goal ID
     * @return Flow of weekly progress records
     */
    fun observeProgressHistory(goalId: String): Flow<List<GoalProgress>>

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get goal by ID.
     * @param goalId Goal ID
     * @return Goal or null if not found
     */
    suspend fun getGoalById(goalId: String): Goal?

    /**
     * Get count of active goals for user.
     * @param userId User's ID
     * @return Count of active goals
     */
    suspend fun getActiveGoalCount(userId: String): Int

    /**
     * Get goals linked to a specific task.
     * Used for displaying goal badge on task.
     * @param goalIds List of goal IDs
     * @return Map of goalId to Goal
     */
    suspend fun getGoalsById(goalIds: List<String>): Map<String, Goal>

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a new goal.
     * @param goal Goal to create (id, timestamps will be generated)
     * @return Created goal with generated ID
     * @throws GoalException.LimitExceeded if user has 10+ active goals
     * @throws GoalException.InvalidGoal if validation fails
     */
    suspend fun createGoal(goal: Goal): Goal

    /**
     * Update goal properties.
     * @param goalId Goal ID
     * @param name Updated name
     * @param icon Updated icon
     * @return Updated goal or null if not found
     */
    suspend fun updateGoal(goalId: String, name: String, icon: String): Goal?

    /**
     * Increment goal progress.
     * Called when linked task is completed.
     * @param goalId Goal ID
     * @param amount Amount to increment (default 1)
     * @return Updated goal or null if not found
     */
    suspend fun incrementProgress(goalId: String, amount: Int = 1): Goal?

    /**
     * Update goal status.
     * @param goalId Goal ID
     * @param status New status
     * @return Updated goal or null if not found
     */
    suspend fun updateStatus(goalId: String, status: GoalStatus): Goal?

    /**
     * Record weekly progress snapshot.
     * Called at week boundary before reset.
     * @param goalId Goal ID
     * @param progressValue Progress achieved
     * @param targetValue Target for that week
     * @param weekId Week ID being recorded
     */
    suspend fun recordWeeklyProgress(
        goalId: String,
        progressValue: Int,
        targetValue: Int,
        weekId: String
    )

    /**
     * Reset weekly progress for a goal.
     * Called at start of new week for WEEKLY_HABIT goals.
     * @param goalId Goal ID
     * @param newWeekId New week's ID
     */
    suspend fun resetWeeklyProgress(goalId: String, newWeekId: String)

    /**
     * Delete a goal.
     * @param goalId Goal ID
     * @return true if deleted, false if not found
     */
    suspend fun deleteGoal(goalId: String): Boolean

    // ═══════════════════════════════════════════════════════════════════════════
    // MAINTENANCE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Check and process weekly resets for all goals.
     * Should be called on app launch and resume.
     * @param currentWeekId Current week ID
     */
    suspend fun processWeeklyResets(currentWeekId: String)

    /**
     * Check and update expired goals.
     * Should be called periodically.
     * @param currentWeekId Current week ID
     */
    suspend fun checkGoalExpirations(currentWeekId: String)
}

/**
 * Goal-related exceptions.
 */
sealed class GoalException(message: String) : Exception(message) {
    object LimitExceeded : GoalException("Maximum of 10 active goals reached")
    data class InvalidGoal(override val message: String) : GoalException(message)
    object NotFound : GoalException("Goal not found")
}
```

---

## Supabase Tables (for shared goals sync)

### goals

```sql
CREATE TABLE goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL CHECK (char_length(name) BETWEEN 1 AND 100),
    icon TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('WEEKLY_HABIT', 'RECURRING_TASK', 'TARGET_AMOUNT')),
    target_per_week INTEGER,
    target_total INTEGER,
    duration_weeks INTEGER CHECK (duration_weeks IN (4, 8, 12) OR duration_weeks IS NULL),
    start_week_id TEXT NOT NULL,
    owner_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    current_progress INTEGER NOT NULL DEFAULT 0,
    current_week_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'EXPIRED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_weekly_habit CHECK (
        type != 'WEEKLY_HABIT' OR target_per_week IS NOT NULL
    ),
    CONSTRAINT chk_target_amount CHECK (
        type != 'TARGET_AMOUNT' OR target_total IS NOT NULL
    )
);

-- Indexes
CREATE INDEX idx_goals_owner ON goals(owner_id);
CREATE INDEX idx_goals_shared ON goals(is_shared) WHERE is_shared = true;
CREATE INDEX idx_goals_status ON goals(status);

-- Row Level Security
ALTER TABLE goals ENABLE ROW LEVEL SECURITY;

-- Owner can do everything with their goals
CREATE POLICY "Users can manage own goals"
ON goals FOR ALL
USING (auth.uid() = owner_id);

-- Partners can view shared goals
CREATE POLICY "Partners can view shared goals"
ON goals FOR SELECT
USING (
    is_shared = true
    AND EXISTS (
        SELECT 1 FROM partnerships p
        WHERE p.status = 'ACTIVE'
        AND (
            (p.user1_id = auth.uid() AND p.user2_id = owner_id)
            OR (p.user2_id = auth.uid() AND p.user1_id = owner_id)
        )
    )
);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_goals_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER goals_updated_at
BEFORE UPDATE ON goals
FOR EACH ROW EXECUTE FUNCTION update_goals_updated_at();
```

### goal_progress

```sql
CREATE TABLE goal_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    week_id TEXT NOT NULL,
    progress_value INTEGER NOT NULL,
    target_value INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(goal_id, week_id)
);

-- Indexes
CREATE INDEX idx_goal_progress_goal ON goal_progress(goal_id);
CREATE INDEX idx_goal_progress_week ON goal_progress(week_id);

-- Row Level Security
ALTER TABLE goal_progress ENABLE ROW LEVEL SECURITY;

-- Users can view progress for their goals
CREATE POLICY "Users can view own goal progress"
ON goal_progress FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM goals g
        WHERE g.id = goal_id
        AND (g.owner_id = auth.uid() OR (
            g.is_shared = true
            AND EXISTS (
                SELECT 1 FROM partnerships p
                WHERE p.status = 'ACTIVE'
                AND (
                    (p.user1_id = auth.uid() AND p.user2_id = g.owner_id)
                    OR (p.user2_id = auth.uid() AND p.user1_id = g.owner_id)
                )
            )
        ))
    )
);

-- Users can insert progress for their goals
CREATE POLICY "Users can insert own goal progress"
ON goal_progress FOR INSERT
WITH CHECK (
    EXISTS (
        SELECT 1 FROM goals g
        WHERE g.id = goal_id AND g.owner_id = auth.uid()
    )
);
```

---

## Realtime Subscriptions

### Shared Goals Channel

```kotlin
// Subscribe to shared goal changes from partner
private fun setupSharedGoalsSync(partnerId: String, partnershipId: String) {
    val channel = supabase.channel("shared-goals-$partnershipId")

    val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
        table = "goals"
        filter = "is_shared=eq.true"
    }

    changes
        .filter { action ->
            // Only process partner's shared goals
            when (action) {
                is PostgresAction.Insert -> action.record.ownerId == partnerId
                is PostgresAction.Update -> action.record.ownerId == partnerId
                is PostgresAction.Delete -> action.oldRecord?.ownerId == partnerId
                else -> false
            }
        }
        .onEach { action ->
            when (action) {
                is PostgresAction.Insert -> handleGoalCreated(action.record)
                is PostgresAction.Update -> handleGoalUpdated(action.record)
                is PostgresAction.Delete -> handleGoalDeleted(action.oldRecord)
            }
        }
        .launchIn(viewModelScope)

    viewModelScope.launch {
        channel.subscribe()
    }
}
```

---

## Week Calculation Utilities

```kotlin
package org.epoque.tandem.domain.util

import kotlinx.datetime.*

object WeekCalculator {
    /**
     * Get ISO 8601 week ID for a date.
     * Format: "YYYY-Www" (e.g., "2026-W01")
     */
    fun getWeekId(
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ): String {
        val dayOfYear = date.dayOfYear
        val dayOfWeek = date.dayOfWeek.isoDayNumber
        val weekNumber = (dayOfYear - dayOfWeek + 10) / 7
        return "${date.year}-W${weekNumber.toString().padStart(2, '0')}"
    }

    /**
     * Calculate end week ID given start and duration.
     */
    fun calculateEndWeekId(startWeekId: String, durationWeeks: Int): String {
        val startDate = parseWeekId(startWeekId)
        val endDate = startDate.plus(durationWeeks, DateTimeUnit.WEEK)
        return getWeekId(endDate)
    }

    /**
     * Parse week ID to LocalDate (Monday of that week).
     */
    fun parseWeekId(weekId: String): LocalDate {
        val (year, week) = weekId.split("-W").let {
            it[0].toInt() to it[1].toInt()
        }
        // Find first Monday of the year
        var date = LocalDate(year, 1, 1)
        while (date.dayOfWeek != DayOfWeek.MONDAY) {
            date = date.plus(1, DateTimeUnit.DAY)
        }
        // Add weeks
        return date.plus(week - 1, DateTimeUnit.WEEK)
    }

    /**
     * Compare week IDs.
     * Returns negative if a < b, 0 if equal, positive if a > b.
     */
    fun compareWeekIds(a: String, b: String): Int {
        return a.compareTo(b)
    }

    /**
     * Check if week a is after week b.
     */
    fun isAfter(a: String, b: String): Boolean {
        return compareWeekIds(a, b) > 0
    }
}
```
