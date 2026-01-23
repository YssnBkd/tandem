package org.epoque.tandem.ui.legacy.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalStatus
import org.epoque.tandem.domain.model.GoalType
import org.epoque.tandem.ui.legacy.goals.components.GoalProgressBar
import org.epoque.tandem.ui.legacy.goals.components.GoalStatusBadge

/**
 * Card displaying a goal with its icon, name, progress bar, and progress text.
 * Tappable with 48dp minimum touch target.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = goal.icon,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Show status badge for non-active goals
                    if (goal.status != GoalStatus.ACTIVE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        GoalStatusBadge(status = goal.status)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                GoalProgressBar(progress = goal.progressFraction)

                Spacer(modifier = Modifier.height(4.dp))

                // Progress text and type indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = goal.progressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = when (goal.type) {
                            is GoalType.WeeklyHabit -> "This week"
                            is GoalType.RecurringTask -> if (goal.currentProgress > 0) "Done" else "This week"
                            is GoalType.TargetAmount -> "Total"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
