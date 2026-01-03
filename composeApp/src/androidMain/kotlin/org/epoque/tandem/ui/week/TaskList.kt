package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.week.model.TaskUiModel

/**
 * Task list composable showing incomplete and completed tasks in separate sections.
 *
 * Following Android LazyColumn best practices:
 * - Uses stable keys (task.id) for performance and state preservation
 * - Uses animateItem() for smooth item transitions
 * - Uses contentPadding instead of modifier padding
 * - Avoids 0-pixel sized items
 * - Uses verticalArrangement.spacedBy for consistent spacing
 */
@Composable
fun TaskList(
    incompleteTasks: List<TaskUiModel>,
    completedTasks: List<TaskUiModel>,
    isReadOnly: Boolean,
    onTaskClick: (String) -> Unit,
    onCheckboxClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp) // Items handle their own spacing
    ) {
        // Incomplete tasks section
        items(
            items = incompleteTasks,
            key = { task -> task.id } // Stable unique key for state preservation
        ) { task ->
            TaskListItem(
                task = task,
                isReadOnly = isReadOnly,
                onCheckboxClick = { onCheckboxClick(task.id) },
                onClick = { onTaskClick(task.id) },
                modifier = Modifier.animateItem() // Smooth animations for reordering
            )
        }

        // Divider between incomplete and completed tasks (if both exist)
        if (incompleteTasks.isNotEmpty() && completedTasks.isNotEmpty()) {
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .animateItem(),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        // Completed tasks section
        items(
            items = completedTasks,
            key = { task -> task.id } // Stable unique key
        ) { task ->
            TaskListItem(
                task = task,
                isReadOnly = isReadOnly,
                onCheckboxClick = { onCheckboxClick(task.id) },
                onClick = { onTaskClick(task.id) },
                modifier = Modifier.animateItem() // Smooth animations
            )
        }
    }
}
