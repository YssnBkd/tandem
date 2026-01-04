package org.epoque.tandem.presentation.progress

/**
 * UI state for Past Week Detail screen.
 */
data class PastWeekDetailUiState(
    val weekId: String = "",
    val dateRange: String = "",
    val userReview: ReviewSummaryUiModel? = null,
    val partnerReview: ReviewSummaryUiModel? = null,
    val tasks: List<TaskOutcomeUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * UI model for review summary display.
 */
data class ReviewSummaryUiModel(
    val name: String,
    val moodEmoji: String?,
    val completionText: String,
    val completionPercentage: Int,
    val note: String?,
    val isReviewed: Boolean
)

/**
 * UI model for task outcome display.
 */
data class TaskOutcomeUiModel(
    val taskId: String,
    val title: String,
    val userStatusIcon: String,
    val userStatusColor: TaskStatusColor,
    val partnerStatusIcon: String?,
    val partnerStatusColor: TaskStatusColor?
)

/**
 * Colors for task status display.
 */
enum class TaskStatusColor {
    COMPLETED,
    SKIPPED,
    PENDING
}
