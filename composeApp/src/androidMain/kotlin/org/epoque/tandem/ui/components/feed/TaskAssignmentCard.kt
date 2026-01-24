package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.presentation.feed.model.FeedUiItem
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.theme.PriorityP1Light
import org.epoque.tandem.ui.theme.PriorityP2
import org.epoque.tandem.ui.theme.PriorityP2Light
import org.epoque.tandem.ui.theme.PriorityP3
import org.epoque.tandem.ui.theme.PriorityP3Light
import org.epoque.tandem.ui.theme.PriorityP4
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemPrimaryContainer
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight
import org.epoque.tandem.ui.theme.TandemSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemTertiary
import org.epoque.tandem.ui.theme.TandemTertiaryContainer

/**
 * Feed card for task assignments from partner.
 * Shows task with priority indicator, optional note, and Accept/Decline buttons.
 */
@Composable
fun TaskAssignmentCard(
    item: FeedUiItem.TaskAssigned,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(TandemTertiaryContainer, TandemSurfaceLight)
    )

    FeedCard(
        modifier = modifier,
        isUnread = !item.isRead,
        borderColor = TandemTertiary,
        borderWidth = 2,
        backgroundGradient = gradient,
        header = {
            FeedCardHeader(
                actorName = item.actorName,
                actorType = item.actorType,
                actorInitial = item.actorInitial,
                timeLabel = item.timeLabel,
                actionLabel = item.actionLabel
            )
        },
        body = {
            Column(
                modifier = Modifier.padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.xs)
            ) {
                // Task row with priority checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TandemSpacing.sm),
                    verticalAlignment = Alignment.Top
                ) {
                    PriorityCheckbox(priority = item.taskPriority)

                    Text(
                        text = item.taskTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TandemOnSurfaceLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Optional assignment note
                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = "\"${item.note}\"",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = TandemOnSurfaceVariantLight,
                        modifier = Modifier.padding(top = TandemSpacing.xs)
                    )
                }
            }
        },
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accept button
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TandemPrimaryContainer,
                        contentColor = TandemPrimary
                    ),
                    shape = TandemShapes.Button.rounded
                ) {
                    Text(
                        text = "Accept",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = TandemSpacing.xxs)
                            .size(16.dp)
                    )
                }

                // Decline button
                Button(
                    onClick = onDecline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TandemSurfaceVariantLight,
                        contentColor = TandemOnSurfaceVariantLight
                    ),
                    shape = TandemShapes.Button.rounded
                ) {
                    Text(
                        text = "Decline",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    )
}

/**
 * Priority-colored checkbox for pending tasks.
 */
@Composable
private fun PriorityCheckbox(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val (borderColor, backgroundColor) = when (priority) {
        TaskPriority.P1 -> PriorityP1 to PriorityP1Light
        TaskPriority.P2 -> PriorityP2 to PriorityP2Light
        TaskPriority.P3 -> PriorityP3 to PriorityP3Light
        TaskPriority.P4 -> PriorityP4 to Color.Transparent
    }

    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                Modifier.background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
            )
    ) {
        // Border ring
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(0.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        ) {
            // Draw border using a border modifier workaround
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                backgroundColor,
                                backgroundColor,
                                borderColor,
                                borderColor
                            ),
                            radius = 100f
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}
