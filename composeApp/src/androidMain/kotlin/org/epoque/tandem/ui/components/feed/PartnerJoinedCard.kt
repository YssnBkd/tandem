package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.presentation.feed.model.FeedUiItem
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemPrimaryContainer
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight
import org.epoque.tandem.ui.theme.TandemTertiary

/**
 * Special celebration card when partner joins.
 * Features gradient background and celebratory styling.
 */
@Composable
fun PartnerJoinedCard(
    item: FeedUiItem.PartnerJoined,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(TandemPrimaryContainer, TandemSurfaceLight)
    )

    FeedCard(
        modifier = modifier,
        isUnread = !item.isRead,
        borderColor = TandemPrimary,
        borderWidth = 2,
        backgroundGradient = gradient,
        header = {
            FeedCardHeader(
                actorName = item.actorName,
                actorType = item.actorType,
                actorInitial = item.actorInitial,
                timeLabel = item.timeLabel,
                actionLabel = item.actionLabel,
                customAvatar = {
                    // Heart icon avatar for partner joining
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TandemTertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
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
                    .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TandemSpacing.xs)
            ) {
                Text(
                    text = "🎉",
                    fontSize = 32.sp
                )

                Text(
                    text = "You're now connected!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TandemOnSurfaceLight,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Start collaborating on tasks together",
                    fontSize = 13.sp,
                    color = TandemOnSurfaceVariantLight,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}
