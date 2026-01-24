package org.epoque.tandem.domain.usecase.feed

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Use case for generating AI planning prompt in the feed.
 *
 * Creates a prompt if:
 * - Current week has not been planned yet
 * - There are rollover tasks from previous week
 */
class GenerateAiPlanPromptUseCase(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val feedRepository: FeedRepository
) {
    /**
     * Check conditions and generate AI plan prompt if needed.
     *
     * @param userId The user to check
     * @return The AI plan prompt feed item, or null if not applicable
     */
    suspend operator fun invoke(userId: String): FeedItem.AiPlanPrompt? {
        // Get current week
        val currentWeekId = weekRepository.getCurrentWeekId()
        val currentWeek = weekRepository.getWeekById(currentWeekId)

        // If planning is already completed, no prompt needed
        if (currentWeek?.planningCompletedAt != null) {
            return null
        }

        // Get previous week ID and count rollover candidates
        val previousWeekId = weekRepository.getPreviousWeekId(currentWeekId)
        val rolloverTasks = taskRepository
            .observeIncompleteTasksForWeek(previousWeekId, userId)
            .first()

        // Create or update the prompt
        return feedRepository.createOrUpdateAiPlanPrompt(
            userId = userId,
            rolloverTaskCount = rolloverTasks.size
        )
    }
}
