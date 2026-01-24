package org.epoque.tandem.domain.usecase.task

import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.GoalRepository
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Use case for completing a task and publishing feed events.
 *
 * Handles:
 * - Updating task status
 * - Creating feed item for task completion
 * - Incrementing goal progress if task is linked to a goal
 */
class CompleteTaskUseCase(
    private val taskRepository: TaskRepository,
    private val feedRepository: FeedRepository,
    private val partnerRepository: PartnerRepository,
    private val goalRepository: GoalRepository
) {
    /**
     * Complete a task and publish feed events.
     *
     * @param taskId The task to complete
     * @param userId The user completing the task
     * @return The task completed feed item, or null if task not found
     */
    suspend operator fun invoke(taskId: String, userId: String): FeedItem.TaskCompleted? {
        // Get the task
        val task = taskRepository.getTaskById(taskId) ?: return null

        // Check if already completed
        val wasCompleted = task.status == TaskStatus.COMPLETED ||
            (task.isRepeating && task.repeatCompleted >= (task.repeatTarget ?: 0))

        // Handle repeating tasks
        if (task.isRepeating) {
            val newCount = task.repeatCompleted + 1
            taskRepository.incrementRepeatCount(taskId)

            if (newCount >= (task.repeatTarget ?: 0)) {
                taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
            }
        } else {
            taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
        }

        // Only create feed item and increment goal if transitioning TO completed
        if (wasCompleted) return null

        // Increment goal progress if task is linked to a goal
        task.linkedGoalId?.let { goalId ->
            goalRepository.incrementProgress(goalId, 1)
        }

        // Check if user has a partner to notify
        val hasPartner = partnerRepository.hasPartner(userId)

        // Create the feed item
        return feedRepository.createTaskCompletedItem(
            taskId = taskId,
            userId = userId,
            notifyPartner = hasPartner
        )
    }

    /**
     * Uncomplete a task (toggle from completed to pending).
     *
     * @param taskId The task to uncomplete
     * @return true if status was updated
     */
    suspend fun uncomplete(taskId: String): Boolean {
        val task = taskRepository.getTaskById(taskId) ?: return false

        if (task.status != TaskStatus.COMPLETED) return false

        taskRepository.updateTaskStatus(taskId, TaskStatus.PENDING)
        return true
    }
}
