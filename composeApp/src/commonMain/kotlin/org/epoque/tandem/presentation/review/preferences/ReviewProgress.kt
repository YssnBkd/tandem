package org.epoque.tandem.presentation.review.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.presentation.review.ReviewMode
import org.epoque.tandem.presentation.review.ReviewStep

/**
 * State object representing review progress persistence.
 * Used to resume review if the user exits mid-flow.
 */
data class ReviewProgressState(
    val weekId: String? = null,
    val reviewMode: ReviewMode = ReviewMode.SOLO,
    val currentStep: ReviewStep = ReviewStep.MODE_SELECT,
    val overallRating: Int? = null,
    val overallNote: String = "",
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, TaskStatus> = emptyMap(),
    val taskNotes: Map<String, String> = emptyMap(),
    val isInProgress: Boolean = false,
    val lastUpdatedAt: Long = 0
)

/**
 * Manages review progress persistence using DataStore.
 * Saves user's position in the review wizard so they can resume if interrupted.
 * Automatically discards stale progress if the week changes.
 */
class ReviewProgress(private val dataStore: DataStore<Preferences>) {

    /**
     * Preference keys - defined as type-safe constants.
     */
    private object Keys {
        val WEEK_ID = stringPreferencesKey("review_week_id")
        val REVIEW_MODE = stringPreferencesKey("review_mode")
        val CURRENT_STEP = stringPreferencesKey("review_current_step")
        val OVERALL_RATING = intPreferencesKey("review_overall_rating")
        val OVERALL_NOTE = stringPreferencesKey("review_overall_note")
        val CURRENT_TASK_INDEX = intPreferencesKey("review_current_task_index")
        val TASK_OUTCOMES = stringSetPreferencesKey("review_task_outcomes")
        val TASK_NOTES = stringSetPreferencesKey("review_task_notes")
        val IS_IN_PROGRESS = booleanPreferencesKey("review_is_in_progress")
        val LAST_UPDATED_AT = longPreferencesKey("review_last_updated_at")
    }

    /**
     * Flow of the current review progress.
     * Emits updates whenever progress is saved.
     * Returns default empty state if no progress is saved.
     */
    val reviewProgress: Flow<ReviewProgressState> = dataStore.data.map { prefs ->
        ReviewProgressState(
            weekId = prefs[Keys.WEEK_ID],
            reviewMode = prefs[Keys.REVIEW_MODE]?.let {
                runCatching { ReviewMode.valueOf(it) }.getOrDefault(ReviewMode.SOLO)
            } ?: ReviewMode.SOLO,
            currentStep = prefs[Keys.CURRENT_STEP]?.let {
                runCatching { ReviewStep.valueOf(it) }.getOrDefault(ReviewStep.MODE_SELECT)
            } ?: ReviewStep.MODE_SELECT,
            overallRating = prefs[Keys.OVERALL_RATING],
            overallNote = prefs[Keys.OVERALL_NOTE] ?: "",
            currentTaskIndex = prefs[Keys.CURRENT_TASK_INDEX] ?: 0,
            taskOutcomes = deserializeTaskOutcomes(prefs[Keys.TASK_OUTCOMES]),
            taskNotes = deserializeTaskNotes(prefs[Keys.TASK_NOTES]),
            isInProgress = prefs[Keys.IS_IN_PROGRESS] ?: false,
            lastUpdatedAt = prefs[Keys.LAST_UPDATED_AT] ?: 0
        )
    }

    /**
     * Save review progress.
     * Call this after each user action (rating, task outcome, navigation).
     *
     * @param state The current review state to persist
     */
    suspend fun saveProgress(state: ReviewProgressState) {
        dataStore.edit { prefs ->
            state.weekId?.let { prefs[Keys.WEEK_ID] = it }
            prefs[Keys.REVIEW_MODE] = state.reviewMode.name
            prefs[Keys.CURRENT_STEP] = state.currentStep.name
            state.overallRating?.let { prefs[Keys.OVERALL_RATING] = it }
            prefs[Keys.OVERALL_NOTE] = state.overallNote
            prefs[Keys.CURRENT_TASK_INDEX] = state.currentTaskIndex
            prefs[Keys.TASK_OUTCOMES] = serializeTaskOutcomes(state.taskOutcomes)
            prefs[Keys.TASK_NOTES] = serializeTaskNotes(state.taskNotes)
            prefs[Keys.IS_IN_PROGRESS] = state.isInProgress
            prefs[Keys.LAST_UPDATED_AT] = state.lastUpdatedAt
        }
    }

    /**
     * Clear all review progress.
     * Call this when review is completed or when progress is stale (week changed).
     */
    suspend fun clearProgress() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.WEEK_ID)
            prefs.remove(Keys.REVIEW_MODE)
            prefs.remove(Keys.CURRENT_STEP)
            prefs.remove(Keys.OVERALL_RATING)
            prefs.remove(Keys.OVERALL_NOTE)
            prefs.remove(Keys.CURRENT_TASK_INDEX)
            prefs.remove(Keys.TASK_OUTCOMES)
            prefs.remove(Keys.TASK_NOTES)
            prefs.remove(Keys.IS_IN_PROGRESS)
            prefs.remove(Keys.LAST_UPDATED_AT)
        }
    }

    /**
     * Serialize task outcomes to string set for DataStore storage.
     * Format: "taskId:STATUS"
     */
    private fun serializeTaskOutcomes(outcomes: Map<String, TaskStatus>): Set<String> {
        return outcomes.map { (taskId, status) -> "$taskId:${status.name}" }.toSet()
    }

    /**
     * Deserialize task outcomes from string set.
     */
    private fun deserializeTaskOutcomes(serialized: Set<String>?): Map<String, TaskStatus> {
        if (serialized == null) return emptyMap()
        return serialized.mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2) {
                val taskId = parts[0]
                val status = runCatching { TaskStatus.valueOf(parts[1]) }.getOrNull()
                if (status != null) taskId to status else null
            } else null
        }.toMap()
    }

    /**
     * Serialize task notes to string set for DataStore storage.
     * Format: "taskId:note" (note is URL-encoded to handle special chars)
     */
    private fun serializeTaskNotes(notes: Map<String, String>): Set<String> {
        return notes.map { (taskId, note) ->
            "$taskId:${note.encodeForStorage()}"
        }.toSet()
    }

    /**
     * Deserialize task notes from string set.
     */
    private fun deserializeTaskNotes(serialized: Set<String>?): Map<String, String> {
        if (serialized == null) return emptyMap()
        return serialized.mapNotNull { entry ->
            val colonIndex = entry.indexOf(':')
            if (colonIndex > 0) {
                val taskId = entry.substring(0, colonIndex)
                val note = entry.substring(colonIndex + 1).decodeFromStorage()
                taskId to note
            } else null
        }.toMap()
    }

    /**
     * Simple encoding to handle colons and special characters in notes.
     */
    private fun String.encodeForStorage(): String {
        return this.replace("\\", "\\\\")
            .replace(":", "\\c")
            .replace("\n", "\\n")
    }

    /**
     * Decode storage-encoded string back to original.
     */
    private fun String.decodeFromStorage(): String {
        return this.replace("\\n", "\n")
            .replace("\\c", ":")
            .replace("\\\\", "\\")
    }
}
