package org.epoque.tandem.ui.week

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.ui.week.components.AnimatedCheckbox
import org.epoque.tandem.ui.week.components.GoalBadge

/**
 * Individual task row in the Week View.
 *
 * Following Android best practices:
 * - Uses clickable modifier with ripple effect
 * - Proper Material 3 color roles for accessibility
 * - Semantic content descriptions for accessibility
 * - Minimum 48dp touch target height
 */
@Composable
fun TaskListItem(
    task: TaskUiModel,
    isReadOnly: Boolean,
    onCheckboxClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                onClickLabel = "View task details"
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 48.dp) // Minimum touch target
            .alpha(if (task.isCompleted) 0.5f else 1f)
            .semantics {
                contentDescription = if (task.isCompleted) {
                    "Completed task: ${task.title}"
                } else {
                    "Task: ${task.title}"
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkbox - hidden in read-only mode (Partner segment)
        // Using AnimatedCheckbox for satisfying spring animation on completion
        if (!isReadOnly) {
            AnimatedCheckbox(
                checked = task.isCompleted,
                onCheckedChange = { onCheckboxClick() }
            )
        } else {
            // Spacer to maintain alignment when checkbox is hidden
            Spacer(modifier = Modifier.width(40.dp))
        }

        // Task title with strikethrough when completed
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (task.isCompleted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (task.isCompleted) {
                TextDecoration.LineThrough
            } else {
                null
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Repeat progress indicator (if repeating task)
        if (task.isRepeating && task.repeatProgressText != null) {
            RepeatProgressIndicator(
                progressText = task.repeatProgressText,
                modifier = Modifier.semantics {
                    contentDescription = "Progress: ${task.repeatProgressText}"
                }
            )
        }

        // Goal badge (if linked to a goal)
        if (task.linkedGoalId != null && task.linkedGoalName != null && task.linkedGoalIcon != null) {
            GoalBadge(
                goalName = task.linkedGoalName,
                goalIcon = task.linkedGoalIcon,
                modifier = Modifier.semantics {
                    contentDescription = "Linked to goal: ${task.linkedGoalName}"
                }
            )
        }

        // Completion attribution for shared tasks
        if (task.completedByName != null) {
            Text(
                text = "by ${task.completedByName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Progress indicator for repeating tasks.
 * Shows completion progress in fraction format (e.g., "2/3").
 *
 * Following Material 3 best practices:
 * - Uses Surface with proper color roles
 * - Maintains readability with onSurfaceVariant
 */
@Composable
fun RepeatProgressIndicator(
    progressText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = progressText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
