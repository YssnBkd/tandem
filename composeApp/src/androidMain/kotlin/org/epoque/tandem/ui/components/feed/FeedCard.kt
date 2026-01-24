package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
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
import org.epoque.tandem.domain.model.FeedActor
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemSecondary
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight
import org.epoque.tandem.ui.theme.TandemTertiary

// AI Purple color from spec
val AiPurple = Color(0xFF7C3AED)
val AiPurpleContainer = Color(0xFFF3E8FF)

// Unread indicator blue
val UnreadBlue = Color(0xFF246FE0)

// Message blue (iOS system blue) - used for message type icon
val MessageBlue = Color(0xFF007AFF)

/**
 * Base card container for feed items.
 * Provides consistent styling with optional header, body, and footer slots.
 */
@Composable
fun FeedCard(
    modifier: Modifier = Modifier,
    isUnread: Boolean = false,
    borderColor: Color = TandemOutlineLight,
    borderWidth: Int = 1,
    backgroundGradient: Brush? = null,
    onClick: (() -> Unit)? = null,
    header: @Composable () -> Unit = {},
    body: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = TandemShapes.Card.default,
        color = if (backgroundGradient != null) Color.Transparent else TandemSurfaceLight,
        border = BorderStroke(borderWidth.dp, borderColor),
        modifier = modifier
    ) {
        Box(
            modifier = if (backgroundGradient != null) {
                Modifier.background(backgroundGradient)
            } else Modifier
        ) {
            Column {
                // Header
                Box(modifier = Modifier.padding(TandemSpacing.sm, TandemSpacing.sm, TandemSpacing.sm, TandemSpacing.sm)) {
                    header()
                }

                // Body (optional)
                if (body != null) {
                    HorizontalDivider(color = TandemOutlineLight, thickness = 1.dp)
                    Box(modifier = Modifier.padding(TandemSpacing.xs, TandemSpacing.xs)) {
                        body()
                    }
                }

                // Footer (optional)
                if (footer != null) {
                    HorizontalDivider(color = TandemOutlineLight, thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(TandemSpacing.xs, TandemSpacing.xs)
                    ) {
                        footer()
                    }
                }
            }

            // Unread indicator dot
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(TandemSpacing.xs)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(UnreadBlue)
                )
            }
        }
    }
}

/**
 * Card header with avatar, actor info, and timestamp.
 *
 * @param typeIcon Optional icon to display in avatar area instead of initials (for type differentiation)
 * @param typeIconTint Tint color for the type icon
 */
@Composable
fun FeedCardHeader(
    actorName: String,
    actorType: FeedActor.ActorType,
    actorInitial: String,
    timeLabel: String,
    actionLabel: String? = null,
    typeIcon: (@Composable () -> Unit)? = null,
    customAvatar: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar or type icon
        when {
            customAvatar != null -> customAvatar()
            typeIcon != null -> typeIcon()
            else -> FeedAvatar(
                initial = actorInitial,
                actorType = actorType
            )
        }

        // Actor info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actorName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TandemOnSurfaceLight
                )

                if (actionLabel != null) {
                    Text(
                        text = actionLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = TandemOnSurfaceVariantLight
                    )
                }
            }

            Text(
                text = timeLabel,
                fontSize = 12.sp,
                color = TandemOnSurfaceVariantLight,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Avatar component for feed cards.
 */
@Composable
fun FeedAvatar(
    initial: String,
    actorType: FeedActor.ActorType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (actorType) {
        FeedActor.ActorType.SELF -> TandemPrimary
        FeedActor.ActorType.PARTNER -> TandemTertiary
        FeedActor.ActorType.SYSTEM -> TandemSecondary
        FeedActor.ActorType.AI -> AiPurple
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * Type icon avatar for feed cards (iOS HIG pattern).
 * Shows an icon instead of initials to indicate content type.
 */
@Composable
fun FeedTypeIcon(
    icon: @Composable () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
