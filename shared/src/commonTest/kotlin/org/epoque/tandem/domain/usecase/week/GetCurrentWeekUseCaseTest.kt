package org.epoque.tandem.domain.usecase.week

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.local.TandemDatabaseFactory
import org.epoque.tandem.data.repository.WeekRepositoryImpl
import org.epoque.tandem.domain.repository.WeekRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetCurrentWeekUseCaseTest {

    private lateinit var database: TandemDatabase
    private lateinit var repository: WeekRepository
    private lateinit var useCase: GetCurrentWeekUseCase
    private val testUserId = "user-123"

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TandemDatabase.Schema.create(driver)
        database = TandemDatabaseFactory.create(driver)
        repository = WeekRepositoryImpl(database)
        useCase = GetCurrentWeekUseCase(repository)
    }

    @AfterTest
    fun teardown() {
        // Database will be garbage collected
    }

    @Test
    fun `invoke returns current week`() = runTest {
        val currentWeek = useCase(testUserId)

        assertNotNull(currentWeek)
        assertNotNull(currentWeek.id)
        assertTrue(currentWeek.id.matches(Regex("""^\d{4}-W\d{2}$""")))
        assertEquals(testUserId, currentWeek.userId)
    }

    @Test
    fun `invoke creates week if not exists`() = runTest {
        val week = useCase(testUserId)

        assertNotNull(week.startDate)
        assertNotNull(week.endDate)
        assertNull(week.overallRating)
        assertNull(week.reviewNote)
        assertNull(week.reviewedAt)
        assertNull(week.planningCompletedAt)
    }

    @Test
    fun `invoke returns same week on subsequent calls`() = runTest {
        val firstCall = useCase(testUserId)
        val secondCall = useCase(testUserId)

        assertEquals(firstCall.id, secondCall.id)
        assertEquals(firstCall.startDate, secondCall.startDate)
        assertEquals(firstCall.endDate, secondCall.endDate)
    }

    @Test
    fun `invoke creates separate weeks for different users`() = runTest {
        val user1Week = useCase("user-1")
        val user2Week = useCase("user-2")

        // Both should have the same week ID (current week) since they're created at the same time
        // But different user IDs since they're separate users
        assertEquals(user1Week.id, user2Week.id) // Same week ID
        assertEquals("user-1", user1Week.userId)
        assertEquals("user-2", user2Week.userId)
        // Verify both are valid ISO 8601 week IDs
        assertTrue(user1Week.id.matches(Regex("""^\d{4}-W\d{2}$""")))
        assertTrue(user2Week.id.matches(Regex("""^\d{4}-W\d{2}$""")))
    }
}
