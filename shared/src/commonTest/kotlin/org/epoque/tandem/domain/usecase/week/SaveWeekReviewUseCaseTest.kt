package org.epoque.tandem.domain.usecase.week

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.local.TandemDatabaseFactory
import org.epoque.tandem.data.repository.WeekRepositoryImpl
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.WeekRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SaveWeekReviewUseCaseTest {

    private lateinit var database: TandemDatabase
    private lateinit var repository: WeekRepository
    private lateinit var useCase: SaveWeekReviewUseCase
    private val testUserId = "user-123"

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TandemDatabase.Schema.create(driver)
        database = TandemDatabaseFactory.create(driver)
        repository = WeekRepositoryImpl(database)
        useCase = SaveWeekReviewUseCase(repository)
    }

    @AfterTest
    fun teardown() {
        // Database will be garbage collected
    }

    private suspend fun createTestWeek(
        id: String = "2026-W01",
        startDate: LocalDate = LocalDate(2025, 12, 29) // Monday
    ): Week {
        val endDate = startDate.plus(6, DateTimeUnit.DAY)
        val week = Week(
            id = id,
            startDate = startDate,
            endDate = endDate,
            userId = testUserId,
            overallRating = null,
            reviewNote = null,
            reviewedAt = null,
            planningCompletedAt = null
        )
        return repository.saveWeek(week)
    }

    @Test
    fun `invoke saves review with rating and note`() = runTest {
        val week = createTestWeek()

        val reviewed = useCase(week.id, 4, "Great week!")

        assertNotNull(reviewed)
        assertEquals(4, reviewed.overallRating)
        assertEquals("Great week!", reviewed.reviewNote)
        assertNotNull(reviewed.reviewedAt)
        assertTrue(reviewed.isReviewed)
    }

    @Test
    fun `invoke saves review with rating only`() = runTest {
        val week = createTestWeek()

        val reviewed = useCase(week.id, 5, null)

        assertNotNull(reviewed)
        assertEquals(5, reviewed.overallRating)
        assertNull(reviewed.reviewNote)
        assertNotNull(reviewed.reviewedAt)
    }

    @Test
    fun `invoke saves review with note only`() = runTest {
        val week = createTestWeek()

        val reviewed = useCase(week.id, null, "Some notes")

        assertNotNull(reviewed)
        assertNull(reviewed.overallRating)
        assertEquals("Some notes", reviewed.reviewNote)
        assertNotNull(reviewed.reviewedAt)
    }

    @Test
    fun `invoke throws IllegalArgumentException for rating less than 1`() = runTest {
        val week = createTestWeek()

        assertFailsWith<IllegalArgumentException> {
            useCase(week.id, 0, null)
        }
    }

    @Test
    fun `invoke throws IllegalArgumentException for rating greater than 5`() = runTest {
        val week = createTestWeek()

        assertFailsWith<IllegalArgumentException> {
            useCase(week.id, 6, null)
        }
    }

    @Test
    fun `invoke returns null for non-existent week`() = runTest {
        val reviewed = useCase("2099-W99", 4, "Note")

        assertNull(reviewed)
    }

    @Test
    fun `invoke updates existing review`() = runTest {
        val week = createTestWeek()
        useCase(week.id, 3, "First review")

        val updated = useCase(week.id, 5, "Updated review")

        assertNotNull(updated)
        assertEquals(5, updated.overallRating)
        assertEquals("Updated review", updated.reviewNote)
    }

    @Test
    fun `invoke sets reviewedAt timestamp`() = runTest {
        val week = createTestWeek()

        val reviewed = useCase(week.id, 4, null)

        assertNotNull(reviewed)
        assertNotNull(reviewed.reviewedAt)
    }
}
