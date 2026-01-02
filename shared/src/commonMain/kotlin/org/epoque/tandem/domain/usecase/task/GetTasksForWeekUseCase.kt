package org.epoque.tandem.domain.usecase.task

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Use case for observing tasks filtered by week.
 */
class GetTasksForWeekUseCase(
    private val taskRepository: TaskRepository
) {
    /**
     * Observes tasks for a specific week.
     *
     * @param weekId ISO 8601 week ID (e.g., "2026-W01")
     * @param userId The user's ID
     * @return Flow of tasks for the week
     */
    operator fun invoke(weekId: String, userId: String): Flow<List<Task>> {
        return taskRepository.observeTasksForWeek(weekId, userId)
    }
}
