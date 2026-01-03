package org.epoque.tandem.ui.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.ui.planning.components.TaskInputField

/**
 * Screen for adding new tasks during the planning wizard.
 *
 * Displays a text input at the top with a visible Add button,
 * a list of added tasks in the middle, and a Done button at the bottom.
 *
 * Following Material Design 3 best practices:
 * - VISIBLE Add button (not keyboard-only) per FR-010
 * - 48dp+ touch targets per FR-023
 * - Accessible content descriptions per FR-024
 * - Clear visual hierarchy
 *
 * @param taskText Current text in the input field
 * @param taskError Error message (null if no error)
 * @param addedTasks List of tasks added during this session
 * @param onTextChange Callback when text changes
 * @param onAddTask Callback when user adds a task
 * @param onDone Callback when user is done adding tasks
 * @param modifier Modifier for customization
 */
@Composable
fun AddTasksStepScreen(
    taskText: String,
    taskError: String?,
    addedTasks: List<TaskUiModel>,
    onTextChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Task input field
        TaskInputField(
            text = taskText,
            onTextChange = onTextChange,
            onSubmit = onAddTask,
            error = taskError,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Added tasks list or empty state
        Box(modifier = Modifier.weight(1f)) {
            if (addedTasks.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No tasks added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add some tasks above, or tap Done to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Task list
                Column {
                    Text(
                        text = "${addedTasks.size} task${if (addedTasks.size == 1) "" else "s"} added this session",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(
                            items = addedTasks,
                            key = { task -> task.id }
                        ) { task ->
                            AddedTaskItem(task = task)
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Done button
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp)
                .semantics {
                    contentDescription = "Finish adding tasks and continue"
                }
        ) {
            Text("Done Adding Tasks")
        }
    }
}

/**
 * Single task item in the added tasks list.
 * Read-only display of task title.
 */
@Composable
private fun AddedTaskItem(
    task: TaskUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}
