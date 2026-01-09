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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemPrimaryContainer

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
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous week arrow
        IconButton(
            onClick = onPreviousWeek,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous week",
                modifier = Modifier.size(20.dp),
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
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next week",
                modifier = Modifier.size(20.dp),
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
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day abbreviation (SUN, MON, etc.)
        Text(
            text = day.dayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = dayNameColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Day number
        Text(
            text = day.dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (day.isSelected || day.isToday) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 16.sp
            ),
            color = dayNumberColor,
            textAlign = TextAlign.Center
        )

        // Task indicator dot
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(color = dotColor, shape = CircleShape)
        )
    }
}
