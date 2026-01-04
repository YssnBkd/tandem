package org.epoque.tandem.presentation.progress

/**
 * Side effects for Progress screen.
 *
 * One-time events that trigger UI behavior like navigation,
 * haptic feedback, or snackbars.
 */
sealed interface ProgressSideEffect {

    /**
     * Navigate to past week detail screen.
     */
    data class NavigateToWeekDetail(val weekId: String) : ProgressSideEffect

    /**
     * Trigger haptic feedback for milestone celebration.
     */
    data object TriggerMilestoneHaptic : ProgressSideEffect

    /**
     * Show error snackbar.
     */
    data class ShowSnackbar(val message: String) : ProgressSideEffect
}
