package org.epoque.tandem.domain.usecase.review

import org.epoque.tandem.domain.model.TaskStatus

/**
 * Statistics from a completed review.
 */
data class ReviewStats(
    val totalTasks: Int,
    val doneCount: Int,
    val triedCount: Int,
    val skippedCount: Int,
    /** Completion percentage based on Done tasks only (Tried/Skipped = incomplete per FR-026) */
    val completionPercentage: Int
)

/**
 * Calculates review statistics from task outcomes.
 *
 * Completion percentage is based on Done tasks only - Tried and Skipped
 * are counted as incomplete, per the "Celebration Over Judgment" principle.
 */
class GetReviewStatsUseCase {
    /**
     * Calculate review statistics from task outcomes.
     *
     * @param taskOutcomes Map of taskId to TaskStatus
     * @return ReviewStats with counts and completion percentage
     */
    operator fun invoke(taskOutcomes: Map<String, TaskStatus>): ReviewStats {
        val doneCount = taskOutcomes.count { it.value == TaskStatus.COMPLETED }
        val triedCount = taskOutcomes.count { it.value == TaskStatus.TRIED }
        val skippedCount = taskOutcomes.count { it.value == TaskStatus.SKIPPED }
        val totalTasks = taskOutcomes.size

        val completionPercentage = if (totalTasks > 0) {
            (doneCount * 100) / totalTasks
        } else {
            0
        }

        return ReviewStats(
            totalTasks = totalTasks,
            doneCount = doneCount,
            triedCount = triedCount,
            skippedCount = skippedCount,
            completionPercentage = completionPercentage
        )
    }
}
