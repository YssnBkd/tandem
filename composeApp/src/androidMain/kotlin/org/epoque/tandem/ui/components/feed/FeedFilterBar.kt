package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.feed.model.FeedFilter
import org.epoque.tandem.ui.theme.TandemBackgroundLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemPrimaryContainer
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight
import org.epoque.tandem.ui.theme.TandemTextStyles

/**
 * Filter bar showing filter chips for All, Tasks, Messages.
 */
@Composable
fun FeedFilterBar(
    activeFilter: FeedFilter,
    onFilterSelected: (FeedFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = TandemOutlineLight
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TandemBackgroundLight)
            .drawBehind {
                // Draw only bottom border to avoid double-border with StickyDayHeader
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(
                horizontal = TandemSpacing.Screen.horizontalPadding,
                vertical = TandemSpacing.xs
            ),
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChipItem(
            label = "All",
            isSelected = activeFilter == FeedFilter.ALL,
            showCheckIcon = true,
            onClick = { onFilterSelected(FeedFilter.ALL) }
        )

        FilterChipItem(
            label = "Tasks",
            isSelected = activeFilter == FeedFilter.TASKS,
            showCheckIcon = false,
            onClick = { onFilterSelected(FeedFilter.TASKS) }
        )

        FilterChipItem(
            label = "Messages",
            isSelected = activeFilter == FeedFilter.MESSAGES,
            showCheckIcon = false,
            onClick = { onFilterSelected(FeedFilter.MESSAGES) }
        )
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    showCheckIcon: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = TandemTextStyles.Label.small.copy(
                    color = if (isSelected) TandemPrimary else TandemOnSurfaceVariantLight
                )
            )
        },
        leadingIcon = if (isSelected && showCheckIcon) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = TandemPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else null,
        shape = TandemShapes.Chip.default,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = TandemSurfaceLight,
            selectedContainerColor = TandemPrimaryContainer,
            labelColor = TandemOnSurfaceVariantLight,
            selectedLabelColor = TandemPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = TandemOutlineLight,
            selectedBorderColor = TandemPrimary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        ),
        modifier = modifier
    )
}
