package org.epoque.tandem.ui.legacy.review.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Progress indicator showing current position in task review sequence.
 *
 * Visual states:
 * - Filled dot: Completed task
 * - Current dot: Larger with highlight color
 * - Outlined dot: Remaining tasks
 *
 * Note: This is display-only - touch targets are not needed.
 *
 * @param currentIndex Zero-based index of current task
 * @param totalCount Total number of tasks to review
 * @param modifier Modifier for the component
 */
@Composable
fun ReviewProgressDots(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    // Don't render if no tasks
    if (totalCount == 0) return

    // Limit visible dots to prevent overflow (show 10 max with ellipsis indication)
    val maxVisibleDots = 10
    val showAllDots = totalCount <= maxVisibleDots

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showAllDots) {
            // Show all dots
            repeat(totalCount) { index ->
                ProgressDot(
                    isCompleted = index < currentIndex,
                    isCurrent = index == currentIndex,
                    isRemaining = index > currentIndex
                )
            }
        } else {
            // Show abbreviated version for many tasks
            // First few dots + current position + last few
            val showStart = 2
            val showEnd = 2

            // Start dots
            repeat(showStart.coerceAtMost(totalCount)) { index ->
                ProgressDot(
                    isCompleted = index < currentIndex,
                    isCurrent = index == currentIndex,
                    isRemaining = index > currentIndex
                )
            }

            // Middle section - show current area if not at extremes
            if (currentIndex > showStart && currentIndex < totalCount - showEnd - 1) {
                // Ellipsis before current
                EllipsisDots()

                // Current and neighbors
                val startShow = (currentIndex - 1).coerceAtLeast(showStart)
                val endShow = (currentIndex + 1).coerceAtMost(totalCount - showEnd - 1)
                for (index in startShow..endShow) {
                    if (index >= showStart && index < totalCount - showEnd) {
                        ProgressDot(
                            isCompleted = index < currentIndex,
                            isCurrent = index == currentIndex,
                            isRemaining = index > currentIndex
                        )
                    }
                }

                // Ellipsis after current
                EllipsisDots()
            } else {
                // Just show ellipsis in middle
                EllipsisDots()
            }

            // End dots
            for (index in (totalCount - showEnd).coerceAtLeast(showStart) until totalCount) {
                ProgressDot(
                    isCompleted = index < currentIndex,
                    isCurrent = index == currentIndex,
                    isRemaining = index > currentIndex
                )
            }
        }
    }
}

/**
 * Individual progress dot with three states.
 */
@Composable
private fun ProgressDot(
    isCompleted: Boolean,
    isCurrent: Boolean,
    isRemaining: Boolean
) {
    val size = if (isCurrent) 12.dp else 8.dp
    val color = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isRemaining) MaterialTheme.colorScheme.surface else color)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
    )
}

/**
 * Ellipsis indicator for abbreviated progress display.
 */
@Composable
private fun EllipsisDots() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    }
}
