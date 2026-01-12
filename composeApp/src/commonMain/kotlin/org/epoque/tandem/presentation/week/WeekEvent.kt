package org.epoque.tandem.presentation.week

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.presentation.week.model.Segment

/**
 * User intents/events for the Week screen.
 * Follows UDF pattern: UI emits events, ViewModel handles them.
 */
sealed interface WeekEvent {

    // ============================================
    // EXISTING EVENTS (from current ViewModel)
    // ============================================

    /** User selected a segment tab (You/Partner/Together) */
    data class SegmentSelected(val segment: Segment) : WeekEvent

    /** User tapped a task checkbox to toggle completion */
    data class TaskCheckboxTapped(val taskId: String) : WeekEvent

    /** User changed quick-add text field */
    data class QuickAddTextChanged(val text: String) : WeekEvent

    /** User submitted quick-add (pressed enter/done) */
    data object QuickAddSubmitted : WeekEvent

    /** User tapped a task row to open detail modal */
    data class TaskTapped(val taskId: String) : WeekEvent

    /** User dismissed the detail sheet */
    data object DetailSheetDismissed : WeekEvent

    /** User changed title in detail sheet (legacy) */
    data class TaskTitleChanged(val title: String) : WeekEvent

    /** User changed notes in detail sheet (legacy) */
    data class TaskNotesChanged(val notes: String) : WeekEvent

    /** User changed goal in detail sheet (legacy) */
    data class TaskGoalChanged(val goalId: String?) : WeekEvent

    /** User tapped save to persist changes */
    data object TaskSaveRequested : WeekEvent

    /** User requested to delete a task (shows confirmation) */
    data object TaskDeleteRequested : WeekEvent

    /** User confirmed task deletion */
    data object TaskDeleteConfirmed : WeekEvent

    /** User tapped mark complete in detail sheet (legacy) */
    data object TaskMarkCompleteRequested : WeekEvent

    /** User requested refresh (pull-to-refresh) */
    data object RefreshRequested : WeekEvent

    /** User tapped "Request task from partner" */
    data object RequestTaskFromPartnerTapped : WeekEvent

    /** User tapped "Invite partner" */
    data object InvitePartnerTapped : WeekEvent

    // ============================================
    // CALENDAR STRIP EVENTS
    // ============================================

    /** User tapped previous week arrow */
    data object PreviousWeekTapped : WeekEvent

    /** User tapped next week arrow */
    data object NextWeekTapped : WeekEvent

    /** User tapped a specific date in calendar strip */
    data class CalendarDateTapped(val date: LocalDate) : WeekEvent

    /** User cleared date filter (show all sections again) */
    data object CalendarFilterCleared : WeekEvent

    // ============================================
    // COMPLETED SECTION EVENTS
    // ============================================

    /** User tapped completed section header to expand/collapse */
    data object CompletedSectionToggled : WeekEvent

    // ============================================
    // ADD TASK MODAL EVENTS
    // ============================================

    /** User tapped FAB to open add task modal */
    data object AddTaskSheetRequested : WeekEvent

    /** User dismissed add task modal */
    data object AddTaskSheetDismissed : WeekEvent

    /** User changed title in add task form */
    data class AddTaskTitleChanged(val title: String) : WeekEvent

    /** User changed description in add task form */
    data class AddTaskDescriptionChanged(val description: String) : WeekEvent

    /** User changed owner type in add task form */
    data class AddTaskOwnerChanged(val ownerType: OwnerType) : WeekEvent

    /** User changed scheduled date in add task form */
    data class AddTaskDateChanged(val date: LocalDate?) : WeekEvent

    /** User changed scheduled time in add task form */
    data class AddTaskTimeChanged(val time: LocalTime?) : WeekEvent

    /** User changed priority in add task form */
    data class AddTaskPriorityChanged(val priority: TaskPriority) : WeekEvent

    /** User changed labels in add task form */
    data class AddTaskLabelsChanged(val labels: List<String>) : WeekEvent

    /** User changed linked goal in add task form */
    data class AddTaskGoalChanged(val goalId: String?) : WeekEvent

    /** User changed deadline in add task form */
    data class AddTaskDeadlineChanged(val deadline: Instant?) : WeekEvent

    /** User submitted add task form */
    data object AddTaskSubmitted : WeekEvent

    /** Legacy: User submitted add task with parameters (kept for compatibility) */
    data class AddTaskSubmittedLegacy(
        val title: String,
        val notes: String?,
        val ownerType: OwnerType,
        val priority: TaskPriority = TaskPriority.P4,
        val scheduledDate: LocalDate? = null,
        val linkedGoalId: String? = null
    ) : WeekEvent

    // ============================================
    // TASK DETAIL MODAL EVENTS
    // ============================================

    /** User changed description/notes in detail modal */
    data class TaskDescriptionChanged(val description: String) : WeekEvent

    /** User changed owner type in detail modal */
    data class TaskOwnerChanged(val ownerType: OwnerType) : WeekEvent

    /** User changed scheduled date in detail modal */
    data class TaskDateChanged(val date: LocalDate?) : WeekEvent

    /** User changed scheduled time in detail modal */
    data class TaskTimeChanged(val time: LocalTime?) : WeekEvent

    /** User changed priority in detail modal */
    data class TaskPriorityChanged(val priority: TaskPriority) : WeekEvent

    /** User changed labels in detail modal */
    data class TaskLabelsChanged(val labels: List<String>) : WeekEvent

    /** User changed deadline in detail modal */
    data class TaskDeadlineChanged(val deadline: Instant?) : WeekEvent

    /** User tapped "Complete" button in detail modal */
    data object TaskCompleteRequested : WeekEvent

    /** User tapped "Skip" button in detail modal */
    data object TaskSkipRequested : WeekEvent

    /** User tries to dismiss detail sheet (checks for unsaved text changes) */
    data object DetailSheetDismissRequested : WeekEvent

    /** User confirmed discarding unsaved text changes */
    data object DiscardChangesConfirmed : WeekEvent

    /** User cancelled discard dialog (returns to sheet) */
    data object DiscardChangesCancelled : WeekEvent

    /** User tapped save button to persist text changes */
    data object SaveTextChangesRequested : WeekEvent

    // ============================================
    // SUBTASK EVENTS (in detail modal)
    // ============================================

    /** User tapped subtask checkbox */
    data class SubtaskCheckboxTapped(val subtaskId: String) : WeekEvent

    /** User changed new subtask title input */
    data class NewSubtaskTitleChanged(val title: String) : WeekEvent

    /** User tapped "Add sub-task" or pressed enter */
    data object AddSubtaskSubmitted : WeekEvent

    /** User deleted a subtask */
    data class SubtaskDeleted(val subtaskId: String) : WeekEvent

    // ============================================
    // COMMENT EVENTS (in detail modal)
    // ============================================

    /** User changed comment text input */
    data class CommentTextChanged(val text: String) : WeekEvent

    /** User submitted comment */
    data object CommentSubmitted : WeekEvent
}
