package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Empty state composable for when no tasks are present.
 *
 * Following Material 3 best practices:
 * - Centered layout with appropriate spacing
 * - Uses bodyLarge typography for readability
 * - Optional action button following Material 3 button styles
 * - Proper color roles for accessibility
 */
@Composable
fun EmptyState(
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty state message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Optional action button
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onActionClick
            ) {
                Text(text = actionText)
            }
        }
    }
}
