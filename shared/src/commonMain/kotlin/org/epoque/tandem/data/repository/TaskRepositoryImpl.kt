package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
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
            repeatTarget = repeat_target?.toInt(),
            repeatCompleted = repeat_completed.toInt(),
            linkedGoalId = linked_goal_id,
            reviewNote = review_note,
            rolledFromWeekId = rolled_from_week_id,
            createdAt = created_at,
            updatedAt = updated_at
        )
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
            repeat_target = newTask.repeatTarget?.toLong(),
            repeat_completed = newTask.repeatCompleted.toLong(),
            linked_goal_id = newTask.linkedGoalId,
            review_note = newTask.reviewNote,
            rolled_from_week_id = newTask.rolledFromWeekId,
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
            repeat_target = existing.repeat_target,
            repeat_completed = existing.repeat_completed,
            linked_goal_id = existing.linked_goal_id,
            review_note = existing.review_note,
            rolled_from_week_id = existing.rolled_from_week_id,
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
            queries.deleteTaskById(taskId)
        }
        exists
    }

    override suspend fun deleteTasksForWeek(weekId: String): Int = withContext(Dispatchers.IO) {
        val count = queries.getTasksByWeekId(weekId).executeAsList().size
        queries.deleteTasksByWeekId(weekId)
        count
    }
}
