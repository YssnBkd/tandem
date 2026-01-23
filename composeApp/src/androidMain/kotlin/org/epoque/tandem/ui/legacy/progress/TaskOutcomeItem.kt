package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import org.epoque.tandem.presentation.progress.TaskOutcomeUiModel
import org.epoque.tandem.ui.components.week.PriorityCheckbox
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTypography

/**
 * Single task outcome row showing title and status checkboxes.
 *
 * Displays user priority checkbox, task title, and optional partner status checkbox.
 * Uses design tokens for consistent styling.
 */
@Composable
fun TaskOutcomeItem(
    task: TaskOutcomeUiModel,
    modifier: Modifier = Modifier
) {
    val userStatusText = when {
        task.isCompleted -> "completed"
        task.isSkipped -> "skipped"
        else -> "pending"
    }
    val partnerStatusText = when {
        task.partnerCompleted == true -> "completed"
        task.partnerSkipped == true -> "skipped"
        task.partnerCompleted != null -> "pending"
        else -> null
    }

    val accessibilityLabel = buildString {
        append("Task: ${task.title}")
        append(", your status: $userStatusText")
        partnerStatusText?.let { append(", partner status: $it") }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = TandemSpacing.List.itemHorizontalPadding,
                vertical = TandemSpacing.List.itemVerticalPadding
            )
            .semantics { contentDescription = accessibilityLabel },
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.Inline.checkboxGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User status checkbox
        PriorityCheckbox(
            checked = task.isCompleted,
            priority = task.priority,
            onCheckedChange = { },
            enabled = false,
            modifier = Modifier.padding(top = TandemSpacing.xxxs)
        )

        // Task title
        Text(
            text = task.title,
            style = TandemTypography.titleMedium.copy(
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (task.isCompleted) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Partner status checkbox (if available)
        if (task.partnerCompleted != null || task.partnerSkipped != null) {
            Spacer(modifier = Modifier.width(TandemSpacing.xs))
            PriorityCheckbox(
                checked = task.partnerCompleted == true,
                priority = task.priority,
                onCheckedChange = { },
                enabled = false,
                modifier = Modifier.padding(top = TandemSpacing.xxxs)
            )
        }
    }
}
