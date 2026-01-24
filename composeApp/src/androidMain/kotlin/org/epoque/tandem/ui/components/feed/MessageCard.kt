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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.domain.model.FeedActor
import org.epoque.tandem.presentation.feed.model.FeedUiItem
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight
import org.epoque.tandem.ui.theme.TandemTertiary

/**
 * Feed card for messages between partners.
 * Avatar is OUTSIDE the bubble to differentiate from other card types.
 * Square corner on top points toward the avatar:
 * - Top-right square + avatar on right = sent by user (SELF)
 * - Top-left square + avatar on left = received from partner (PARTNER)
 */
@Composable
fun MessageCard(
    item: FeedUiItem.Message,
    modifier: Modifier = Modifier
) {
    val isSentByUser = item.actorType == FeedActor.ActorType.SELF
    val bubbleShape = MessageBubbleShape(squareCornerOnRight = isSentByUser)

    // Bubble background color - subtle tint based on sender
    val bubbleColor = if (isSentByUser) {
        MessageBlue.copy(alpha = 0.08f)
    } else {
        TandemSurfaceLight
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Avatar on left for received messages
        if (!isSentByUser) {
            Avatar(
                initial = item.actorInitial,
                color = TandemTertiary,
                modifier = Modifier.padding(end = TandemSpacing.xs)
            )
        }

        // Message bubble
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Column(
                modifier = Modifier.padding(TandemSpacing.sm)
            ) {
                // Header: name and time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.actorName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TandemOnSurfaceLight
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.timeLabel,
                            fontSize = 11.sp,
                            color = TandemOnSurfaceVariantLight
                        )
                        // Unread indicator
                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(UnreadBlue)
                            )
                        }
                    }
                }

                // Message text
                Text(
                    text = item.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TandemOnSurfaceLight,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = TandemSpacing.xs)
                )
            }
        }

        // Avatar on right for sent messages
        if (isSentByUser) {
            Avatar(
                initial = item.actorInitial,
                color = TandemPrimary,
                modifier = Modifier.padding(start = TandemSpacing.xs)
            )
        }
    }
}

@Composable
private fun Avatar(
    initial: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * Simple message bubble shape: 3 rounded corners, 1 square corner on top.
 * Square corner indicates sender direction.
 *
 * @param squareCornerOnRight If true, top-right is square (sent by user).
 *                            If false, top-left is square (received from partner).
 */
class MessageBubbleShape(
    private val squareCornerOnRight: Boolean
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = androidx.compose.ui.graphics.Outline.Generic(
        path = Path().apply {
            val r = 16f * density.density // corner radius

            if (squareCornerOnRight) {
                // Sent: square top-right, round others
                moveTo(r, 0f)
                lineTo(size.width, 0f) // straight to top-right corner (square)
                lineTo(size.width, size.height - r)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height),
                    startAngleDegrees = 0f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
                lineTo(r, size.height)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(0f, size.height - 2 * r, 2 * r, size.height),
                    startAngleDegrees = 90f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
                lineTo(0f, r)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(0f, 0f, 2 * r, 2 * r),
                    startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
            } else {
                // Received: square top-left, round others
                moveTo(0f, 0f) // start at top-left corner (square)
                lineTo(size.width - r, 0f)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(size.width - 2 * r, 0f, size.width, 2 * r),
                    startAngleDegrees = -90f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
                lineTo(size.width, size.height - r)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height),
                    startAngleDegrees = 0f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
                lineTo(r, size.height)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(0f, size.height - 2 * r, 2 * r, size.height),
                    startAngleDegrees = 90f, sweepAngleDegrees = 90f, forceMoveTo = false
                )
                lineTo(0f, 0f) // back to top-left (square)
            }
            close()
        }
    )
}
