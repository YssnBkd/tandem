package org.epoque.tandem.domain.usecase.task

import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Use case for creating a new task.
 * Validates input and delegates to repository.
 */
class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    /**
     * Creates a new task.
     *
     * @param task The task to create (id, createdAt, updatedAt will be generated)
     * @return The created task with generated fields
     * @throws IllegalArgumentException if validation fails
     */
    suspend operator fun invoke(task: Task): Task {
        return taskRepository.createTask(task)
    }
}
