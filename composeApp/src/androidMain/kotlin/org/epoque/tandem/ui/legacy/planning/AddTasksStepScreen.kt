package org.epoque.tandem.ui.legacy.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.ui.legacy.planning.components.GoalSuggestionsCard
import org.epoque.tandem.ui.legacy.planning.components.TaskInputField

/**
 * Screen for adding new tasks during the planning wizard.
 *
 * Displays a text input at the top with a visible Add button,
 * goal suggestions for linking tasks (Feature 007),
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
 * @param goalSuggestions List of active goals to suggest linking
 * @param selectedGoal Currently selected goal for the new task
 * @param onTextChange Callback when text changes
 * @param onAddTask Callback when user adds a task
 * @param onGoalSelected Callback when user selects a goal suggestion
 * @param onClearGoal Callback when user clears the selected goal
 * @param onDone Callback when user is done adding tasks
 * @param modifier Modifier for customization
 */
@Composable
fun AddTasksStepScreen(
    taskText: String,
    taskError: String?,
    addedTasks: List<TaskUiModel>,
    goalSuggestions: List<Goal>,
    selectedGoal: Goal?,
    onTextChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onGoalSelected: (Goal) -> Unit,
    onClearGoal: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Goal suggestions (Feature 007: Goals System)
        if (goalSuggestions.isNotEmpty() && selectedGoal == null) {
            GoalSuggestionsCard(
                goals = goalSuggestions,
                onGoalSelected = onGoalSelected,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Selected goal indicator
        if (selectedGoal != null) {
            SelectedGoalIndicator(
                goal = selectedGoal,
                onClear = onClearGoal,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Task input field
        TaskInputField(
            text = taskText,
            onTextChange = onTextChange,
            onSubmit = onAddTask,
            error = taskError,
            modifier = if (selectedGoal == null && goalSuggestions.isEmpty()) {
                Modifier.padding(top = 16.dp)
            } else {
                Modifier
            }
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

/**
 * Indicator showing the currently selected goal for the new task.
 * Allows clearing the selection.
 */
@Composable
private fun SelectedGoalIndicator(
    goal: Goal,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goal.icon,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Linking to goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = "Clear goal selection"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
