package org.epoque.tandem.presentation.week.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.TaskPriority

/**
 * State for the Task Detail modal (expanded view).
 * Contains both display data and editable fields.
 */
data class TaskDetailState(
    // Identity
    val taskId: String = "",

    // Editable fields
    val title: String = "",
    val description: String = "",
    val ownerType: OwnerType = OwnerType.SELF,
    val scheduledDate: LocalDate? = null,
    val scheduledTime: LocalTime? = null,
    val priority: TaskPriority = TaskPriority.P4,
    val labels: List<String> = emptyList(),
    val linkedGoalId: String? = null,
    val deadline: Instant? = null,

    // Original values for detecting text changes (set when sheet opens)
    val originalTitle: String = "",
    val originalDescription: String = "",

    // Goal display (read-only, fetched from goal)
    val linkedGoalName: String? = null,
    val linkedGoalIcon: String? = null,
    val linkedGoalProgress: String? = null,  // "Weekly: 2 of 3"
    val linkedGoalProgressFraction: Float = 0f,  // For progress dots

    // Subtasks
    val subtasks: List<SubtaskUiModel> = emptyList(),
    val newSubtaskTitle: String = "",

    // Comment input
    val commentText: String = "",

    // Validation
    val titleError: String? = null,

    // State flags
    val isLoading: Boolean = false,
    val hasUnsavedChanges: Boolean = false
) {
    val isValid: Boolean get() = title.isNotBlank()
    val subtaskProgressText: String get() = "${subtasks.count { it.isCompleted }}/${subtasks.size}"

    /** True if title or description has been modified from original */
    val hasUnsavedTextChanges: Boolean get() =
        title != originalTitle || description != originalDescription
}

/**
 * UI model for a subtask in the detail view.
 */
data class SubtaskUiModel(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)
