package org.epoque.tandem.ui.components.week

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemPrimaryContainer
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSizing
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTextStyles
import org.epoque.tandem.ui.theme.TandemTypography

/**
 * Day item for the week selector.
 */
data class WeekDayItem(
    val dayName: String,    // e.g., "SUN", "MON"
    val dayNumber: Int,     // e.g., 5, 6, 7
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val hasTasks: Boolean = false
)

/**
 * Week day selector strip with navigation arrows.
 * Shows 7 days (Sun-Sat) with selection indicator and task dots.
 * Matches the Todoist-inspired mockup design.
 */
@Composable
fun WeekDaySelector(
    days: List<WeekDayItem>,
    onDaySelected: (Int) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TandemSpacing.xs, vertical = TandemSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous week arrow
        IconButton(
            onClick = onPreviousWeek,
            modifier = Modifier.size(TandemSizing.minTouchTarget)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous week",
                modifier = Modifier.size(TandemSizing.Icon.lg),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Day strip
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEachIndexed { index, day ->
                DayChip(
                    day = day,
                    onClick = { onDaySelected(index) }
                )
            }
        }

        // Next week arrow
        IconButton(
            onClick = onNextWeek,
            modifier = Modifier.size(TandemSizing.minTouchTarget)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next week",
                modifier = Modifier.size(TandemSizing.Icon.lg),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Individual day chip with selection state.
 */
@Composable
private fun DayChip(
    day: WeekDayItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            day.isSelected -> TandemPrimary
            day.isToday -> TandemPrimaryContainer
            else -> Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "day_background"
    )

    val dayNameColor by animateColorAsState(
        targetValue = when {
            day.isSelected -> Color.White.copy(alpha = 0.9f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "day_name_color"
    )

    val dayNumberColor by animateColorAsState(
        targetValue = when {
            day.isSelected -> Color.White
            day.isToday -> TandemPrimary
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "day_number_color"
    )

    val dotColor by animateColorAsState(
        targetValue = when {
            !day.hasTasks -> Color.Transparent
            day.isSelected -> Color.White.copy(alpha = 0.7f)
            else -> TandemPrimary
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "dot_color"
    )

    Column(
        modifier = modifier
            .clip(TandemShapes.md)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = TandemSpacing.xxs, vertical = TandemSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day abbreviation (SUN, MON, etc.)
        Text(
            text = day.dayName,
            style = TandemTextStyles.Label.tabBar.copy(fontWeight = FontWeight.Medium),
            color = dayNameColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(TandemSpacing.xxxs))

        // Day number
        Text(
            text = day.dayNumber.toString(),
            style = TandemTypography.titleLarge.copy(
                fontWeight = if (day.isSelected || day.isToday) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = dayNumberColor,
            textAlign = TextAlign.Center
        )

        // Task indicator dot
        Spacer(modifier = Modifier.height(TandemSpacing.xxxs))
        Box(
            modifier = Modifier
                .size(TandemSizing.Indicator.dot)
                .background(color = dotColor, shape = CircleShape)
        )
    }
}
