package org.epoque.tandem.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.epoque.tandem.data.seed.MockDataSeeder
import org.epoque.tandem.data.seed.SeedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * DataStore for seed preferences.
 *
 * Uses extension property pattern per Jetpack DataStore best practices:
 * - Single instance per file
 * - Name is descriptive and scoped to feature
 * - Separate from other feature preferences to avoid conflicts
 *
 * DEBUG builds only. This file should be deleted before production.
 */
private val Context.seedPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "seed_preferences"
)

/**
 * Koin module for mock data seeding dependencies.
 *
 * DEBUG builds only. Remove this module before production by:
 * 1. Deleting this file
 * 2. Removing `seedModule` from TandemApp.kt
 * 3. Removing seeding trigger from TandemNavHost.kt
 */
val seedModule = module {
    // DataStore singleton for seed preferences (named to avoid conflict with other preferences)
    single<DataStore<Preferences>>(named("seed")) {
        androidContext().seedPreferencesDataStore
    }

    // Seed preferences manager
    single { SeedPreferences(get(named("seed"))) }

    // Mock data seeder
    single { MockDataSeeder(get(), get()) }
}
