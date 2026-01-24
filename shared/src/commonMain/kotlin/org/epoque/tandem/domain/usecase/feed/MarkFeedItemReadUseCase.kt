package org.epoque.tandem.domain.usecase.feed

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.epoque.tandem.domain.repository.FeedRepository

/**
 * Use case for marking feed items as read.
 * Supports marking individual items or all items up to a timestamp.
 */
class MarkFeedItemReadUseCase(
    private val feedRepository: FeedRepository
) {
    /**
     * Mark a single feed item as read.
     *
     * @param itemId The feed item ID to mark as read
     * @return true if marked successfully
     */
    suspend fun markSingleAsRead(itemId: String): Boolean {
        return feedRepository.markAsRead(itemId)
    }

    /**
     * Mark all feed items as read up to the current time.
     * Updates the last read timestamp for "caught up" calculation.
     *
     * @param userId The current user's ID
     * @return Number of items marked as read
     */
    suspend fun markAllAsRead(userId: String): Int {
        val now = Clock.System.now()
        val count = feedRepository.markAllAsReadUpTo(userId, now)
        feedRepository.updateLastReadTimestamp(userId, now)
        return count
    }

    /**
     * Mark all feed items as read up to a specific timestamp.
     *
     * @param userId The current user's ID
     * @param timestamp Mark all items at or before this time as read
     * @return Number of items marked as read
     */
    suspend fun markAllAsReadUpTo(userId: String, timestamp: Instant): Int {
        val count = feedRepository.markAllAsReadUpTo(userId, timestamp)
        feedRepository.updateLastReadTimestamp(userId, timestamp)
        return count
    }
}
