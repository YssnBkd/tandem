package org.epoque.tandem.presentation.partner

/**
 * One-time side effects from the Partner screen.
 * Consumed once by the UI layer.
 *
 * Side effects are for actions that:
 * - Should only happen once (not on recomposition)
 * - Are not part of UI state
 * - Trigger external actions (navigation, haptics, share sheets, etc.)
 */
sealed interface PartnerSideEffect {
    /**
     * Show the native share sheet with the invite link.
     */
    data class ShowShareSheet(val link: String) : PartnerSideEffect

    /**
     * Navigate to the connection confirmation screen.
     */
    data object NavigateToConfirmation : PartnerSideEffect

    /**
     * Navigate back to the home screen.
     */
    data object NavigateToHome : PartnerSideEffect

    /**
     * Navigate back (pop the current screen).
     */
    data object NavigateBack : PartnerSideEffect

    /**
     * Navigate to partner settings screen.
     */
    data object NavigateToPartnerSettings : PartnerSideEffect

    /**
     * Show an error message (e.g., dialog or snackbar).
     */
    data class ShowError(val message: String) : PartnerSideEffect

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : PartnerSideEffect

    /**
     * Trigger haptic feedback.
     */
    data object TriggerHapticFeedback : PartnerSideEffect
}
