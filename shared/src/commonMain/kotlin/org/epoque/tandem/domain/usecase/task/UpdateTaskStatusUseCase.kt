package org.epoque.tandem.domain.usecase.task

import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Use case for updating a task's status.
 */
class UpdateTaskStatusUseCase(
    private val taskRepository: TaskRepository
) {
    /**
     * Updates the status of a task.
     *
     * @param taskId The task ID
     * @param status The new status
     * @return The updated task, or null if task not found
     */
    suspend operator fun invoke(taskId: String, status: TaskStatus): Task? {
        return taskRepository.updateTaskStatus(taskId, status)
    }
}
