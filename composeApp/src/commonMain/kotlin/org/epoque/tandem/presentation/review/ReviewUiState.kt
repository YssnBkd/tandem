package org.epoque.tandem.presentation.review

import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.model.Week

/**
 * UI State for the Week Review feature.
 *
 * Follows the MVI pattern - single source of truth for the review wizard.
 */
data class ReviewUiState(
    // Review mode and step
    val reviewMode: ReviewMode = ReviewMode.SOLO,
    val currentStep: ReviewStep = ReviewStep.MODE_SELECT,

    // Week data
    val currentWeek: Week? = null,
    val isReviewWindowOpen: Boolean = false,

    // Rating step
    val overallRating: Int? = null,
    val overallNote: String = "",

    // Task review step
    val tasksToReview: List<Task> = emptyList(),
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, TaskStatus> = emptyMap(),
    val taskNotes: Map<String, String> = emptyMap(),

    // Summary
    val currentStreak: Int = 0,
    val completionPercentage: Int = 0,

    // Loading and error states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,

    // Resume capability
    val hasIncompleteProgress: Boolean = false
) {
    // Computed properties

    /** Current task being reviewed, or null if index is out of bounds */
    val currentTask: Task?
        get() = tasksToReview.getOrNull(currentTaskIndex)

    /** Total number of tasks to review */
    val totalTasks: Int
        get() = tasksToReview.size

    /** Number of tasks that have been reviewed (have an outcome) */
    val reviewedTaskCount: Int
        get() = taskOutcomes.size

    /** Whether the user can proceed from the rating step (rating is required) */
    val canProceedFromRating: Boolean
        get() = overallRating != null

    /** Whether currently on the last task in the review sequence */
    val isLastTask: Boolean
        get() = currentTaskIndex >= tasksToReview.size - 1

    /** Count of tasks marked as Done (COMPLETED) */
    val doneCount: Int
        get() = taskOutcomes.count { it.value == TaskStatus.COMPLETED }

    /** Count of tasks marked as Tried */
    val triedCount: Int
        get() = taskOutcomes.count { it.value == TaskStatus.TRIED }

    /** Count of tasks marked as Skipped */
    val skippedCount: Int
        get() = taskOutcomes.count { it.value == TaskStatus.SKIPPED }
}

/**
 * Review mode - Solo or Together with partner.
 */
enum class ReviewMode {
    /** User reviews their own tasks alone */
    SOLO,
    /** Partners review together, passing the phone back and forth (P5 - deferred to v1.1) */
    TOGETHER
}

/**
 * Steps in the review wizard flow.
 */
enum class ReviewStep {
    /** Choose solo or together mode */
    MODE_SELECT,
    /** Rate overall week satisfaction (1-5 emojis) */
    RATING,
    /** Review each task one by one (Done/Tried/Skipped) */
    TASK_REVIEW,
    /** Summary with completion percentage and streak */
    SUMMARY
}
