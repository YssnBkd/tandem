package org.epoque.tandem.ui.planning.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Full-screen card for reviewing tasks during planning.
 *
 * Used in rollover step and partner requests step to display
 * individual tasks for add/skip or accept/discuss decisions.
 *
 * Following Material Design 3 best practices:
 * - Surface with rounded corners (16dp)
 * - Clear visual hierarchy with headline and body text
 * - Action buttons at the bottom with proper spacing
 * - 48dp+ touch targets on all buttons
 *
 * @param title The task title to display
 * @param notes Optional notes/description for the task
 * @param primaryAction Composable for the primary action button
 * @param secondaryAction Composable for the secondary action button
 * @param modifier Modifier for customization
 */
@Composable
fun PlanningCard(
    title: String,
    notes: String?,
    primaryAction: @Composable () -> Unit,
    secondaryAction: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Task title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Task notes (if present)
            if (!notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary action takes less visual weight
                Column(modifier = Modifier.weight(1f)) {
                    secondaryAction()
                }

                // Primary action is more prominent
                Column(modifier = Modifier.weight(1f)) {
                    primaryAction()
                }
            }
        }
    }
}
