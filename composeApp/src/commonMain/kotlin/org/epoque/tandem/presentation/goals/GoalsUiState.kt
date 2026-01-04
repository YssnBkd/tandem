package org.epoque.tandem.presentation.goals

import kotlinx.datetime.Instant
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalType

/**
 * UI state for the Goals screen.
 */
data class GoalsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Goal lists
    val myGoals: List<Goal> = emptyList(),
    val partnerGoals: List<Goal> = emptyList(),

    // Segment selection
    val selectedSegment: GoalSegment = GoalSegment.YOURS,

    // Status filter (Active only vs All)
    val showActiveOnly: Boolean = true,

    // Add goal sheet
    val showAddGoalSheet: Boolean = false,
    val newGoalName: String = "",
    val newGoalIcon: String = "\uD83C\uDFAF", // 🎯
    val newGoalType: GoalType = GoalType.WeeklyHabit(3),
    val newGoalDuration: Int? = 4,
    val isCreatingGoal: Boolean = false,

    // Goal detail
    val selectedGoalId: String? = null,
    val selectedGoal: Goal? = null,
    val selectedGoalProgress: List<GoalProgress> = emptyList(),
    val showGoalDetail: Boolean = false,
    val isViewingPartnerGoal: Boolean = false,

    // Edit goal
    val isEditingGoal: Boolean = false,
    val editGoalName: String = "",
    val editGoalIcon: String = "",

    // Delete confirmation
    val showDeleteConfirmation: Boolean = false,

    // Partner state
    val hasPartner: Boolean = false,
    val partnerGoalsLastSyncTime: Instant? = null
) {
    /** Goals filtered by current segment and status filter */
    val displayedGoals: List<Goal> get() {
        val segmentGoals = when (selectedSegment) {
            GoalSegment.YOURS -> myGoals
            GoalSegment.PARTNERS -> partnerGoals
        }
        return if (showActiveOnly) {
            segmentGoals.filter { it.isActive }
        } else {
            segmentGoals
        }
    }

    val showEmptyState: Boolean get() = !isLoading && displayedGoals.isEmpty()

    val emptyStateMessage: String get() = when (selectedSegment) {
        GoalSegment.YOURS -> if (showActiveOnly && myGoals.isNotEmpty()) {
            "No active goals.\nToggle the filter to see completed goals."
        } else {
            "No goals yet.\nTap + to create your first goal!"
        }
        GoalSegment.PARTNERS -> if (hasPartner) {
            if (showActiveOnly && partnerGoals.isNotEmpty()) {
                "Your partner has no active goals."
            } else {
                "Your partner hasn't created any goals yet."
            }
        } else {
            "Connect with a partner to see their goals."
        }
    }

    val activeGoalCount: Int get() = myGoals.count { it.isActive }

    val canCreateNewGoal: Boolean get() = activeGoalCount < 10

    /** Whether the currently selected goal can be edited (own goals only) */
    val canEditSelectedGoal: Boolean get() = !isViewingPartnerGoal
}
