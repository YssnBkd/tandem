package org.epoque.tandem.ui.week.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Small badge showing a linked goal on task cards.
 *
 * Displays the goal icon and name in a compact chip format.
 * Following Material Design 3 best practices for accessibility.
 *
 * @param goalName The display name of the linked goal
 * @param goalIcon The emoji icon of the linked goal
 * @param modifier Modifier for customization
 */
@Composable
fun GoalBadge(
    goalName: String,
    goalIcon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .semantics {
                contentDescription = "Linked to goal: $goalName"
            },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goalIcon,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = goalName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
