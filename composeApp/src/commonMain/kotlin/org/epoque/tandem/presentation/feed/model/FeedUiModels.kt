package org.epoque.tandem.presentation.feed.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.domain.model.FeedActor
import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.model.TaskPriority

/**
 * Filter options for the feed.
 */
enum class FeedFilter {
    ALL,      // Shows all feed items
    TASKS,    // Shows only task-related items
    MESSAGES  // Shows only messages
}

/**
 * A group of feed items for a single day.
 */
data class FeedDayGroup(
    val date: LocalDate,
    val dayLabel: String,     // "Today", "Yesterday", "Monday", etc.
    val dateLabel: String,    // "Thursday, Jan 23", "Jan 20", etc.
    val items: List<FeedUiItem>
)

/**
 * UI representation of a feed item.
 * Contains all display-ready data for rendering feed cards.
 */
sealed class FeedUiItem {
    abstract val id: String
    abstract val timestamp: Instant
    abstract val isRead: Boolean
    abstract val actorName: String
    abstract val actorType: FeedActor.ActorType
    abstract val actorInitial: String
    abstract val timeLabel: String  // "9:30 AM"

    /**
     * Task completed by user or partner.
     */
    data class TaskCompleted(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String,
        val taskId: String,
        val taskTitle: String,
        val taskPriority: TaskPriority,
        val notifiedPartner: Boolean
    ) : FeedUiItem() {
        val actionLabel: String = "completed a task"
        val showNotificationIndicator: Boolean = notifiedPartner && actorType == FeedActor.ActorType.PARTNER
    }

    /**
     * Task assigned by partner.
     */
    data class TaskAssigned(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String,
        val taskId: String,
        val taskTitle: String,
        val taskPriority: TaskPriority,
        val note: String?
    ) : FeedUiItem() {
        val actionLabel: String = "assigned you a task"
    }

    /**
     * Task accepted by partner.
     */
    data class TaskAccepted(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String,
        val taskId: String,
        val taskTitle: String,
        val taskPriority: TaskPriority
    ) : FeedUiItem() {
        val actionLabel: String = "accepted your task"
    }

    /**
     * Task declined by partner.
     */
    data class TaskDeclined(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String,
        val taskId: String,
        val taskTitle: String,
        val taskPriority: TaskPriority
    ) : FeedUiItem() {
        val actionLabel: String = "declined your task"
    }

    /**
     * Message from user or partner.
     */
    data class Message(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String,
        val text: String
    ) : FeedUiItem() {
        // Messages don't have an action label, just the actor name
        val actionLabel: String? = null
    }

