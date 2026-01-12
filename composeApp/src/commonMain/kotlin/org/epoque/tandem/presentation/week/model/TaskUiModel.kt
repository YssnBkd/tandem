package org.epoque.tandem.presentation.week.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus

/**
 * UI model for displaying a task in the Week View.
 * Optimized for Compose rendering with pre-computed display values.
 */
data class TaskUiModel(
    val id: String,
    val title: String,
    val notes: String?,
    val isCompleted: Boolean,
    val ownerType: OwnerType,
    val segment: Segment,
    val weekId: String,

    // Repeat task support
    val isRepeating: Boolean,
    val repeatTarget: Int?,
    val repeatCompleted: Int,
    val repeatProgressText: String?,     // "2/3"
    val repeatProgressDots: String?,     // "●●○"

    // Metadata for detail view
    val statusDisplayText: String,       // "Completed", "In Progress"
    val createdByCurrentUser: Boolean,
    val rolledOver: Boolean,
    val completedByName: String?,        // For shared tasks

    // Goal linking (Feature 007)
    val linkedGoalId: String? = null,
    val linkedGoalName: String? = null,
    val linkedGoalIcon: String? = null,

    // Feature 009: UI Redesign fields
    val priority: TaskPriority = TaskPriority.P4,
    val scheduledDate: LocalDate? = null,
    val scheduledTime: LocalTime? = null,
    val deadline: Instant? = null,
    val parentTaskId: String? = null,
    val labels: List<String> = emptyList(),

    // Subtitle display (computed based on section context)
    val subtitleIcon: SubtitleIcon? = null,      // Which icon to show
    val subtitleText: String? = null,            // "7:30 AM", "3 items", "Yesterday", "Sat"
    val showRepeatIndicator: Boolean = false,    // Show ↻ after subtitle (only with time)

    // Subtask info (needed for subtitle computation)
    val subtaskCount: Int = 0,
    val completedSubtaskCount: Int = 0,

    // Label display (right side of task item)
    val primaryLabelText: String? = null         // "Fitness #", "Work #" (first label + " #", or null)
) {
    companion object {
        /**
         * Create TaskUiModel from domain Task.
         *
         * @param task Domain task entity
         * @param currentUserId Current user's ID for ownership checks
         * @param partnerName Partner's display name (for shared task completion)
         * @param goalName Display name of linked goal (if any)
         * @param goalIcon Emoji icon of linked goal (if any)
         * @param subtaskCount Total number of subtasks
         * @param completedSubtaskCount Number of completed subtasks
         */
        fun fromTask(
            task: Task,
            currentUserId: String,
            partnerName: String? = null,
            goalName: String? = null,
            goalIcon: String? = null,
            subtaskCount: Int = 0,
            completedSubtaskCount: Int = 0
        ): TaskUiModel {
            val isCompleted = task.status == TaskStatus.COMPLETED ||
                (task.isRepeating && task.repeatCompleted >= (task.repeatTarget ?: 0))

            // Compute primary label text (first label + " #" suffix)
            val primaryLabel = task.labels.firstOrNull()?.let { "$it #" }

            return TaskUiModel(
                id = task.id,
                title = task.title,
                notes = task.notes,
                isCompleted = isCompleted,
                ownerType = task.ownerType,
                segment = Segment.fromOwnerType(task.ownerType),
                weekId = task.weekId,
                isRepeating = task.isRepeating,
                repeatTarget = task.repeatTarget,
                repeatCompleted = task.repeatCompleted,
                repeatProgressText = task.repeatTarget?.let { "${task.repeatCompleted}/$it" },
                repeatProgressDots = task.repeatTarget?.let { target ->
                    buildString {
                        repeat(task.repeatCompleted.coerceAtMost(target)) { append("●") }
                        repeat((target - task.repeatCompleted).coerceAtLeast(0)) { append("○") }
                    }
                },
                statusDisplayText = when (task.status) {
                    TaskStatus.PENDING -> "In Progress"
                    TaskStatus.PENDING_ACCEPTANCE -> "Pending Acceptance"
                    TaskStatus.COMPLETED -> "Completed"
                    TaskStatus.TRIED -> "Tried"
                    TaskStatus.SKIPPED -> "Skipped"
                    TaskStatus.DECLINED -> "Declined"
                },
                createdByCurrentUser = task.createdBy == currentUserId,
                rolledOver = task.rolledFromWeekId != null,
                completedByName = if (task.ownerType == OwnerType.SHARED && isCompleted) {
                    if (task.createdBy == currentUserId) "you" else partnerName
                } else null,
                linkedGoalId = task.linkedGoalId,
                linkedGoalName = goalName,
                linkedGoalIcon = goalIcon,
                // Feature 009 fields
                priority = task.priority,
                scheduledDate = task.scheduledDate,
                scheduledTime = task.scheduledTime,
                deadline = task.deadline,
                parentTaskId = task.parentTaskId,
                labels = task.labels,
                // Subtitle fields - computed later by ViewModel based on section
                subtitleIcon = null,
                subtitleText = null,
                showRepeatIndicator = false,
                // Subtask info
                subtaskCount = subtaskCount,
                completedSubtaskCount = completedSubtaskCount,
                // Primary label
                primaryLabelText = primaryLabel
            )
        }
    }
}
