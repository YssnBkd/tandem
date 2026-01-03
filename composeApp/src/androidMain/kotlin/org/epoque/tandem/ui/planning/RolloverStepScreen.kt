package org.epoque.tandem.ui.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.ui.planning.components.PlanningCard

/**
 * Screen for reviewing and rolling over incomplete tasks from last week.
 *
 * Shows each task as a full-screen card with Add/Skip buttons.
 * Automatically advances when all tasks are processed.
 *
 * Following Material Design 3 best practices:
 * - Full-screen card display for focus
 * - Clear primary (Add) and secondary (Skip) actions
 * - 48dp+ touch targets per FR-023
 * - Accessible content descriptions per FR-024
 * - Progress indicator showing current position
 *
 * @param currentTask The task currently being reviewed (null if all processed)
 * @param currentIndex Current position in the list (0-based)
 * @param totalTasks Total number of tasks to review
 * @param onAddToWeek Callback when user adds task to current week
 * @param onSkip Callback when user skips the task
 * @param onStepComplete Callback when all tasks are processed
 * @param modifier Modifier for customization
 */
@Composable
fun RolloverStepScreen(
    currentTask: TaskUiModel?,
    currentIndex: Int,
    totalTasks: Int,
    onAddToWeek: () -> Unit,
    onSkip: () -> Unit,
    onStepComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-complete when all tasks processed
    LaunchedEffect(currentIndex, totalTasks) {
        if (currentIndex >= totalTasks && totalTasks > 0) {
            onStepComplete()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // No rollover tasks - show empty state
            totalTasks == 0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No tasks from last week",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Great job completing everything!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onStepComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics {
                                contentDescription = "Continue to next step"
                            }
                    ) {
                        Text("Continue")
                    }
                }
            }

            // Current task to review
            currentTask != null -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Progress text
                    Text(
                        text = "Task ${currentIndex + 1} of $totalTasks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Task card
                    PlanningCard(
                        title = currentTask.title,
                        notes = currentTask.notes,
                        primaryAction = {
                            Button(
                                onClick = onAddToWeek,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .semantics {
                                        contentDescription = "Add task to this week"
                                    }
                            ) {
                                Text("Add to This Week")
                            }
                        },
                        secondaryAction = {
                            OutlinedButton(
                                onClick = onSkip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .semantics {
                                        contentDescription = "Skip this task"
                                    }
                            ) {
                                Text("Skip")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // All tasks processed - waiting for navigation
            else -> {
                // This state should be brief as onStepComplete will be called
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "All tasks reviewed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
