package org.epoque.tandem.ui.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Banner prompting users to start weekly planning.
 *
 * Following Material Design 3 best practices:
 * - Uses primaryContainer for attention-grabbing surface
 * - Clear call-to-action button
 * - 48dp+ touch target on button
 * - Accessible content descriptions
 *
 * Shows on Sunday after 6pm until planning is completed.
 *
 * @param onStartPlanning Callback when user taps Start button
 * @param modifier Modifier for customization
 */
@Composable
fun PlanningBanner(
    onStartPlanning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plan your week",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onStartPlanning,
                modifier = Modifier.semantics {
                    contentDescription = "Start planning your week"
                }
            ) {
                Text(text = "Start")
            }
        }
    }
}

/**
 * Determines if the planning banner should be shown.
 *
 * The banner appears when:
 * 1. Planning has not been completed for the current week
 * 2. It's Sunday after 6pm local time
 *
 * @param isPlanningComplete Whether planning has been completed for the current week
 * @return true if the banner should be shown
 */
fun shouldShowPlanningBanner(isPlanningComplete: Boolean): Boolean {
    // Already completed planning this week
    if (isPlanningComplete) return false

    // Check if it's Sunday after 6pm
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())

    val isSunday = localDateTime.dayOfWeek == DayOfWeek.SUNDAY
    val isAfter6pm = localDateTime.hour >= 18

    return isSunday && isAfter6pm
}
