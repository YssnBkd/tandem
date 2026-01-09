package org.epoque.tandem.ui.legacy.planning.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * Text input field for adding new tasks during planning.
 *
 * Following Material Design 3 best practices and spec requirements:
 * - OutlinedTextField with clear label
 * - VISIBLE Add button (not keyboard-only) per FR-010
 * - 48dp IconButton touch target per FR-023
 * - Inline error display via supportingText
 * - IME action triggers submission as convenience
 *
 * @param text Current text value
 * @param onTextChange Callback when text changes
 * @param onSubmit Callback when user submits (button tap or keyboard action)
 * @param error Error message to display (null if no error)
 * @param modifier Modifier for customization
 */
@Composable
fun TaskInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Add a task") },
        placeholder = { Text("What do you need to do?") },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(text = error) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSubmit() }
        ),
        trailingIcon = {
            // VISIBLE Add button - required per FR-010
            // 48dp touch target per FR-023
            IconButton(
                onClick = onSubmit,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = "Add task"
                    },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null // Description on IconButton
                )
            }
        }
    )
}
