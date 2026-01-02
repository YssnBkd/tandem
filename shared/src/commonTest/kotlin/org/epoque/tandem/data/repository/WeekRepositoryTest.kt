package org.epoque.tandem.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.local.TandemDatabaseFactory
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.WeekRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeekRepositoryTest {

    private lateinit var database: TandemDatabase
    private lateinit var repository: WeekRepository
    private val testUserId = "user-123"

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TandemDatabase.Schema.create(driver)
        database = TandemDatabaseFactory.create(driver)
        repository = WeekRepositoryImpl(database)
    }

    @AfterTest
    fun teardown() {
        // Database will be garbage collected
    }

    private fun createTestWeek(
        id: String = "2026-W01",
        startDate: LocalDate = LocalDate(2025, 12, 29), // Monday
        userId: String = testUserId
    ): Week {
        val endDate = startDate.plus(6, DateTimeUnit.DAY)
        return Week(
            id = id,
            startDate = startDate,
            endDate = endDate,
            userId = userId,
            overallRating = null,
            reviewNote = null,
            reviewedAt = null,
            planningCompletedAt = null
        )
    }

    @Test
    fun `getCurrentWeekId returns valid ISO 8601 week ID`() {
        val weekId = repository.getCurrentWeekId()

        assertTrue(weekId.matches(Regex("""^\d{4}-W\d{2}$""")))
    }

    @Test
    fun `saveWeek persists week to database`() = runTest {
        val week = createTestWeek()

        val saved = repository.saveWeek(week)

        assertEquals(week.id, saved.id)
        assertEquals(week.startDate, saved.startDate)
        assertEquals(week.endDate, saved.endDate)
    }

    @Test
    fun `saveWeek throws IllegalArgumentException for invalid week ID`() = runTest {
        val week = createTestWeek(id = "invalid-week")

        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveWeek(week)
        }
        assertTrue(exception.message!!.contains("Invalid week ID format"))
    }

    @Test
    fun `saveWeek throws IllegalArgumentException if startDate is not Monday`() = runTest {
        val week = createTestWeek(startDate = LocalDate(2026, 1, 1)) // Thursday

        val exception = assertFailsWith<IllegalArgumentException> {
            repository.saveWeek(week)
        }
        assertTrue(exception.message!!.contains("must be a Monday"))
    }

    @Test
    fun `getWeekById returns week when exists`() = runTest {
        val week = repository.saveWeek(createTestWeek())

        val retrieved = repository.getWeekById(week.id)

        assertNotNull(retrieved)
        assertEquals(week.id, retrieved.id)
    }

    @Test
    fun `getWeekById returns null when not exists`() = runTest {
        val retrieved = repository.getWeekById("2099-W99")

        assertNull(retrieved)
    }

    @Test
    fun `getOrCreateCurrentWeek creates week if not exists`() = runTest {
        val currentWeek = repository.getOrCreateCurrentWeek(testUserId)

        assertNotNull(currentWeek)
        assertNotNull(currentWeek.id)
        assertEquals(testUserId, currentWeek.userId)
        assertNotNull(currentWeek.startDate)
        assertNotNull(currentWeek.endDate)
    }

    @Test
    fun `getOrCreateCurrentWeek returns existing week if already exists`() = runTest {
        val firstCall = repository.getOrCreateCurrentWeek(testUserId)
        val secondCall = repository.getOrCreateCurrentWeek(testUserId)

        assertEquals(firstCall.id, secondCall.id)
        assertEquals(firstCall.startDate, secondCall.startDate)
    }

    @Test
    fun `observeWeek emits week when exists`() = runTest {
        val week = repository.saveWeek(createTestWeek())

        val observed = repository.observeWeek(week.id).first()

        assertNotNull(observed)
        assertEquals(week.id, observed.id)
    }

    @Test
    fun `observeWeek emits null when week does not exist`() = runTest {
        val observed = repository.observeWeek("2099-W99").first()

        assertNull(observed)
    }

    @Test
    fun `observeWeeksForUser returns all weeks for user`() = runTest {
        repository.saveWeek(createTestWeek(id = "2026-W01"))
        repository.saveWeek(createTestWeek(id = "2026-W02"))
        repository.saveWeek(createTestWeek(id = "2026-W03"))

        val weeks = repository.observeWeeksForUser(testUserId).first()

        assertEquals(3, weeks.size)
    }

    @Test
    fun `observeWeeksForUser filters by userId`() = runTest {
        repository.saveWeek(createTestWeek(id = "2026-W01", userId = testUserId))
        repository.saveWeek(createTestWeek(id = "2026-W02", userId = "other-user"))

        val weeks = repository.observeWeeksForUser(testUserId).first()

        assertEquals(1, weeks.size)
        assertEquals("2026-W01", weeks[0].id)
    }

    @Test
    fun `observePastWeeks filters weeks before current week`() = runTest {
        repository.saveWeek(createTestWeek(id = "2026-W01"))
        repository.saveWeek(createTestWeek(id = "2026-W02"))
        repository.saveWeek(createTestWeek(id = "2026-W03"))
        repository.saveWeek(createTestWeek(id = "2026-W04"))

        val pastWeeks = repository.observePastWeeks("2026-W03", testUserId).first()

        assertEquals(2, pastWeeks.size)
        assertTrue(pastWeeks.all { it.id < "2026-W03" })
    }

    @Test
    fun `updateWeekReview updates rating, note, and timestamp`() = runTest {
        val week = repository.saveWeek(createTestWeek())

        val updated = repository.updateWeekReview(
            weekId = week.id,
            overallRating = 4,
            reviewNote = "Great week!"
        )

        assertNotNull(updated)
        assertEquals(4, updated.overallRating)
        assertEquals("Great week!", updated.reviewNote)
        assertNotNull(updated.reviewedAt)
    }

    @Test
    fun `updateWeekReview throws IllegalArgumentException for invalid rating`() = runTest {
        val week = repository.saveWeek(createTestWeek())

        val exception = assertFailsWith<IllegalArgumentException> {
            repository.updateWeekReview(week.id, 6, null)
        }
        assertTrue(exception.message!!.contains("Rating must be between 1 and 5"))
    }

    @Test
    fun `updateWeekReview returns null for non-existent week`() = runTest {
        val updated = repository.updateWeekReview("2099-W99", 4, null)

        assertNull(updated)
    }

    @Test
    fun `markPlanningCompleted sets timestamp`() = runTest {
        val week = repository.saveWeek(createTestWeek())

        val updated = repository.markPlanningCompleted(week.id)

        assertNotNull(updated)
        assertNotNull(updated.planningCompletedAt)
        assertTrue(updated.isPlanningComplete)
    }

    @Test
    fun `markPlanningCompleted returns null for non-existent week`() = runTest {
        val updated = repository.markPlanningCompleted("2099-W99")

        assertNull(updated)
    }

    @Test
    fun `deleteWeek removes week and returns true`() = runTest {
        val week = repository.saveWeek(createTestWeek())

        val deleted = repository.deleteWeek(week.id)

        assertTrue(deleted)
        assertNull(repository.getWeekById(week.id))
    }

    @Test
    fun `deleteWeek returns false for non-existent week`() = runTest {
        val deleted = repository.deleteWeek("2099-W99")

        assertFalse(deleted)
    }
}
