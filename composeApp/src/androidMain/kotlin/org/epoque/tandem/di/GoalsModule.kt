package org.epoque.tandem.di

import org.epoque.tandem.data.repository.GoalRepositoryAndroidImpl
import org.epoque.tandem.data.repository.GoalRepositoryImpl
import org.epoque.tandem.domain.repository.GoalRepository
import org.epoque.tandem.presentation.goals.GoalsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Goals feature dependencies.
 *
 * Provides:
 * - GoalRepository implementation with Supabase sync capabilities
 * - GoalsViewModel with all required dependencies
 */
val goalsModule = module {
    // Base repository (local-only operations)
    single { GoalRepositoryImpl(get()) }

    // Android repository with Supabase sync
    single<GoalRepository> {
        GoalRepositoryAndroidImpl(
            database = get(),
            supabase = get(),
            baseRepository = get<GoalRepositoryImpl>()
        )
    }

    // ViewModel
    viewModel {
        GoalsViewModel(
            goalRepository = get(),
            authRepository = get(),
            partnerRepository = get()
        )
    }
}