    /**
     * Week planned event.
     */
    data class WeekPlanned(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String,
        val weekId: String,
        val weekStartDate: LocalDate,
        val taskCount: Int
    ) : FeedUiItem() {
        val actionLabel: String = "finished planning"
        val weekLabel: String get() = "Week of ${formatMonthDay(weekStartDate)}"
        val taskCountLabel: String get() = "Planned $taskCount tasks"

        private fun formatMonthDay(date: LocalDate): String {
            val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }
            return "$month ${date.dayOfMonth}"
        }
    }

    /**
     * Week reviewed event.
     */
    data class WeekReviewed(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String,
        val weekId: String,
        val weekStartDate: LocalDate
    ) : FeedUiItem() {
        val actionLabel: String = "finished review"
        val weekLabel: String get() = "Week of ${formatMonthDay(weekStartDate)}"

        private fun formatMonthDay(date: LocalDate): String {
            val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }
            return "$month ${date.dayOfMonth}"
        }
    }

    /**
     * Partner joined event.
     */
    data class PartnerJoined(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val actorName: String,
        override val actorType: FeedActor.ActorType,
        override val actorInitial: String,
        override val timeLabel: String
    ) : FeedUiItem() {
        val actionLabel: String = "joined as your partner"
    }

    /**
     * AI plan prompt.
     */
    data class AiPlanPrompt(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val timeLabel: String,
        val rolloverTaskCount: Int
    ) : FeedUiItem() {
        override val actorName: String = "Tandem"
        override val actorType: FeedActor.ActorType = FeedActor.ActorType.AI
        override val actorInitial: String = "T"

        val title: String = "Ready to plan your week?"
        val subtitle: String = if (rolloverTaskCount > 0) {
            "You have $rolloverTaskCount tasks rolled over from last week."
        } else {
            "Start fresh and set your priorities for the week."
        }
        val buttonLabel: String = "Start Planning"
    }

    /**
     * AI review prompt.
     */
    data class AiReviewPrompt(
        override val id: String,
        override val timestamp: Instant,
        override val isRead: Boolean,
        override val timeLabel: String,
        val weekId: String,
        val completedTaskCount: Int,
        val totalTaskCount: Int
    ) : FeedUiItem() {
        override val actorName: String = "Tandem"
        override val actorType: FeedActor.ActorType = FeedActor.ActorType.AI
        override val actorInitial: String = "T"

        val title: String = "Time for your weekly review"
        val subtitle: String = "You completed $completedTaskCount of $totalTaskCount tasks."
        val buttonLabel: String = "Start Review"
    }

    companion object {
        /**
         * Convert domain FeedItem to UI model.
         */
        fun fromDomain(item: FeedItem, currentUserId: String): FeedUiItem {
            val timeLabel = formatTime(item.timestamp)

            return when (item) {
                is FeedItem.TaskCompleted -> TaskCompleted(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = getActorDisplayName(item.actor, currentUserId),
                    actorType = item.actor.type,
                    actorInitial = item.actor.initial,
                    timeLabel = timeLabel,
                    taskId = item.task.id,
                    taskTitle = item.task.title,
                    taskPriority = item.task.priority,
                    notifiedPartner = item.notifiedPartner
                )

                is FeedItem.TaskAssigned -> TaskAssigned(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = getActorDisplayName(item.actor, currentUserId),
                    actorType = item.actor.type,
                    actorInitial = item.actor.initial,
                    timeLabel = timeLabel,
                    taskId = item.task.id,
                    taskTitle = item.task.title,
                    taskPriority = item.task.priority,
                    note = item.note
                )

                is FeedItem.TaskAccepted -> TaskAccepted(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = getActorDisplayName(item.actor, currentUserId),
                    actorType = item.actor.type,
                    actorInitial = item.actor.initial,
                    timeLabel = timeLabel,
                    taskId = item.task.id,
                    taskTitle = item.task.title,
                    taskPriority = item.task.priority
                )

                is FeedItem.TaskDeclined -> TaskDeclined(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = getActorDisplayName(item.actor, currentUserId),
                    actorType = item.actor.type,
                    actorInitial = item.actor.initial,
                    timeLabel = timeLabel,
                    taskId = item.task.id,
                    taskTitle = item.task.title,
                    taskPriority = item.task.priority
                )

                is FeedItem.Message -> Message(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = getActorDisplayName(item.actor, currentUserId),
                    actorType = item.actor.type,
                    actorInitial = item.actor.initial,
                    timeLabel = timeLabel,
                    text = item.text
                )

                is FeedItem.WeekPlanned -> WeekPlanned(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = getActorDisplayName(item.actor, currentUserId),
                    actorType = item.actor.type,
                    actorInitial = item.actor.initial,
                    timeLabel = timeLabel,
                    weekId = item.weekId,
                    weekStartDate = item.weekStartDate,
                    taskCount = item.taskCount
                )

                is FeedItem.WeekReviewed -> WeekReviewed(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = getActorDisplayName(item.actor, currentUserId),
                    actorType = item.actor.type,
                    actorInitial = item.actor.initial,
                    timeLabel = timeLabel,
                    weekId = item.weekId,
                    weekStartDate = item.weekStartDate
                )

                is FeedItem.PartnerJoined -> PartnerJoined(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    actorName = item.partner.name,
                    actorType = FeedActor.ActorType.SYSTEM,
                    actorInitial = item.partner.initial,
                    timeLabel = timeLabel
                )

                is FeedItem.AiPlanPrompt -> AiPlanPrompt(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    timeLabel = timeLabel,
                    rolloverTaskCount = item.rolloverTaskCount
                )

                is FeedItem.AiReviewPrompt -> AiReviewPrompt(
                    id = item.id,
                    timestamp = item.timestamp,
                    isRead = item.isRead,
                    timeLabel = timeLabel,
                    weekId = item.weekId,
                    completedTaskCount = item.completedTaskCount,
                    totalTaskCount = item.totalTaskCount
                )
            }
        }

        private fun getActorDisplayName(actor: FeedActor, currentUserId: String): String {
            return if (actor.id == currentUserId) "You" else actor.name
        }

        private fun formatTime(instant: Instant): String {
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val hour = dateTime.hour
            val minute = dateTime.minute
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
        }
    }
}
