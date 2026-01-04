package org.epoque.tandem.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.epoque.tandem.presentation.week.WeekViewModel
import org.epoque.tandem.presentation.week.preferences.SegmentPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * DataStore singleton using delegate property.
 *
 * Following Android best practices:
 * - Single instance per file to avoid IllegalStateException
 * - Property delegate ensures lazy initialization
 * - Name is descriptive and scoped to feature
 */
private val Context.weekPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "week_preferences"
)

/**
 * Koin module for Week View feature dependencies.
 *
 * Provides:
 * - DataStore for segment preference persistence
 * - SegmentPreferences wrapper
 * - WeekViewModel with all required dependencies
 */
val weekModule = module {
    // DataStore singleton for week preferences
    single<DataStore<Preferences>> {
        androidContext().weekPreferencesDataStore
    }

    // Segment preferences manager
    single { SegmentPreferences(get()) }

    // Week ViewModel with lifecycle-aware scope
    viewModel { WeekViewModel(
        taskRepository = get(),
        weekRepository = get(),
        segmentPreferences = get(),
        authRepository = get(),
        isReviewWindowOpenUseCase = get(),
        calculateStreakUseCase = get()
    ) }
}
