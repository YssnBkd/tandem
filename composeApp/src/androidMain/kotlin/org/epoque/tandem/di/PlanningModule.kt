package org.epoque.tandem.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.epoque.tandem.domain.usecase.planning.CompletePlanningUseCase
import org.epoque.tandem.presentation.planning.PlanningViewModel
import org.epoque.tandem.presentation.planning.preferences.PlanningProgress
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * DataStore singleton for planning preferences.
 *
 * Following Android best practices:
 * - Single instance per file to avoid IllegalStateException
 * - Property delegate ensures lazy initialization
 * - Separate from week preferences to avoid conflicts
 */
private val Context.planningPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "planning_preferences"
)

/**
 * Koin module for Weekly Planning feature dependencies.
 *
 * Provides:
 * - DataStore for planning progress persistence (named "planning")
 * - PlanningProgress wrapper
 * - PlanningViewModel with all required dependencies
 */
val planningModule = module {
    // DataStore singleton for planning preferences (named to avoid conflict with week preferences)
    single<DataStore<Preferences>>(named("planning")) {
        androidContext().planningPreferencesDataStore
    }

    // Planning progress manager
    single { PlanningProgress(get(named("planning"))) }

    // Use cases
    factory { CompletePlanningUseCase(get(), get(), get(), get()) }

    // Planning ViewModel with lifecycle-aware scope
    viewModel {
        PlanningViewModel(
            taskRepository = get(),
            weekRepository = get(),
            authRepository = get(),
            goalRepository = get(),
            planningProgress = get(),
            completePlanningUseCase = get()
        )
    }
}
