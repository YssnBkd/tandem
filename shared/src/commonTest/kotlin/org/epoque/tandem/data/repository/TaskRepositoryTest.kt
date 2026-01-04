package org.epoque.tandem.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.local.TandemDatabaseFactory
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.TaskRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskRepositoryTest {

    private lateinit var database: TandemDatabase
    private lateinit var repository: TaskRepository
    private val testUserId = "user-123"
    private val testPartnerId = "partner-456"

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TandemDatabase.Schema.create(driver)
        database = TandemDatabaseFactory.create(driver)
        repository = TaskRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        // Database will be garbage collected
    }

    private fun createTestTask(
        title: String = "Test Task",
        weekId: String = "2026-W01",
        ownerId: String = testUserId,
        ownerType: OwnerType = OwnerType.SELF,
        status: TaskStatus = TaskStatus.PENDING
    ): Task {
        return Task(
            id = "",
            title = title,
            notes = null,
            ownerId = ownerId,
            ownerType = ownerType,
            weekId = weekId,
            status = status,
            createdBy = testUserId,
            requestNote = null,
            repeatTarget = null,
            repeatCompleted = 0,
            linkedGoalId = null,
            reviewNote = null,
            rolledFromWeekId = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    @Test
    fun `createTask generates ID and timestamps`() = runTest {
        val task = createTestTask(title = "Buy groceries")

        val created = repository.createTask(task)

        assertNotNull(created.id)
        assertTrue(created.id.isNotEmpty())
        assertNotNull(created.createdAt)
        assertNotNull(created.updatedAt)
        assertEquals("Buy groceries", created.title)
    }

    @Test
    fun `createTask throws IllegalArgumentException for empty title`() = runTest {
        val task = createTestTask(title = "")

        assertFailsWith<IllegalArgumentException> {
            repository.createTask(task)
        }
    }

    @Test
    fun `createTask throws IllegalArgumentException for blank title`() = runTest {
        val task = createTestTask(title = "   ")

        assertFailsWith<IllegalArgumentException> {
            repository.createTask(task)
        }
    }

    @Test
    fun `createTask throws IllegalArgumentException for invalid week ID format`() = runTest {
        val task = createTestTask(weekId = "2026-01")

        val exception = assertFailsWith<IllegalArgumentException> {
            repository.createTask(task)
        }
        assertTrue(exception.message!!.contains("Invalid week ID format"))
    }

    @Test
    fun `createTask accepts valid week ID format`() = runTest {
        val task = createTestTask(weekId = "2026-W52")

        val created = repository.createTask(task)

        assertEquals("2026-W52", created.weekId)
    }

    @Test
    fun `getTaskById returns task when exists`() = runTest {
        val task = repository.createTask(createTestTask())

        val retrieved = repository.getTaskById(task.id)

        assertNotNull(retrieved)
        assertEquals(task.id, retrieved.id)
        assertEquals(task.title, retrieved.title)
    }

    @Test
    fun `getTaskById returns null when not exists`() = runTest {
        val retrieved = repository.getTaskById("non-existent-id")

        assertNull(retrieved)
    }

    @Test
    fun `observeAllTasks emits all tasks for user`() = runTest {
        repository.createTask(createTestTask(title = "Task 1"))
        repository.createTask(createTestTask(title = "Task 2"))
        repository.createTask(createTestTask(title = "Task 3"))

        val tasks = repository.observeAllTasks(testUserId).first()

        assertEquals(3, tasks.size)
    }

    @Test
    fun `observeAllTasks filters by userId`() = runTest {
        repository.createTask(createTestTask(title = "My Task", ownerId = testUserId))
        val partnerTask = createTestTask(title = "Partner Task", ownerId = testPartnerId).copy(createdBy = testPartnerId)
        repository.createTask(partnerTask)

        val tasks = repository.observeAllTasks(testUserId).first()

        assertEquals(1, tasks.size)
        assertEquals("My Task", tasks[0].title)
    }

    @Test
    fun `observeTasksForWeek filters by week ID`() = runTest {
        repository.createTask(createTestTask(title = "Week 1 Task", weekId = "2026-W01"))
        repository.createTask(createTestTask(title = "Week 2 Task", weekId = "2026-W02"))

        val tasks = repository.observeTasksForWeek("2026-W01", testUserId).first()

        assertEquals(1, tasks.size)
        assertEquals("Week 1 Task", tasks[0].title)
    }

    @Test
    fun `observeTasksByOwnerType filters by owner type`() = runTest {
        repository.createTask(createTestTask(title = "Self Task", ownerType = OwnerType.SELF))
        repository.createTask(createTestTask(title = "Partner Task", ownerType = OwnerType.PARTNER))
        repository.createTask(createTestTask(title = "Shared Task", ownerType = OwnerType.SHARED))

        val selfTasks = repository.observeTasksByOwnerType(OwnerType.SELF, testUserId).first()
        val partnerTasks = repository.observeTasksByOwnerType(OwnerType.PARTNER, testUserId).first()

        assertEquals(1, selfTasks.size)
        assertEquals("Self Task", selfTasks[0].title)
        assertEquals(1, partnerTasks.size)
        assertEquals("Partner Task", partnerTasks[0].title)
    }

    @Test
    fun `observeTasksByWeekAndOwnerType filters by both`() = runTest {
        repository.createTask(createTestTask(title = "W01 Self", weekId = "2026-W01", ownerType = OwnerType.SELF))
        repository.createTask(createTestTask(title = "W01 Partner", weekId = "2026-W01", ownerType = OwnerType.PARTNER))
        repository.createTask(createTestTask(title = "W02 Self", weekId = "2026-W02", ownerType = OwnerType.SELF))

        val tasks = repository.observeTasksByWeekAndOwnerType("2026-W01", OwnerType.SELF, testUserId).first()

        assertEquals(1, tasks.size)
        assertEquals("W01 Self", tasks[0].title)
    }

    @Test
    fun `updateTask updates title, notes, and status`() = runTest {
        val task = repository.createTask(createTestTask(title = "Original Title"))

        val updated = repository.updateTask(
            taskId = task.id,
            title = "Updated Title",
            notes = "Updated notes",
            status = TaskStatus.COMPLETED
        )

        assertNotNull(updated)
        assertEquals("Updated Title", updated.title)
        assertEquals("Updated notes", updated.notes)
        assertEquals(TaskStatus.COMPLETED, updated.status)
        assertTrue(updated.updatedAt > task.updatedAt)
    }

    @Test
    fun `updateTask throws IllegalArgumentException for empty title`() = runTest {
        val task = repository.createTask(createTestTask())

        assertFailsWith<IllegalArgumentException> {
            repository.updateTask(task.id, "", null, TaskStatus.PENDING)
        }
    }

    @Test
    fun `updateTask returns null for non-existent task`() = runTest {
        val updated = repository.updateTask("non-existent", "Title", null, TaskStatus.PENDING)

        assertNull(updated)
    }

    @Test
    fun `updateTaskStatus updates only status`() = runTest {
        val task = repository.createTask(createTestTask(title = "Task", status = TaskStatus.PENDING))

        val updated = repository.updateTaskStatus(task.id, TaskStatus.COMPLETED)

        assertNotNull(updated)
        assertEquals(TaskStatus.COMPLETED, updated.status)
        assertEquals("Task", updated.title)
    }

    @Test
    fun `updateTaskStatus returns null for non-existent task`() = runTest {
        val updated = repository.updateTaskStatus("non-existent", TaskStatus.COMPLETED)

        assertNull(updated)
    }

    @Test
    fun `incrementRepeatCount increases count by one`() = runTest {
        val task = repository.createTask(
            createTestTask().copy(repeatTarget = 5, repeatCompleted = 2)
        )

        val updated = repository.incrementRepeatCount(task.id)

        assertNotNull(updated)
        assertEquals(3, updated.repeatCompleted)
    }

    @Test
    fun `incrementRepeatCount returns null for non-existent task`() = runTest {
        val updated = repository.incrementRepeatCount("non-existent")

        assertNull(updated)
    }

    @Test
    fun `updateTaskReviewNote updates review note`() = runTest {
        val task = repository.createTask(createTestTask())

        val updated = repository.updateTaskReviewNote(task.id, "Great job!")

        assertNotNull(updated)
        assertEquals("Great job!", updated.reviewNote)
    }

    @Test
    fun `updateTaskReviewNote returns null for non-existent task`() = runTest {
        val updated = repository.updateTaskReviewNote("non-existent", "Note")

        assertNull(updated)
    }

    @Test
    fun `deleteTask removes task and returns true`() = runTest {
        val task = repository.createTask(createTestTask())

        val deleted = repository.deleteTask(task.id)

        assertTrue(deleted)
        assertNull(repository.getTaskById(task.id))
    }

    @Test
    fun `deleteTask returns false for non-existent task`() = runTest {
        val deleted = repository.deleteTask("non-existent")

        assertFalse(deleted)
    }

    @Test
    fun `deleteTasksForWeek removes all tasks for week`() = runTest {
        repository.createTask(createTestTask(title = "Task 1", weekId = "2026-W01"))
        repository.createTask(createTestTask(title = "Task 2", weekId = "2026-W01"))
        repository.createTask(createTestTask(title = "Task 3", weekId = "2026-W02"))

        val count = repository.deleteTasksForWeek("2026-W01")

        assertEquals(2, count)
        val remaining = repository.observeAllTasks(testUserId).first()
        assertEquals(1, remaining.size)
        assertEquals("Task 3", remaining[0].title)
    }

    @Test
    fun `deleteTasksForWeek returns zero for non-existent week`() = runTest {
        val count = repository.deleteTasksForWeek("2026-W99")

        assertEquals(0, count)
    }
}
