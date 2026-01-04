package org.epoque.tandem.presentation.week

import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.presentation.week.model.Segment

/**
 * User events in the Week View.
 * Processed by WeekViewModel to update state.
 *
 * Following Android UDF pattern: events go UP to ViewModel,
 * state flows DOWN to UI.
 */
sealed class WeekEvent {
    // Segment navigation
    data class SegmentSelected(val segment: Segment) : WeekEvent()

    // Task interactions
    data class TaskTapped(val taskId: String) : WeekEvent()
    data class TaskCheckboxTapped(val taskId: String) : WeekEvent()

    // Quick add
    data class QuickAddTextChanged(val text: String) : WeekEvent()
    data object QuickAddSubmitted : WeekEvent()

    // Task detail sheet
    data object DetailSheetDismissed : WeekEvent()
    data class TaskTitleChanged(val title: String) : WeekEvent()
    data class TaskNotesChanged(val notes: String) : WeekEvent()
    data class TaskGoalChanged(val goalId: String?) : WeekEvent()
    data object TaskSaveRequested : WeekEvent()
    data object TaskDeleteRequested : WeekEvent()
    data object TaskDeleteConfirmed : WeekEvent()
    data object TaskMarkCompleteRequested : WeekEvent()

    // Add task sheet
    data object AddTaskSheetRequested : WeekEvent()
    data object AddTaskSheetDismissed : WeekEvent()
    data class AddTaskSubmitted(
        val title: String,
        val notes: String?,
        val ownerType: OwnerType
    ) : WeekEvent()

    // Refresh
    data object RefreshRequested : WeekEvent()

    // Partner actions
    data object RequestTaskFromPartnerTapped : WeekEvent()
    data object InvitePartnerTapped : WeekEvent()
}
