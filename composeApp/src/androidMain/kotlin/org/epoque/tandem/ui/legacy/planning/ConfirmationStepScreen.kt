package org.epoque.tandem.ui.legacy.planning

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Confirmation screen shown after completing the planning wizard.
 *
 * Following Material Design 3 best practices:
 * - Success icon with spring animation
 * - Clear summary of tasks planned
 * - Primary action button with 48dp+ height
 * - Accessible content descriptions
 *
 * @param totalTasksPlanned Total count of all tasks planned
 * @param rolloverTasksAdded Count of tasks rolled over from last week
 * @param newTasksCreated Count of new tasks created
 * @param partnerRequestsAccepted Count of partner requests accepted
 * @param onDone Callback when user taps Done button
 * @param onViewPartnerWeek Optional callback for future feature
 * @param modifier Modifier for customization
 */
@Composable
fun ConfirmationStepScreen(
    totalTasksPlanned: Int,
    rolloverTasksAdded: Int,
    newTasksCreated: Int,
    partnerRequestsAccepted: Int,
    onDone: () -> Unit,
    onViewPartnerWeek: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Animate the checkmark icon scale
    var animationTriggered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkmark_scale"
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success checkmark icon with animation
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Planning complete",
            modifier = Modifier
                .size(96.dp)
                .scale(scale),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main headline
        Text(
            text = when (totalTasksPlanned) {
                0 -> "Ready for the Week!"
                1 -> "1 Task Planned"
                else -> "$totalTasksPlanned Tasks Planned"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed breakdown (only show if there are tasks)
        if (totalTasksPlanned > 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (rolloverTasksAdded > 0) {
                    Text(
                        text = "$rolloverTasksAdded rolled over from last week",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (newTasksCreated > 0) {
                    Text(
                        text = "$newTasksCreated new tasks added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (partnerRequestsAccepted > 0) {
                    Text(
                        text = "$partnerRequestsAccepted partner requests accepted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Encouraging message for zero tasks
        if (totalTasksPlanned == 0) {
            Text(
                text = "Sometimes less is more. You can always add tasks later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Done button (primary action)
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    contentDescription = "Complete planning and return to week view"
                }
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Future feature: View Partner's Week button
        if (onViewPartnerWeek != null) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onViewPartnerWeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = "View partner's week"
                    }
            ) {
                Text(
                    text = "See Partner's Week",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
