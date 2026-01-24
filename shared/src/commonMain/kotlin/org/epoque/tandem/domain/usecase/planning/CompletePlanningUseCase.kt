package org.epoque.tandem.domain.usecase.planning

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Use case for completing weekly planning and publishing feed events.
 *
 * Handles:
 * - Marking planning as completed
 * - Creating feed item for the user
 * - Creating feed item for partner if connected
 */
class CompletePlanningUseCase(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val feedRepository: FeedRepository,
    private val partnerRepository: PartnerRepository
) {
    /**
     * Complete planning for a week and publish feed events.
     *
     * @param weekId The week that was planned
     * @param userId The user who completed planning
     * @return The week planned feed item
     */
    suspend operator fun invoke(weekId: String, userId: String): FeedItem.WeekPlanned {
        // Mark planning as completed
        weekRepository.markPlanningCompleted(weekId)

        // Count tasks for this week
        val tasks = taskRepository.observeTasksForWeek(weekId, userId).first()
        val taskCount = tasks.size

        // Create feed item for the user
        val feedItem = feedRepository.createWeekPlannedItem(
            weekId = weekId,
            userId = userId,
            taskCount = taskCount
        )

        // If user has a partner, create feed item for partner's feed too
        val partner = partnerRepository.getPartner(userId)
        if (partner != null) {
            feedRepository.createWeekPlannedItem(
                weekId = weekId,
                userId = userId,  // The user who planned (shows "Partner planned their week")
                taskCount = taskCount
            )
        }

        return feedItem
    }
}
