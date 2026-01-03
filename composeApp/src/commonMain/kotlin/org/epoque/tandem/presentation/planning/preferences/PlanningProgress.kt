package org.epoque.tandem.presentation.planning.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * State object representing planning progress persistence.
 * Used to resume planning if the user exits mid-flow.
 */
data class PlanningProgressState(
    val currentStep: Int = 0,
    val processedRolloverTaskIds: Set<String> = emptySet(),
    val addedTaskIds: Set<String> = emptySet(),
    val acceptedRequestIds: Set<String> = emptySet(),
    val isInProgress: Boolean = false,
    val weekId: String? = null
)

/**
 * Manages planning progress persistence using DataStore.
 * Saves user's position in the planning wizard so they can resume if interrupted.
 * Automatically discards stale progress if the week changes.
 */
class PlanningProgress(private val dataStore: DataStore<Preferences>) {

    /**
     * Preference keys - defined as type-safe constants.
     */
    private object Keys {
        val CURRENT_STEP = intPreferencesKey("planning_current_step")
        val PROCESSED_ROLLOVER_TASK_IDS = stringSetPreferencesKey("planning_processed_rollover_ids")
        val ADDED_TASK_IDS = stringSetPreferencesKey("planning_added_task_ids")
        val ACCEPTED_REQUEST_IDS = stringSetPreferencesKey("planning_accepted_request_ids")
        val IS_IN_PROGRESS = booleanPreferencesKey("planning_is_in_progress")
        val WEEK_ID = stringPreferencesKey("planning_week_id")
    }

    /**
     * Flow of the current planning progress.
     * Emits updates whenever progress is saved.
     * Returns default empty state if no progress is saved.
     */
    val planningProgress: Flow<PlanningProgressState> = dataStore.data.map { prefs ->
        PlanningProgressState(
            currentStep = prefs[Keys.CURRENT_STEP] ?: 0,
            processedRolloverTaskIds = prefs[Keys.PROCESSED_ROLLOVER_TASK_IDS] ?: emptySet(),
            addedTaskIds = prefs[Keys.ADDED_TASK_IDS] ?: emptySet(),
            acceptedRequestIds = prefs[Keys.ACCEPTED_REQUEST_IDS] ?: emptySet(),
            isInProgress = prefs[Keys.IS_IN_PROGRESS] ?: false,
            weekId = prefs[Keys.WEEK_ID]
        )
    }

    /**
     * Save planning progress.
     * Call this after each user action (rollover add/skip, task created, request accepted).
     *
     * @param state The current planning state to persist
     */
    suspend fun saveProgress(state: PlanningProgressState) {
        dataStore.edit { prefs ->
            prefs[Keys.CURRENT_STEP] = state.currentStep
            prefs[Keys.PROCESSED_ROLLOVER_TASK_IDS] = state.processedRolloverTaskIds
            prefs[Keys.ADDED_TASK_IDS] = state.addedTaskIds
            prefs[Keys.ACCEPTED_REQUEST_IDS] = state.acceptedRequestIds
            prefs[Keys.IS_IN_PROGRESS] = state.isInProgress
            state.weekId?.let { prefs[Keys.WEEK_ID] = it }
        }
    }

    /**
     * Clear all planning progress.
     * Call this when planning is completed or when progress is stale (week changed).
     */
    suspend fun clearProgress() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.CURRENT_STEP)
            prefs.remove(Keys.PROCESSED_ROLLOVER_TASK_IDS)
            prefs.remove(Keys.ADDED_TASK_IDS)
            prefs.remove(Keys.ACCEPTED_REQUEST_IDS)
            prefs.remove(Keys.IS_IN_PROGRESS)
            prefs.remove(Keys.WEEK_ID)
        }
    }
}
