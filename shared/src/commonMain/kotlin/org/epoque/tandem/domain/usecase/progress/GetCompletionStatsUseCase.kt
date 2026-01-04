package org.epoque.tandem.domain.usecase.progress

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.model.CompletionStats
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.TaskRepository

/**
 * Calculates completion statistics for a single week.
 *
 * Completion is based on tasks with status COMPLETED.
 * Tried and Skipped are counted as incomplete, per the "Celebration Over Judgment" principle.
 */
class GetCompletionStatsUseCase(
    private val taskRepository: TaskRepository
) {
    /**
     * Calculate completion stats for a user's tasks in a specific week.
     *
     * @param weekId ISO 8601 week ID (e.g., "2026-W01")
     * @param userId The user's ID
     * @return CompletionStats with completed count, total count, and percentage
     */
    suspend operator fun invoke(weekId: String, userId: String): CompletionStats {
        val tasks = taskRepository.observeTasksForWeek(weekId, userId).first()

        val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
        val totalCount = tasks.size

        return CompletionStats(
            completedCount = completedCount,
            totalCount = totalCount
        )
    }
}
