package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.presentation.feed.model.FeedUiItem
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemSpacing

// Red color for declined tasks
private val DeclinedRed = Color(0xFFDC2626)

/**
 * Feed card for task acceptance responses.
 * Shows when partner accepts a task assignment.
 */
@Composable
fun TaskResponseCard(
    item: FeedUiItem.TaskAccepted,
    onCardClick: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Green checkmark indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(TandemPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accepted",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = item.taskTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TandemOnSurfaceLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}

/**
 * Feed card for task decline responses.
 * Shows when partner declines a task assignment.
 */
@Composable
fun TaskResponseCard(
    item: FeedUiItem.TaskDeclined,
    onCardClick: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Red X indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(DeclinedRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Declined",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = item.taskTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TandemOnSurfaceLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}
