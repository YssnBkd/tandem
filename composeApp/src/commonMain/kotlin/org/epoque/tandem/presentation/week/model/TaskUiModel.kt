package org.epoque.tandem.presentation.week.model

import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
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
    val linkedGoalIcon: String? = null
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
         */
        fun fromTask(
            task: Task,
            currentUserId: String,
            partnerName: String? = null,
            goalName: String? = null,
            goalIcon: String? = null
        ): TaskUiModel {
            val isCompleted = task.status == TaskStatus.COMPLETED ||
                (task.isRepeating && task.repeatCompleted >= (task.repeatTarget ?: 0))

            return TaskUiModel(
                id = task.id,
                title = task.title,
                notes = task.notes,
                isCompleted = isCompleted,
                ownerType = task.ownerType,
                segment = Segment.fromOwnerType(task.ownerType),
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
                linkedGoalIcon = goalIcon
            )
        }
    }
}
