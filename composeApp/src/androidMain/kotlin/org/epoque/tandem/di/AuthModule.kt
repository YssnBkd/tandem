package org.epoque.tandem.di

import org.epoque.tandem.data.repository.AuthRepositoryImpl
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.presentation.auth.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for authentication dependencies.
 */
val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthViewModel(get()) }
}
