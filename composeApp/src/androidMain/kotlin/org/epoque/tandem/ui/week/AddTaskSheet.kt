package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.epoque.tandem.domain.model.OwnerType

/**
 * Modal bottom sheet for creating a new task with full details.
 *
 * Following Material 3 best practices:
 * - ModalBottomSheet with proper state management
 * - Form validation with inline errors
 * - Owner type selection with SegmentedButton
 * - Accessible form inputs
 *
 * Based on best practices from:
 * - https://developer.android.com/develop/ui/compose/components/bottom-sheets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    onSubmit: (title: String, notes: String?, ownerType: OwnerType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sheet state
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Form state
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedOwnerType by remember { mutableStateOf(OwnerType.SELF) }
    var titleError by remember { mutableStateOf<String?>(null) }

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
                text = "Add Task",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Title") },
                placeholder = { Text("What needs to be done?") },
                isError = titleError != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Task title field"
                    }
            )

            if (titleError != null) {
                Text(
                    text = titleError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Add any additional details...") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Task notes field (optional)"
                    }
            )

            // Owner type selection
            Text(
                text = "Task Type",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            OwnerTypeSelector(
                selectedOwnerType = selectedOwnerType,
                onOwnerTypeSelected = { selectedOwnerType = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit button
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isBlank()) {
                        titleError = "Title cannot be empty"
                        return@Button
                    }

                    val trimmedNotes = notes.trim().takeIf { it.isNotEmpty() }
                    onSubmit(trimmedTitle, trimmedNotes, selectedOwnerType)

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
                        contentDescription = "Create task"
                    }
            ) {
                Text("Add Task")
            }
        }
    }
}

/**
 * Owner type selector using segmented buttons.
 *
 * Following Material 3 best practices:
 * - SingleChoiceSegmentedButtonRow for exclusive selection
 * - Clear labels for each type
 * - Proper semantics for accessibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerTypeSelector(
    selectedOwnerType: OwnerType,
    onOwnerTypeSelected: (OwnerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        OwnerType.SELF to "You",
        OwnerType.PARTNER to "Partner",
        OwnerType.SHARED to "Shared"
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Select task owner type. Current selection: ${
                    options.find { it.first == selectedOwnerType }?.second
                }"
            }
    ) {
        options.forEachIndexed { index, (ownerType, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onOwnerTypeSelected(ownerType) },
                selected = ownerType == selectedOwnerType,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.semantics {
                    contentDescription = "$label tasks"
                }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
