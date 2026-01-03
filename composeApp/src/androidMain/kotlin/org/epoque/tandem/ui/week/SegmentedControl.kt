package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.week.model.Segment

/**
 * Segmented control for switching between task views (You/Partner/Shared).
 *
 * Following Material 3 best practices:
 * - SingleChoiceSegmentedButtonRow for single-selection
 * - SegmentedButtonDefaults.itemShape() for proper corner radii
 * - Proper state management with selectedSegment parameter
 * - Accessibility semantics for screen readers
 * - Minimum touch target compliance
 *
 * Based on best practices from:
 * - https://developer.android.com/develop/ui/compose/components/segmented-button
 * - https://composables.com/material3/singlechoicesegmentedbuttonrow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedControl(
    selectedSegment: Segment,
    onSegmentSelected: (Segment) -> Unit,
    modifier: Modifier = Modifier
) {
    val segments = Segment.entries.toTypedArray()

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics {
                contentDescription = "Task view selector. Current selection: ${selectedSegment.displayName}"
            }
    ) {
        segments.forEachIndexed { index, segment ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = segments.size
                ),
                onClick = { onSegmentSelected(segment) },
                selected = segment == selectedSegment,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.semantics {
                    contentDescription = "${segment.displayName} tasks view"
                }
            ) {
                Text(
                    text = segment.displayName,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
