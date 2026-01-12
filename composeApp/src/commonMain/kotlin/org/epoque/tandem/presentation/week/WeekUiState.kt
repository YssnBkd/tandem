package org.epoque.tandem.presentation.week

import kotlinx.datetime.LocalDate
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.presentation.week.model.AddTaskFormState
import org.epoque.tandem.presentation.week.model.CalendarDay
import org.epoque.tandem.presentation.week.model.Segment
import org.epoque.tandem.presentation.week.model.TaskDetailState
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.presentation.week.model.WeekInfo

/**
 * Complete UI state for the Week View screen.
 * Single source of truth for all UI elements.
 *
 * Following Android best practices:
 * - Immutable data class
 * - Single responsibility (only UI state)
 * - Naming convention: [Functionality]UiState
 */
data class WeekUiState(
    // Week header
    val weekInfo: WeekInfo? = null,

    // Segment selection
    val selectedSegment: Segment = Segment.YOU,

    // Calendar strip
    val calendarDays: List<CalendarDay> = emptyList(),

    // Task list (legacy - kept for compatibility)
    val tasks: List<TaskUiModel> = emptyList(),
    val incompleteTasks: List<TaskUiModel> = emptyList(),
    val completedTasks: List<TaskUiModel> = emptyList(),

    // Section-grouped tasks (new UI design)
    val overdueTasks: List<TaskUiModel> = emptyList(),
    val todayTasks: List<TaskUiModel> = emptyList(),
    val tomorrowTasks: List<TaskUiModel> = emptyList(),
    val laterThisWeekTasks: List<TaskUiModel> = emptyList(),
    val unscheduledTasks: List<TaskUiModel> = emptyList(),

    // Section visibility
    val isCompletedSectionExpanded: Boolean = false,

    // Progress
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressText: String = "0/0",

    // Quick add
    val quickAddText: String = "",
    val quickAddError: String? = null,

    // Loading/error states
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    // Sheet states (legacy)
    val selectedTaskId: String? = null,
    val showDetailSheet: Boolean = false,
    val showAddTaskSheet: Boolean = false,
    val editedTaskTitle: String = "",
    val editedTaskNotes: String = "",

    // Add Task modal form state (new)
    val addTaskForm: AddTaskFormState = AddTaskFormState(),

    // Task Detail modal state (new) - null = modal closed
    val taskDetailState: TaskDetailState? = null,

    // Discard changes dialog (shown when dismissing task detail with unsaved text changes)
    val showDiscardChangesDialog: Boolean = false,

    // Date filter (for calendar day selection)
    val selectedCalendarDate: LocalDate? = null,  // null = show all sections

    // Partner state
    val hasPartner: Boolean = false,
    val partnerName: String? = null,

    // Planning state
    val isPlanningComplete: Boolean = false,

    // Review state
    val isReviewWindowOpen: Boolean = false,
    val isWeekReviewed: Boolean = false,
    val currentStreak: Int = 0,

    // Goal linking (Feature 007)
    val availableGoals: List<Goal> = emptyList()
) {
    /**
     * Whether the current segment allows task completion.
     * Partner segment is read-only.
     */
    val isReadOnly: Boolean get() = selectedSegment == Segment.PARTNER

    /**
     * Whether to show empty state.
     */
    val showEmptyState: Boolean get() = !isLoading && tasks.isEmpty()

    /**
     * Get the selected task for the detail sheet.
     */
    val selectedTask: TaskUiModel? get() = selectedTaskId?.let { id ->
        tasks.find { it.id == id }
    }

    /**
     * Whether any incomplete tasks exist (for empty state).
     */
    val hasIncompleteTasks: Boolean get() =
        overdueTasks.isNotEmpty() ||
        todayTasks.isNotEmpty() ||
        tomorrowTasks.isNotEmpty() ||
        laterThisWeekTasks.isNotEmpty() ||
        unscheduledTasks.isNotEmpty()

    /**
     * Total incomplete task count for progress display.
     */
    val incompleteTaskCount: Int get() =
        overdueTasks.size + todayTasks.size + tomorrowTasks.size + laterThisWeekTasks.size + unscheduledTasks.size

    /**
     * Empty state message based on segment.
     */
    val emptyStateMessage: String get() = when (selectedSegment) {
        Segment.YOU -> "No tasks for this week yet.\nAdd one using the field above!"
        Segment.PARTNER -> if (hasPartner) {
            "Your partner hasn't added any tasks yet."
        } else {
            "Connect with your partner to see their tasks."
        }
        Segment.SHARED -> if (hasPartner) {
            "No shared tasks yet.\nAdd one to work on together!"
        } else {
            "Connect with your partner to see their tasks."
        }
    }

    /**
     * Empty state action text.
     */
    val emptyStateActionText: String? get() = when (selectedSegment) {
        Segment.YOU -> null  // Quick add is visible
        Segment.PARTNER -> if (!hasPartner) "Invite Partner" else null
        Segment.SHARED -> if (hasPartner) "Add Shared Task" else "Invite Partner"
    }

    /**
     * Whether to show the review banner.
     * Show when review window is open and week hasn't been reviewed.
     */
    val showReviewBanner: Boolean get() = isReviewWindowOpen && !isWeekReviewed
}
