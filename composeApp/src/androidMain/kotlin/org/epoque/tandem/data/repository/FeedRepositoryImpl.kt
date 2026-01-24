package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.FeedActor
import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.model.FeedItemType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import java.util.UUID

/**
 * Implementation of FeedRepository using SQLDelight for local persistence.
 */
class FeedRepositoryImpl(
    private val database: TandemDatabase,
    private val taskRepository: TaskRepository,
    private val weekRepository: WeekRepository
) : FeedRepository {

    private val feedItemQueries = database.feedItemQueries

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    override fun observeFeedItems(userId: String): Flow<List<FeedItem>> {
        return feedItemQueries.getAllFeedItemsForUser(userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { items -> items.mapNotNull { mapToFeedItem(it) } }
    }

    override fun observeFeedItemsByType(userId: String, types: List<FeedItemType>): Flow<List<FeedItem>> {
        return feedItemQueries.getFeedItemsByType(userId, types)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { items -> items.mapNotNull { mapToFeedItem(it) } }
    }

    override fun observeUnreadCount(userId: String): Flow<Int> {
        return feedItemQueries.getUnreadCount(userId)
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { it.toInt() }
    }

    override fun observeFeedItemsForDate(userId: String, date: LocalDate): Flow<List<FeedItem>> {
        return feedItemQueries.getFeedItemsForDate(userId, date.toString())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { items -> items.mapNotNull { mapToFeedItem(it) } }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun getFeedItemById(itemId: String): FeedItem? = withContext(Dispatchers.IO) {
        feedItemQueries.getFeedItemById(itemId).executeAsOneOrNull()?.let { mapToFeedItem(it) }
    }

    override suspend fun getLastReadTimestamp(userId: String): Instant? = withContext(Dispatchers.IO) {
        feedItemQueries.getLastReadTimestamp(userId).executeAsOneOrNull()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun createFeedItem(item: FeedItem): FeedItem = withContext(Dispatchers.IO) {
        val id = if (item.id.isEmpty()) UUID.randomUUID().toString() else item.id
        val now = Clock.System.now()

        when (item) {
            is FeedItem.TaskCompleted -> insertTaskCompletedItem(id, item, now)
            is FeedItem.TaskAssigned -> insertTaskAssignedItem(id, item, now)
            is FeedItem.TaskAccepted -> insertTaskAcceptedItem(id, item, now)
            is FeedItem.TaskDeclined -> insertTaskDeclinedItem(id, item, now)
            is FeedItem.Message -> insertMessageItem(id, item, now)
            is FeedItem.WeekPlanned -> insertWeekPlannedItem(id, item, now)
            is FeedItem.WeekReviewed -> insertWeekReviewedItem(id, item, now)
            is FeedItem.PartnerJoined -> insertPartnerJoinedItem(id, item, now)
            is FeedItem.AiPlanPrompt -> insertAiPlanPromptItem(id, item, now)
            is FeedItem.AiReviewPrompt -> insertAiReviewPromptItem(id, item, now)
        }

        // Return item with generated ID
        getFeedItemById(id) ?: item
    }

    override suspend fun markAsRead(itemId: String): Boolean = withContext(Dispatchers.IO) {
        feedItemQueries.markAsRead(itemId)
        true
    }

    override suspend fun markAllAsReadUpTo(userId: String, timestamp: Instant): Int = withContext(Dispatchers.IO) {
        feedItemQueries.markAllAsReadUpTo(userId, timestamp)
        // Return approximate count
        feedItemQueries.getReadItemsCount(userId).executeAsOne().toInt()
    }

    override suspend fun updateLastReadTimestamp(userId: String, timestamp: Instant) = withContext(Dispatchers.IO) {
        feedItemQueries.upsertLastReadTimestamp(userId, timestamp)
    }

    override suspend fun dismissAiPrompt(itemId: String): Boolean = withContext(Dispatchers.IO) {
        feedItemQueries.dismissAiPrompt(itemId)
        true
    }

    override suspend fun deleteFeedItem(itemId: String): Boolean = withContext(Dispatchers.IO) {
        feedItemQueries.deleteFeedItem(itemId)
        true
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MESSAGE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun sendMessage(senderId: String, recipientId: String, text: String): FeedItem.Message = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val actor = FeedActor(
            id = senderId,
            name = "You", // Will be resolved by UI
            type = FeedActor.ActorType.SELF
        )

        val item = FeedItem.Message(
            id = id,
            text = text,
            actor = actor,
            sentAt = now,
            isRead = true // Own messages are always read
        )

        // Insert for sender (self)
        insertMessageItem(id, item, now)

        // Also create a copy for the recipient with unread state
        val recipientItemId = UUID.randomUUID().toString()
        feedItemQueries.upsertFeedItem(
            id = recipientItemId,
            user_id = recipientId,
            type = FeedItemType.MESSAGE,
            timestamp = now,
            is_read = 0L, // false
            actor_id = senderId,
            actor_name = "Partner", // Will be resolved by sync
            actor_type = "PARTNER",
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = text,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = now
        )

        item
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TASK ASSIGNMENT OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun acceptTaskAssignment(assignmentItemId: String, userId: String): FeedItem.TaskAccepted? = withContext(Dispatchers.IO) {
        val assignment = getFeedItemById(assignmentItemId) as? FeedItem.TaskAssigned ?: return@withContext null

        // Create TaskAccepted feed item for the assigner
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val actor = FeedActor(
            id = userId,
            name = "You",
            type = FeedActor.ActorType.SELF
        )

        val item = FeedItem.TaskAccepted(
            id = id,
            task = assignment.task,
            actor = actor,
            acceptedAt = now,
            isRead = true
        )

        insertTaskAcceptedItem(id, item, now)
        markAsRead(assignmentItemId)

        item
    }

    override suspend fun declineTaskAssignment(assignmentItemId: String, userId: String): FeedItem.TaskDeclined? = withContext(Dispatchers.IO) {
        val assignment = getFeedItemById(assignmentItemId) as? FeedItem.TaskAssigned ?: return@withContext null

        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val actor = FeedActor(
            id = userId,
            name = "You",
            type = FeedActor.ActorType.SELF
        )

        val item = FeedItem.TaskDeclined(
            id = id,
            task = assignment.task,
            actor = actor,
            declinedAt = now,
            isRead = true
        )

        insertTaskDeclinedItem(id, item, now)
        markAsRead(assignmentItemId)

        item
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FEED ITEM CREATION HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun createTaskCompletedItem(
        taskId: String,
        userId: String,
        notifyPartner: Boolean
    ): FeedItem.TaskCompleted = withContext(Dispatchers.IO) {
        val task = taskRepository.getTaskById(taskId)
            ?: throw IllegalArgumentException("Task not found: $taskId")

        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val actor = FeedActor(
            id = userId,
            name = "You",
            type = FeedActor.ActorType.SELF
        )

        val item = FeedItem.TaskCompleted(
            id = id,
            task = task,
            actor = actor,
            completedAt = now,
            notifiedPartner = notifyPartner,
            isRead = true
        )

        insertTaskCompletedItem(id, item, now)
        item
    }

    override suspend fun createTaskAssignedItem(
        taskId: String,
        assignerId: String,
        assigneeId: String,
        note: String?
    ): FeedItem.TaskAssigned = withContext(Dispatchers.IO) {
        val task = taskRepository.getTaskById(taskId)
            ?: throw IllegalArgumentException("Task not found: $taskId")

        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val actor = FeedActor(
            id = assignerId,
            name = "Partner", // Will be resolved
            type = FeedActor.ActorType.PARTNER
        )

        val item = FeedItem.TaskAssigned(
            id = id,
            task = task,
            actor = actor,
            assignedAt = now,
            note = note,
            isRead = false
        )

        // Insert for the assignee (they see this item)
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = assigneeId,
            type = FeedItemType.TASK_ASSIGNED,
            timestamp = now,
            is_read = 0L, // false
            actor_id = assignerId,
            actor_name = actor.name,
            actor_type = actor.type.name,
            task_id = task.id,
            task_title = task.title,
            task_priority = task.priority.name,
            notified_partner = null,
            assignment_note = note,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = now
        )

        item
    }

    override suspend fun createWeekPlannedItem(
        weekId: String,
        userId: String,
        taskCount: Int
    ): FeedItem.WeekPlanned = withContext(Dispatchers.IO) {
        val week = weekRepository.getWeekById(weekId)
        val weekStartDate = week?.startDate ?: parseWeekStartDate(weekId)

        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val actor = FeedActor(
            id = userId,
            name = "You",
            type = FeedActor.ActorType.SELF
        )

        val item = FeedItem.WeekPlanned(
            id = id,
            weekId = weekId,
            weekStartDate = weekStartDate,
            actor = actor,
            plannedAt = now,
            taskCount = taskCount,
            isRead = true
        )

        insertWeekPlannedItem(id, item, now)
        item
    }

    override suspend fun createWeekReviewedItem(
        weekId: String,
        userId: String
    ): FeedItem.WeekReviewed = withContext(Dispatchers.IO) {
        val week = weekRepository.getWeekById(weekId)
        val weekStartDate = week?.startDate ?: parseWeekStartDate(weekId)

        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val actor = FeedActor(
            id = userId,
            name = "You",
            type = FeedActor.ActorType.SELF
        )

        val item = FeedItem.WeekReviewed(
            id = id,
            weekId = weekId,
            weekStartDate = weekStartDate,
            actor = actor,
            reviewedAt = now,
            isRead = true
        )

        insertWeekReviewedItem(id, item, now)
        item
    }

    override suspend fun createPartnerJoinedItem(
        partnerId: String,
        partnerName: String,
        forUserId: String
    ): FeedItem.PartnerJoined = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val partner = FeedActor(
            id = partnerId,
            name = partnerName,
            type = FeedActor.ActorType.PARTNER
        )

        val item = FeedItem.PartnerJoined(
            id = id,
            partner = partner,
            joinedAt = now,
            isRead = false
        )

        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = forUserId,
            type = FeedItemType.PARTNER_JOINED,
            timestamp = now,
            is_read = 0L, // false
            actor_id = partnerId,
            actor_name = partnerName,
            actor_type = FeedActor.ActorType.PARTNER.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = partnerId,
            partner_name = partnerName,
            created_at = now
        )

        item
    }

    override suspend fun createOrUpdateAiPlanPrompt(
        userId: String,
        rolloverTaskCount: Int
    ): FeedItem.AiPlanPrompt = withContext(Dispatchers.IO) {
        // Check for existing active prompt
        val existing = feedItemQueries.getActiveAiPlanPrompt(userId).executeAsOneOrNull()
        if (existing != null) {
            return@withContext mapToFeedItem(existing) as FeedItem.AiPlanPrompt
        }

        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()

        val item = FeedItem.AiPlanPrompt(
            id = id,
            rolloverTaskCount = rolloverTaskCount,
            createdAt = now,
            dismissed = false,
            isRead = false
        )

        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = userId,
            type = FeedItemType.AI_PLAN_PROMPT,
            timestamp = now,
            is_read = 0L, // false
            actor_id = null,
            actor_name = "Tandem",
            actor_type = FeedActor.ActorType.AI.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = rolloverTaskCount.toLong(),
            dismissed = 0L, // false
            partner_id = null,
            partner_name = null,
            created_at = now
        )

        item
    }

    override suspend fun createOrUpdateAiReviewPrompt(
        userId: String,
        weekId: String,
        completedTaskCount: Int,
        totalTaskCount: Int
    ): FeedItem.AiReviewPrompt = withContext(Dispatchers.IO) {
        // Check for existing active prompt for this week
        val existing = feedItemQueries.getActiveAiReviewPrompt(userId, weekId).executeAsOneOrNull()
        if (existing != null) {
            return@withContext mapToFeedItem(existing) as FeedItem.AiReviewPrompt
        }

        val week = weekRepository.getWeekById(weekId)
        val weekStartDate = week?.startDate ?: parseWeekStartDate(weekId)

        val id = UUID.randomUUID().toString()
        val now = Clock.System.now()

        val item = FeedItem.AiReviewPrompt(
            id = id,
            weekId = weekId,
            weekStartDate = weekStartDate,
            completedTaskCount = completedTaskCount,
            totalTaskCount = totalTaskCount,
            createdAt = now,
            dismissed = false,
            isRead = false
        )

        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = userId,
            type = FeedItemType.AI_REVIEW_PROMPT,
            timestamp = now,
            is_read = 0L, // false
            actor_id = null,
            actor_name = "Tandem",
            actor_type = FeedActor.ActorType.AI.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = weekId,
            week_start_date = weekStartDate,
            task_count = null,
            completed_task_count = completedTaskCount.toLong(),
            total_task_count = totalTaskCount.toLong(),
            rollover_task_count = null,
            dismissed = 0L, // false
            partner_id = null,
            partner_name = null,
            created_at = now
        )

        item
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun mapToFeedItem(row: org.epoque.tandem.data.local.FeedItem): FeedItem? {
        val actorId = row.actor_id
        val actor = if (actorId != null) {
            FeedActor(
                id = actorId,
                name = row.actor_name ?: "Unknown",
                type = FeedActor.ActorType.valueOf(row.actor_type ?: "SELF")
            )
        } else {
            FeedActor(
                id = "",
                name = row.actor_name ?: "Tandem",
                type = FeedActor.ActorType.AI
            )
        }

        val isRead = row.is_read != 0L

        return when (row.type) {
            FeedItemType.TASK_COMPLETED -> {
                val task = createTaskFromRow(row) ?: return null
                FeedItem.TaskCompleted(
                    id = row.id,
                    task = task,
                    actor = actor,
                    completedAt = row.timestamp,
                    notifiedPartner = row.notified_partner?.let { it != 0L } ?: false,
                    isRead = isRead
                )
            }
            FeedItemType.TASK_ASSIGNED -> {
                val task = createTaskFromRow(row) ?: return null
                FeedItem.TaskAssigned(
                    id = row.id,
                    task = task,
                    actor = actor,
                    assignedAt = row.timestamp,
                    note = row.assignment_note,
                    isRead = isRead
                )
            }
            FeedItemType.TASK_ACCEPTED -> {
                val task = createTaskFromRow(row) ?: return null
                FeedItem.TaskAccepted(
                    id = row.id,
                    task = task,
                    actor = actor,
                    acceptedAt = row.timestamp,
                    isRead = isRead
                )
            }
            FeedItemType.TASK_DECLINED -> {
                val task = createTaskFromRow(row) ?: return null
                FeedItem.TaskDeclined(
                    id = row.id,
                    task = task,
                    actor = actor,
                    declinedAt = row.timestamp,
                    isRead = isRead
                )
            }
            FeedItemType.MESSAGE -> {
                FeedItem.Message(
                    id = row.id,
                    text = row.message_text ?: "",
                    actor = actor,
                    sentAt = row.timestamp,
                    isRead = isRead
                )
            }
            FeedItemType.WEEK_PLANNED -> {
                FeedItem.WeekPlanned(
                    id = row.id,
                    weekId = row.week_id ?: "",
                    weekStartDate = row.week_start_date ?: parseWeekStartDate(row.week_id ?: ""),
                    actor = actor,
                    plannedAt = row.timestamp,
                    taskCount = row.task_count?.toInt() ?: 0,
                    isRead = isRead
                )
            }
            FeedItemType.WEEK_REVIEWED -> {
                FeedItem.WeekReviewed(
                    id = row.id,
                    weekId = row.week_id ?: "",
                    weekStartDate = row.week_start_date ?: parseWeekStartDate(row.week_id ?: ""),
                    actor = actor,
                    reviewedAt = row.timestamp,
                    isRead = isRead
                )
            }
            FeedItemType.PARTNER_JOINED -> {
                val partner = FeedActor(
                    id = row.partner_id ?: "",
                    name = row.partner_name ?: "Partner",
                    type = FeedActor.ActorType.PARTNER
                )
                FeedItem.PartnerJoined(
                    id = row.id,
                    partner = partner,
                    joinedAt = row.timestamp,
                    isRead = isRead
                )
            }
            FeedItemType.AI_PLAN_PROMPT -> {
                FeedItem.AiPlanPrompt(
                    id = row.id,
                    rolloverTaskCount = row.rollover_task_count?.toInt() ?: 0,
                    createdAt = row.timestamp,
                    dismissed = row.dismissed?.let { it != 0L } ?: false,
                    isRead = isRead
                )
            }
            FeedItemType.AI_REVIEW_PROMPT -> {
                FeedItem.AiReviewPrompt(
                    id = row.id,
                    weekId = row.week_id ?: "",
                    weekStartDate = row.week_start_date ?: parseWeekStartDate(row.week_id ?: ""),
                    completedTaskCount = row.completed_task_count?.toInt() ?: 0,
                    totalTaskCount = row.total_task_count?.toInt() ?: 0,
                    createdAt = row.timestamp,
                    dismissed = row.dismissed?.let { it != 0L } ?: false,
                    isRead = isRead
                )
            }
        }
    }

    private fun createTaskFromRow(row: org.epoque.tandem.data.local.FeedItem): Task? {
        val taskId = row.task_id ?: return null
        val taskTitle = row.task_title ?: return null
        val now = Clock.System.now()

        return Task(
            id = taskId,
            title = taskTitle,
            notes = null,
            ownerId = row.actor_id ?: "",
            ownerType = org.epoque.tandem.domain.model.OwnerType.SELF,
            weekId = "",
            status = org.epoque.tandem.domain.model.TaskStatus.PENDING,
            createdBy = row.actor_id ?: "",
            requestNote = null,
            repeatTarget = null,
            repeatCompleted = 0,
            linkedGoalId = null,
            reviewNote = null,
            rolledFromWeekId = null,
            priority = TaskPriority.valueOf(row.task_priority ?: "P4"),
            scheduledDate = null,
            scheduledTime = null,
            deadline = null,
            parentTaskId = null,
            labels = emptyList(),
            createdAt = now,
            updatedAt = now
        )
    }

    private fun parseWeekStartDate(weekId: String): LocalDate {
        // Parse "2026-W03" format and return Monday of that week
        return try {
            val parts = weekId.split("-W")
            val year = parts[0].toInt()
            val weekNum = parts[1].toInt()
            // Simple approximation: January 1 + (week - 1) * 7 days
            val jan1 = LocalDate(year, 1, 1)
            val daysToAdd = (weekNum - 1) * 7
            jan1.plus(DatePeriod(days = daysToAdd))
        } catch (e: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
    }

    private fun Boolean.toLong(): Long = if (this) 1L else 0L

    private suspend fun insertTaskCompletedItem(id: String, item: FeedItem.TaskCompleted, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.actor.id,
            type = FeedItemType.TASK_COMPLETED,
            timestamp = item.completedAt,
            is_read = item.isRead.toLong(),
            actor_id = item.actor.id,
            actor_name = item.actor.name,
            actor_type = item.actor.type.name,
            task_id = item.task.id,
            task_title = item.task.title,
            task_priority = item.task.priority.name,
            notified_partner = item.notifiedPartner.toLong(),
            assignment_note = null,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertTaskAssignedItem(id: String, item: FeedItem.TaskAssigned, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.actor.id,
            type = FeedItemType.TASK_ASSIGNED,
            timestamp = item.assignedAt,
            is_read = item.isRead.toLong(),
            actor_id = item.actor.id,
            actor_name = item.actor.name,
            actor_type = item.actor.type.name,
            task_id = item.task.id,
            task_title = item.task.title,
            task_priority = item.task.priority.name,
            notified_partner = null,
            assignment_note = item.note,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertTaskAcceptedItem(id: String, item: FeedItem.TaskAccepted, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.actor.id,
            type = FeedItemType.TASK_ACCEPTED,
            timestamp = item.acceptedAt,
            is_read = item.isRead.toLong(),
            actor_id = item.actor.id,
            actor_name = item.actor.name,
            actor_type = item.actor.type.name,
            task_id = item.task.id,
            task_title = item.task.title,
            task_priority = item.task.priority.name,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertTaskDeclinedItem(id: String, item: FeedItem.TaskDeclined, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.actor.id,
            type = FeedItemType.TASK_DECLINED,
            timestamp = item.declinedAt,
            is_read = item.isRead.toLong(),
            actor_id = item.actor.id,
            actor_name = item.actor.name,
            actor_type = item.actor.type.name,
            task_id = item.task.id,
            task_title = item.task.title,
            task_priority = item.task.priority.name,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertMessageItem(id: String, item: FeedItem.Message, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.actor.id,
            type = FeedItemType.MESSAGE,
            timestamp = item.sentAt,
            is_read = item.isRead.toLong(),
            actor_id = item.actor.id,
            actor_name = item.actor.name,
            actor_type = item.actor.type.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = item.text,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertWeekPlannedItem(id: String, item: FeedItem.WeekPlanned, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.actor.id,
            type = FeedItemType.WEEK_PLANNED,
            timestamp = item.plannedAt,
            is_read = item.isRead.toLong(),
            actor_id = item.actor.id,
            actor_name = item.actor.name,
            actor_type = item.actor.type.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = item.weekId,
            week_start_date = item.weekStartDate,
            task_count = item.taskCount.toLong(),
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertWeekReviewedItem(id: String, item: FeedItem.WeekReviewed, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.actor.id,
            type = FeedItemType.WEEK_REVIEWED,
            timestamp = item.reviewedAt,
            is_read = item.isRead.toLong(),
            actor_id = item.actor.id,
            actor_name = item.actor.name,
            actor_type = item.actor.type.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = item.weekId,
            week_start_date = item.weekStartDate,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertPartnerJoinedItem(id: String, item: FeedItem.PartnerJoined, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = item.partner.id, // The user who joined
            type = FeedItemType.PARTNER_JOINED,
            timestamp = item.joinedAt,
            is_read = item.isRead.toLong(),
            actor_id = item.partner.id,
            actor_name = item.partner.name,
            actor_type = item.partner.type.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = null,
            dismissed = null,
            partner_id = item.partner.id,
            partner_name = item.partner.name,
            created_at = createdAt
        )
    }

    private suspend fun insertAiPlanPromptItem(id: String, item: FeedItem.AiPlanPrompt, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = "", // Will be set by caller
            type = FeedItemType.AI_PLAN_PROMPT,
            timestamp = item.createdAt,
            is_read = item.isRead.toLong(),
            actor_id = null,
            actor_name = "Tandem",
            actor_type = FeedActor.ActorType.AI.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = null,
            week_start_date = null,
            task_count = null,
            completed_task_count = null,
            total_task_count = null,
            rollover_task_count = item.rolloverTaskCount.toLong(),
            dismissed = item.dismissed.toLong(),
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }

    private suspend fun insertAiReviewPromptItem(id: String, item: FeedItem.AiReviewPrompt, createdAt: Instant) {
        feedItemQueries.upsertFeedItem(
            id = id,
            user_id = "", // Will be set by caller
            type = FeedItemType.AI_REVIEW_PROMPT,
            timestamp = item.createdAt,
            is_read = item.isRead.toLong(),
            actor_id = null,
            actor_name = "Tandem",
            actor_type = FeedActor.ActorType.AI.name,
            task_id = null,
            task_title = null,
            task_priority = null,
            notified_partner = null,
            assignment_note = null,
            message_text = null,
            week_id = item.weekId,
            week_start_date = item.weekStartDate,
            task_count = null,
            completed_task_count = item.completedTaskCount.toLong(),
            total_task_count = item.totalTaskCount.toLong(),
            rollover_task_count = null,
            dismissed = item.dismissed.toLong(),
            partner_id = null,
            partner_name = null,
            created_at = createdAt
        )
    }
}
