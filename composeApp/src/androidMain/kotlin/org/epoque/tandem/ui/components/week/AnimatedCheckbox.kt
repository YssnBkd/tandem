package org.epoque.tandem.ui.components.week

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Animated checkbox with spring animation on state change.
 *
 * Following Android animation best practices:
 * - Uses animateFloatAsState for single value animation
 * - Spring animation with medium bouncy damping for satisfying feel
 * - Scale transformation for visual feedback
 * - Brief "pop" animation when checked
 *
 * Based on best practices from:
 * https://developer.android.com/develop/ui/compose/animation/introduction
 */
@Composable
fun AnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Track when animation should trigger
    var animationTrigger by remember { mutableStateOf(false) }

    // Animate scale with spring physics
    val scale by animateFloatAsState(
        targetValue = if (animationTrigger) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkbox_scale"
    )

    // Trigger animation when checked state changes to true
    LaunchedEffect(checked) {
        if (checked) {
            animationTrigger = true
            delay(150) // Duration of the "pop"
            animationTrigger = false
        }
    }

    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier
            .size(40.dp) // Ensures minimum touch target
            .scale(scale),
        colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
