package org.epoque.tandem.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.ui.review.components.ReviewProgressDots
import org.epoque.tandem.ui.review.components.TaskOutcomeCard

/**
 * Task review step screen - Step 2 of the review wizard.
 *
 * Users review each task one-by-one, selecting an outcome
 * (Done/Tried/Skipped) and optionally adding a note.
 *
 * Requirements:
 * - FR-009: Review tasks one at a time
 * - FR-010: Navigate between tasks
 * - FR-014: Pre-fill outcome for already-completed tasks
 *
 * @param task Current task being reviewed, or null if no tasks
 * @param taskIndex Current task index (0-based)
 * @param totalTasks Total number of tasks to review
 * @param currentOutcome Currently selected outcome for this task
 * @param note Current note text for this task
 * @param isLastTask Whether this is the last task in the sequence
 * @param onOutcomeSelected Callback when user selects an outcome
 * @param onNoteChanged Callback when user updates the note
 * @param onNext Callback when user taps Next/Done
 * @param onPrevious Callback when user taps Back
 * @param onQuickFinish Callback when user taps Quick Finish
 */
@Composable
fun TaskReviewStepScreen(
    task: Task?,
    taskIndex: Int,
    totalTasks: Int,
    currentOutcome: TaskStatus?,
    note: String,
    isLastTask: Boolean,
    onOutcomeSelected: (TaskStatus) -> Unit,
    onNoteChanged: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onQuickFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with progress dots and navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous"
                )
            }

            // Progress dots
            ReviewProgressDots(
                currentIndex = taskIndex,
                totalCount = totalTasks
            )

            // Quick Finish button
            TextButton(onClick = onQuickFinish) {
                Text("Quick Finish")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main content
        if (task != null) {
            // Task card fills remaining space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                TaskOutcomeCard(
                    task = task,
                    currentOutcome = currentOutcome,
                    note = note,
                    onOutcomeSelected = onOutcomeSelected,
                    onNoteChanged = onNoteChanged
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next/Done button
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = if (isLastTask) "Done" else "Next",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            // Empty state - no tasks to review
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No tasks this week",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNext,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Continue to Summary")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
