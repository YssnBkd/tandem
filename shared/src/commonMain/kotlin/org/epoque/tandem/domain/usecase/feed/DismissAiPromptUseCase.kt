package org.epoque.tandem.domain.usecase.feed

import org.epoque.tandem.domain.repository.FeedRepository

/**
 * Use case for dismissing an AI prompt from the feed.
 * AI prompts (plan prompts and review prompts) can be dismissed by the user
 * and will not reappear once dismissed.
 */
class DismissAiPromptUseCase(
    private val feedRepository: FeedRepository
) {
    /**
     * Dismiss an AI prompt.
     *
     * @param itemId The AI prompt feed item ID (AiPlanPrompt or AiReviewPrompt)
     * @return true if dismissed successfully, false if not found or not an AI prompt
     */
    suspend operator fun invoke(itemId: String): Boolean {
        // Also mark as read when dismissing
        feedRepository.markAsRead(itemId)
        return feedRepository.dismissAiPrompt(itemId)
    }
}
