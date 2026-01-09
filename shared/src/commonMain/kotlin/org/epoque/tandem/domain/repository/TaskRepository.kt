package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus

/**
 * Repository interface for task data access.
 * Provides CRUD operations, filtering, and reactive data streams using Kotlin Flow.
 */
interface TaskRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe all tasks accessible to the current user.
     * Emits when any task is created, updated, or deleted.
     *
     * @param userId The current user's ID
     * @return Flow of all tasks where user is owner or creator
     */
    fun observeAllTasks(userId: String): Flow<List<Task>>

    /**
     * Observe tasks for a specific week.
     *
     * @param weekId ISO 8601 week ID (e.g., "2026-W01")
     * @param userId The current user's ID
     * @return Flow of tasks for the specified week
     */
    fun observeTasksForWeek(weekId: String, userId: String): Flow<List<Task>>

    /**
     * Observe tasks filtered by owner type.
     *
     * @param ownerType SELF, PARTNER, or SHARED
     * @param userId The current user's ID
     * @return Flow of tasks matching the owner type
     */
    fun observeTasksByOwnerType(ownerType: OwnerType, userId: String): Flow<List<Task>>

    /**
     * Observe tasks filtered by both week and owner type.
     *
     * @param weekId ISO 8601 week ID
     * @param ownerType SELF, PARTNER, or SHARED
     * @param userId The current user's ID
     * @return Flow of matching tasks
     */
    fun observeTasksByWeekAndOwnerType(
        weekId: String,
        ownerType: OwnerType,
        userId: String
    ): Flow<List<Task>>

    /**
     * Observe incomplete tasks from a specific week (for rollover in planning).
     * Returns only tasks with status = PENDING.
     *
     * @param weekId ISO 8601 week ID
     * @param userId The current user's ID
     * @return Flow of incomplete tasks from the specified week
     */
    fun observeIncompleteTasksForWeek(weekId: String, userId: String): Flow<List<Task>>

    /**
     * Observe tasks by status for the current user.
     * Used for querying partner requests (PENDING_ACCEPTANCE status).
     *
     * @param status The task status to filter by
     * @param userId The current user's ID
     * @return Flow of tasks with the specified status
     */
    fun observeTasksByStatus(status: TaskStatus, userId: String): Flow<List<Task>>

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get a single task by ID.
     *
     * @param taskId The task's unique identifier
     * @return The task, or null if not found
     */
    suspend fun getTaskById(taskId: String): Task?

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a new task.
     *
     * @param task The task to create (id, createdAt, updatedAt will be set automatically)
     * @return The created task with generated ID and timestamps
     * @throws IllegalArgumentException if title is empty
     * @throws IllegalArgumentException if weekId format is invalid
     */
    suspend fun createTask(task: Task): Task

    /**
     * Update an existing task's content.
     *
     * @param taskId The task ID to update
     * @param title New title (non-empty)
     * @param notes New notes (nullable)
     * @param status New status
     * @return The updated task, or null if task not found
     * @throws IllegalArgumentException if title is empty
     */
    suspend fun updateTask(
        taskId: String,
        title: String,
        notes: String?,
        status: TaskStatus
    ): Task?

    /**
     * Update only the task's status.
     *
     * @param taskId The task ID
     * @param status The new status
     * @return The updated task, or null if not found
     */
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Task?

    /**
     * Increment the repeat completion count for a repeating task.
     *
     * @param taskId The task ID
     * @return The updated task, or null if not found
     */
    suspend fun incrementRepeatCount(taskId: String): Task?

    /**
     * Update the review note for a task.
     *
     * @param taskId The task ID
     * @param reviewNote The review note
     * @return The updated task, or null if not found
     */
    suspend fun updateTaskReviewNote(taskId: String, reviewNote: String?): Task?

    /**
     * Delete a task.
     *
     * @param taskId The task ID to delete
     * @return true if deleted, false if task not found
     */
    suspend fun deleteTask(taskId: String): Boolean

    /**
     * Delete all tasks for a specific week.
     *
     * @param weekId The week ID
     * @return Number of tasks deleted
     */
    suspend fun deleteTasksForWeek(weekId: String): Int

    // ═══════════════════════════════════════════════════════════════════════════
    // GOAL LINKING OPERATIONS (Feature 007: Goals System)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Link a task to a goal.
     *
     * @param taskId The task ID
     * @param goalId The goal ID to link to
     * @return The updated task, or null if not found
     */
    suspend fun linkTaskToGoal(taskId: String, goalId: String): Task?

    /**
     * Unlink a task from its goal.
     *
     * @param taskId The task ID
     * @return The updated task, or null if not found
     */
    suspend fun unlinkTaskFromGoal(taskId: String): Task?

    /**
     * Observe tasks linked to a specific goal.
     *
     * @param goalId The goal ID
     * @return Flow of tasks linked to this goal
     */
    fun observeTasksForGoal(goalId: String): Flow<List<Task>>

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBTASK OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe subtasks for a parent task.
     *
     * @param parentTaskId The parent task's ID
     * @return Flow of subtasks
     */
    fun observeSubtasks(parentTaskId: String): Flow<List<Task>>

    /**
     * Create a subtask under a parent task.
     * The subtask inherits weekId and ownerId from parent.
     *
     * @param parentTaskId The parent task's ID
     * @param task The subtask to create
     * @return The created subtask
     */
    suspend fun createSubtask(parentTaskId: String, task: Task): Task

    /**
     * Get subtask counts for a parent task.
     *
     * @param parentTaskId The parent task's ID
     * @return Pair of (total subtasks, completed subtasks)
     */
    suspend fun getSubtaskCounts(parentTaskId: String): Pair<Int, Int>

    // ═══════════════════════════════════════════════════════════════════════════
    // SCHEDULE OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Update a task's schedule (date and time).
     *
     * @param taskId The task ID
     * @param date The scheduled date (null to clear)
     * @param time The scheduled time (null for all-day)
     * @return The updated task, or null if not found
     */
    suspend fun updateTaskSchedule(
        taskId: String,
        date: LocalDate?,
        time: LocalTime?
    ): Task?

    /**
     * Observe tasks for a specific scheduled date within a week.
     *
     * @param weekId ISO 8601 week ID
     * @param date The scheduled date
     * @param userId The current user's ID
     * @return Flow of tasks scheduled for the specified date
     */
    fun observeTasksByScheduledDate(
        weekId: String,
        date: LocalDate,
        userId: String
    ): Flow<List<Task>>

    /**
     * Observe unscheduled tasks for a week (no scheduled date).
     *
     * @param weekId ISO 8601 week ID
     * @param userId The current user's ID
     * @return Flow of unscheduled tasks
     */
    fun observeUnscheduledTasks(weekId: String, userId: String): Flow<List<Task>>

    /**
     * Observe overdue tasks for a week.
     *
     * @param weekId ISO 8601 week ID
     * @param today Today's date
     * @param userId The current user's ID
     * @return Flow of overdue tasks
     */
    fun observeOverdueTasks(
        weekId: String,
        today: LocalDate,
        userId: String
    ): Flow<List<Task>>

    // ═══════════════════════════════════════════════════════════════════════════
    // DEADLINE OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Update a task's deadline.
     *
     * @param taskId The task ID
     * @param deadline The deadline instant (null to clear)
     * @return The updated task, or null if not found
     */
    suspend fun updateTaskDeadline(taskId: String, deadline: Instant?): Task?

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIORITY OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Update a task's priority.
     *
     * @param taskId The task ID
     * @param priority The new priority (P1-P4)
     * @return The updated task, or null if not found
     */
    suspend fun updateTaskPriority(taskId: String, priority: TaskPriority): Task?

    // ═══════════════════════════════════════════════════════════════════════════
    // LABEL OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Update a task's labels.
     *
     * @param taskId The task ID
     * @param labelIds List of label IDs
     * @return The updated task, or null if not found
     */
    suspend fun updateTaskLabels(taskId: String, labelIds: List<String>): Task?

    // ═══════════════════════════════════════════════════════════════════════════
    // TITLE/NOTES OPERATIONS (Feature 009: UI Redesign)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Update only a task's title and notes.
     *
     * @param taskId The task ID
     * @param title The new title
     * @param notes The new notes (nullable)
     * @return The updated task, or null if not found
     */
    suspend fun updateTaskTitleAndNotes(taskId: String, title: String, notes: String?): Task?
}
