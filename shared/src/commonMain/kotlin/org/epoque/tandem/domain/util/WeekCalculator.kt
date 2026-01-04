package org.epoque.tandem.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

object WeekCalculator {
    /**
     * Get ISO day number (Monday=1, Sunday=7).
     */
    private fun DayOfWeek.toIsoDayNumber(): Int = when (this) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 7
    }

    /**
     * Get ISO 8601 week ID for a date.
     * Format: "YYYY-Www" (e.g., "2026-W01")
     */
    fun getWeekId(
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ): String {
        val dayOfYear = date.dayOfYear
        val dayOfWeek = date.dayOfWeek.toIsoDayNumber()
        val weekNumber = (dayOfYear - dayOfWeek + 10) / 7

        // Handle edge case: week 53 might belong to next year
        val year = if (weekNumber == 0) {
            date.year - 1
        } else if (weekNumber > 52 && date.monthNumber == 1) {
            // Check if it's actually week 1 of next year
            date.year - 1
        } else {
            date.year
        }

        val adjustedWeekNumber = if (weekNumber == 0) {
            // Last week of previous year
            getLastWeekOfYear(year)
        } else if (weekNumber > getLastWeekOfYear(date.year)) {
            // First week of next year
            1
        } else {
            weekNumber
        }

        return "$year-W${adjustedWeekNumber.toString().padStart(2, '0')}"
    }

    /**
     * Calculate end week ID given start and duration.
     */
    fun calculateEndWeekId(startWeekId: String, durationWeeks: Int): String {
        val startDate = parseWeekId(startWeekId)
        val endDate = startDate.plus(durationWeeks, DateTimeUnit.WEEK)
        return getWeekId(endDate)
    }

    /**
     * Parse week ID to LocalDate (Monday of that week).
     */
    fun parseWeekId(weekId: String): LocalDate {
        val (year, week) = weekId.split("-W").let {
            it[0].toInt() to it[1].toInt()
        }
        // Find January 4th (always in week 1 per ISO 8601)
        var date = LocalDate(year, 1, 4)
        // Find Monday of week 1
        while (date.dayOfWeek != DayOfWeek.MONDAY) {
            date = date.plus(-1, DateTimeUnit.DAY)
        }
        // Add weeks
        return date.plus(week - 1, DateTimeUnit.WEEK)
    }

    /**
     * Compare week IDs.
     * Returns negative if a < b, 0 if equal, positive if a > b.
     */
    fun compareWeekIds(a: String, b: String): Int {
        return a.compareTo(b)
    }

    /**
     * Check if week a is after week b.
     */
    fun isAfter(a: String, b: String): Boolean {
        return compareWeekIds(a, b) > 0
    }

    /**
     * Check if week a is before or equal to week b.
     */
    fun isBeforeOrEqual(a: String, b: String): Boolean {
        return compareWeekIds(a, b) <= 0
    }

    /**
     * Get the number of weeks in a year (52 or 53).
     */
    private fun getLastWeekOfYear(year: Int): Int {
        // A year has 53 weeks if it starts on Thursday
        // or is a leap year starting on Wednesday
        val jan1 = LocalDate(year, 1, 1)
        val dec31 = LocalDate(year, 12, 31)
        return if (jan1.dayOfWeek == DayOfWeek.THURSDAY ||
                   dec31.dayOfWeek == DayOfWeek.THURSDAY) {
            53
        } else {
            52
        }
    }
}
