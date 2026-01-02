package org.epoque.tandem.domain.usecase.task

import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Use case for deleting a task.
 */
class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    /**
     * Deletes a task by ID.
     *
     * @param taskId The task ID to delete
     * @return true if deleted, false if task not found
     */
    suspend operator fun invoke(taskId: String): Boolean {
        return taskRepository.deleteTask(taskId)
    }
}
