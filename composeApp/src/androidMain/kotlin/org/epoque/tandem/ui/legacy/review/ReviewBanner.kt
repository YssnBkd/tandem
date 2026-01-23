package org.epoque.tandem.ui.legacy.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Banner displayed on the Week view to prompt users to review their week.
 *
 * Shown when:
 * - Review window is open (Friday 6PM - Sunday 11:59PM)
 * - Current week has not been reviewed yet
 *
 * Requirements:
 * - FR-015: Banner appears Friday 6PM
 * - FR-016: Banner hidden after review complete
 *
 * @param currentStreak Current streak in weeks (0 if no streak)
 * @param onStartReview Callback when user taps "Review" button
 * @param modifier Modifier for the component
 */
@Composable
fun ReviewBanner(
    currentStreak: Int,
    onStartReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Time to review your week!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (currentStreak > 0) {
                    Text(
                        text = "Current streak: $currentStreak week${if (currentStreak != 1) "s" else ""} 🔥",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Button(
                onClick = onStartReview,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Review")
            }
        }
    }
}
