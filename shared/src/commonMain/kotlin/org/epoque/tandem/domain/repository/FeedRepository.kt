package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.model.FeedItemType

/**
 * Repository interface for feed data access.
 * Provides CRUD operations for the activity feed and reactive data streams.
 */
interface FeedRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe all feed items for the current user.
     * Returns items in reverse chronological order (newest first).
     *
     * @param userId The current user's ID
     * @return Flow of all feed items
     */
    fun observeFeedItems(userId: String): Flow<List<FeedItem>>

    /**
     * Observe feed items filtered by type.
     *
     * @param userId The current user's ID
     * @param types The types to include (e.g., only TASK_* types for "Tasks" filter)
     * @return Flow of filtered feed items
     */
    fun observeFeedItemsByType(userId: String, types: List<FeedItemType>): Flow<List<FeedItem>>

    /**
     * Observe unread feed items count.
     *
     * @param userId The current user's ID
     * @return Flow of unread count
     */
    fun observeUnreadCount(userId: String): Flow<Int>

    /**
     * Observe feed items for a specific date.
     *
     * @param userId The current user's ID
     * @param date The date to filter by
     * @return Flow of feed items for that date
     */
    fun observeFeedItemsForDate(userId: String, date: LocalDate): Flow<List<FeedItem>>

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get a single feed item by ID.
     *
     * @param itemId The feed item's unique identifier
     * @return The feed item, or null if not found
     */
    suspend fun getFeedItemById(itemId: String): FeedItem?

    /**
     * Get the last read timestamp for calculating unread items.
     *
     * @param userId The current user's ID
     * @return The last read timestamp, or null if never read
     */
    suspend fun getLastReadTimestamp(userId: String): Instant?

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a new feed item.
     *
     * @param item The feed item to create
     * @return The created feed item with generated ID
     */
    suspend fun createFeedItem(item: FeedItem): FeedItem

    /**
     * Mark a feed item as read.
     *
     * @param itemId The feed item ID
     * @return true if updated, false if not found
     */
    suspend fun markAsRead(itemId: String): Boolean

    /**
     * Mark all feed items up to a timestamp as read.
     *
     * @param userId The current user's ID
     * @param timestamp Mark all items at or before this time as read
     * @return Number of items marked as read
     */
    suspend fun markAllAsReadUpTo(userId: String, timestamp: Instant): Int

    /**
     * Update the last read timestamp (for "caught up" calculation).
     *
     * @param userId The current user's ID
     * @param timestamp The new last read timestamp
     */
    suspend fun updateLastReadTimestamp(userId: String, timestamp: Instant)

    /**
     * Dismiss an AI prompt.
     *
     * @param itemId The AI prompt feed item ID
     * @return true if dismissed, false if not found or not an AI prompt
     */
    suspend fun dismissAiPrompt(itemId: String): Boolean

    /**
     * Delete a feed item.
     *
     * @param itemId The feed item ID
     * @return true if deleted, false if not found
     */
    suspend fun deleteFeedItem(itemId: String): Boolean

    // ═══════════════════════════════════════════════════════════════════════════
    // MESSAGE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Send a message to partner.
     *
     * @param senderId The sender's user ID
     * @param recipientId The recipient's user ID (partner)
     * @param text The message text
     * @return The created message feed item
     */
    suspend fun sendMessage(senderId: String, recipientId: String, text: String): FeedItem.Message

    // ═══════════════════════════════════════════════════════════════════════════
    // TASK ASSIGNMENT OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Accept a task assignment.
     * Creates a TaskAccepted feed item and updates the task status.
     *
     * @param assignmentItemId The TaskAssigned feed item ID
     * @param userId The user accepting the task
     * @return The created TaskAccepted feed item, or null if assignment not found
     */
    suspend fun acceptTaskAssignment(assignmentItemId: String, userId: String): FeedItem.TaskAccepted?

    /**
     * Decline a task assignment.
     * Creates a TaskDeclined feed item and updates the task status.
     *
     * @param assignmentItemId The TaskAssigned feed item ID
     * @param userId The user declining the task
     * @return The created TaskDeclined feed item, or null if assignment not found
     */
    suspend fun declineTaskAssignment(assignmentItemId: String, userId: String): FeedItem.TaskDeclined?

    // ═══════════════════════════════════════════════════════════════════════════
    // FEED ITEM CREATION HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a task completed feed item.
     * Called when a task is marked as completed.
     *
     * @param taskId The completed task's ID
     * @param userId The user who completed it
     * @param notifyPartner Whether to show notification to partner
     * @return The created feed item
     */
    suspend fun createTaskCompletedItem(
        taskId: String,
        userId: String,
        notifyPartner: Boolean
    ): FeedItem.TaskCompleted

    /**
     * Create a task assigned feed item.
     * Called when user assigns a task to partner.
     *
     * @param taskId The assigned task's ID
     * @param assignerId The user assigning the task
     * @param assigneeId The partner receiving the task
     * @param note Optional note explaining the request
     * @return The created feed item
     */
    suspend fun createTaskAssignedItem(
        taskId: String,
        assignerId: String,
        assigneeId: String,
        note: String?
    ): FeedItem.TaskAssigned

    /**
     * Create a week planned feed item.
     * Called when user completes planning.
     *
     * @param weekId The planned week's ID
     * @param userId The user who planned
     * @param taskCount Number of tasks planned
     * @return The created feed item
     */
    suspend fun createWeekPlannedItem(
        weekId: String,
        userId: String,
        taskCount: Int
    ): FeedItem.WeekPlanned

    /**
     * Create a week reviewed feed item.
     * Called when user completes review.
     *
     * @param weekId The reviewed week's ID
     * @param userId The user who reviewed
     * @return The created feed item
     */
    suspend fun createWeekReviewedItem(
        weekId: String,
        userId: String
    ): FeedItem.WeekReviewed

    /**
     * Create a partner joined feed item.
     * Called when partner accepts invitation.
     *
     * @param partnerId The partner's user ID
     * @param partnerName The partner's display name
     * @param forUserId The user receiving this feed item
     * @return The created feed item
     */
    suspend fun createPartnerJoinedItem(
        partnerId: String,
        partnerName: String,
        forUserId: String
    ): FeedItem.PartnerJoined

    /**
     * Create or update an AI plan prompt.
     * Generated at the start of the week if user hasn't planned yet.
     *
     * @param userId The user to prompt
     * @param rolloverTaskCount Number of tasks rolled over
     * @return The created or existing feed item
     */
    suspend fun createOrUpdateAiPlanPrompt(
        userId: String,
        rolloverTaskCount: Int
    ): FeedItem.AiPlanPrompt

    /**
     * Create or update an AI review prompt.
     * Generated during review window if user hasn't reviewed yet.
     *
     * @param userId The user to prompt
     * @param weekId The week to review
     * @param completedTaskCount Completed tasks
     * @param totalTaskCount Total tasks
     * @return The created or existing feed item
     */
    suspend fun createOrUpdateAiReviewPrompt(
        userId: String,
        weekId: String,
        completedTaskCount: Int,
        totalTaskCount: Int
    ): FeedItem.AiReviewPrompt
}
