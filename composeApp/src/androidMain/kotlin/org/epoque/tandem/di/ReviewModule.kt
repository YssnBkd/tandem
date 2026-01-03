package org.epoque.tandem.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.epoque.tandem.domain.usecase.review.CalculateStreakUseCase
import org.epoque.tandem.domain.usecase.review.GetReviewStatsUseCase
import org.epoque.tandem.domain.usecase.review.IsReviewWindowOpenUseCase
import org.epoque.tandem.presentation.review.ReviewViewModel
import org.epoque.tandem.presentation.review.preferences.ReviewProgress
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * DataStore singleton for review preferences.
 *
 * Following Android best practices:
 * - Single instance per file to avoid IllegalStateException
 * - Property delegate ensures lazy initialization
 * - Separate from other feature preferences to avoid conflicts
 */
private val Context.reviewPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "review_preferences"
)

/**
 * Koin module for Week Review feature dependencies.
 *
 * Provides:
 * - Use cases for streak calculation, review window check, and stats
 * - DataStore for review progress persistence (named "review")
 * - ReviewProgress wrapper
 * - ReviewViewModel with all required dependencies
 */
val reviewModule = module {
    // Use cases
    factory { CalculateStreakUseCase(get()) }
    factory { IsReviewWindowOpenUseCase() }
    factory { GetReviewStatsUseCase() }

    // DataStore singleton for review preferences (named to avoid conflict with other preferences)
    single<DataStore<Preferences>>(named("review")) {
        androidContext().reviewPreferencesDataStore
    }

    // Review progress manager
    single { ReviewProgress(get(named("review"))) }

    // Review ViewModel with lifecycle-aware scope
    viewModel {
        ReviewViewModel(
            authRepository = get(),
            weekRepository = get(),
            taskRepository = get(),
            calculateStreakUseCase = get(),
            isReviewWindowOpenUseCase = get(),
            getReviewStatsUseCase = get(),
            reviewProgress = get()
        )
    }
}
