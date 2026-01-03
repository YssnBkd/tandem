package org.epoque.tandem.presentation.planning

/**
 * One-time side effects for the planning wizard.
 * These are consumed by the UI layer to trigger actions like navigation, snackbars, or haptic feedback.
 */
sealed class PlanningSideEffect {
    /**
     * Show a snackbar message to the user.
     */
    data class ShowSnackbar(val message: String) : PlanningSideEffect()

    /**
     * Navigate to a specific step in the planning wizard.
     */
    data class NavigateToStep(val step: PlanningStep) : PlanningSideEffect()

    /**
     * Navigate back one step in the wizard.
     */
    data object NavigateBack : PlanningSideEffect()

    /**
     * Exit the planning wizard and return to the week view.
     */
    data object ExitPlanning : PlanningSideEffect()

    /**
     * Trigger haptic feedback (e.g., after adding/skipping a task).
     */
    data object TriggerHapticFeedback : PlanningSideEffect()

    /**
     * Clear focus from the current input field (e.g., after submitting a task).
     */
    data object ClearFocus : PlanningSideEffect()
}
