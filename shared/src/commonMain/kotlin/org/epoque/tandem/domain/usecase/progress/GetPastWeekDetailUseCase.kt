package org.epoque.tandem.domain.usecase.progress

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.model.CompletionStats
import org.epoque.tandem.domain.model.PastWeekDetail
import org.epoque.tandem.domain.model.ReviewDetail
import org.epoque.tandem.domain.model.TaskOutcome
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Get detailed information for a specific past week.
 *
 * Retrieves review data for both user and partner (if connected),
 * and task outcomes showing completion status for each.
 */
class GetPastWeekDetailUseCase(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val partnerRepository: PartnerRepository
) {
    /**
     * Get full detail for a past week.
     *
     * @param weekId The week ID to get details for
     * @param userId Current user's ID
     * @return PastWeekDetail with review info and task outcomes
     * @throws IllegalArgumentException if week not found
     */
    suspend operator fun invoke(weekId: String, userId: String): PastWeekDetail {
        val partner = partnerRepository.getPartner(userId)

        // Get user's week
        val userWeek = weekRepository.observeWeeksForUser(userId)
            .first()
            .find { it.id == weekId }
            ?: throw IllegalArgumentException("Week not found: $weekId")

        // Get partner's week (if connected)
        val partnerWeek = partner?.let {
            weekRepository.observeWeeksForUser(it.id)
                .first()
                .find { w -> w.id == weekId }
        }

        // Get user's tasks
        val userTasks = taskRepository.observeTasksForWeek(weekId, userId).first()

        // Get partner's tasks
        val partnerTasks = partner?.let {
            taskRepository.observeTasksForWeek(weekId, it.id).first()
        } ?: emptyList()

        // Calculate completion stats
        val userCompletion = CompletionStats(
            completedCount = userTasks.count { it.status == TaskStatus.COMPLETED },
            totalCount = userTasks.size
        )

        val partnerCompletion = if (partner != null && partnerTasks.isNotEmpty()) {
            CompletionStats(
                completedCount = partnerTasks.count { it.status == TaskStatus.COMPLETED },
                totalCount = partnerTasks.size
            )
        } else null

        // Build review details
        val userReview = ReviewDetail(
            rating = userWeek.overallRating,
            note = userWeek.reviewNote,
            completion = userCompletion,
            reviewedAt = userWeek.reviewedAt
        )

        val partnerReview = partnerWeek?.let {
            ReviewDetail(
                rating = it.overallRating,
                note = it.reviewNote,
                completion = partnerCompletion ?: CompletionStats(0, 0),
                reviewedAt = it.reviewedAt
            )
        }

        // Build task outcomes - merge user and partner status
        val partnerTasksById = partnerTasks.associateBy { it.id }
        val taskOutcomes = userTasks.map { userTask ->
            val partnerTask = partnerTasksById[userTask.id]
            TaskOutcome(
                taskId = userTask.id,
                title = userTask.title,
                priority = userTask.priority,
                userStatus = userTask.status,
                partnerStatus = partnerTask?.status
            )
        }

        return PastWeekDetail(
            weekId = weekId,
            startDate = userWeek.startDate,
            endDate = userWeek.endDate,
            userReview = userReview,
            partnerReview = partnerReview,
            tasks = taskOutcomes
        )
    }
}
