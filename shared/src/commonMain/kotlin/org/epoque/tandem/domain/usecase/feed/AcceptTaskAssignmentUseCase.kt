package org.epoque.tandem.domain.usecase.feed

import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Use case for accepting a task assignment from partner.
 * Updates the task status and creates a TaskAccepted feed item.
 */
class AcceptTaskAssignmentUseCase(
    private val feedRepository: FeedRepository,
    private val taskRepository: TaskRepository
) {
    /**
     * Accept a task assignment.
     *
     * @param assignmentItemId The TaskAssigned feed item ID
     * @param userId The user accepting the task
     * @return The TaskAccepted feed item, or null if assignment not found
     */
    suspend operator fun invoke(
        assignmentItemId: String,
        userId: String
    ): FeedItem.TaskAccepted? {
        // Get the assignment item
        val assignmentItem = feedRepository.getFeedItemById(assignmentItemId)
            as? FeedItem.TaskAssigned ?: return null

        // Update task status from PENDING_ACCEPTANCE to PENDING
        taskRepository.updateTaskStatus(assignmentItem.task.id, TaskStatus.PENDING)

        // Create TaskAccepted feed item and mark assignment as read
        return feedRepository.acceptTaskAssignment(assignmentItemId, userId)
    }
}
