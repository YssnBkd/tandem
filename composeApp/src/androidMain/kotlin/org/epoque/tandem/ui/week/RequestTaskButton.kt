package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Button for requesting a task from partner in the Partner segment.
 *
 * Following Material 3 best practices:
 * - FilledTonalButton for secondary action prominence
 * - Proper color roles for accessibility
 * - Icon with text label for clarity
 * - Minimum touch target compliance
 * - Semantic content descriptions
 *
 * Based on best practices from:
 * - https://developer.android.com/develop/ui/compose/components/button
 */
@Composable
fun RequestTaskButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics {
                contentDescription = "Request a task from your partner"
            },
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Request a Task",
            style = MaterialTheme.typography.labelLarge
        )
    }
}
