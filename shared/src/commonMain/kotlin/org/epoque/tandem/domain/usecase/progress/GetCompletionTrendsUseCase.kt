package org.epoque.tandem.domain.usecase.progress

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.model.TrendChartData
import org.epoque.tandem.domain.model.TrendDataPoint
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Get completion trend data for past 8 weeks.
 *
 * Returns data points for trend chart visualization showing
 * completion percentages for user and optionally partner.
 */
class GetCompletionTrendsUseCase(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val partnerRepository: PartnerRepository
) {
    /**
     * Calculate completion trends for the past 8 weeks.
     *
     * @param userId Current user's ID
     * @return TrendChartData with up to 8 data points
     */
    suspend operator fun invoke(userId: String): TrendChartData {
        val currentWeekId = weekRepository.getCurrentWeekId()
        val partner = partnerRepository.getPartner(userId)
        val hasPartner = partner != null

        // Get weeks for user
        val userWeeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .filter { it.id < currentWeekId } // Past weeks only
            .sortedByDescending { it.id }
            .take(8)
            .reversed() // Oldest to newest

        if (userWeeks.isEmpty()) {
            return TrendChartData.EMPTY
        }

        val dataPoints = userWeeks.map { week ->
            val userPercentage = calculateWeekCompletion(week.id, userId)
            val partnerPercentage = partner?.let {
                calculateWeekCompletion(week.id, it.id)
            }

            TrendDataPoint(
                weekId = week.id,
                weekLabel = extractWeekLabel(week.id),
                userPercentage = userPercentage,
                partnerPercentage = partnerPercentage
            )
        }

        return TrendChartData(
            dataPoints = dataPoints,
            hasPartner = hasPartner,
            insufficientData = dataPoints.size < 4
        )
    }

    private suspend fun calculateWeekCompletion(weekId: String, userId: String): Int {
        val tasks = taskRepository.observeTasksForWeek(weekId, userId).first()
        if (tasks.isEmpty()) return 0

        val completed = tasks.count { it.status == TaskStatus.COMPLETED }
        return (completed * 100) / tasks.size
    }

    /**
     * Extract week label from week ID (e.g., "2026-W01" -> "W01").
     */
    private fun extractWeekLabel(weekId: String): String {
        return weekId.substringAfter("-")
    }
}
