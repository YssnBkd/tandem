package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.week.model.WeekInfo

/**
 * Week header showing date range and progress indicator.
 *
 * Following Material 3 best practices:
 * - Uses proper typography roles (headlineSmall, bodyLarge)
 * - Maintains color contrast with onSurface/onSurfaceVariant
 * - Includes semantic descriptions for accessibility
 */
@Composable
fun WeekHeader(
    weekInfo: WeekInfo,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "${weekInfo.dateRangeText}. " +
                        "$completedCount of $totalCount tasks completed."
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Week date range
            Text(
                text = weekInfo.dateRangeText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Progress indicator (fraction format)
            Text(
                text = "$completedCount/$totalCount",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
