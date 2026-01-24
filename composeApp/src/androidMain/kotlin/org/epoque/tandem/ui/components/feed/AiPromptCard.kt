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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.presentation.feed.model.FeedUiItem
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight

/**
 * AI prompt card for nudging users to plan their week.
 * Features purple accent color and dismiss capability.
 */
@Composable
fun AiPromptCard(
    item: FeedUiItem.AiPlanPrompt,
    onActionClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(AiPurpleContainer, TandemSurfaceLight)
    )

    FeedCard(
        modifier = modifier,
        isUnread = !item.isRead,
        borderColor = AiPurple,
        borderWidth = 2,
        backgroundGradient = gradient,
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                FeedCardHeader(
                    actorName = "Tandem AI",
                    actorType = item.actorType,
                    actorInitial = "AI",
                    timeLabel = item.timeLabel,
                    actionLabel = null,
                    customAvatar = {
                        // AI avatar with purple background
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AiPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 16.sp
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Dismiss button
                IconButton(
                    onClick = onDismissClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TandemOnSurfaceVariantLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        body = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.xs)
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TandemOnSurfaceLight
                )

                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TandemOnSurfaceVariantLight,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = TandemSpacing.xxs)
                )
            }
        },
        footer = {
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AiPurple,
                    contentColor = Color.White
                ),
                shape = TandemShapes.Button.rounded,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.buttonLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

/**
 * AI prompt card for nudging users to review their week.
 */
@Composable
fun AiPromptCard(
    item: FeedUiItem.AiReviewPrompt,
    onActionClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(AiPurpleContainer, TandemSurfaceLight)
    )

    FeedCard(
        modifier = modifier,
        isUnread = !item.isRead,
        borderColor = AiPurple,
        borderWidth = 2,
        backgroundGradient = gradient,
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                FeedCardHeader(
                    actorName = "Tandem AI",
                    actorType = item.actorType,
                    actorInitial = "AI",
                    timeLabel = item.timeLabel,
                    actionLabel = null,
                    customAvatar = {
                        // AI avatar with purple background
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AiPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 16.sp
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Dismiss button
                IconButton(
                    onClick = onDismissClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TandemOnSurfaceVariantLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        body = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.xs)
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TandemOnSurfaceLight
                )

                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TandemOnSurfaceVariantLight,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = TandemSpacing.xxs)
                )
            }
        },
        footer = {
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AiPurple,
                    contentColor = Color.White
                ),
                shape = TandemShapes.Button.rounded,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.buttonLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
