/**
 * Progress & Insights ViewModel Contracts
 *
 * Feature: 008-progress-insights
 * Date: 2026-01-04
 *
 * These interfaces define the presentation layer contracts following MVI pattern.
 */

package org.epoque.tandem.presentation.progress

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// ============================================================================
// PROGRESS SCREEN (Main Tab)
// ============================================================================

/**
 * ViewModel contract for Progress screen.
 */
interface ProgressViewModelContract {
    /** Observable UI state */
    val uiState: StateFlow<ProgressUiState>

    /** One-time side effects */
    val sideEffects: Flow<ProgressSideEffect>

    /** Handle user events */
    fun onEvent(event: ProgressEvent)
}

/**
 * Complete UI state for Progress screen.
 */
data class ProgressUiState(
    // Streak Section
    val currentStreak: Int = 0,
    val isPartnerStreak: Boolean = false,
    val streakMessage: String = "",
    val showMilestoneCelebration: Boolean = false,
    val milestoneValue: Int? = null,

    // Completion Bars (This Month)
    val userMonthlyCompletion: Int = 0,
    val partnerMonthlyCompletion: Int? = null,
    val userMonthlyText: String = "0/0",
    val partnerMonthlyText: String? = null,

    // Trend Chart
    val trendData: org.epoque.tandem.domain.model.TrendChartData? = null,
    val showTrendChart: Boolean = false,

    // Past Weeks List
    val pastWeeks: List<WeekSummaryUiModel> = emptyList(),
    val hasMoreWeeks: Boolean = false,
    val isLoadingMoreWeeks: Boolean = false,

    // Partner Info
    val hasPartner: Boolean = false,
    val partnerName: String? = null,

    // General State
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /** Whether to show empty state (no history) */
    val showEmptyState: Boolean
        get() = !isLoading && pastWeeks.isEmpty() && currentStreak == 0

    /** Formatted streak display text */
    val streakDisplayText: String
        get() = if (isPartnerStreak) {
            "$currentStreak-week streak together"
        } else {
            "$currentStreak-week streak"
        }
}

/**
 * UI model for past weeks list item.
 */
data class WeekSummaryUiModel(
    val weekId: String,
    val dateRange: String,
    val userCompletionText: String,
    val partnerCompletionText: String?,
    val userMoodEmoji: String?,
    val partnerMoodEmoji: String?,
    val isReviewed: Boolean
)

/**
 * User events for Progress screen.
 */
sealed class ProgressEvent {
    /** User tapped a past week to view details */
    data class PastWeekTapped(val weekId: String) : ProgressEvent()

    /** User scrolled to bottom, load more weeks */
    data object LoadMoreWeeks : ProgressEvent()

    /** User dismissed milestone celebration */
    data object DismissMilestone : ProgressEvent()

    /** Retry after error */
    data object Retry : ProgressEvent()

    /** Screen became visible (trigger data refresh) */
    data object ScreenVisible : ProgressEvent()
}

/**
 * Side effects for Progress screen.
 */
sealed class ProgressSideEffect {
    /** Navigate to past week detail */
    data class NavigateToWeekDetail(val weekId: String) : ProgressSideEffect()

    /** Trigger haptic feedback for milestone */
    data object TriggerMilestoneHaptic : ProgressSideEffect()

    /** Show error snackbar */
    data class ShowSnackbar(val message: String) : ProgressSideEffect()
}

// ============================================================================
// PAST WEEK DETAIL SCREEN
// ============================================================================

/**
 * ViewModel contract for Past Week Detail screen.
 */
interface PastWeekDetailViewModelContract {
    val uiState: StateFlow<PastWeekDetailUiState>
    val sideEffects: Flow<PastWeekDetailSideEffect>
    fun onEvent(event: PastWeekDetailEvent)
}

/**
 * UI state for Past Week Detail screen.
 */
data class PastWeekDetailUiState(
    val weekId: String = "",
    val dateRange: String = "",
    val userReviewSummary: ReviewSummaryUiModel? = null,
    val partnerReviewSummary: ReviewSummaryUiModel? = null,
    val tasks: List<TaskOutcomeUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val hasPartnerData: Boolean
        get() = partnerReviewSummary != null
}

/**
 * Review summary for display.
 */
data class ReviewSummaryUiModel(
    val name: String,
    val moodEmoji: String?,
    val completionText: String,
    val note: String?
)

/**
 * Task outcome for display.
 */
data class TaskOutcomeUiModel(
    val id: String,
    val title: String,
    val userStatusLabel: String,
    val partnerStatusLabel: String?,
    val userStatusEmoji: String,
    val partnerStatusEmoji: String?
)

/**
 * Events for Past Week Detail screen.
 */
sealed class PastWeekDetailEvent {
    /** User wants to navigate back */
    data object NavigateBack : PastWeekDetailEvent()

    /** Retry loading after error */
    data object Retry : PastWeekDetailEvent()
}

/**
 * Side effects for Past Week Detail screen.
 */
sealed class PastWeekDetailSideEffect {
    /** Navigate back to Progress screen */
    data object NavigateBack : PastWeekDetailSideEffect()

    /** Show error snackbar */
    data class ShowSnackbar(val message: String) : PastWeekDetailSideEffect()
}

// ============================================================================
// HELPER CONSTANTS
// ============================================================================

/**
 * Milestone values for streak celebrations.
 */
object StreakMilestones {
    val VALUES = listOf(5, 10, 20, 50)

    fun getNextMilestone(currentStreak: Int): Int? =
        VALUES.firstOrNull { it > currentStreak }

    fun getReachedMilestones(currentStreak: Int): List<Int> =
        VALUES.filter { it <= currentStreak }
}

/**
 * Emoji mapping for mood ratings.
 */
object MoodEmojis {
    fun fromRating(rating: Int?): String? = when (rating) {
        1 -> "😞"
        2 -> "😐"
        3 -> "😊"
        4 -> "😄"
        5 -> "🎉"
        else -> null
    }
}

/**
 * Status labels and emojis for task outcomes.
 */
object TaskStatusDisplay {
    fun labelFor(status: org.epoque.tandem.domain.model.TaskStatus): String = when (status) {
        org.epoque.tandem.domain.model.TaskStatus.COMPLETED -> "Done"
        org.epoque.tandem.domain.model.TaskStatus.TRIED -> "Tried"
        org.epoque.tandem.domain.model.TaskStatus.SKIPPED -> "Skipped"
        org.epoque.tandem.domain.model.TaskStatus.PENDING -> "Pending"
        org.epoque.tandem.domain.model.TaskStatus.PENDING_ACCEPTANCE -> "Awaiting"
        org.epoque.tandem.domain.model.TaskStatus.DECLINED -> "Declined"
    }

    fun emojiFor(status: org.epoque.tandem.domain.model.TaskStatus): String = when (status) {
        org.epoque.tandem.domain.model.TaskStatus.COMPLETED -> "✅"
        org.epoque.tandem.domain.model.TaskStatus.TRIED -> "💪"
        org.epoque.tandem.domain.model.TaskStatus.SKIPPED -> "⏭️"
        org.epoque.tandem.domain.model.TaskStatus.PENDING -> "⏳"
        org.epoque.tandem.domain.model.TaskStatus.PENDING_ACCEPTANCE -> "📬"
        org.epoque.tandem.domain.model.TaskStatus.DECLINED -> "❌"
    }
}
