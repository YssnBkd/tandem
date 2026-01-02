package org.epoque.tandem.presentation.week

/**
 * One-time side effects from the Week View.
 * Consumed once by the UI layer.
 *
 * Side effects are for actions that:
 * - Should only happen once (not on recomposition)
 * - Are not part of UI state
 * - Trigger external actions (navigation, haptics, etc.)
 */
sealed class WeekSideEffect {
    /**
     * Trigger haptic feedback for task completion.
     */
    data object TriggerHapticFeedback : WeekSideEffect()

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : WeekSideEffect()

    /**
     * Navigate to partner invitation flow.
     */
    data object NavigateToPartnerInvite : WeekSideEffect()

    /**
     * Navigate to request task flow.
     */
    data object NavigateToRequestTask : WeekSideEffect()

    /**
     * Clear keyboard focus.
     */
    data object ClearFocus : WeekSideEffect()
}
