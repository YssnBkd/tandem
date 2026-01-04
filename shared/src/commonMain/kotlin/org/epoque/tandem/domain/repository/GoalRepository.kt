package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalStatus

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
    suspend fun getPartnerGoalsLastSyncTime(partnerId: String): Instant?

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

    /**
     * Get active goals for planning suggestions.
     * Returns goals that would benefit from linked tasks this week.
     * @param userId User's ID
     * @return List of active goals suitable for suggestions
     */
    suspend fun getActiveGoalsForSuggestions(userId: String): List<Goal>

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

    /**
     * Clear linkedGoalId from all tasks linked to a goal.
     * Called when goal is deleted.
     * @param goalId Goal ID being deleted
     */
    suspend fun clearLinkedGoalFromTasks(goalId: String)

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
