package org.epoque.tandem.domain.model

/**
 * Task completion statistics for a single week/user.
 *
 * @property completedCount Number of tasks with status COMPLETED
 * @property totalCount Total number of tasks for the period
 */
data class CompletionStats(
    val completedCount: Int,
    val totalCount: Int
) {
    init {
        require(completedCount >= 0) { "completedCount cannot be negative" }
        require(totalCount >= 0) { "totalCount cannot be negative" }
        require(completedCount <= totalCount) { "completedCount cannot exceed totalCount" }
    }

    /**
     * Completion percentage (0-100).
     */
    val percentage: Int
        get() = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    /**
     * Display text showing completion ratio (e.g., "6/8").
     */
    val displayText: String
        get() = "$completedCount/$totalCount"

    companion object {
        val EMPTY = CompletionStats(completedCount = 0, totalCount = 0)
    }
}
