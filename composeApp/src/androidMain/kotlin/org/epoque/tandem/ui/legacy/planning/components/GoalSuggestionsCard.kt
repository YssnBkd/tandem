package org.epoque.tandem.ui.legacy.planning.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalType

/**
 * Card displaying goal-based task suggestions during the planning wizard.
 *
 * Shows active goals that need progress this week, allowing users to quickly
 * create tasks linked to their goals.
 *
 * Following Material Design 3 best practices:
 * - 48dp+ touch targets per FR-023
 * - Accessible content descriptions per FR-024
 * - Clear visual hierarchy with goal icons
 *
 * @param goals List of active goals to suggest
 * @param onGoalSelected Callback when user taps a goal suggestion
 * @param modifier Modifier for customization
 */
@Composable
fun GoalSuggestionsCard(
    goals: List<Goal>,
    onGoalSelected: (Goal) -> Unit,
    modifier: Modifier = Modifier
) {
    if (goals.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Based on your goals",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add a task to make progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalSuggestionChip(
                        goal = goal,
                        onClick = { onGoalSelected(goal) }
                    )
                }
            }
        }
    }
}

/**
 * Individual goal suggestion chip.
 * Displays goal icon, name, and progress hint.
 */
@Composable
private fun GoalSuggestionChip(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressHint = when (val type = goal.type) {
        is GoalType.WeeklyHabit -> "${goal.currentProgress}/${type.targetPerWeek} this week"
        is GoalType.RecurringTask -> if (goal.currentProgress > 0) "Done this week" else "Not yet"
        is GoalType.TargetAmount -> "${goal.currentProgress}/${type.targetTotal} total"
    }

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Add task for ${goal.name}. Progress: $progressHint"
            },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goal.icon,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = progressHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
