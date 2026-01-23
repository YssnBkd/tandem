package org.epoque.tandem.presentation.progress

import org.epoque.tandem.domain.model.TaskPriority

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
 * Uses clean status fields instead of emoji icons.
 */
data class TaskOutcomeUiModel(
    val taskId: String,
    val title: String,
    val priority: TaskPriority,
    val isCompleted: Boolean,
    val isSkipped: Boolean,
    val partnerCompleted: Boolean?,
    val partnerSkipped: Boolean?
)
