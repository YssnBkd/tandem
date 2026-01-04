package org.epoque.tandem.presentation.goals

import org.epoque.tandem.domain.model.GoalType

/**
 * Events that can be dispatched from the Goals UI.
 */
sealed interface GoalsEvent {
    // Segment
    data class SegmentSelected(val segment: GoalSegment) : GoalsEvent

    // Status filter
    data object ToggleStatusFilter : GoalsEvent

    // Goal list
    data class GoalTapped(val goalId: String, val isPartnerGoal: Boolean = false) : GoalsEvent
    data object AddGoalTapped : GoalsEvent

    // Add goal sheet
    data object DismissAddGoalSheet : GoalsEvent
    data class NewGoalNameChanged(val name: String) : GoalsEvent
    data class NewGoalIconChanged(val icon: String) : GoalsEvent
    data class NewGoalTypeChanged(val type: GoalType) : GoalsEvent
    data class NewGoalDurationChanged(val weeks: Int?) : GoalsEvent
    data object CreateGoal : GoalsEvent

    // Goal detail
    data object DismissGoalDetail : GoalsEvent
    data object EditGoalTapped : GoalsEvent
    data object DeleteGoalTapped : GoalsEvent
    data object ConfirmDeleteGoal : GoalsEvent
    data object CancelDeleteGoal : GoalsEvent

    // Edit goal
    data class EditGoalNameChanged(val name: String) : GoalsEvent
    data class EditGoalIconChanged(val icon: String) : GoalsEvent
    data object SaveGoalEdit : GoalsEvent
    data object CancelGoalEdit : GoalsEvent

    // Error handling
    data object DismissError : GoalsEvent
}
