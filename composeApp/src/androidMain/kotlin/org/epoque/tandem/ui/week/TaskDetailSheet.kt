package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.presentation.week.model.TaskUiModel

/**
 * Modal bottom sheet for viewing and editing task details.
 *
 * Following Material 3 best practices:
 * - ModalBottomSheet with rememberModalBottomSheetState()
 * - Proper dismissal handling (swipe, scrim tap, back button)
 * - Remove from composition when hidden (if/when pattern)
 * - Accessible form inputs with proper labels
 * - Confirmation dialog for destructive actions
 *
 * Based on best practices from:
 * - https://developer.android.com/develop/ui/compose/components/bottom-sheets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    task: TaskUiModel?,
    isReadOnly: Boolean,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveRequested: () -> Unit,
    onMarkCompleteRequested: () -> Unit,
    onDeleteRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sheet state for programmatic control
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Local state for editing
    var editedTitle by remember(task) { mutableStateOf(task?.title ?: "") }
    var editedNotes by remember(task) { mutableStateOf(task?.notes ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet title
            Text(
                text = if (isReadOnly) "Task Details" else "Edit Task",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Task title field
            OutlinedTextField(
                value = editedTitle,
                onValueChange = {
                    editedTitle = it
                    onTitleChange(it)
                },
                label = { Text("Title") },
                enabled = !isReadOnly,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = if (isReadOnly) {
                            "Task title: $editedTitle"
                        } else {
                            "Edit task title"
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Notes field
            OutlinedTextField(
                value = editedNotes,
                onValueChange = {
                    editedNotes = it
                    onNotesChange(it)
                },
                label = { Text("Notes (optional)") },
                enabled = !isReadOnly,
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = if (isReadOnly) {
                            "Task notes: ${editedNotes.ifBlank { "None" }}"
                        } else {
                            "Edit task notes"
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Task metadata section
            if (task != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Status display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (task.isCompleted) "Completed" else "Pending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                // Owner info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Owner",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = task.segment.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Completion attribution for completed shared tasks
                if (task.completedByName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completed by",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = task.completedByName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Repeat progress for repeating tasks
                if (task.isRepeating && task.repeatProgressText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = task.repeatProgressText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Rollover indicator for rolled-over tasks
                if (task.rolledOver) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rolled Over",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "From previous week",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            if (!isReadOnly && task != null) {
                // Mark Complete button (for incomplete tasks only)
                if (!task.isCompleted) {
                    FilledButton(
                        onClick = onMarkCompleteRequested,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Mark task as complete"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Complete")
                    }
                }

                // Save button (for edited fields)
                FilledTonalButton(
                    onClick = {
                        onSaveRequested()
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Save task changes"
                        }
                ) {
                    Text("Save")
                }

                // Delete button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Delete task"
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Task")
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteRequested()
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Helper composable for filled button with primary emphasis.
 */
@Composable
private fun FilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        content = content
    )
}
