package org.epoque.tandem.ui.components.week

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.ui.theme.TandemSizing
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.theme.PriorityP1Light
import org.epoque.tandem.ui.theme.PriorityP2
import org.epoque.tandem.ui.theme.PriorityP2Light
import org.epoque.tandem.ui.theme.PriorityP3
import org.epoque.tandem.ui.theme.PriorityP3Light
import org.epoque.tandem.ui.theme.PriorityP4
import org.epoque.tandem.ui.theme.TandemOutlineLight

/**
 * A circular checkbox that displays task priority with colored borders.
 * Follows Todoist-style design:
 * - P1: Red border with light red fill
 * - P2: Orange border with light orange fill
 * - P3: Blue border with light blue fill
 * - P4: Gray border, transparent fill
 * - Completed: Gray fill with white checkmark (all priorities)
 *
 * Features haptic feedback and spring animation on check.
 */
@Composable
fun PriorityCheckbox(
    checked: Boolean,
    priority: TaskPriority,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = TandemSizing.Checkbox.visualSize,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    // Get colors for priority
    val priorityColor = priority.toColor()
    val priorityLightColor = priority.toLightColor()

    // Animate scale on check
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkbox_scale"
    )

    // Completed state uses gray, uncompleted uses priority colors
    val backgroundColor by animateColorAsState(
        targetValue = when {
            checked -> TandemOutlineLight
            else -> priorityLightColor
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "checkbox_background"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            checked -> TandemOutlineLight
            else -> priorityColor
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "checkbox_border"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(color = backgroundColor)
            .border(
                width = TandemSizing.Border.emphasis,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                modifier = Modifier.size(size * 0.6f),
                tint = Color.White
            )
        }
    }
}

/**
 * A larger variant of PriorityCheckbox for task detail view.
 */
@Composable
fun LargePriorityCheckbox(
    checked: Boolean,
    priority: TaskPriority,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    PriorityCheckbox(
        checked = checked,
        priority = priority,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        size = TandemSizing.Checkbox.visualSizeLarge,
        enabled = enabled
    )
}

/**
 * Extension to get the main color for a priority.
 */
fun TaskPriority.toColor(): Color = when (this) {
    TaskPriority.P1 -> PriorityP1
    TaskPriority.P2 -> PriorityP2
    TaskPriority.P3 -> PriorityP3
    TaskPriority.P4 -> PriorityP4
}

/**
 * Extension to get the light background color for a priority.
 */
fun TaskPriority.toLightColor(): Color = when (this) {
    TaskPriority.P1 -> PriorityP1Light
    TaskPriority.P2 -> PriorityP2Light
    TaskPriority.P3 -> PriorityP3Light
    TaskPriority.P4 -> Color.Transparent
}
