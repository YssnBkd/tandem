package org.epoque.tandem.presentation.timeline

import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.Week

/**
 * Sealed class representing items in the timeline LazyColumn.
 * Used to display weeks grouped by month/quarter with gap indicators.
 */
sealed class TimelineItem {
    /**
     * Unique key for LazyColumn item identification.
     */
    abstract val key: String

    /**
     * Section header for grouping weeks by quarter/month/year.
     */
    data class SectionHeader(
        val quarter: Int,
        val month: String,
        val year: Int
    ) : TimelineItem() {
        override val key: String = "section_${year}_Q${quarter}_$month"
    }

    /**
     * Week card showing week dates, completion stats, and expandable task list.
     */
    data class WeekCard(
        val week: Week,
        val tasks: List<Task>,
        val totalTasks: Int,
        val completedTasks: Int,
        val isCurrentWeek: Boolean,
        val isExpanded: Boolean
    ) : TimelineItem() {
        override val key: String = "week_${week.id}"

        val completionRatio: Float
            get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

        val completionPercentage: Int
            get() = (completionRatio * 100).toInt()

        val isEmpty: Boolean
            get() = totalTasks == 0
    }

    /**
     * Gap indicator showing count of consecutive empty weeks.
     */
    data class GapIndicator(
        val emptyWeekCount: Int,
        val startWeekId: String,
        val endWeekId: String
    ) : TimelineItem() {
        override val key: String = "gap_${startWeekId}_${endWeekId}"
    }
}
