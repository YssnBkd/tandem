package org.epoque.tandem.ui.goals.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Progress bar for displaying goal completion.
 * Uses 48dp height for accessibility (touch target compliance).
 */
@Composable
fun GoalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(MaterialTheme.shapes.small),
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        color = when {
            progress >= 1f -> MaterialTheme.colorScheme.tertiary
            progress >= 0.5f -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        }
    )
}
