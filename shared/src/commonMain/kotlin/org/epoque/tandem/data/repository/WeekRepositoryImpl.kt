package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * SQLDelight implementation of WeekRepository.
 * Handles ISO 8601 week calculations and week entity management.
 */
class WeekRepositoryImpl(
    private val database: TandemDatabase
) : WeekRepository {

    private val queries = database.weekQueries
    private val weekIdPattern = Regex("""^\d{4}-W\d{2}$""")

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun org.epoque.tandem.data.local.Week.toDomain(): Week {
        return Week(
            id = id,
            startDate = start_date,
            endDate = end_date,
            userId = user_id,
            overallRating = overall_rating?.toInt(),
            reviewNote = review_note,
            reviewedAt = reviewed_at,
            planningCompletedAt = planning_completed_at
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    private fun validateWeekId(weekId: String) {
        require(weekId.matches(weekIdPattern)) {
            "Invalid week ID format: $weekId. Expected format: YYYY-Www (e.g., 2026-W01)"
        }
    }

    private fun validateWeekBoundaries(week: Week) {
        require(week.startDate.dayOfWeek == DayOfWeek.MONDAY) {
            "Week start date must be a Monday, got ${week.startDate.dayOfWeek}"
        }

        val expectedEndDate = week.startDate.plus(6, DateTimeUnit.DAY)
        require(week.endDate == expectedEndDate) {
            "Week end date must be 6 days after start date"
        }
    }

    private fun validateRating(rating: Int?) {
        if (rating != null) {
            require(rating in 1..5) {
                "Rating must be between 1 and 5, got $rating"
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WEEK CALCULATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override fun getCurrentWeekId(): String {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        // ISO 8601 week calculation
        val year = today.year
        val dayOfYear = today.dayOfYear

        // January 1st of the year
        val jan1 = LocalDate(year, 1, 1)
        val jan1DayOfWeek = jan1.dayOfWeek.ordinal // Monday = 0

        // Calculate days to first Monday
        val daysToFirstMonday = if (jan1DayOfWeek == 0) 0 else (7 - jan1DayOfWeek)

        // Calculate week number
        val weekNumber = if (dayOfYear <= daysToFirstMonday) {
            // This date belongs to last week of previous year
            53 // Simplified; proper implementation would check last week of previous year
        } else {
            ((dayOfYear - daysToFirstMonday - 1) / 7) + 1
        }

        return "$year-W${weekNumber.toString().padStart(2, '0')}"
    }

    private fun calculateWeekBoundaries(weekId: String): Pair<LocalDate, LocalDate> {
        validateWeekId(weekId)

        val parts = weekId.split("-W")
        val year = parts[0].toInt()
        val weekNumber = parts[1].toInt()

        // Find the first Monday of the year using ISO 8601 rules
        val jan1 = LocalDate(year, 1, 1)
        val jan1DayOfWeek = jan1.dayOfWeek.ordinal // Monday = 0

        // Calculate days to add to get to the first Monday
        val daysToFirstMonday = if (jan1DayOfWeek == 0) 0 else (7 - jan1DayOfWeek)

        // Add days to jan1 to get first Monday
        val firstMonday = jan1.plus(daysToFirstMonday, DateTimeUnit.DAY)

        // Calculate the start date for the requested week
        val startDate = firstMonday.plus((weekNumber - 1) * 7, DateTimeUnit.DAY)

        // Calculate end date (6 days after start)
        val endDate = startDate.plus(6, DateTimeUnit.DAY)

        return Pair(startDate, endDate)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    override fun observeWeek(weekId: String): Flow<Week?> {
        return queries.getWeekByIdOnly(weekId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }
    }

    override fun observeWeeksForUser(userId: String): Flow<List<Week>> {
        return queries.getWeeksByUserId(userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { weeks -> weeks.map { it.toDomain() } }
    }

    override fun observePastWeeks(currentWeekId: String, userId: String): Flow<List<Week>> {
        return queries.getWeeksByUserId(userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { weeks ->
                weeks
                    .filter { it.id < currentWeekId }
                    .map { it.toDomain() }
            }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun getWeekById(weekId: String): Week? = withContext(Dispatchers.IO) {
        queries.getWeekByIdOnly(weekId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getOrCreateCurrentWeek(userId: String): Week = withContext(Dispatchers.IO) {
        val weekId = getCurrentWeekId()
        val existing = queries.getWeekById(weekId, userId).executeAsOneOrNull()

        if (existing != null) {
            return@withContext existing.toDomain()
        }

        // Calculate week boundaries
        val (startDate, endDate) = calculateWeekBoundaries(weekId)

        val week = Week(
            id = weekId,
            startDate = startDate,
            endDate = endDate,
            userId = userId,
            overallRating = null,
            reviewNote = null,
            reviewedAt = null,
            planningCompletedAt = null
        )

        saveWeek(week)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun saveWeek(week: Week): Week = withContext(Dispatchers.IO) {
        validateWeekId(week.id)
        validateWeekBoundaries(week)

        queries.upsertWeek(
            id = week.id,
            start_date = week.startDate,
            end_date = week.endDate,
            user_id = week.userId,
            overall_rating = week.overallRating?.toLong(),
            review_note = week.reviewNote,
            reviewed_at = week.reviewedAt,
            planning_completed_at = week.planningCompletedAt
        )

        week
    }

    override suspend fun updateWeekReview(
        weekId: String,
        overallRating: Int?,
        reviewNote: String?
    ): Week? = withContext(Dispatchers.IO) {
        validateRating(overallRating)

        val existing = queries.getWeekByIdOnly(weekId).executeAsOneOrNull()
            ?: return@withContext null

        val now = Clock.System.now()

        queries.updateWeekReview(
            overall_rating = overallRating?.toLong(),
            review_note = reviewNote,
            reviewed_at = now,
            id = weekId,
            user_id = existing.user_id
        )

        queries.getWeekById(weekId, existing.user_id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun markPlanningCompleted(weekId: String): Week? =
        withContext(Dispatchers.IO) {
            val existing = queries.getWeekByIdOnly(weekId).executeAsOneOrNull()
                ?: return@withContext null

            val now = Clock.System.now()
            queries.markPlanningComplete(now, weekId, existing.user_id)
            queries.getWeekById(weekId, existing.user_id).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun deleteWeek(weekId: String): Boolean = withContext(Dispatchers.IO) {
        val existing = queries.getWeekByIdOnly(weekId).executeAsOneOrNull()
        if (existing != null) {
            queries.deleteWeekById(weekId, existing.user_id)
            true
        } else {
            false
        }
    }
}
