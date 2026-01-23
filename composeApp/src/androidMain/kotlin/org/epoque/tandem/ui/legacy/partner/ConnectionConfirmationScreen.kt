package org.epoque.tandem.ui.legacy.partner

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
import androidx.compose.material.icons.filled.Favorite
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
 * Confirmation screen shown after successfully connecting with a partner.
 *
 * Following Material Design 3 best practices:
 * - Success icon with spring animation
 * - Clear partner connection confirmation
 * - Primary action button with 48dp+ height
 * - Accessible content descriptions
 *
 * @param partnerName The name of the connected partner
 * @param onPlanWeek Callback when user taps Plan Your Week button
 * @param onDone Callback when user taps Done button
 * @param modifier Modifier for customization
 */
@Composable
fun ConnectionConfirmationScreen(
    partnerName: String,
    onPlanWeek: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate the heart icon scale
    var animationTriggered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heart_scale"
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
        // Heart icon with animation
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Connected",
            modifier = Modifier
                .size(96.dp)
                .scale(scale),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main headline
        Text(
            text = "You're connected with $partnerName!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Now you can plan your weeks together, share tasks, and support each other's goals.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Plan Your Week button (primary action)
        Button(
            onClick = onPlanWeek,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    contentDescription = "Start planning your week"
                }
        ) {
            Text(
                text = "Plan Your Week",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Done button (secondary action)
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    contentDescription = "Return to home"
                }
        ) {
            Text(
                text = "Go to Home",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
