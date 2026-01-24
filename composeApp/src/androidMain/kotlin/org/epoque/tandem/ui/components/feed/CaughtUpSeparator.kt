package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemSpacing

/**
 * Separator indicating the user is caught up with new items.
 * Shows "You're all caught up" text with horizontal lines.
 */
@Composable
fun CaughtUpSeparator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = TandemSpacing.Screen.horizontalPadding,
                vertical = TandemSpacing.md
            ),
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TandemOutlineLight
        )

        Text(
            text = "You're all caught up",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TandemOnSurfaceVariantLight
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TandemOutlineLight
        )
    }
}
