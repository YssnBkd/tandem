package org.epoque.tandem.ui.legacy.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalStatus
import org.epoque.tandem.domain.model.GoalType
import org.epoque.tandem.ui.legacy.goals.components.GoalProgressBar
import org.epoque.tandem.ui.legacy.goals.components.GoalStatusBadge

/**
 * Bottom sheet showing goal details, with edit and delete options for own goals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailSheet(
    goal: Goal,
    progressHistory: List<GoalProgress>,
    isPartnerGoal: Boolean,
    isEditing: Boolean,
    editName: String,
    editIcon: String,
    showDeleteConfirmation: Boolean,
    onEditTapped: () -> Unit,
    onDeleteTapped: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditIconChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            if (isEditing) {
                EditGoalContent(
                    name = editName,
                    icon = editIcon,
                    onNameChange = onEditNameChange,
                    onIconChange = onEditIconChange,
                    onSave = onSaveEdit,
                    onCancel = onCancelEdit
                )
            } else {
                GoalDetailContent(
                    goal = goal,
                    progressHistory = progressHistory,
                    isPartnerGoal = isPartnerGoal,
                    onEditTapped = onEditTapped,
                    onDeleteTapped = onDeleteTapped
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete Goal?") },
            text = {
                Text(
                    "This will permanently delete \"${goal.name}\" and unlink all associated tasks. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GoalDetailContent(
    goal: Goal,
    progressHistory: List<GoalProgress>,
    isPartnerGoal: Boolean,
    onEditTapped: () -> Unit,
    onDeleteTapped: () -> Unit
) {
    // Header with icon and name
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = goal.icon,
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = goal.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            GoalStatusBadge(status = goal.status)
        }

        // Edit/Delete buttons (only for own goals)
        if (!isPartnerGoal) {
            IconButton(onClick = onEditTapped, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Goal")
            }
            IconButton(onClick = onDeleteTapped, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Goal",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Progress section
    Text(
        text = "Progress",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(8.dp))

    GoalProgressBar(progress = goal.progressFraction)

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = goal.progressText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = when {
                goal.hasMetTarget -> "Target reached!"
                else -> "Keep going!"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (goal.hasMetTarget) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Goal type info
    Text(
        text = "Details",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(8.dp))

    val typeDescription = when (val type = goal.type) {
        is GoalType.WeeklyHabit -> "Weekly habit: ${type.targetPerWeek}x per week"
        is GoalType.RecurringTask -> "Recurring task: Once per week"
        is GoalType.TargetAmount -> "Target: ${type.targetTotal} total"
    }

    Text(
        text = typeDescription,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (goal.durationWeeks != null) {
        Text(
            text = "Duration: ${goal.durationWeeks} weeks (started ${goal.startWeekId})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Text(
            text = "Duration: Ongoing (started ${goal.startWeekId})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Progress history
    if (progressHistory.isNotEmpty()) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Weekly History",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        progressHistory.take(4).forEach { progress ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = progress.weekId,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = progress.progressText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (progress.progressFraction >= 1f) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun EditGoalContent(
    name: String,
    icon: String,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = "Edit Goal",
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Goal name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Simple icon selector (same as AddGoalSheet)
    Text(
        text = "Icon",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val emojis = listOf("\uD83C\uDFAF", "\uD83D\uDCAA", "\uD83D\uDCDA", "\uD83C\uDFC3", "\uD83E\uDDD8", "\uD83D\uDCBC")
        emojis.forEach { emoji ->
            TextButton(
                onClick = { onIconChange(emoji) },
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (icon == emoji) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }

        FilledTonalButton(
            onClick = onSave,
            enabled = name.isNotBlank(),
            modifier = Modifier.weight(1f)
        ) {
            Text("Save")
        }
    }
}
