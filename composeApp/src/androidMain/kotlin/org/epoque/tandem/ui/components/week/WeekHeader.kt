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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.TandemOnPrimaryContainer
import org.epoque.tandem.ui.theme.TandemPrimaryContainer

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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Subtitle (date range · task count)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Season context chip
        if (seasonInfo != null) {
            Spacer(modifier = Modifier.height(8.dp))
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
            .clip(RoundedCornerShape(50))
            .background(TandemPrimaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = TandemOnPrimaryContainer
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View season",
            modifier = Modifier.size(12.dp),
            tint = TandemOnPrimaryContainer
        )
    }
}
