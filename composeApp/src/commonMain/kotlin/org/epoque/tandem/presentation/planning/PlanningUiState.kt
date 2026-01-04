package org.epoque.tandem.presentation.planning

import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.presentation.week.model.TaskUiModel

/**
 * UI state for the weekly planning flow.
 * Manages all data needed across the 4-step wizard (Rollover, Add Tasks, Partner Requests, Confirmation).
 */
data class PlanningUiState(
    val currentStep: PlanningStep = PlanningStep.ROLLOVER,
    val currentWeek: Week? = null,

    // Rollover Step (Step 1)
    val rolloverTasks: List<TaskUiModel> = emptyList(),
    val currentRolloverIndex: Int = 0,
    val processedRolloverCount: Int = 0,

    // Add Tasks Step (Step 2)
    val newTaskText: String = "",
    val newTaskError: String? = null,
    val addedTasks: List<TaskUiModel> = emptyList(),

    // Goal-Based Suggestions (Feature 007: Goals System)
    val goalSuggestions: List<Goal> = emptyList(),
    val selectedGoalForNewTask: Goal? = null,

    // Partner Requests Step (Step 3)
    val partnerRequests: List<TaskUiModel> = emptyList(),
    val currentRequestIndex: Int = 0,
    val processedRequestCount: Int = 0,

    // General State
    val isLoading: Boolean = true,
    val error: String? = null,

    // Summary Metrics (computed from actions during planning)
    val totalTasksPlanned: Int = 0,
    val rolloverTasksAdded: Int = 0,
    val newTasksCreated: Int = 0,
    val partnerRequestsAccepted: Int = 0
)
