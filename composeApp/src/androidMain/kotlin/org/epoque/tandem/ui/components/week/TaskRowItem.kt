package org.epoque.tandem.ui.components.week

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.ui.theme.OverdueRed
import org.epoque.tandem.ui.theme.ScheduleGreen

/**
 * UI model for a task in the week view.
 */
data class TaskUiItem(
    val id: String,
    val title: String,
    val priority: TaskPriority,
    val isCompleted: Boolean = false,
    val schedule: String? = null, // e.g., "7:30 AM", "Yesterday", "Sat"
    val isOverdue: Boolean = false,
    val isRecurring: Boolean = false,
    val subtaskCount: Int? = null, // e.g., 3 means "3 items"
    val projectOrGoal: String? = null // e.g., "Work", "Fitness"
)

/**
 * Task row item matching the Todoist-inspired mockup design.
 * Shows priority checkbox, title, and metadata in a clean layout.
 */
@Composable
fun TaskRowItem(
    task: TaskUiItem,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Priority checkbox
        PriorityCheckbox(
            checked = task.isCompleted,
            priority = task.priority,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Content column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (task.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Metadata row - only show if there's metadata
            if (task.schedule != null || task.isRecurring || task.subtaskCount != null || task.projectOrGoal != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Schedule time/date
                    if (task.schedule != null) {
                        ScheduleIndicator(
                            schedule = task.schedule,
                            isOverdue = task.isOverdue
                        )
                    }

                    // Recurring indicator
                    if (task.isRecurring) {
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = "Recurring",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Subtask count
                    if (task.subtaskCount != null && task.subtaskCount > 0) {
                        SubtaskIndicator(count = task.subtaskCount)
                    }

                    // Spacer to push project/goal to the right
                    Spacer(modifier = Modifier.weight(1f))

                    // Project or Goal tag (right-aligned)
                    if (task.projectOrGoal != null) {
                        ProjectTag(name = task.projectOrGoal)
                    }
                }
            }
        }
    }
}

/**
 * Schedule indicator with calendar icon.
 * Shows in green normally, red if overdue.
 */
@Composable
private fun ScheduleIndicator(
    schedule: String,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isOverdue) OverdueRed else ScheduleGreen

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color
        )
        Text(
            text = schedule,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = color
        )
    }
}

/**
 * Subtask count indicator.
 */
@Composable
private fun SubtaskIndicator(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Checklist,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$count items",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Project or Goal tag (right-aligned).
 * Shows as "ProjectName #"
 */
@Composable
private fun ProjectTag(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$name #",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = modifier
    )
}
