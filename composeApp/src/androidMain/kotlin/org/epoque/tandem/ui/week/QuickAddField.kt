package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Quick add text field for inline task creation.
 *
 * Following Android best practices:
 * - OutlinedTextField for clear visual separation (Material 3)
 * - ImeAction.Done for keyboard submit action
 * - KeyboardActions.onDone to handle submission and clear focus
 * - LocalFocusManager.clearFocus() to hide keyboard after submit
 * - Inline error display below field (not snackbar)
 * - Single line input for task titles
 * - Proper accessibility semantics
 *
 * Based on best practices from:
 * - https://developer.android.com/develop/ui/compose/text/user-input
 * - https://canopas.com/keyboard-handling-in-jetpack-compose-all-you-need-to-know-3e6fddd30d9a
 * - https://developer.android.com/develop/ui/compose/touch-input/focus/change-focus-behavior
 */
@Composable
fun QuickAddField(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    // Focus manager to clear focus and hide keyboard after submission
    // https://composables.com/blog/focus-text
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Quick add task field. Enter task title and press done."
                },
            placeholder = {
                Text(
                    text = "Add a task...",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = errorMessage != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSubmit()
                    // Clear focus and hide keyboard after submission
                    // https://canopas.com/keyboard-handling-in-jetpack-compose-all-you-need-to-know-3e6fddd30d9a
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        // Inline error message below field
        // Better UX than snackbar for immediate validation feedback
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .semantics {
                        contentDescription = "Error: $errorMessage"
                    }
            )
        }
    }
}
