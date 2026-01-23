package org.epoque.tandem.ui.components.week

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import org.epoque.tandem.ui.theme.TandemSizing
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTypography

/**
 * Collapsible section for completed tasks.
 * Shows "X completed" header that expands/collapses on tap.
 * Matches the Todoist-inspired mockup design.
 */
@Composable
fun CompletedSection(
    completedCount: Int,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (completedCount == 0) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Header row
        CompletedSectionHeader(
            count = completedCount,
            expanded = expanded,
            onClick = onExpandToggle
        )

        // Collapsible content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * Header for completed section showing count and expand indicator.
 * Chevron rotates 90 degrees when expanded.
 */
@Composable
private fun CompletedSectionHeader(
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "chevron_rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = TandemSpacing.List.itemHorizontalPadding,
                vertical = TandemSpacing.List.itemVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chevron icon
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = if (expanded) "Collapse" else "Expand",
            modifier = Modifier
                .size(TandemSizing.Icon.md)
                .rotate(rotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(TandemSpacing.xs))

        // Count text
        Text(
            text = "$count completed",
            style = TandemTypography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
