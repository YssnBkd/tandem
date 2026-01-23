package org.epoque.tandem.ui.screens.week

import androidx.compose.ui.graphics.Color
import org.epoque.tandem.domain.model.TaskPriority

/**
 * UI model for task detail view.
 */
data class TaskDetailUiModel(
    val id: String,
    val title: String,
    val description: String? = null,
    val priority: TaskPriority,
    val owner: String = "Me",
    val dueDate: String? = null,
    val project: String = "This Week",
    val labels: List<LabelUiModel> = emptyList(),
    val goal: GoalProgressUiModel? = null,
    val subtasks: List<SubtaskUiModel> = emptyList()
)

/**
 * Label with colored dot.
 */
data class LabelUiModel(
    val name: String,
    val color: Color
)

/**
 * Goal progress indicator.
 */
data class GoalProgressUiModel(
    val emoji: String,
    val name: String,
    val current: Int,
    val total: Int
)

/**
 * Subtask item.
 */
data class SubtaskUiModel(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

/**
 * Option chip for the horizontal scroll.
 */
enum class TaskOption(val label: String) {
    DEADLINE("Deadline"),
    REMINDERS("Reminders"),
    LOCATION("Location"),
    REPEAT("Repeat")
}
