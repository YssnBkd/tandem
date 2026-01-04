/**
 * Progress & Insights Use Case Contracts
 *
 * Feature: 008-progress-insights
 * Date: 2026-01-04
 *
 * These interfaces define the domain layer contracts for Progress & Insights.
 * All use cases are suspend functions invokable via operator invoke pattern.
 */

package org.epoque.tandem.domain.usecase.progress

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.*

// ============================================================================
// STREAK USE CASES
// ============================================================================

/**
 * Calculate current streak for user (and partner if connected).
 *
 * Streak is consecutive weeks where:
 * - Solo user: User completed weekly review (Week.reviewedAt != null)
 * - With partner: Both user AND partner completed weekly review
 *
 * @param userId Current user's ID
 * @return StreakResult with count, partner flag, and pending milestone
 */
interface CalculatePartnerStreakUseCase {
    suspend operator fun invoke(userId: String): StreakResult
}

/**
 * Check for unseen milestones based on current streak.
 *
 * Milestones: 5, 10, 20, 50 weeks
 * Returns highest unseen milestone that streak qualifies for.
 *
 * @param currentStreak Current streak count
 * @param lastCelebratedMilestone Last milestone user has seen
 * @return Milestone value to celebrate, or null if none
 */
interface GetPendingMilestoneUseCase {
    suspend operator fun invoke(currentStreak: Int, lastCelebratedMilestone: Int): Int?
}

/**
 * Mark a milestone as celebrated (persist to DataStore).
 *
 * @param milestone The milestone value (5, 10, 20, or 50)
 */
interface MarkMilestoneCelebratedUseCase {
    suspend operator fun invoke(milestone: Int)
}

// ============================================================================
// COMPLETION STATS USE CASES
// ============================================================================

/**
 * Get completion statistics for a single week.
 *
 * Completion = tasks with status COMPLETED
 * Total = all tasks for the week
 * Percentage = (completed / total) * 100
 *
 * @param weekId ISO 8601 week ID (e.g., "2026-W01")
 * @param userId User to calculate stats for
 * @return CompletionStats with counts and percentage
 */
interface GetCompletionStatsUseCase {
    suspend operator fun invoke(weekId: String, userId: String): CompletionStats
}

/**
 * Get monthly completion statistics (current calendar month).
 *
 * Aggregates completion across all weeks in current month.
 *
 * @param userId User to calculate stats for
 * @return CompletionStats for the month
 */
interface GetMonthlyCompletionUseCase {
    suspend operator fun invoke(userId: String): CompletionStats
}

// ============================================================================
// TREND USE CASES
// ============================================================================

/**
 * Get completion trend data for past 8 weeks.
 *
 * Returns data points for trend chart visualization.
 * Each point has user percentage and optional partner percentage.
 *
 * @param userId Current user's ID
 * @return TrendChartData with up to 8 data points
 */
interface GetCompletionTrendsUseCase {
    suspend operator fun invoke(userId: String): TrendChartData
}

// ============================================================================
// PAST WEEKS USE CASES
// ============================================================================

/**
 * Get paginated list of past weeks with summary data.
 *
 * @param userId Current user's ID
 * @param offset Number of weeks to skip (for pagination)
 * @param limit Number of weeks to return (default 10)
 * @return PastWeeksResult with weeks list and pagination metadata
 */
interface GetPastWeeksUseCase {
    suspend operator fun invoke(
        userId: String,
        offset: Int = 0,
        limit: Int = 10
    ): PastWeeksResult
}

/**
 * Get full detail for a specific past week.
 *
 * Includes both user and partner review data plus all task outcomes.
 *
 * @param weekId ISO 8601 week ID
 * @param userId Current user's ID
 * @return PastWeekDetail with full review and task data
 */
interface GetPastWeekDetailUseCase {
    suspend operator fun invoke(weekId: String, userId: String): PastWeekDetail
}

// ============================================================================
// DATA MODELS
// ============================================================================

/**
 * Result of streak calculation.
 */
data class StreakResult(
    val count: Int,
    val isPartnerStreak: Boolean,
    val pendingMilestone: Int?
)

/**
 * Task completion statistics.
 */
data class CompletionStats(
    val completedCount: Int,
    val totalCount: Int
) {
    val percentage: Int
        get() = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    val displayText: String
        get() = "$completedCount/$totalCount"
}

/**
 * Single data point for trend visualization.
 */
data class TrendDataPoint(
    val weekId: String,
    val weekLabel: String,
    val userPercentage: Int,
    val partnerPercentage: Int?
)

/**
 * Complete trend chart data.
 */
data class TrendChartData(
    val dataPoints: List<TrendDataPoint>,
    val hasPartner: Boolean,
    val insufficientData: Boolean
) {
    val weekCount: Int get() = dataPoints.size
}

/**
 * Summary data for past weeks list.
 */
data class WeekSummary(
    val weekId: String,
    val startDate: kotlinx.datetime.LocalDate,
    val endDate: kotlinx.datetime.LocalDate,
    val userCompletion: CompletionStats,
    val partnerCompletion: CompletionStats?,
    val userRating: Int?,
    val partnerRating: Int?,
    val isReviewed: Boolean
)

/**
 * Paginated result for past weeks.
 */
data class PastWeeksResult(
    val weeks: List<WeekSummary>,
    val hasMore: Boolean,
    val totalCount: Int
)

/**
 * Full detail for a past week.
 */
data class PastWeekDetail(
    val weekId: String,
    val startDate: kotlinx.datetime.LocalDate,
    val endDate: kotlinx.datetime.LocalDate,
    val userReview: ReviewDetail,
    val partnerReview: ReviewDetail?,
    val tasks: List<TaskOutcome>
)

/**
 * Review detail for a single user.
 */
data class ReviewDetail(
    val rating: Int?,
    val note: String?,
    val completion: CompletionStats,
    val reviewedAt: kotlinx.datetime.Instant?
)

/**
 * Task outcome showing both user and partner status.
 */
data class TaskOutcome(
    val taskId: String,
    val title: String,
    val userStatus: org.epoque.tandem.domain.model.TaskStatus,
    val partnerStatus: org.epoque.tandem.domain.model.TaskStatus?
)
