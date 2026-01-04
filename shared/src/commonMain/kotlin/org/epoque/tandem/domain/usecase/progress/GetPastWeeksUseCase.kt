package org.epoque.tandem.domain.usecase.progress

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.epoque.tandem.domain.model.CompletionStats
import org.epoque.tandem.domain.model.PastWeeksResult
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.model.WeekSummary
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Get paginated list of past weeks with summary data.
 *
 * Supports offset-based pagination with configurable limit.
 */
class GetPastWeeksUseCase(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val partnerRepository: PartnerRepository
) {
    /**
     * Get a page of past weeks with summary data.
     *
     * @param userId Current user's ID
     * @param offset Number of weeks to skip (for pagination)
     * @param limit Number of weeks to return (default 10)
     * @return PastWeeksResult with weeks list and pagination metadata
     */
    suspend operator fun invoke(
        userId: String,
        offset: Int = 0,
        limit: Int = 10
    ): PastWeeksResult {
        val currentWeekId = weekRepository.getCurrentWeekId()
        val partner = partnerRepository.getPartner(userId)

        // Get all past weeks for user
        val allUserWeeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .filter { it.id < currentWeekId }
            .sortedByDescending { it.id }

        val totalCount = allUserWeeks.size

        // Apply pagination
        val pagedWeeks = allUserWeeks
            .drop(offset)
            .take(limit)

        if (pagedWeeks.isEmpty()) {
            return PastWeeksResult(
                weeks = emptyList(),
                hasMore = false,
                totalCount = totalCount
            )
        }

        // Get partner weeks if connected
        val partnerWeeks = partner?.let {
            weekRepository.observeWeeksForUser(it.id)
                .first()
                .associateBy { week -> week.id }
        } ?: emptyMap()

        // Build summaries
        val summaries = pagedWeeks.map { week ->
            val userCompletion = calculateCompletion(week.id, userId)
            val partnerCompletion = partner?.let {
                calculateCompletion(week.id, it.id)
            }

            val partnerWeek = partnerWeeks[week.id]

            WeekSummary(
                weekId = week.id,
                startDate = week.startDate,
                endDate = week.endDate,
                userCompletion = userCompletion,
                partnerCompletion = partnerCompletion,
                userRating = week.overallRating,
                partnerRating = partnerWeek?.overallRating,
                isReviewed = week.isReviewed
            )
        }

        return PastWeeksResult(
            weeks = summaries,
            hasMore = offset + limit < totalCount,
            totalCount = totalCount
        )
    }

    private suspend fun calculateCompletion(weekId: String, userId: String): CompletionStats {
        val tasks = taskRepository.observeTasksForWeek(weekId, userId).first()
        val completed = tasks.count { it.status == TaskStatus.COMPLETED }
        return CompletionStats(
            completedCount = completed,
            totalCount = tasks.size
        )
    }
}
