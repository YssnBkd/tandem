package org.epoque.tandem.domain.model

/**
 * Week data combined with task statistics for timeline display.
 */
data class WeekWithStats(
    val week: Week,
    val totalTasks: Int,
    val completedTasks: Int
) {
    /**
     * Completion ratio as a float between 0.0 and 1.0.
     */
    val completionRatio: Float
        get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    /**
     * Whether this week has no tasks.
     */
    val isEmpty: Boolean
        get() = totalTasks == 0

    /**
     * Completion percentage as an integer (0-100).
     */
    val completionPercentage: Int
        get() = (completionRatio * 100).toInt()
}
