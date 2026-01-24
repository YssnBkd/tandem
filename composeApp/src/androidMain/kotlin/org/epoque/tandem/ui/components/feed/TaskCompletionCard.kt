package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.presentation.feed.model.FeedUiItem
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTertiary

/**
 * Feed card for completed tasks.
 * Shows completed task with strikethrough text and optional notification indicator.
 */
@Composable
fun TaskCompletionCard(
    item: FeedUiItem.TaskCompleted,
    onCardClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FeedCard(
        modifier = modifier,
        isUnread = !item.isRead,
        onClick = onCardClick,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(TandemSpacing.sm),
                verticalAlignment = Alignment.Top
            ) {
                // Completed checkbox
                CompletedCheckbox(
                    onClick = onCheckboxClick,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Task content
                Column(modifier = Modifier.weight(1f)) {
                    // Task title with strikethrough
                    Text(
                        text = item.taskTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TandemOnSurfaceVariantLight.copy(alpha = 0.6f),
                        textDecoration = TextDecoration.LineThrough
                    )

                    // Notification indicator if partner notified
                    if (item.showNotificationIndicator) {
                        Row(
                            modifier = Modifier.padding(top = TandemSpacing.xxs),
                            horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xxs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = TandemTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Notified you",
                                fontSize = 11.sp,
                                color = TandemTertiary
                            )
                        }
                    }
                }
            }
        }
    )
}

/**
 * Completed checkbox with gray fill and white checkmark.
 */
@Composable
private fun CompletedCheckbox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(TandemOutlineLight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Completed",
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
    }
}
