package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * SQLDelight implementation of TaskRepository.
 * Thread-safe and reactive using Kotlin Coroutines and Flow.
 */
@OptIn(ExperimentalUuidApi::class)
class TaskRepositoryImpl(
    private val database: TandemDatabase
) : TaskRepository {

    private val queries = database.taskQueries
    private val weekIdPattern = Regex("""^\d{4}-W\d{2}$""")

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun org.epoque.tandem.data.local.Task.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            notes = notes,
            ownerId = owner_id,
            ownerType = owner_type,
            weekId = week_id,
            status = status,
            createdBy = created_by,
            requestNote = request_note,
            repeatTarget = repeat_target?.toInt(),
            repeatCompleted = repeat_completed.toInt(),
            linkedGoalId = linked_goal_id,
            reviewNote = review_note,
            rolledFromWeekId = rolled_from_week_id,
            priority = priority,
            scheduledDate = scheduled_date,
            scheduledTime = scheduled_time,
            deadline = deadline,
            parentTaskId = parent_task_id,
            labels = parseLabels(labels),
            createdAt = created_at,
            updatedAt = updated_at
        )
    }

    /**
     * Parse labels from JSON array string format: ["label1","label2"]
     * Uses simple string parsing to avoid kotlinx.serialization dependency.
     */
    private fun parseLabels(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            // Simple JSON array parsing: ["a","b","c"] -> listOf("a", "b", "c")
            json.trim()
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Encode labels to JSON array string format: ["label1","label2"]
     */
    private fun encodeLabels(labels: List<String>): String? {
        if (labels.isEmpty()) return null
        return labels.joinToString(",", "[", "]") { "\"$it\"" }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    private fun validateTitle(title: String) {
        require(title.isNotBlank()) { "Task title cannot be empty" }
    }

    private fun validateWeekId(weekId: String) {
        require(weekId.matches(weekIdPattern)) {
            "Invalid week ID format: $weekId. Expected format: YYYY-Www (e.g., 2026-W01)"
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    override fun observeAllTasks(userId: String): Flow<List<Task>> {
        return queries.getAllTasks()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }

    override fun observeTasksForWeek(weekId: String, userId: String): Flow<List<Task>> {
        return queries.getTasksByWeekId(weekId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }

    override fun observeTasksByOwnerType(ownerType: OwnerType, userId: String): Flow<List<Task>> {
        return queries.getAllTasks()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter {
                        (it.owner_id == userId || it.created_by == userId) &&
                        it.owner_type == ownerType
                    }
                    .map { it.toDomain() }
            }
    }

    override fun observeTasksByWeekAndOwnerType(
        weekId: String,
        ownerType: OwnerType,
        userId: String
    ): Flow<List<Task>> {
        return queries.getTasksByWeekIdAndOwnerType(weekId, ownerType)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }

    override fun observeIncompleteTasksForWeek(weekId: String, userId: String): Flow<List<Task>> {
        return queries.getIncompleteTasksByWeekId(weekId, userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomain() } }
    }

    override fun observeTasksByStatus(status: TaskStatus, userId: String): Flow<List<Task>> {
        return queries.getTasksByStatusAndOwnerId(status, userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomain() } }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun getTaskById(taskId: String): Task? = withContext(Dispatchers.IO) {
        queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun createTask(task: Task): Task = withContext(Dispatchers.IO) {
        validateTitle(task.title)
        validateWeekId(task.weekId)

        val now = Clock.System.now()
        val id = Uuid.random().toString()

        val newTask = task.copy(
            id = id,
            createdAt = now,
            updatedAt = now
        )

        queries.upsertTask(
            id = newTask.id,
            title = newTask.title,
            notes = newTask.notes,
            owner_id = newTask.ownerId,
            owner_type = newTask.ownerType,
            week_id = newTask.weekId,
            status = newTask.status,
            created_by = newTask.createdBy,
            request_note = newTask.requestNote,
            repeat_target = newTask.repeatTarget?.toLong(),
            repeat_completed = newTask.repeatCompleted.toLong(),
            linked_goal_id = newTask.linkedGoalId,
            review_note = newTask.reviewNote,
            rolled_from_week_id = newTask.rolledFromWeekId,
            priority = newTask.priority,
            scheduled_date = newTask.scheduledDate,
            scheduled_time = newTask.scheduledTime,
            deadline = newTask.deadline,
            parent_task_id = newTask.parentTaskId,
            labels = encodeLabels(newTask.labels),
            created_at = newTask.createdAt,
            updated_at = newTask.updatedAt
        )

        newTask
    }

    override suspend fun updateTask(
        taskId: String,
        title: String,
        notes: String?,
        status: TaskStatus
    ): Task? = withContext(Dispatchers.IO) {
        validateTitle(title)

        val existing = queries.getTaskById(taskId).executeAsOneOrNull()
            ?: return@withContext null

        val now = Clock.System.now()

        queries.upsertTask(
            id = existing.id,
            title = title,
            notes = notes,
            owner_id = existing.owner_id,
            owner_type = existing.owner_type,
            week_id = existing.week_id,
            status = status,
            created_by = existing.created_by,
            request_note = existing.request_note,
            repeat_target = existing.repeat_target,
            repeat_completed = existing.repeat_completed,
            linked_goal_id = existing.linked_goal_id,
            review_note = existing.review_note,
            rolled_from_week_id = existing.rolled_from_week_id,
            priority = existing.priority,
            scheduled_date = existing.scheduled_date,
            scheduled_time = existing.scheduled_time,
            deadline = existing.deadline,
            parent_task_id = existing.parent_task_id,
            labels = existing.labels,
            created_at = existing.created_at,
            updated_at = now
        )

        queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateTaskStatus(status, now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun incrementRepeatCount(taskId: String): Task? = withContext(Dispatchers.IO) {
        val existing = queries.getTaskById(taskId).executeAsOneOrNull()
            ?: return@withContext null

        val now = Clock.System.now()
        val newCount = existing.repeat_completed + 1

        queries.updateRepeatProgress(newCount, now, taskId)
        queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun updateTaskReviewNote(taskId: String, reviewNote: String?): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateReviewNote(reviewNote, now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun deleteTask(taskId: String): Boolean = withContext(Dispatchers.IO) {
        val exists = queries.getTaskById(taskId).executeAsOneOrNull() != null
        if (exists) {
            // Delete subtasks first (Feature 009)
            queries.deleteSubtasks(taskId)
            queries.deleteTaskById(taskId)
        }
        exists
    }

    override suspend fun deleteTasksForWeek(weekId: String): Int = withContext(Dispatchers.IO) {
        val count = queries.getTasksByWeekId(weekId).executeAsList().size
        queries.deleteTasksByWeekId(weekId)
        count
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // OWNER OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun updateTaskOwner(taskId: String, ownerType: OwnerType): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateTaskOwner(ownerType, now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // GOAL LINKING OPERATIONS (Feature 007: Goals System)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun linkTaskToGoal(taskId: String, goalId: String): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateLinkedGoalId(goalId, now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun unlinkTaskFromGoal(taskId: String): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateLinkedGoalId(null, now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    override fun observeTasksForGoal(goalId: String): Flow<List<Task>> {
        return queries.getTasksByLinkedGoalId(goalId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomain() } }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBTASK OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override fun observeSubtasks(parentTaskId: String): Flow<List<Task>> {
        return queries.getSubtasks(parentTaskId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomain() } }
    }

    override suspend fun createSubtask(parentTaskId: String, task: Task): Task =
        withContext(Dispatchers.IO) {
            val parent = queries.getTaskById(parentTaskId).executeAsOneOrNull()
                ?: throw IllegalArgumentException("Parent task not found: $parentTaskId")

            val subtask = task.copy(
                parentTaskId = parentTaskId,
                weekId = parent.week_id,
                ownerId = parent.owner_id,
                ownerType = parent.owner_type
            )

            createTask(subtask)
        }

    override suspend fun getSubtaskCounts(parentTaskId: String): Pair<Int, Int> =
        withContext(Dispatchers.IO) {
            val total = queries.countSubtasks(parentTaskId).executeAsOne().toInt()
            val completed = queries.countCompletedSubtasks(parentTaskId).executeAsOne().toInt()
            Pair(total, completed)
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCHEDULE OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun updateTaskSchedule(
        taskId: String,
        date: LocalDate?,
        time: LocalTime?
    ): Task? = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        queries.updateTaskSchedule(date, time, now, taskId)
        queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
    }

    override fun observeTasksByScheduledDate(
        weekId: String,
        date: LocalDate,
        userId: String
    ): Flow<List<Task>> {
        return queries.getTasksByScheduledDate(weekId, date)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }

    override fun observeUnscheduledTasks(weekId: String, userId: String): Flow<List<Task>> {
        return queries.getUnscheduledTasksByWeekId(weekId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }

    override fun observeOverdueTasks(
        weekId: String,
        today: LocalDate,
        userId: String
    ): Flow<List<Task>> {
        return queries.getOverdueTasks(weekId, today)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DEADLINE OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun updateTaskDeadline(taskId: String, deadline: Instant?): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateTaskDeadline(deadline, now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIORITY OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun updateTaskPriority(taskId: String, priority: TaskPriority): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateTaskPriority(priority, now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // LABEL OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun updateTaskLabels(taskId: String, labelIds: List<String>): Task? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            queries.updateTaskLabels(encodeLabels(labelIds), now, taskId)
            queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // TITLE/NOTES OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun updateTaskTitleAndNotes(
        taskId: String,
        title: String,
        notes: String?
    ): Task? = withContext(Dispatchers.IO) {
        validateTitle(title)
        val now = Clock.System.now()
        queries.updateTaskTitleAndNotes(title, notes, now, taskId)
        queries.getTaskById(taskId).executeAsOneOrNull()?.toDomain()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WEEK SCREEN SUPPORT (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    override fun observeCompletedTasksForWeek(weekId: String, userId: String): Flow<List<Task>> {
        return queries.getCompletedTasksByWeekId(weekId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }

    override suspend fun getTaskCountsByDate(weekId: String): Map<LocalDate, Int> =
        withContext(Dispatchers.IO) {
            queries.getTaskCountsByDateForWeek(weekId)
                .executeAsList()
                .associate { row ->
                    // scheduled_date is guaranteed non-null by the SQL WHERE clause
                    row.scheduled_date to row.task_count.toInt()
                }
        }

    override fun observeUpcomingTasks(
        weekId: String,
        fromDate: LocalDate,
        userId: String
    ): Flow<List<Task>> {
        return queries.getUpcomingTasksByWeekId(weekId, fromDate)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks ->
                tasks
                    .filter { it.owner_id == userId || it.created_by == userId }
                    .map { it.toDomain() }
            }
    }
}
