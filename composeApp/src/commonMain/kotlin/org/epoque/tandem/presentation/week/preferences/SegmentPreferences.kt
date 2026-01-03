package org.epoque.tandem.presentation.week.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.epoque.tandem.presentation.week.model.Segment

/**
 * Manages segment selection persistence using DataStore.
 *
 * Following Android DataStore best practices:
 * - Type-safe preference keys
 * - Flow-based reactive reads
 * - Transactional writes with updateData
 * - Graceful error handling with default values
 */
class SegmentPreferences(private val dataStore: DataStore<Preferences>) {

    /**
     * Preference keys - defined as type-safe constants.
     */
    private object Keys {
        val SELECTED_SEGMENT = stringPreferencesKey("selected_segment")
    }

    /**
     * Flow of the currently selected segment.
     * Emits updates whenever the segment changes.
     * Defaults to YOU if no preference is saved.
     */
    val selectedSegment: Flow<Segment> = dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_SEGMENT]?.let { segmentName ->
            try {
                Segment.valueOf(segmentName)
            } catch (e: IllegalArgumentException) {
                // If stored value is invalid, return default
                Segment.YOU
            }
        } ?: Segment.YOU
    }

    /**
     * Save the selected segment.
     * Uses transactional update for atomic writes.
     *
     * @param segment The segment to persist
     */
    suspend fun setSelectedSegment(segment: Segment) {
        dataStore.edit { prefs ->
            prefs[Keys.SELECTED_SEGMENT] = segment.name
        }
    }
}
