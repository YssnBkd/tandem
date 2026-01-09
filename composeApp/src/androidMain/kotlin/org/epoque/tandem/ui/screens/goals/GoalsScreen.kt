package org.epoque.tandem.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.ui.components.SegmentedControl
import org.epoque.tandem.ui.components.goals.GoalCard
import org.epoque.tandem.ui.theme.GoalDashedBorder
import org.epoque.tandem.ui.theme.GoalPrimary
import org.epoque.tandem.ui.theme.GoalTextMain
import org.epoque.tandem.ui.theme.GoalTextMuted

/**
 * Goals screen showing goal list with Active/Completed tabs.
 * Uses mock data for visual iteration.
 */
@Composable
fun GoalsScreen(
    onNavigateToGoalDetail: (String) -> Unit = {},
    onNavigateToAddGoal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSegment by remember { mutableStateOf(GoalStatusSegment.ACTIVE) }

    // Mock data matching the HTML mockup
    val mockGoals = listOf(
        GoalUiModel(
            id = "1",
            icon = "\uD83E\uDDD8\u200D\u2640\uFE0F",
            name = "Morning Yoga",
            ownershipLabel = "Together",
            ownershipType = OwnershipType.TOGETHER,
            progressFraction = 0.66f,
            progressText = "2 of 3 this week",
            goalTypeLabel = "Habit"
        ),
        GoalUiModel(
            id = "2",
            icon = "\uD83C\uDFE1",
            name = "Dream Home Fund",
            ownershipLabel = "Together",
            ownershipType = OwnershipType.TOGETHER,
            progressFraction = 0.40f,
            progressText = "$2,000 / $5,000",
            goalTypeLabel = "Savings"
        ),
        GoalUiModel(
            id = "3",
            icon = "\u2708\uFE0F",
            name = "Italy Trip Planning",
            ownershipLabel = "For Partner",
            ownershipType = OwnershipType.PARTNER,
            progressFraction = 0.15f,
            progressText = "1 of 8 milestones",
            goalTypeLabel = "Project"
        )
    )

    // Filter goals by segment (for demo, all are "active")
    val displayedGoals = when (selectedSegment) {
        GoalStatusSegment.ACTIVE -> mockGoals
        GoalStatusSegment.COMPLETED -> emptyList()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFFFBF7)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            GoalsHeader(
                onAddClick = onNavigateToAddGoal
            )

            // Segment control
            SegmentedControl(
                segments = GoalStatusSegment.entries.map { it.displayName },
                selectedIndex = GoalStatusSegment.entries.indexOf(selectedSegment),
                onSegmentSelected = { selectedSegment = GoalStatusSegment.entries[it] },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Goals list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedGoals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onNavigateToGoalDetail(goal.id) }
                    )
                }

                // Add goal button
                item {
                    AddGoalButton(
                        onClick = onNavigateToAddGoal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Goals header with title and add button.
 */
@Composable
private fun GoalsHeader(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Our Goals",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GoalTextMain,
            letterSpacing = (-0.5).sp
        )

        // Add button with shadow
        Surface(
            modifier = Modifier
                .size(36.dp)
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clickable(onClick = onAddClick),
            shape = RoundedCornerShape(10.dp),
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add goal",
                    tint = GoalTextMain,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Add goal button with dashed border.
 */
@Composable
private fun AddGoalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Dashed border using Canvas
        Canvas(modifier = Modifier.matchParentSize()) {
            val dashWidth = 6.dp.toPx()
            val dashGap = 4.dp.toPx()
            drawRoundRect(
                color = GoalDashedBorder,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap))
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = GoalTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Add a new goal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GoalTextMuted
            )
        }
    }
}
