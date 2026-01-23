package org.epoque.tandem.ui.components.week

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.CoralPrimary
import org.epoque.tandem.ui.theme.StreakEnd
import org.epoque.tandem.ui.theme.StreakStart
import org.epoque.tandem.ui.theme.TodayBlue

/**
 * Banner type for plan/review navigation.
 */
enum class BannerType {
    PLAN_WEEK,
    REVIEW_WEEK
}

/**
 * Navigation banner for Plan Week or Review Week actions.
 * Shows gradient background with icon and call-to-action.
 */
@Composable
fun PlanReviewBanner(
    type: BannerType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, title, subtitle, gradientColors) = when (type) {
        BannerType.PLAN_WEEK -> Quadruple(
            Icons.Outlined.CalendarMonth,
            "Plan your week",
            "Set intentions and priorities",
            listOf(TodayBlue.copy(alpha = 0.15f), TodayBlue.copy(alpha = 0.05f))
        )
        BannerType.REVIEW_WEEK -> Quadruple(
            Icons.Outlined.RateReview,
            "Review your week",
            "Reflect on your progress",
            listOf(StreakStart.copy(alpha = 0.15f), StreakEnd.copy(alpha = 0.05f))
        )
    }

    val accentColor = when (type) {
        BannerType.PLAN_WEEK -> TodayBlue
        BannerType.REVIEW_WEEK -> CoralPrimary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(gradientColors)
            )
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go",
                modifier = Modifier.size(20.dp),
                tint = accentColor
            )
        }
    }
}

/**
 * Simple quadruple data class for destructuring.
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
