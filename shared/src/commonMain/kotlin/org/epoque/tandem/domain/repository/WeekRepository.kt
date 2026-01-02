package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.Week

/**
 * Repository interface for week data access.
 * Manages week entities, including automatic creation of the current week and review data storage.
 */
interface WeekRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe a specific week by ID.
     * Emits null if week doesn't exist, then creates it if it's the current week.
     *
     * @param weekId ISO 8601 week ID (e.g., "2026-W01")
     * @return Flow of the week (null if not found)
     */
    fun observeWeek(weekId: String): Flow<Week?>

    /**
     * Observe all weeks for a user, ordered by ID descending (most recent first).
     *
     * @param userId The user's ID
     * @return Flow of all weeks for the user
     */
    fun observeWeeksForUser(userId: String): Flow<List<Week>>

    /**
     * Observe past weeks (before the specified week).
     *
     * @param currentWeekId The reference week ID
     * @param userId The user's ID
     * @return Flow of past weeks, ordered descending
     */
    fun observePastWeeks(currentWeekId: String, userId: String): Flow<List<Week>>

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get a week by ID.
     *
     * @param weekId ISO 8601 week ID
     * @return The week, or null if not found
     */
    suspend fun getWeekById(weekId: String): Week?

    /**
     * Get the current week, creating it if it doesn't exist.
     *
     * @param userId The user's ID
     * @return The current week (guaranteed non-null)
     */
    suspend fun getOrCreateCurrentWeek(userId: String): Week

    /**
     * Calculate the current week ID based on system time.
     *
     * @return ISO 8601 week ID for the current date
     */
    fun getCurrentWeekId(): String

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create or update a week.
     * Use for initial week creation or full week updates.
     *
     * @param week The week to save
     * @return The saved week
     * @throws IllegalArgumentException if week ID format is invalid
     * @throws IllegalArgumentException if startDate is not a Monday
     */
    suspend fun saveWeek(week: Week): Week

    /**
     * Update week review data.
     *
     * @param weekId The week ID
     * @param overallRating Rating 1-5 (nullable to clear)
     * @param reviewNote Review notes (nullable)
     * @return The updated week, or null if week not found
     * @throws IllegalArgumentException if rating not in 1-5 range
     */
    suspend fun updateWeekReview(
        weekId: String,
        overallRating: Int?,
        reviewNote: String?
    ): Week?

    /**
     * Mark planning as completed for a week.
     *
     * @param weekId The week ID
     * @return The updated week, or null if not found
     */
    suspend fun markPlanningCompleted(weekId: String): Week?

    /**
     * Delete a week (and cascade delete its tasks via TaskRepository).
     *
     * @param weekId The week ID to delete
     * @return true if deleted, false if not found
     */
    suspend fun deleteWeek(weekId: String): Boolean
}
