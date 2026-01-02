package org.epoque.tandem.presentation.week.model

import kotlinx.datetime.LocalDate
import org.epoque.tandem.domain.model.Week

/**
 * Week display information for the header.
 */
data class WeekInfo(
    val weekId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dateRangeText: String,          // "Week of Dec 30 - Jan 5"
    val isCurrentWeek: Boolean
) {
    companion object {
        /**
         * Create WeekInfo from domain Week.
         */
        fun fromWeek(week: Week, currentWeekId: String): WeekInfo {
            return WeekInfo(
                weekId = week.id,
                startDate = week.startDate,
                endDate = week.endDate,
                dateRangeText = formatDateRange(week.startDate, week.endDate),
                isCurrentWeek = week.id == currentWeekId
            )
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
