package org.epoque.tandem.di

import org.epoque.tandem.presentation.timeline.TimelineViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Timeline feature dependencies.
 *
 * Provides:
 * - TimelineViewModel with required repository dependencies
 */
val timelineModule = module {
    viewModel {
        TimelineViewModel(
            weekRepository = get(),
            taskRepository = get(),
            authRepository = get()
        )
    }
}
