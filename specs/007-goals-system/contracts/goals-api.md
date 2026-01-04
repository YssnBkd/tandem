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
    current_progress INTEGER NOT NULL DEFAULT 0,
    current_week_id TEXT NOT NULL,
    status TEXT AS GoalStatus NOT NULL DEFAULT 'ACTIVE',
    created_at INTEGER AS Instant NOT NULL,
    updated_at INTEGER AS Instant NOT NULL
);

CREATE INDEX idx_goals_owner ON Goal(owner_id);
CREATE INDEX idx_goals_status ON Goal(status);

-- Insert or replace a goal
upsertGoal:
INSERT OR REPLACE INTO Goal (
    id, name, icon, type, target_per_week, target_total, duration_weeks,
    start_week_id, owner_id, current_progress, current_week_id,
    status, created_at, updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- Get all goals owned by user
getMyGoals:
SELECT * FROM Goal
WHERE owner_id = :userId
ORDER BY created_at DESC;

-- Get active goals owned by user
getMyActiveGoals:
SELECT * FROM Goal
WHERE owner_id = :userId
  AND status = 'ACTIVE'
ORDER BY created_at DESC;

-- Get goal by ID
getGoalById:
SELECT * FROM Goal WHERE id = ?;

-- Get goals by owner
getGoalsByOwner:
SELECT * FROM Goal WHERE owner_id = ? ORDER BY created_at DESC;

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

### PartnerGoal.sq (Local cache for partner's goals)

```sql
import kotlinx.datetime.Instant;
import org.epoque.tandem.domain.model.GoalType;
import org.epoque.tandem.domain.model.GoalStatus;

-- Separate table for caching partner's goals (read-only local copy)
CREATE TABLE PartnerGoal (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    icon TEXT NOT NULL,
    type TEXT AS GoalType NOT NULL,
    target_per_week INTEGER,
    target_total INTEGER,
    duration_weeks INTEGER,
    start_week_id TEXT NOT NULL,
    owner_id TEXT NOT NULL,
    current_progress INTEGER NOT NULL DEFAULT 0,
    current_week_id TEXT NOT NULL,
    status TEXT AS GoalStatus NOT NULL DEFAULT 'ACTIVE',
    created_at INTEGER AS Instant NOT NULL,
    updated_at INTEGER AS Instant NOT NULL,
    synced_at INTEGER AS Instant NOT NULL
);

CREATE INDEX idx_partner_goals_owner ON PartnerGoal(owner_id);

-- Insert or replace partner goal (from sync)
upsertPartnerGoal:
INSERT OR REPLACE INTO PartnerGoal (
    id, name, icon, type, target_per_week, target_total, duration_weeks,
    start_week_id, owner_id, current_progress, current_week_id,
    status, created_at, updated_at, synced_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- Get all partner goals
getPartnerGoals:
SELECT * FROM PartnerGoal
WHERE owner_id = :partnerId
ORDER BY created_at DESC;

-- Get active partner goals
getActivePartnerGoals:
SELECT * FROM PartnerGoal
WHERE owner_id = :partnerId
  AND status = 'ACTIVE'
ORDER BY created_at DESC;

-- Get partner goal by ID
getPartnerGoalById:
SELECT * FROM PartnerGoal WHERE id = ?;

-- Delete partner goal (when partner deletes their goal)
deletePartnerGoalById:
DELETE FROM PartnerGoal WHERE id = ?;

-- Clear all partner goals (on partner disconnect)
clearPartnerGoals:
DELETE FROM PartnerGoal WHERE owner_id = ?;

-- Get last sync time for partner goals
getLastSyncTime:
SELECT MAX(synced_at) FROM PartnerGoal WHERE owner_id = ?;
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
 * Each user owns their goals exclusively. Partners can view each other's goals (read-only).
 */
interface GoalRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS - Own Goals (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe all goals owned by the user.
     * @param userId Current user's ID
     * @return Flow of user's goals
     */
    fun observeMyGoals(userId: String): Flow<List<Goal>>

    /**
     * Observe active goals owned by the user.
     * @param userId Current user's ID
     * @return Flow of user's active goals
     */
    fun observeMyActiveGoals(userId: String): Flow<List<Goal>>

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
    // READ OPERATIONS - Partner Goals (Read-Only)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe partner's goals (read-only, from local cache).
     * @param partnerId Partner's user ID
     * @return Flow of partner's goals
     */
    fun observePartnerGoals(partnerId: String): Flow<List<Goal>>

    /**
     * Get a partner's goal by ID (read-only).
     * @param goalId Goal ID
     * @return Goal or null if not found
     */
    suspend fun getPartnerGoalById(goalId: String): Goal?

    /**
     * Get last sync time for partner goals.
     * Used to show "Last updated" indicator when offline.
     * @param partnerId Partner's user ID
     * @return Last sync timestamp or null
     */
    suspend fun getPartnerGoalsLastSyncTime(partnerId: String): kotlinx.datetime.Instant?

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get goal by ID (own goals only).
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
     * Get goals by IDs (for task goal badges).
     * @param goalIds List of goal IDs
     * @return Map of goalId to Goal
     */
    suspend fun getGoalsById(goalIds: List<String>): Map<String, Goal>

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS (Own Goals Only)
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
     * Update goal properties (own goals only).
     * @param goalId Goal ID
     * @param name Updated name
     * @param icon Updated icon
     * @return Updated goal or null if not found
     */
    suspend fun updateGoal(goalId: String, name: String, icon: String): Goal?

    /**
     * Increment goal progress (own goals only).
     * Called when linked task is completed.
     * @param goalId Goal ID
     * @param amount Amount to increment (default 1)
     * @return Updated goal or null if not found
     */
    suspend fun incrementProgress(goalId: String, amount: Int = 1): Goal?

    /**
     * Update goal status (own goals only).
     * @param goalId Goal ID
     * @param status New status
     * @return Updated goal or null if not found
     */
    suspend fun updateStatus(goalId: String, status: GoalStatus): Goal?

    /**
     * Record weekly progress snapshot (own goals only).
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
     * Reset weekly progress for a goal (own goals only).
     * Called at start of new week for WEEKLY_HABIT goals.
     * @param goalId Goal ID
     * @param newWeekId New week's ID
     */
    suspend fun resetWeeklyProgress(goalId: String, newWeekId: String)

    /**
     * Delete a goal (own goals only).
     * @param goalId Goal ID
     * @return true if deleted, false if not found
     */
    suspend fun deleteGoal(goalId: String): Boolean

    // ═══════════════════════════════════════════════════════════════════════════
    // SYNC OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Sync partner's goals from Supabase to local cache.
     * Called when partner changes or on app resume.
     * @param partnerId Partner's user ID
     */
    suspend fun syncPartnerGoals(partnerId: String)

    /**
     * Clear partner goal cache (on partner disconnect).
     * @param partnerId Partner's user ID
     */
    suspend fun clearPartnerGoalCache(partnerId: String)

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
    object NotOwner : GoalException("Cannot modify goals you don't own")
}
```

---

## Supabase Tables (for partner goal visibility sync)

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
CREATE INDEX idx_goals_status ON goals(status);

-- Row Level Security
ALTER TABLE goals ENABLE ROW LEVEL SECURITY;

-- Owner can do everything with their goals
CREATE POLICY "Users can manage own goals"
ON goals FOR ALL
USING (auth.uid() = owner_id);

-- Partners can VIEW each other's goals (read-only)
CREATE POLICY "Partners can view partner goals"
ON goals FOR SELECT
USING (
    EXISTS (
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

-- Users can manage progress for their own goals
CREATE POLICY "Users can manage own goal progress"
ON goal_progress FOR ALL
USING (
    EXISTS (
        SELECT 1 FROM goals g
        WHERE g.id = goal_id AND g.owner_id = auth.uid()
    )
);

-- Partners can VIEW progress for partner's goals (read-only)
CREATE POLICY "Partners can view partner goal progress"
ON goal_progress FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM goals g
        WHERE g.id = goal_id
        AND EXISTS (
            SELECT 1 FROM partnerships p
            WHERE p.status = 'ACTIVE'
            AND (
                (p.user1_id = auth.uid() AND p.user2_id = g.owner_id)
                OR (p.user2_id = auth.uid() AND p.user1_id = g.owner_id)
            )
        )
    )
);
```

---

## Realtime Subscriptions

### Partner Goals Channel (Read-Only Sync)

```kotlin
// Subscribe to partner's goal changes for read-only visibility
private fun setupPartnerGoalsSync(partnerId: String, partnershipId: String) {
    val channel = supabase.channel("partner-goals-$partnershipId")

    val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
        table = "goals"
        filter = "owner_id=eq.$partnerId"
    }

    changes
        .onEach { action ->
            when (action) {
                is PostgresAction.Insert -> handlePartnerGoalCreated(action.record)
                is PostgresAction.Update -> handlePartnerGoalUpdated(action.record)
                is PostgresAction.Delete -> handlePartnerGoalDeleted(action.oldRecord)
            }
        }
        .launchIn(viewModelScope)

    viewModelScope.launch {
        channel.subscribe()
    }
}

// Handle partner goal created - cache locally
private suspend fun handlePartnerGoalCreated(record: GoalRecord) {
    goalRepository.cachePartnerGoal(record.toGoal())
}

// Handle partner goal updated - update local cache
private suspend fun handlePartnerGoalUpdated(record: GoalRecord) {
    goalRepository.cachePartnerGoal(record.toGoal())
}

// Handle partner goal deleted - remove from local cache
private suspend fun handlePartnerGoalDeleted(record: GoalRecord?) {
    record?.id?.let { goalRepository.removePartnerGoalFromCache(it) }
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
