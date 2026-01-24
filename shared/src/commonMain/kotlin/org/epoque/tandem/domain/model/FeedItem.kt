package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Represents an actor in the feed (who performed an action).
 */
data class FeedActor(
    val id: String,
    val name: String,
    val type: ActorType
) {
    enum class ActorType {
        SELF,
        PARTNER,
        SYSTEM,
        AI
    }

    val initial: String get() = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val isSelf: Boolean get() = type == ActorType.SELF
    val isPartner: Boolean get() = type == ActorType.PARTNER
    val isSystem: Boolean get() = type == ActorType.SYSTEM
    val isAi: Boolean get() = type == ActorType.AI
}

/**
 * Sealed class representing different types of feed items.
 * Each variant contains the data specific to that type of activity.
 */
sealed class FeedItem {
    abstract val id: String
    abstract val timestamp: Instant
    abstract val isRead: Boolean

    /**
     * Task was completed by user or partner.
     * @param task The task that was completed
     * @param actor Who completed it (self or partner)
     * @param completedAt When it was completed
     * @param notifiedPartner Whether partner was notified of completion
     */
    data class TaskCompleted(
        override val id: String,
        val task: Task,
        val actor: FeedActor,
        val completedAt: Instant,
        val notifiedPartner: Boolean,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = completedAt
    }

    /**
     * Partner assigned a task to the user.
     * @param task The assigned task (status = PENDING_ACCEPTANCE)
     * @param actor The partner who assigned it
     * @param assignedAt When it was assigned
     * @param note Optional note from partner explaining the request
     */
    data class TaskAssigned(
        override val id: String,
        val task: Task,
        val actor: FeedActor,
        val assignedAt: Instant,
        val note: String?,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = assignedAt
    }

    /**
     * Partner accepted a task you assigned to them.
     * @param task The accepted task
     * @param actor The partner who accepted
     * @param acceptedAt When they accepted
     */
    data class TaskAccepted(
        override val id: String,
        val task: Task,
        val actor: FeedActor,
        val acceptedAt: Instant,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = acceptedAt
    }

    /**
     * Partner declined a task you assigned to them.
     * @param task The declined task
     * @param actor The partner who declined
     * @param declinedAt When they declined
     */
    data class TaskDeclined(
        override val id: String,
        val task: Task,
        val actor: FeedActor,
        val declinedAt: Instant,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = declinedAt
    }

    /**
     * Free-form message between partners.
     * @param text The message content
     * @param actor Who sent it
     * @param sentAt When it was sent
     */
    data class Message(
        override val id: String,
        val text: String,
        val actor: FeedActor,
        val sentAt: Instant,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = sentAt
    }

    /**
     * User or partner finished planning their week.
     * @param weekId The week that was planned
     * @param weekStartDate The start date of the planned week
     * @param actor Who did the planning
     * @param plannedAt When planning was completed
     * @param taskCount Number of tasks planned
     */
    data class WeekPlanned(
        override val id: String,
        val weekId: String,
        val weekStartDate: LocalDate,
        val actor: FeedActor,
        val plannedAt: Instant,
        val taskCount: Int,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = plannedAt
    }

    /**
     * User or partner finished reviewing their week.
     * @param weekId The week that was reviewed
     * @param weekStartDate The start date of the reviewed week
     * @param actor Who did the review
     * @param reviewedAt When review was completed
     */
    data class WeekReviewed(
        override val id: String,
        val weekId: String,
        val weekStartDate: LocalDate,
        val actor: FeedActor,
        val reviewedAt: Instant,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = reviewedAt
    }

    /**
     * System event: Partner accepted invite and joined.
     * @param partner The partner who joined
     * @param joinedAt When they joined
     */
    data class PartnerJoined(
        override val id: String,
        val partner: FeedActor,
        val joinedAt: Instant,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = joinedAt
    }

    /**
     * AI prompt suggesting user start planning their week.
     * @param rolloverTaskCount Number of tasks rolled over from last week
     * @param createdAt When the prompt was generated
     * @param dismissed Whether user dismissed this prompt
     */
    data class AiPlanPrompt(
        override val id: String,
        val rolloverTaskCount: Int,
        val createdAt: Instant,
        val dismissed: Boolean = false,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = createdAt
    }

    /**
     * AI prompt suggesting user do their weekly review.
     * @param weekId The week to review
     * @param weekStartDate Start date of the week
     * @param completedTaskCount Number of completed tasks
     * @param totalTaskCount Total tasks for the week
     * @param createdAt When the prompt was generated
     * @param dismissed Whether user dismissed this prompt
     */
    data class AiReviewPrompt(
        override val id: String,
        val weekId: String,
        val weekStartDate: LocalDate,
        val completedTaskCount: Int,
        val totalTaskCount: Int,
        val createdAt: Instant,
        val dismissed: Boolean = false,
        override val isRead: Boolean = false
    ) : FeedItem() {
        override val timestamp: Instant get() = createdAt
    }
}

/**
 * Type of feed item for filtering and SQLite storage.
 */
enum class FeedItemType {
    TASK_COMPLETED,
    TASK_ASSIGNED,
    TASK_ACCEPTED,
    TASK_DECLINED,
    MESSAGE,
    WEEK_PLANNED,
    WEEK_REVIEWED,
    PARTNER_JOINED,
    AI_PLAN_PROMPT,
    AI_REVIEW_PROMPT
}
