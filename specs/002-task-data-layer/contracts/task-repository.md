# Contract: TaskRepository

**Feature Branch**: `002-task-data-layer`
**Date**: 2026-01-01

## Overview

The TaskRepository interface defines the contract for task data access. It provides CRUD operations, filtering, and reactive data streams using Kotlin Flow.

---

## Interface Definition

```kotlin
package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus

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
}
```

---

## Method Requirements Mapping

| Method | Spec Requirement | Notes |
|--------|------------------|-------|
| observeAllTasks | FR-003 | Reactive, filters by userId |
| observeTasksForWeek | FR-004 | Reactive, ISO 8601 week format |
| observeTasksByOwnerType | FR-005 | Reactive, SELF/PARTNER/SHARED |
| observeTasksByWeekAndOwnerType | FR-004, FR-005 | Combined filter |
| getTaskById | - | One-shot for edit screens |
| createTask | FR-001, FR-002, FR-015 | Validates title, generates ID/timestamps |
| updateTask | FR-006, FR-007 | Updates title, notes, status |
| updateTaskStatus | FR-009 | Convenience for status-only updates |
| incrementRepeatCount | FR-010 | For repeating tasks |
| updateTaskReviewNote | - | Used during week review |
| deleteTask | FR-008 | By ID |
| deleteTasksForWeek | - | Batch delete for week cleanup |

---

## Validation Behavior

### createTask

```
IF title.isBlank() THEN
  throw IllegalArgumentException("Task title cannot be empty")

IF NOT weekId.matches(ISO_8601_WEEK_PATTERN) THEN
  throw IllegalArgumentException("Invalid week ID format: $weekId")

SET task.id = generateUuid()
SET task.createdAt = Clock.System.now()
SET task.updatedAt = Clock.System.now()
SET task.status = PENDING (if not specified)
SET task.repeatCompleted = 0 (if not specified)

INSERT task
RETURN task
```

### updateTask

```
IF title.isBlank() THEN
  throw IllegalArgumentException("Task title cannot be empty")

IF NOT exists(taskId) THEN
  RETURN null

SET updatedAt = Clock.System.now()
UPDATE task
RETURN updated task
```

---

## Flow Behavior

All `observe*` methods return `Flow<List<Task>>` with the following behavior:

1. **Immediate emission**: First emission contains current database state
2. **Reactive updates**: New emissions on any relevant database change
3. **Empty state**: Returns `emptyList()` if no matching tasks (not an error)
4. **Dispatcher**: Queries execute on `Dispatchers.IO`

---

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Empty title | `IllegalArgumentException` |
| Invalid week ID format | `IllegalArgumentException` |
| Task not found (update/delete) | Return `null` or `false` |
| Database error | Propagate as exception |

---

## Thread Safety

- All suspend functions are main-safe
- Flow emissions occur on IO dispatcher
- Collect on Main dispatcher for UI updates

---

## Usage Examples

### Creating a Task

```kotlin
val task = Task(
    id = "", // Will be generated
    title = "Grocery shopping",
    notes = "Buy milk, eggs, bread",
    ownerId = currentUserId,
    ownerType = OwnerType.SELF,
    weekId = "2026-W01",
    status = TaskStatus.PENDING,
    createdBy = currentUserId,
    repeatTarget = null,
    repeatCompleted = 0,
    linkedGoalId = null,
    reviewNote = null,
    rolledFromWeekId = null,
    createdAt = Instant.DISTANT_PAST, // Will be set
    updatedAt = Instant.DISTANT_PAST  // Will be set
)

val created = taskRepository.createTask(task)
```

### Observing Tasks for Current Week

```kotlin
taskRepository.observeTasksForWeek(currentWeekId, userId)
    .collect { tasks ->
        updateUi(tasks)
    }
```

### Completing a Task

```kotlin
taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
```
