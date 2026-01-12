package org.epoque.tandem.presentation.week.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.epoque.tandem.domain.model.Week

/**
 * Week display information for the header.
 */
data class WeekInfo(
    val weekId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dateRangeText: String,          // "Week of Dec 30 - Jan 5"
    val isCurrentWeek: Boolean,
    val taskCount: Int = 0              // For "7 tasks" display
) {
    companion object {
        /**
         * Create WeekInfo from domain Week.
         *
         * @param week Domain week entity
         * @param currentWeekId ID of the current week for comparison
         * @param taskCount Number of tasks for display (e.g., "7 tasks")
         */
        fun fromWeek(
            week: Week,
            currentWeekId: String,
            taskCount: Int = 0
        ): WeekInfo {
            return WeekInfo(
                weekId = week.id,
                startDate = week.startDate,
                endDate = week.endDate,
                dateRangeText = formatDateRange(week.startDate, week.endDate),
                isCurrentWeek = week.id == currentWeekId,
                taskCount = taskCount
            )
        }

        /**
         * Create WeekInfo from weekId without requiring a Week entity.
         * Used for navigating to weeks that may not exist in the database yet (e.g., future weeks).
         *
         * @param weekId ISO week ID (format: "2026-W02")
         * @param currentWeekId ID of the current week for comparison
         * @param taskCount Number of tasks for display
         */
        fun fromWeekId(
            weekId: String,
            currentWeekId: String,
            taskCount: Int = 0
        ): WeekInfo {
            val (startDate, endDate) = calculateWeekDates(weekId)
            return WeekInfo(
                weekId = weekId,
                startDate = startDate,
                endDate = endDate,
                dateRangeText = formatDateRange(startDate, endDate),
                isCurrentWeek = weekId == currentWeekId,
                taskCount = taskCount
            )
        }

        /**
         * Calculate start and end dates for a given ISO week ID.
         * Week starts on Monday and ends on Sunday.
         */
        private fun calculateWeekDates(weekId: String): Pair<LocalDate, LocalDate> {
            // Parse weekId (format: "2026-W02")
            val parts = weekId.split("-W")
            val year = parts[0].toInt()
            val weekNumber = parts[1].toInt()

            // Find January 4th of the year (always in week 1 per ISO 8601)
            val jan4 = LocalDate(year, 1, 4)

            // Calculate the Monday of week 1
            val daysFromMonday = (jan4.dayOfWeek.ordinal) // Monday = 0
            val mondayOfWeek1 = jan4.plus(DatePeriod(days = -daysFromMonday))

            // Calculate the Monday of the target week
            val startDate = mondayOfWeek1.plus(DatePeriod(days = (weekNumber - 1) * 7))
            val endDate = startDate.plus(DatePeriod(days = 6))

            return Pair(startDate, endDate)
        }

        private fun formatDateRange(start: LocalDate, end: LocalDate): String {
            val startMonth = start.month.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
            val endMonth = end.month.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }

            return if (start.month == end.month) {
                "Week of $startMonth ${start.dayOfMonth} - ${end.dayOfMonth}"
            } else {
                "Week of $startMonth ${start.dayOfMonth} - $endMonth ${end.dayOfMonth}"
            }
        }
    }
}
