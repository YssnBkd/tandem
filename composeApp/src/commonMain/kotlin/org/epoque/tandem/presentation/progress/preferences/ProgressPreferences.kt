package org.epoque.tandem.presentation.progress.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.epoque.tandem.domain.repository.ProgressPreferencesRepository

/**
 * Manages Progress & Insights preferences using DataStore.
 *
 * Stores milestone celebration tracking to ensure each milestone
 * is only celebrated once.
 *
 * Following Android DataStore best practices:
 * - Type-safe preference keys
 * - Flow-based reactive reads
 * - Transactional writes with edit
 */
class ProgressPreferences(
    private val dataStore: DataStore<Preferences>
) : ProgressPreferencesRepository {

    /**
     * Preference keys - defined as type-safe constants.
     */
    private object Keys {
        val LAST_CELEBRATED_MILESTONE = intPreferencesKey("last_celebrated_milestone")
    }

    /**
     * Flow of the last celebrated milestone value.
     * Emits updates whenever the value changes.
     * Defaults to 0 (no milestones celebrated) if no preference is saved.
     *
     * Valid values: 0, 5, 10, 20, 50
     */
    override val lastCelebratedMilestone: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_CELEBRATED_MILESTONE] ?: 0
    }

    /**
     * Save the last celebrated milestone.
     * Uses transactional update for atomic writes.
     *
     * @param milestone The milestone value (5, 10, 20, or 50)
     */
    override suspend fun setLastCelebratedMilestone(milestone: Int) {
        require(milestone in ProgressPreferencesRepository.VALID_MILESTONES) {
            "Invalid milestone value: $milestone. Must be one of ${ProgressPreferencesRepository.VALID_MILESTONES}"
        }
        dataStore.edit { prefs ->
            prefs[Keys.LAST_CELEBRATED_MILESTONE] = milestone
        }
    }
}
