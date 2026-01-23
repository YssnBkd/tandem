package org.epoque.tandem.domain.model

/**
 * Task priority levels following Todoist-style conventions.
 *
 * Colors:
 * - P1: Red (#D1453B) - Highest priority
 * - P2: Orange (#EB8909) - High priority
 * - P3: Blue (#246FE0) - Medium priority
 * - P4: Gray (#79747E) - Low/No priority (default)
 */
enum class TaskPriority {
    P1,  // Highest priority - Red
    P2,  // High priority - Orange
    P3,  // Medium priority - Blue
    P4;  // Low/No priority - Gray (default)

    companion object {
        val DEFAULT = P4

        /**
         * Parse priority from string, returns P4 if invalid.
         */
        fun fromString(value: String?): TaskPriority {
            return entries.find { it.name == value } ?: DEFAULT
        }
    }
}
