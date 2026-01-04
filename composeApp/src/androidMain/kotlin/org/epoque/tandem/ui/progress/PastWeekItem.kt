package org.epoque.tandem.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.progress.WeekSummaryUiModel

/**
 * Single row for past week in progress list.
 *
 * Displays date range, completion stats, and mood emoji.
 * Minimum 48dp touch target for accessibility.
 */
@Composable
fun PastWeekItem(
    week: WeekSummaryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityLabel = buildString {
        append("Week ${week.dateRange}")
        append(", completed ${week.userCompletionText}")
        week.userMoodEmoji?.let { append(", mood $it") }
        if (week.isReviewed) append(", reviewed")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = accessibilityLabel },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Date range and completion
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = week.dateRange,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = week.userCompletionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    week.partnerCompletionText?.let { partnerText ->
                        Text(
                            text = "| $partnerText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Right side: Mood emojis
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.userMoodEmoji?.let { emoji ->
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                week.partnerMoodEmoji?.let { emoji ->
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
