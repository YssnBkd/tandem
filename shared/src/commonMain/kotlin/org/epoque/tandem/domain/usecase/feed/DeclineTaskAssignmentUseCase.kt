package org.epoque.tandem.domain.usecase.feed

import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Use case for declining a task assignment from partner.
 * Deletes the task and creates a TaskDeclined feed item.
 */
class DeclineTaskAssignmentUseCase(
    private val feedRepository: FeedRepository,
    private val taskRepository: TaskRepository
) {
    /**
     * Decline a task assignment.
     *
     * @param assignmentItemId The TaskAssigned feed item ID
     * @param userId The user declining the task
     * @return The TaskDeclined feed item, or null if assignment not found
     */
    suspend operator fun invoke(
        assignmentItemId: String,
        userId: String
    ): FeedItem.TaskDeclined? {
        // Get the assignment item
        val assignmentItem = feedRepository.getFeedItemById(assignmentItemId)
            as? FeedItem.TaskAssigned ?: return null

        // Delete the task since it was declined
        taskRepository.deleteTask(assignmentItem.task.id)

        // Create TaskDeclined feed item and mark assignment as read
        return feedRepository.declineTaskAssignment(assignmentItemId, userId)
    }
}
