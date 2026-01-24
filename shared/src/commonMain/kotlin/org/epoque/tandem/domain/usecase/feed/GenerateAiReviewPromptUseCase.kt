package org.epoque.tandem.domain.usecase.feed

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.domain.usecase.review.IsReviewWindowOpenUseCase

/**
 * Use case for generating AI review prompt in the feed.
 *
 * Creates a prompt if:
 * - Review window is open
 * - Current week has not been reviewed yet
 */
class GenerateAiReviewPromptUseCase(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val feedRepository: FeedRepository,
    private val isReviewWindowOpenUseCase: IsReviewWindowOpenUseCase
) {
    /**
     * Check conditions and generate AI review prompt if needed.
     *
     * @param userId The user to check
     * @return The AI review prompt feed item, or null if not applicable
     */
    suspend operator fun invoke(userId: String): FeedItem.AiReviewPrompt? {
        // Check if review window is open
        if (!isReviewWindowOpenUseCase()) {
            return null
        }

        // Get current week
        val currentWeekId = weekRepository.getCurrentWeekId()
        val currentWeek = weekRepository.getWeekById(currentWeekId)

        // If already reviewed, no prompt needed
        if (currentWeek?.isReviewed == true) {
            return null
        }

        // Get task stats for the week
        val tasks = taskRepository.observeTasksForWeek(currentWeekId, userId).first()
        val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
        val totalCount = tasks.size

        // Create or update the prompt
        return feedRepository.createOrUpdateAiReviewPrompt(
            userId = userId,
            weekId = currentWeekId,
            completedTaskCount = completedCount,
            totalTaskCount = totalCount
        )
    }
}
