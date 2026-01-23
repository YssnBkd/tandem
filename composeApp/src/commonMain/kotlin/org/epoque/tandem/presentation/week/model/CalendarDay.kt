package org.epoque.tandem.presentation.week.model

import kotlinx.datetime.LocalDate

/**
 * Represents a single day in the calendar strip.
 */
data class CalendarDay(
    val date: LocalDate,
    val dayOfWeekLabel: String,   // "SUN", "MON", "TUE", etc.
    val dayNumber: Int,           // 5, 6, 7, etc.
    val isToday: Boolean,
    val hasTasks: Boolean,        // Red dot indicator
    val isSelected: Boolean = false
)
