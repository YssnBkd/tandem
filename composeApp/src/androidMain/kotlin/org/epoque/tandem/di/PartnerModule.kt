package org.epoque.tandem.di

import org.epoque.tandem.data.repository.InviteRepositoryImpl
import org.epoque.tandem.data.repository.PartnerRepositoryImpl
import org.epoque.tandem.domain.repository.InviteRepository
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.presentation.partner.PartnerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for partner system dependencies.
 */
val partnerModule = module {
    // Repositories
    single<PartnerRepository> { PartnerRepositoryImpl(get(), get()) }
    single<InviteRepository> { InviteRepositoryImpl(get(), get()) }

    // ViewModel
    viewModel {
        PartnerViewModel(
            authRepository = get(),
            partnerRepository = get(),
            inviteRepository = get(),
            taskRepository = get(),
            weekRepository = get()
        )
    }
}
