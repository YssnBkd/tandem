package org.epoque.tandem.ui.legacy.review.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus

/**
 * Full-screen card for reviewing a single task.
 *
 * Displays the task title prominently with three outcome options:
 * - Done (✓): Task was completed as intended
 * - Tried (~): Made an attempt but didn't fully complete
 * - Skipped (○): Didn't work on this task
 *
 * Following "Celebration Over Judgment" principle:
 * - "Tried" not "Failed"
 * - "Skipped" not "Abandoned"
 *
 * @param task The task being reviewed
 * @param currentOutcome Currently selected outcome, or null if none
 * @param note Optional note text for the task
 * @param onOutcomeSelected Callback when user selects an outcome
 * @param onNoteChanged Callback when user updates the note
 * @param modifier Modifier for the component
 */
@Composable
fun TaskOutcomeCard(
    task: Task,
    currentOutcome: TaskStatus?,
    note: String,
    onOutcomeSelected: (TaskStatus) -> Unit,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Task title - prominently displayed
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Outcome buttons - minimum 56dp height for easy tapping
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutcomeButton(
                    text = "Done",
                    icon = "✓",
                    isSelected = currentOutcome == TaskStatus.COMPLETED,
                    onClick = { onOutcomeSelected(TaskStatus.COMPLETED) }
                )
                OutcomeButton(
                    text = "Tried",
                    icon = "~",
                    isSelected = currentOutcome == TaskStatus.TRIED,
                    onClick = { onOutcomeSelected(TaskStatus.TRIED) }
                )
                OutcomeButton(
                    text = "Skipped",
                    icon = "○",
                    isSelected = currentOutcome == TaskStatus.SKIPPED,
                    onClick = { onOutcomeSelected(TaskStatus.SKIPPED) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional note field - clearly labeled as optional
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                label = { Text("Quick note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Outcome selection button with icon and selection state.
 * Minimum height: 56dp for comfortable tapping.
 */
@Composable
private fun OutcomeButton(
    text: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .heightIn(min = 56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = "$icon  $text",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
