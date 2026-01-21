package org.epoque.tandem.ui.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.theme.PriorityP2
import org.epoque.tandem.ui.theme.PriorityP3
import org.epoque.tandem.ui.theme.PriorityP4

/**
 * Simplified task row for timeline display.
 * Shows completion state, priority indicator, and title.
 */
@Composable
fun TimelineTaskRow(
    task: Task,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val priorityColor = when (task.priority) {
        TaskPriority.P1 -> PriorityP1
        TaskPriority.P2 -> PriorityP2
        TaskPriority.P3 -> PriorityP3
        TaskPriority.P4 -> PriorityP4
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox - outlined circle with priority color fill
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (isCompleted) {
                        Modifier.background(MaterialTheme.colorScheme.outline)
                    } else {
                        Modifier
                            .background(priorityColor.copy(alpha = 0.15f))
                            .border(2.dp, priorityColor, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Task title
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCompleted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
