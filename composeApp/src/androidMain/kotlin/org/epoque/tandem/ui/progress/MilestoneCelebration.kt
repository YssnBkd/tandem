package org.epoque.tandem.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.epoque.tandem.presentation.progress.StreakMilestones

/**
 * Milestone celebration overlay/banner.
 *
 * Shows celebration message when user reaches a milestone.
 * Auto-dismisses after 3 seconds with visible dismiss button.
 * Triggers haptic feedback on display.
 */
@Composable
fun MilestoneCelebration(
    milestoneValue: Int,
    isPartnerStreak: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val celebrationMessage = StreakMilestones.getCelebrationMessage(milestoneValue, isPartnerStreak)

    // Trigger haptic feedback when celebration appears
    LaunchedEffect(milestoneValue) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Auto-dismiss after 3 seconds
    LaunchedEffect(milestoneValue) {
        delay(3000)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp), // Space for dismiss button
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = celebrationMessage,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You've reached a milestone!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }

        // Dismiss button - 48dp minimum touch target
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .semantics {
                    contentDescription = "Dismiss milestone celebration"
                }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
