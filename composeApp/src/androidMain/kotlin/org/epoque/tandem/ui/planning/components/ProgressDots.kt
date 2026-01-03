package org.epoque.tandem.ui.planning.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Progress indicator showing current step in the planning wizard.
 *
 * Following Material Design 3 best practices:
 * - Uses primary color for active step, outline for inactive
 * - Spring animation for smooth transitions
 * - Semantic descriptions for accessibility
 *
 * @param totalSteps Total number of steps in the wizard
 * @param currentStep Current step index (0-based)
 * @param modifier Modifier for customization
 */
@Composable
fun ProgressDots(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "Step ${currentStep + 1} of $totalSteps"
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            ProgressDot(
                isActive = index <= currentStep,
                isCurrent = index == currentStep
            )
        }
    }
}

/**
 * Individual dot in the progress indicator.
 *
 * @param isActive Whether this step has been completed or is current
 * @param isCurrent Whether this is the currently active step
 */
@Composable
private fun ProgressDot(
    isActive: Boolean,
    isCurrent: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dot_scale"
    )

    val color by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        label = "dot_color"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}
