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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
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
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemSecondary
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTertiary

/**
 * Feed card for week planning events.
 * Shows when user or partner completes their weekly planning.
 */
@Composable
fun WeekEventCard(
    item: FeedUiItem.WeekPlanned,
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
                actionLabel = item.actionLabel,
                customAvatar = {
                    // Calendar icon avatar for planning events
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TandemSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        },
        body = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.xs)
            ) {
                Text(
                    text = item.weekLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TandemOnSurfaceLight
                )

                if (item.taskCount > 0) {
                    Text(
                        text = "${item.taskCount} tasks planned",
                        fontSize = 13.sp,
                        color = TandemOnSurfaceVariantLight,
                        modifier = Modifier.padding(top = TandemSpacing.xxs)
                    )
                }
            }
        }
    )
}

/**
 * Feed card for week review events.
 * Shows when user or partner completes their weekly review.
 */
@Composable
fun WeekEventCard(
    item: FeedUiItem.WeekReviewed,
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
                actionLabel = item.actionLabel,
                customAvatar = {
                    // Star icon avatar for review events
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TandemTertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        },
        body = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.xs)
            ) {
                Text(
                    text = item.weekLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TandemOnSurfaceLight
                )

                Text(
                    text = "Week review completed",
                    fontSize = 13.sp,
                    color = TandemOnSurfaceVariantLight,
                    modifier = Modifier.padding(top = TandemSpacing.xxs)
                )
            }
        }
    )
}
