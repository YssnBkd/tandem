package org.epoque.tandem.presentation.feed

/**
 * One-time side effects from the Feed ViewModel.
 * These are consumed once by the UI (navigation, snackbars, haptics, etc.).
 */
sealed class FeedSideEffect {

    // ═══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Navigate to the planning flow.
     */
    data object NavigateToPlanning : FeedSideEffect()

    /**
     * Navigate to the review flow for a specific week.
     */
    data class NavigateToReview(val weekId: String) : FeedSideEffect()

    /**
     * Navigate to task detail screen.
     */
    data class NavigateToTaskDetail(val taskId: String) : FeedSideEffect()

    /**
     * Navigate to week screen.
     */
    data object NavigateToWeek : FeedSideEffect()

    // ═══════════════════════════════════════════════════════════════════════════
    // FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : FeedSideEffect()

    /**
     * Trigger haptic feedback.
     */
    data object TriggerHapticFeedback : FeedSideEffect()

    /**
     * Clear the message input field after sending.
     */
    data object ClearMessageInput : FeedSideEffect()

    // ═══════════════════════════════════════════════════════════════════════════
    // SHEET/DIALOG
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Show more options menu/sheet.
     */
    data object ShowMoreOptionsMenu : FeedSideEffect()
}
