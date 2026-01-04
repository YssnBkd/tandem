package org.epoque.tandem.presentation.goals

/**
 * One-time side effects from the Goals ViewModel.
 */
sealed interface GoalsSideEffect {
    data class ShowSnackbar(val message: String) : GoalsSideEffect
    data class NavigateToDetail(val goalId: String, val isPartnerGoal: Boolean) : GoalsSideEffect
    data object NavigateBack : GoalsSideEffect
    data object TriggerHapticFeedback : GoalsSideEffect
}
