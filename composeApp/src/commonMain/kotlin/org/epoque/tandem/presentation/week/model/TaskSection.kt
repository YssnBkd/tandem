package org.epoque.tandem.presentation.week.model

/**
 * Sections for grouping tasks in the week view.
 * Order matters - this is display order.
 */
enum class TaskSection {
    OVERDUE,          // Red header, past scheduled dates
    TODAY,            // Tasks scheduled for today
    TOMORROW,         // Tasks scheduled for tomorrow
    LATER_THIS_WEEK,  // Tasks scheduled for days after tomorrow
    UNSCHEDULED,      // Tasks with no scheduled date (optional, may not show)
    COMPLETED         // Completed tasks (collapsible)
}
