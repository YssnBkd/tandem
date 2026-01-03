package org.epoque.tandem.presentation.week

import org.epoque.tandem.presentation.week.model.Segment
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

    // Task list
    val tasks: List<TaskUiModel> = emptyList(),
    val incompleteTasks: List<TaskUiModel> = emptyList(),
    val completedTasks: List<TaskUiModel> = emptyList(),

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

    // Sheet states
    val selectedTaskId: String? = null,
    val showDetailSheet: Boolean = false,
    val showAddTaskSheet: Boolean = false,
    val editedTaskTitle: String = "",
    val editedTaskNotes: String = "",

    // Partner state
    val hasPartner: Boolean = false,
    val partnerName: String? = null,

    // Planning state
    val isPlanningComplete: Boolean = false
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
     * Empty state message based on segment.
     */
    val emptyStateMessage: String get() = when (selectedSegment) {
        Segment.YOU -> "No tasks for this week yet.\nAdd one using the field above!"
        Segment.PARTNER -> if (hasPartner) {
            "Your partner hasn't added any tasks yet."
        } else {
            "Connect with your partner to see their tasks."
        }
        Segment.SHARED -> "No shared tasks yet.\nAdd one to work on together!"
    }

    /**
     * Empty state action text.
     */
    val emptyStateActionText: String? get() = when (selectedSegment) {
        Segment.YOU -> null  // Quick add is visible
        Segment.PARTNER -> if (!hasPartner) "Invite Partner" else null
        Segment.SHARED -> "Add Shared Task"
    }
}
