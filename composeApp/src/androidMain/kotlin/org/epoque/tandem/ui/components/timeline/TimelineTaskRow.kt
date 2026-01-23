package org.epoque.tandem.ui.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.theme.PriorityP2
import org.epoque.tandem.ui.theme.PriorityP3
import org.epoque.tandem.ui.theme.PriorityP4
import org.epoque.tandem.ui.theme.TandemTertiary
import org.epoque.tandem.ui.theme.TandemTertiaryContainer

/**
 * Task row for timeline display.
 * Shows completion state, priority indicator, title, and optional metadata (goal tag, partner).
 */
@Composable
fun TimelineTaskRow(
    task: Task,
    modifier: Modifier = Modifier,
    goalName: String? = null,
    goalIcon: String? = null,
    partnerName: String? = null
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val priorityColor = when (task.priority) {
        TaskPriority.P1 -> PriorityP1
        TaskPriority.P2 -> PriorityP2
        TaskPriority.P3 -> PriorityP3
        TaskPriority.P4 -> PriorityP4
    }

    val hasMetadata = goalName != null || partnerName != null

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = if (hasMetadata) Alignment.Top else Alignment.CenterVertically
    ) {
        // Checkbox - outlined circle with priority color fill (10% opacity)
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (isCompleted) {
                        Modifier.background(MaterialTheme.colorScheme.outline)
                    } else {
                        Modifier
                            .background(priorityColor.copy(alpha = 0.1f))
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

        Spacer(modifier = Modifier.width(12.dp))

        // Task content (title + optional metadata)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Metadata row (goal tag and/or partner name)
            if (hasMetadata && !isCompleted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Goal tag
                    if (goalName != null) {
                        GoalTag(
                            icon = goalIcon,
                            name = goalName
                        )
                    }

                    // Partner assignment
                    if (partnerName != null) {
                        PartnerTag(name = partnerName)
                    }
                }
            }
        }
    }
}

/**
 * Goal tag pill showing icon and name.
 */
@Composable
private fun GoalTag(
    icon: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TandemTertiaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Text(
                text = icon,
                fontSize = 10.sp
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TandemTertiary
        )
    }
}

/**
 * Partner assignment indicator.
 */
@Composable
private fun PartnerTag(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = TandemTertiary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "From $name",
            style = MaterialTheme.typography.labelSmall,
            color = TandemTertiary
        )
    }
}
