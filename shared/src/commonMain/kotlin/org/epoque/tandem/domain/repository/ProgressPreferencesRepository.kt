package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Progress & Insights preferences.
 *
 * Stores milestone celebration tracking to ensure each milestone
 * is only celebrated once.
 */
interface ProgressPreferencesRepository {

    /**
     * Flow of the last celebrated milestone value.
     * Emits updates whenever the value changes.
     * Defaults to 0 (no milestones celebrated) if no preference is saved.
     *
     * Valid values: 0, 5, 10, 20, 50
     */
    val lastCelebratedMilestone: Flow<Int>

    /**
     * Save the last celebrated milestone.
     *
     * @param milestone The milestone value (5, 10, 20, or 50)
     * @throws IllegalArgumentException if milestone is not a valid value
     */
    suspend fun setLastCelebratedMilestone(milestone: Int)

    companion object {
        /**
         * Valid milestone values that can be celebrated.
         */
        val VALID_MILESTONES = listOf(0, 5, 10, 20, 50)
    }
}
