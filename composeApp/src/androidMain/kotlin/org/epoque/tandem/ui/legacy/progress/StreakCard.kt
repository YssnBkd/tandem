package org.epoque.tandem.ui.legacy.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.progress.StreakMilestones

/**
 * Streak display card with flame icon, streak count, and partner indicator.
 *
 * Shows the current streak count with visual emphasis and optional
 * milestone celebration overlay.
 */
@Composable
fun StreakCard(
    streakCount: Int,
    isPartnerStreak: Boolean,
    showCelebration: Boolean,
    milestoneValue: Int?,
    onDismissCelebration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streakText = if (isPartnerStreak) {
        "$streakCount-week streak together"
    } else {
        "$streakCount-week streak"
    }

    val progressMessage = StreakMilestones.getProgressMessage(streakCount)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = streakText
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(48.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = streakCount.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = if (isPartnerStreak) "week streak together" else "week streak",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Progress to next milestone
            if (progressMessage != null && streakCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = progressMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Milestone celebration overlay
        AnimatedVisibility(
            visible = showCelebration && milestoneValue != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (milestoneValue != null) {
                MilestoneCelebration(
                    milestoneValue = milestoneValue,
                    isPartnerStreak = isPartnerStreak,
                    onDismiss = onDismissCelebration
                )
            }
        }
    }
}
