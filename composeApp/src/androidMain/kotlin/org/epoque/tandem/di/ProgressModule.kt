package org.epoque.tandem.di

import org.epoque.tandem.domain.repository.ProgressPreferencesRepository
import org.epoque.tandem.domain.usecase.progress.CalculatePartnerStreakUseCase
import org.epoque.tandem.domain.usecase.progress.GetCompletionStatsUseCase
import org.epoque.tandem.domain.usecase.progress.GetCompletionTrendsUseCase
import org.epoque.tandem.domain.usecase.progress.GetMonthlyCompletionUseCase
import org.epoque.tandem.domain.usecase.progress.GetPastWeekDetailUseCase
import org.epoque.tandem.domain.usecase.progress.GetPastWeeksUseCase
import org.epoque.tandem.domain.usecase.progress.GetPendingMilestoneUseCase
import org.epoque.tandem.domain.usecase.progress.MarkMilestoneCelebratedUseCase
import org.epoque.tandem.presentation.progress.PastWeekDetailViewModel
import org.epoque.tandem.presentation.progress.ProgressViewModel
import org.epoque.tandem.presentation.progress.preferences.ProgressPreferences
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Progress & Insights feature dependencies.
 *
 * Provides:
 * - ProgressPreferences for milestone celebration tracking
 * - Use cases for streak calculation and completion stats
 * - ProgressViewModel with all required dependencies
 */
val progressModule = module {
    // Preferences (implements ProgressPreferencesRepository)
    single<ProgressPreferencesRepository> { ProgressPreferences(get()) }

    // Use Cases
    factory { GetPendingMilestoneUseCase() }
    factory { GetCompletionStatsUseCase(get()) }
    factory { MarkMilestoneCelebratedUseCase(get()) }
    factory {
        CalculatePartnerStreakUseCase(
            weekRepository = get(),
            partnerRepository = get(),
            progressPreferencesRepository = get(),
            getPendingMilestoneUseCase = get()
        )
    }
    factory {
        GetCompletionTrendsUseCase(
            weekRepository = get(),
            taskRepository = get(),
            partnerRepository = get()
        )
    }
    factory {
        GetMonthlyCompletionUseCase(
            weekRepository = get(),
            taskRepository = get()
        )
    }
    factory {
        GetPastWeeksUseCase(
            weekRepository = get(),
            taskRepository = get(),
            partnerRepository = get()
        )
    }
    factory {
        GetPastWeekDetailUseCase(
            weekRepository = get(),
            taskRepository = get(),
            partnerRepository = get()
        )
    }

    // ViewModels
    viewModel {
        ProgressViewModel(
            authRepository = get(),
            partnerRepository = get(),
            calculatePartnerStreakUseCase = get(),
            markMilestoneCelebratedUseCase = get(),
            getCompletionTrendsUseCase = get(),
            getMonthlyCompletionUseCase = get(),
            getPastWeeksUseCase = get()
        )
    }
    viewModel { parameters ->
        PastWeekDetailViewModel(
            savedStateHandle = parameters.get(),
            authRepository = get(),
            getPastWeekDetailUseCase = get()
        )
    }
}
