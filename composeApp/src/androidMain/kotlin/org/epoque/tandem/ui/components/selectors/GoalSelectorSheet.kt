package org.epoque.tandem.ui.components.selectors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Goal option data for the selector.
 */
data class GoalOption(
    val id: String,
    val emoji: String,
    val name: String,
    val progress: String? // e.g., "45% complete" or "Weekly: 2 of 3"
)

/**
 * Mock goals for testing.
 */
val mockGoals = listOf(
    GoalOption("none", "\u2796", "No goal", null),
    GoalOption("fitness", "\uD83D\uDCAA", "Get fit together", "45% complete"),
    GoalOption("healthy", "\uD83E\uDD57", "Healthy eating", "30% complete"),
    GoalOption("reading", "\uD83D\uDCDA", "Read more books", "60% complete"),
    GoalOption("savings", "\uD83D\uDCB0", "Save for vacation", "25% complete")
)

/**
 * Bottom sheet for selecting a goal to link to the task.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSelectorSheet(
    goals: List<GoalOption> = mockGoals,
    selectedGoalId: String?,
    onGoalSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            SelectorHeader(
                title = "Select Goal",
                onDone = onDismiss
            )

            // Goals list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(goals) { goal ->
                    GoalOptionItem(
                        goal = goal,
                        isSelected = goal.id == selectedGoalId ||
                                    (goal.id == "none" && selectedGoalId == null),
                        onClick = {
                            val newGoalId = if (goal.id == "none") null else goal.id
                            onGoalSelected(newGoalId)
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SelectorHeader(
    title: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        TextButton(onClick = onDone) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GoalOptionItem(
    goal: GoalOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji
        Text(
            text = goal.emoji,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name and progress
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = goal.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (goal.progress != null) {
                Text(
                    text = goal.progress,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Checkmark for selected
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Box(modifier = Modifier.size(20.dp))
        }
    }
}
