package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.TandemBackgroundLight
import org.epoque.tandem.ui.theme.TandemOnBackgroundLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemSpacing

/**
 * Sticky section header showing day and date.
 * Example: "Today" + "Thursday, Jan 23"
 *
 * Uses only a bottom border to avoid double-border effect
 * when stacked below FeedFilterBar (which has its own bottom border).
 */
@Composable
fun StickyDayHeader(
    dayLabel: String,
    dateLabel: String,
    modifier: Modifier = Modifier
) {
    val borderColor = TandemOutlineLight
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TandemBackgroundLight)
            .drawBehind {
                // Draw only bottom border to avoid double-border with FeedFilterBar
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(
                horizontal = TandemSpacing.Screen.horizontalPadding,
                vertical = TandemSpacing.sm
            ),
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TandemOnBackgroundLight
        )

        Text(
            text = dateLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TandemOnSurfaceVariantLight
        )
    }
}
