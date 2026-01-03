package org.epoque.tandem.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Mode selection screen - Entry point of the review wizard.
 *
 * Users choose between:
 * - Solo: Review their own tasks alone
 * - Together: Review with partner (P5 - deferred to v1.1)
 *
 * @param currentStreak Current streak in weeks
 * @param onSoloSelected Callback when user selects Solo mode
 * @param onTogetherSelected Callback when user selects Together mode
 */
@Composable
fun ReviewModeSelectionScreen(
    currentStreak: Int,
    onSoloSelected: () -> Unit,
    onTogetherSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = "How do you want to review?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Streak display
        if (currentStreak > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔥",
                    fontSize = 24.sp
                )
                Text(
                    text = "$currentStreak week${if (currentStreak != 1) "s" else ""} streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Mode selection buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Solo mode - primary action
            Button(
                onClick = onSoloSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Review Solo",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Reflect on your week alone",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Together mode - de-emphasized (P5 deferred)
            OutlinedButton(
                onClick = onTogetherSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Review Together",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Share with your partner",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
