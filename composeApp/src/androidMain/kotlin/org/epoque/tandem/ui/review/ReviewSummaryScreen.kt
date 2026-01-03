package org.epoque.tandem.ui.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Review summary screen - Step 3 (final) of the review wizard.
 *
 * Shows completion statistics and streak information,
 * providing encouragement and next actions.
 *
 * Requirements:
 * - FR-026: Completion percentage (Done only = complete)
 * - FR-027: Current streak with message
 * - FR-028: "Start Next Week" → Planning
 * - FR-029: "Done" → Close review
 *
 * @param completionPercentage Percentage of tasks completed (0-100)
 * @param doneCount Number of tasks marked as Done
 * @param triedCount Number of tasks marked as Tried
 * @param skippedCount Number of tasks marked as Skipped
 * @param totalTasks Total number of tasks reviewed
 * @param currentStreak Current streak in weeks
 * @param onStartNextWeek Callback when user taps "Start Next Week"
 * @param onDone Callback when user taps "Done"
 */
@Composable
fun ReviewSummaryScreen(
    completionPercentage: Int,
    doneCount: Int,
    triedCount: Int,
    skippedCount: Int,
    totalTasks: Int,
    currentStreak: Int,
    onStartNextWeek: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate progress bar
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    LaunchedEffect(completionPercentage) {
        targetProgress = completionPercentage / 100f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Week Complete!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Completion percentage with animated progress bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${completionPercentage}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "completed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stats breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                count = doneCount,
                label = "Done",
                emoji = "✓"
            )
            StatItem(
                count = triedCount,
                label = "Tried",
                emoji = "~"
            )
            StatItem(
                count = skippedCount,
                label = "Skipped",
                emoji = "○"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Streak display with encouragement
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔥",
                    fontSize = 32.sp
                )
                Text(
                    text = "$currentStreak week${if (currentStreak != 1) "s" else ""}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getStreakMessage(currentStreak),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary: Start Next Week
            Button(
                onClick = onStartNextWeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = "Start Next Week",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Secondary: Done
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Individual stat item in the breakdown row.
 */
@Composable
private fun StatItem(
    count: Int,
    label: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Get encouraging message based on streak count.
 * Following "Celebration Over Judgment" principle.
 */
private fun getStreakMessage(streak: Int): String {
    return when {
        streak == 0 -> "Start your streak!"
        streak in 1..3 -> "Keep it going!"
        streak in 4..7 -> "Amazing consistency!"
        streak in 8..12 -> "You're on fire!"
        else -> "Incredible dedication!"
    }
}
