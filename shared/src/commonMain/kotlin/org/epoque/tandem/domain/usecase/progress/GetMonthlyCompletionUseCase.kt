package org.epoque.tandem.domain.usecase.progress

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.domain.model.CompletionStats
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Get monthly completion statistics for the current calendar month.
 *
 * Aggregates completion across all weeks that fall within the current month.
 */
class GetMonthlyCompletionUseCase(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository
) {
    /**
     * Calculate completion stats for the current calendar month.
     *
     * @param userId User to calculate stats for
     * @return CompletionStats aggregated for the month
     */
    suspend operator fun invoke(userId: String): CompletionStats {
        val now = Clock.System.now()
        val currentDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val currentYear = currentDate.year
        val currentMonth = currentDate.monthNumber

        // Get all weeks for user
        val weeks = weekRepository.observeWeeksForUser(userId).first()

        // Filter weeks that fall within current month
        val monthWeeks = weeks.filter { week ->
            val startYear = week.startDate.year
            val startMonth = week.startDate.monthNumber
            val endMonth = week.endDate.monthNumber

            // Week starts or ends in current month
            (startYear == currentYear && startMonth == currentMonth) ||
                    (startYear == currentYear && endMonth == currentMonth)
        }

        if (monthWeeks.isEmpty()) {
            return CompletionStats.EMPTY
        }

        // Aggregate task stats across all weeks in the month
        var totalCompleted = 0
        var totalTasks = 0

        for (week in monthWeeks) {
            val tasks = taskRepository.observeTasksForWeek(week.id, userId).first()
            totalTasks += tasks.size
            totalCompleted += tasks.count { it.status == TaskStatus.COMPLETED }
        }

        return CompletionStats(
            completedCount = totalCompleted,
            totalCount = totalTasks
        )
    }
}
