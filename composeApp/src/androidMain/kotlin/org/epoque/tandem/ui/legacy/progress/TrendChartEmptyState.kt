package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Empty state shown when insufficient data for trend chart.
 *
 * Displays a friendly message encouraging the user to complete
 * more weeks to unlock the trend chart.
 */
@Composable
fun TrendChartEmptyState(
    weekCount: Int,
    modifier: Modifier = Modifier
) {
    val weeksNeeded = 4 - weekCount

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Completion Trends",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "\uD83D\uDCC8", // chart emoji
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (weekCount == 0) {
                    "Complete your first week to start tracking trends!"
                } else {
                    "Complete $weeksNeeded more ${if (weeksNeeded == 1) "week" else "weeks"} to unlock trend chart"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (weekCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$weekCount ${if (weekCount == 1) "week" else "weeks"} completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
