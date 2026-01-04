package org.epoque.tandem.presentation.planning

/**
 * User actions and events in the planning wizard.
 * Each event triggers a specific state update in the PlanningViewModel.
 */
sealed class PlanningEvent {
    // Rollover Step Events
    data class RolloverTaskAdded(val taskId: String) : PlanningEvent()
    data class RolloverTaskSkipped(val taskId: String) : PlanningEvent()
    data object RolloverStepComplete : PlanningEvent()

    // Add Tasks Step Events
    data class NewTaskTextChanged(val text: String) : PlanningEvent()
    data object NewTaskSubmitted : PlanningEvent()
    data object DoneAddingTasks : PlanningEvent()

    // Goal Suggestion Events (Feature 007: Goals System)
    data class GoalSuggestionSelected(val goalId: String) : PlanningEvent()
    data object ClearSelectedGoal : PlanningEvent()

    // Partner Requests Step Events
    data class PartnerRequestAccepted(val taskId: String) : PlanningEvent()
    data class PartnerRequestDiscussed(val taskId: String) : PlanningEvent()
    data object PartnerRequestsStepComplete : PlanningEvent()

    // Navigation Events
    data object BackPressed : PlanningEvent()
    data object ExitRequested : PlanningEvent()
    data object PlanningCompleted : PlanningEvent()
}
