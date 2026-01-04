/**
 * Progress & Insights UI Component Contracts
 *
 * Feature: 008-progress-insights
 * Date: 2026-01-04
 *
 * These define the expected API for Compose UI components.
 */

package org.epoque.tandem.ui.progress

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ============================================================================
// PROGRESS SCREEN
// ============================================================================

/**
 * Main Progress screen composable.
 *
 * @param uiState Current UI state from ViewModel
 * @param onEvent Callback to send events to ViewModel
 * @param onNavigateToWeekDetail Callback when user taps a past week
 * @param modifier Optional modifier
 */
@Composable
fun ProgressScreen(
    uiState: org.epoque.tandem.presentation.progress.ProgressUiState,
    onEvent: (org.epoque.tandem.presentation.progress.ProgressEvent) -> Unit,
    onNavigateToWeekDetail: (weekId: String) -> Unit,
    modifier: Modifier = Modifier
)

// ============================================================================
// STREAK CARD
// ============================================================================

/**
 * Streak display card with optional milestone celebration.
 *
 * @param streakCount Current streak number
 * @param isPartnerStreak Whether this is a partner streak
 * @param showCelebration Whether to show milestone celebration
 * @param milestoneValue The milestone being celebrated (5, 10, 20, 50)
 * @param onDismissCelebration Callback when celebration is dismissed
 * @param modifier Optional modifier
 */
@Composable
fun StreakCard(
    streakCount: Int,
    isPartnerStreak: Boolean,
    showCelebration: Boolean,
    milestoneValue: Int?,
    onDismissCelebration: () -> Unit,
    modifier: Modifier = Modifier
)

// ============================================================================
// COMPLETION BARS
// ============================================================================

/**
 * Horizontal completion bars comparing user and partner.
 *
 * @param userPercentage User's completion percentage (0-100)
 * @param partnerPercentage Partner's percentage, or null if no partner
 * @param userText Display text for user ("6/8")
 * @param partnerText Display text for partner
 * @param partnerName Partner's name for label
 * @param modifier Optional modifier
 */
@Composable
fun CompletionBars(
    userPercentage: Int,
    partnerPercentage: Int?,
    userText: String,
    partnerText: String?,
    partnerName: String?,
    modifier: Modifier = Modifier
)

// ============================================================================
// TREND CHART
// ============================================================================

/**
 * Line chart showing completion trends over past 8 weeks.
 *
 * @param trendData Data points for the chart
 * @param hasPartner Whether to show partner line
 * @param modifier Optional modifier
 */
@Composable
fun TrendChart(
    trendData: org.epoque.tandem.domain.model.TrendChartData,
    modifier: Modifier = Modifier
)

/**
 * Empty state shown when insufficient data for trend chart.
 *
 * @param weekCount Number of weeks with data (< 4)
 * @param modifier Optional modifier
 */
@Composable
fun TrendChartEmptyState(
    weekCount: Int,
    modifier: Modifier = Modifier
)

// ============================================================================
// PAST WEEKS LIST
// ============================================================================

/**
 * Lazy list of past weeks with pagination.
 *
 * @param weeks List of week summaries to display
 * @param hasMore Whether more weeks can be loaded
 * @param isLoadingMore Whether currently loading more
 * @param onWeekClick Callback when a week is tapped
 * @param onLoadMore Callback when user scrolls to bottom
 * @param modifier Optional modifier
 */
@Composable
fun PastWeeksList(
    weeks: List<org.epoque.tandem.presentation.progress.WeekSummaryUiModel>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onWeekClick: (weekId: String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
)

/**
 * Single week item in past weeks list.
 *
 * @param week Week summary data
 * @param onClick Callback when tapped
 * @param modifier Optional modifier
 */
@Composable
fun PastWeekItem(
    week: org.epoque.tandem.presentation.progress.WeekSummaryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// ============================================================================
// PAST WEEK DETAIL
// ============================================================================

/**
 * Past week detail screen composable.
 *
 * @param uiState Current UI state
 * @param onEvent Callback to send events
 * @param onNavigateBack Callback to navigate back
 * @param modifier Optional modifier
 */
@Composable
fun PastWeekDetailScreen(
    uiState: org.epoque.tandem.presentation.progress.PastWeekDetailUiState,
    onEvent: (org.epoque.tandem.presentation.progress.PastWeekDetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
)

/**
 * Side-by-side review summaries for user and partner.
 *
 * @param userReview User's review summary
 * @param partnerReview Partner's review summary (nullable)
 * @param modifier Optional modifier
 */
@Composable
fun ReviewSummaryCards(
    userReview: org.epoque.tandem.presentation.progress.ReviewSummaryUiModel,
    partnerReview: org.epoque.tandem.presentation.progress.ReviewSummaryUiModel?,
    modifier: Modifier = Modifier
)

/**
 * Task outcomes list showing status for both partners.
 *
 * @param tasks List of task outcomes
 * @param modifier Optional modifier
 */
@Composable
fun TaskOutcomesList(
    tasks: List<org.epoque.tandem.presentation.progress.TaskOutcomeUiModel>,
    modifier: Modifier = Modifier
)

// ============================================================================
// EMPTY & ERROR STATES
// ============================================================================

/**
 * Empty state for Progress tab (no history).
 *
 * Shows encouraging message to complete first week.
 *
 * @param modifier Optional modifier
 */
@Composable
fun ProgressEmptyState(
    modifier: Modifier = Modifier
)

/**
 * Error state with retry button.
 *
 * @param message Error message to display
 * @param onRetry Callback when retry is tapped
 * @param modifier Optional modifier
 */
@Composable
fun ProgressErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
)

// ============================================================================
// MILESTONE CELEBRATION
// ============================================================================

/**
 * Milestone celebration overlay/banner.
 *
 * Auto-dismisses after ~3 seconds.
 *
 * @param milestoneValue The milestone reached (5, 10, 20, 50)
 * @param isPartnerStreak Whether this is a partner streak
 * @param onDismiss Callback when dismissed (manual or auto)
 * @param modifier Optional modifier
 */
@Composable
fun MilestoneCelebration(
    milestoneValue: Int,
    isPartnerStreak: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
)
