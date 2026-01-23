package org.epoque.tandem.ui.components.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.screens.goals.GoalUiModel
import org.epoque.tandem.ui.theme.GoalCardBorder
import org.epoque.tandem.ui.theme.GoalIconBackground
import org.epoque.tandem.ui.theme.GoalPrimary
import org.epoque.tandem.ui.theme.GoalProgressBackground
import org.epoque.tandem.ui.theme.GoalTextMain
import org.epoque.tandem.ui.theme.GoalTextMuted

/**
 * Goal card component displaying goal info with progress.
 * Matches the HTML mockup design with warm terracotta accent colors.
 */
@Composable
fun GoalCard(
    goal: GoalUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: Icon + Goal info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Goal icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoalIconBackground, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goal.icon,
                        fontSize = 18.sp
                    )
                }

                // Goal info (ownership + name)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.ownershipLabel.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoalPrimary,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = goal.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoalTextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }

            // Progress section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(GoalProgressBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(goal.progressFraction.coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(GoalPrimary)
                    )
                }

                // Progress meta (text left, type right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = goal.progressText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoalTextMuted
                    )
                    Text(
                        text = goal.goalTypeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoalTextMuted
                    )
                }
            }
        }
    }
}
