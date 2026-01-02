package org.epoque.tandem.domain.usecase.task

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.local.TandemDatabaseFactory
import org.epoque.tandem.data.repository.TaskRepositoryImpl
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.TaskRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CreateTaskUseCaseTest {

    private lateinit var database: TandemDatabase
    private lateinit var repository: TaskRepository
    private lateinit var useCase: CreateTaskUseCase
    private val testUserId = "user-123"

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TandemDatabase.Schema.create(driver)
        database = TandemDatabaseFactory.create(driver)
        repository = TaskRepositoryImpl(database)
        useCase = CreateTaskUseCase(repository)
    }

    @AfterTest
    fun teardown() {
        // Database will be garbage collected
    }

    private fun createTestTask(
        title: String = "Test Task",
        weekId: String = "2026-W01"
    ): Task {
        return Task(
            id = "",
            title = title,
            notes = null,
            ownerId = testUserId,
            ownerType = OwnerType.SELF,
            weekId = weekId,
            status = TaskStatus.PENDING,
            createdBy = testUserId,
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
    fun `invoke creates task successfully`() = runTest {
        val task = createTestTask(title = "Buy groceries")

        val created = useCase(task)

        assertNotNull(created)
        assertNotNull(created.id)
        assertTrue(created.id.isNotEmpty())
        assertEquals("Buy groceries", created.title)
        assertEquals(testUserId, created.ownerId)
        assertEquals("2026-W01", created.weekId)
    }

    @Test
    fun `invoke generates unique IDs for multiple tasks`() = runTest {
        val task1 = useCase(createTestTask(title = "Task 1"))
        val task2 = useCase(createTestTask(title = "Task 2"))

        assertTrue(task1.id != task2.id)
    }

    @Test
    fun `invoke sets timestamps correctly`() = runTest {
        val before = Clock.System.now()
        val created = useCase(createTestTask())
        val after = Clock.System.now()

        assertTrue(created.createdAt >= before)
        assertTrue(created.createdAt <= after)
        assertTrue(created.updatedAt >= before)
        assertTrue(created.updatedAt <= after)
    }

    @Test
    fun `invoke throws IllegalArgumentException for empty title`() = runTest {
        val task = createTestTask(title = "")

        assertFailsWith<IllegalArgumentException> {
            useCase(task)
        }
    }

    @Test
    fun `invoke throws IllegalArgumentException for blank title`() = runTest {
        val task = createTestTask(title = "   ")

        assertFailsWith<IllegalArgumentException> {
            useCase(task)
        }
    }

    @Test
    fun `invoke throws IllegalArgumentException for invalid week ID`() = runTest {
        val task = createTestTask(weekId = "invalid-week")

        val exception = assertFailsWith<IllegalArgumentException> {
            useCase(task)
        }
        assertTrue(exception.message!!.contains("Invalid week ID format"))
    }

    @Test
    fun `invoke creates task with repeating target`() = runTest {
        val task = createTestTask().copy(repeatTarget = 7)

        val created = useCase(task)

        assertEquals(7, created.repeatTarget)
        assertEquals(0, created.repeatCompleted)
        assertTrue(created.isRepeating)
    }

    @Test
    fun `invoke creates task with different owner types`() = runTest {
        val selfTask = useCase(createTestTask().copy(ownerType = OwnerType.SELF))
        val partnerTask = useCase(createTestTask().copy(ownerType = OwnerType.PARTNER))
        val sharedTask = useCase(createTestTask().copy(ownerType = OwnerType.SHARED))

        assertEquals(OwnerType.SELF, selfTask.ownerType)
        assertEquals(OwnerType.PARTNER, partnerTask.ownerType)
        assertEquals(OwnerType.SHARED, sharedTask.ownerType)
    }

    @Test
    fun `invoke creates task with notes`() = runTest {
        val task = createTestTask().copy(notes = "Important details")

        val created = useCase(task)

        assertEquals("Important details", created.notes)
    }

    @Test
    fun `invoke creates task with linked goal`() = runTest {
        val task = createTestTask().copy(linkedGoalId = "goal-123")

        val created = useCase(task)

        assertEquals("goal-123", created.linkedGoalId)
    }
}
