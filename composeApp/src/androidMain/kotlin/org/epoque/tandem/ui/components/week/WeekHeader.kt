package org.epoque.tandem.ui.components.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import org.epoque.tandem.ui.theme.TandemOnPrimaryContainer
import org.epoque.tandem.ui.theme.TandemPrimaryContainer
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSizing
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTextStyles
import org.epoque.tandem.ui.theme.TandemTypography

/**
 * Week header showing title, subtitle, and Season context chip.
 * Matches the Todoist-inspired mockup design.
 */
@Composable
fun WeekHeader(
    title: String,
    subtitle: String,
    seasonInfo: String?,
    onSeasonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = TandemSpacing.Screen.horizontalPadding,
                vertical = TandemSpacing.sm
            )
    ) {
        // Title
        Text(
            text = title,
            style = TandemTextStyles.Title.section.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(TandemSpacing.xxxs))

        // Subtitle (date range · task count)
        Text(
            text = subtitle,
            style = TandemTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Season context chip
        if (seasonInfo != null) {
            Spacer(modifier = Modifier.height(TandemSpacing.xs))
            SeasonContextChip(
                text = seasonInfo,
                onClick = onSeasonClick
            )
        }
    }
}

/**
 * Season context chip showing current season info.
 * E.g., "🌱 Q1 2026 · Week 3 of 12"
 */
@Composable
private fun SeasonContextChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(TandemShapes.pill)
            .background(TandemPrimaryContainer)
            .clickable(onClick = onClick)
            .padding(
                horizontal = TandemSpacing.Chip.horizontalPadding,
                vertical = TandemSpacing.Chip.verticalPaddingCompact
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = TandemTextStyles.Label.small.copy(fontWeight = FontWeight.Medium),
            color = TandemOnPrimaryContainer
        )

        Spacer(modifier = Modifier.width(TandemSpacing.xxs))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View season",
            modifier = Modifier.size(TandemSizing.Icon.xs),
            tint = TandemOnPrimaryContainer
        )
    }
}
