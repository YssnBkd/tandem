package org.epoque.tandem.di

import org.epoque.tandem.data.repository.FeedRepositoryImpl
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.usecase.feed.AcceptTaskAssignmentUseCase
import org.epoque.tandem.domain.usecase.feed.DeclineTaskAssignmentUseCase
import org.epoque.tandem.domain.usecase.feed.DismissAiPromptUseCase
import org.epoque.tandem.domain.usecase.feed.GetFeedItemsUseCase
import org.epoque.tandem.domain.usecase.feed.MarkFeedItemReadUseCase
import org.epoque.tandem.domain.usecase.feed.SendMessageUseCase
import org.epoque.tandem.presentation.feed.FeedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Feed feature dependencies.
 *
 * Provides:
 * - FeedRepository for data access
 * - Feed use cases
 * - FeedViewModel with all required dependencies
 */
val feedModule = module {
    // Repository
    single<FeedRepository> { FeedRepositoryImpl(get(), get(), get()) }

    // Use Cases
    factory { GetFeedItemsUseCase(get()) }
    factory { MarkFeedItemReadUseCase(get()) }
    factory { AcceptTaskAssignmentUseCase(get(), get()) }
    factory { DeclineTaskAssignmentUseCase(get(), get()) }
    factory { DismissAiPromptUseCase(get()) }
    factory { SendMessageUseCase(get(), get()) }

    // ViewModel
    viewModel {
        FeedViewModel(
            feedRepository = get(),
            authRepository = get(),
            partnerRepository = get(),
            getFeedItemsUseCase = get(),
            markFeedItemReadUseCase = get(),
            acceptTaskAssignmentUseCase = get(),
            declineTaskAssignmentUseCase = get(),
            dismissAiPromptUseCase = get(),
            sendMessageUseCase = get()
        )
    }
}
