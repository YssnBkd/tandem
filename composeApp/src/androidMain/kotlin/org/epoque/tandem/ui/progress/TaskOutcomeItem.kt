package org.epoque.tandem.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.progress.TaskOutcomeUiModel
import org.epoque.tandem.presentation.progress.TaskStatusColor

/**
 * Single task outcome row showing title and status icons.
 *
 * Displays user status icon, task title, and optional partner status icon.
 */
@Composable
fun TaskOutcomeItem(
    task: TaskOutcomeUiModel,
    modifier: Modifier = Modifier
) {
    val accessibilityLabel = buildString {
        append("Task: ${task.title}")
        append(", your status: ${task.userStatusIcon}")
        task.partnerStatusIcon?.let { append(", partner status: $it") }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 8.dp)
            .semantics { contentDescription = accessibilityLabel },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User status icon
        Text(
            text = task.userStatusIcon,
            style = MaterialTheme.typography.titleMedium,
            color = task.userStatusColor.toComposeColor()
        )

        // Task title
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Partner status icon (if available)
        task.partnerStatusIcon?.let { icon ->
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                color = task.partnerStatusColor?.toComposeColor() ?: Color.Unspecified
            )
        }
    }
}

@Composable
private fun TaskStatusColor.toComposeColor(): Color = when (this) {
    TaskStatusColor.COMPLETED -> MaterialTheme.colorScheme.primary
    TaskStatusColor.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant
    TaskStatusColor.PENDING -> MaterialTheme.colorScheme.outline
}
