package org.epoque.tandem.presentation.week.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.TaskPriority

/**
 * Form state for the Add Task modal.
 * Tracks all input fields and validation state.
 */
data class AddTaskFormState(
    // Core fields
    val title: String = "",
    val description: String = "",

    // Assignment & scheduling
    val ownerType: OwnerType = OwnerType.SELF,
    val scheduledDate: LocalDate? = null,      // null = no date selected
    val scheduledTime: LocalTime? = null,

    // Attributes
    val priority: TaskPriority = TaskPriority.P4,
    val labels: List<String> = emptyList(),
    val linkedGoalId: String? = null,
    val deadline: Instant? = null,

    // Validation
    val titleError: String? = null
) {
    /**
     * Whether the form is valid for submission.
     */
    val isValid: Boolean get() = title.isNotBlank()

    /**
     * Display text for priority chip.
     */
    val priorityDisplayText: String get() = priority.name  // "P1", "P2", "P3", "P4"
}
