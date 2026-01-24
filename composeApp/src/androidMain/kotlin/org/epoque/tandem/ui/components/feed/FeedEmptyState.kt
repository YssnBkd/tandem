package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.theme.TandemOnSurfaceLight
import org.epoque.tandem.ui.theme.TandemOnSurfaceVariantLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemSpacing

/**
 * Empty state shown when there are no feed items.
 * Different messages for no partner connected vs just no activity.
 *
 * Note: The feed works with or without a partner - it shows your own activity
 * and AI prompts regardless of partnership status.
 */
@Composable
fun FeedEmptyState(
    hasPartner: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(TandemSpacing.Screen.horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasPartner) {
            // Has partner but no activity yet
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = TandemOutlineLight,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(TandemSpacing.md))

            Text(
                text = "No activity yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TandemOnSurfaceLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(TandemSpacing.xs))

            Text(
                text = "Complete tasks, send messages, or plan your week together to see activity here",
                fontSize = 14.sp,
                color = TandemOnSurfaceVariantLight,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )
        } else {
            // No partner connected - but feed is still useful!
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = TandemPrimary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(TandemSpacing.md))

            Text(
                text = "Your activity feed",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TandemOnSurfaceLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(TandemSpacing.xs))

            Text(
                text = "Complete tasks, plan your week, and track your progress. Your activity will appear here.",
                fontSize = 14.sp,
                color = TandemOnSurfaceVariantLight,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(TandemSpacing.lg))

            Text(
                text = "Invite a partner anytime to share activity and collaborate together",
                fontSize = 13.sp,
                color = TandemOnSurfaceVariantLight.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}
