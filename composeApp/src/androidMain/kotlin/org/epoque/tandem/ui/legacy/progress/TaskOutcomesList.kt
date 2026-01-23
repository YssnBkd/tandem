package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.epoque.tandem.presentation.progress.TaskOutcomeUiModel
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTextStyles

/**
 * List of task outcomes for past week detail.
 *
 * Shows all tasks with their user/partner status checkboxes.
 * Uses design tokens for consistent styling.
 */
@Composable
fun TaskOutcomesList(
    tasks: List<TaskOutcomeUiModel>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header
        Text(
            text = "Tasks",
            style = TandemTextStyles.Title.listSection,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = TandemSpacing.List.itemHorizontalPadding)
                .padding(
                    top = TandemSpacing.List.sectionHeaderTopPadding,
                    bottom = TandemSpacing.List.sectionHeaderBottomPadding
                )
        )

        if (tasks.isEmpty()) {
            Text(
                text = "No tasks for this week",
                style = TandemTextStyles.Body.secondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = TandemSpacing.List.itemHorizontalPadding)
                    .padding(vertical = TandemSpacing.md)
            )
        } else {
            tasks.forEachIndexed { index, task ->
                TaskOutcomeItem(task = task)
                if (index < tasks.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = TandemSpacing.List.itemHorizontalPadding)
                    )
                }
            }
        }
    }
}
