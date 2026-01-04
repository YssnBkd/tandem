package org.epoque.tandem.presentation.progress

import org.epoque.tandem.domain.model.TrendChartData

/**
 * Complete UI state for Progress screen.
 *
 * This data class holds all UI state for the Progress tab, including
 * streak information, completion data, trend chart, and past weeks list.
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
    val trendData: TrendChartData? = null,
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
    /**
     * Whether to show empty state (no history).
     */
    val showEmptyState: Boolean
        get() = !isLoading && error == null && pastWeeks.isEmpty() && currentStreak == 0

    /**
     * Formatted streak display text.
     */
    val streakDisplayText: String
        get() = if (isPartnerStreak) {
            "$currentStreak-week streak together"
        } else {
            "$currentStreak-week streak"
        }
}

/**
 * UI model for past weeks list item.
 *
 * Pre-computed display values to avoid computation during Compose recomposition.
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
