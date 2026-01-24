package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 */
@Composable
fun StickyDayHeader(
    dayLabel: String,
    dateLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TandemBackgroundLight,
        border = BorderStroke(1.dp, TandemOutlineLight),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
}
