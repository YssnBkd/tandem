package org.epoque.tandem.data.seed

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Preferences for tracking mock data seeding status.
 *
 * Used to ensure seeding only happens once per user.
 * DEBUG builds only.
 */
class SeedPreferences(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val DATA_SEEDED = booleanPreferencesKey("mock_data_seeded")
        val SEEDED_USER_ID = stringPreferencesKey("seeded_user_id")
    }

    /**
     * Flow indicating whether data has been seeded.
     */
    val isSeeded: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DATA_SEEDED] ?: false
    }

    /**
     * Get the user ID that was seeded, if any.
     */
    val seededUserId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.SEEDED_USER_ID]
    }

    /**
     * Check if seeding has been done (blocking call).
     */
    suspend fun isSeededSync(): Boolean = isSeeded.first()

    /**
     * Mark seeding as complete for a user.
     */
    suspend fun markSeeded(userId: String) {
        dataStore.edit { prefs ->
            prefs[Keys.DATA_SEEDED] = true
            prefs[Keys.SEEDED_USER_ID] = userId
        }
    }

    /**
     * Reset seeding status (for testing/debugging).
     */
    suspend fun reset() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.DATA_SEEDED)
            prefs.remove(Keys.SEEDED_USER_ID)
        }
    }
}
