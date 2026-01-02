package org.epoque.tandem.di

import org.epoque.tandem.data.local.DriverFactory
import org.epoque.tandem.data.local.TandemDatabaseFactory
import org.epoque.tandem.data.repository.TaskRepositoryImpl
import org.epoque.tandem.data.repository.WeekRepositoryImpl
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.domain.usecase.task.CreateTaskUseCase
import org.epoque.tandem.domain.usecase.task.DeleteTaskUseCase
import org.epoque.tandem.domain.usecase.task.GetTasksForWeekUseCase
import org.epoque.tandem.domain.usecase.task.UpdateTaskStatusUseCase
import org.epoque.tandem.domain.usecase.week.GetCurrentWeekUseCase
import org.epoque.tandem.domain.usecase.week.SaveWeekReviewUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for task and week data layer dependencies.
 */
val taskModule = module {
    // Database
    single { DriverFactory(androidContext()).createDriver() }
    single { TandemDatabaseFactory.create(get()) }

    // Repositories
    single<TaskRepository> { TaskRepositoryImpl(get()) }
    single<WeekRepository> { WeekRepositoryImpl(get()) }

    // Task Use Cases
    factory { CreateTaskUseCase(get()) }
    factory { UpdateTaskStatusUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { GetTasksForWeekUseCase(get()) }

    // Week Use Cases
    factory { GetCurrentWeekUseCase(get()) }
    factory { SaveWeekReviewUseCase(get()) }
}
