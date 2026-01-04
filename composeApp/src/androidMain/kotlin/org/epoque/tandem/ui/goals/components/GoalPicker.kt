package org.epoque.tandem.ui.goals.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Goal

/**
 * Goal picker component for linking tasks to goals.
 *
 * Displays a horizontal list of available goals as filter chips.
 * The user can select one goal or choose "None" to unlink.
 * Follows Material Design 3 with 48dp minimum touch targets.
 *
 * @param availableGoals List of active goals the user can select from
 * @param selectedGoalId Currently selected goal ID, or null if none
 * @param onGoalSelected Callback when a goal is selected (null to unlink)
 * @param modifier Modifier for customization
 */
@Composable
fun GoalPicker(
    availableGoals: List<Goal>,
    selectedGoalId: String?,
    onGoalSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Link to Goal",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (availableGoals.isEmpty()) {
            Text(
                text = "No active goals. Create a goal first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // "None" option to unlink
                item {
                    FilterChip(
                        selected = selectedGoalId == null,
                        onClick = { onGoalSelected(null) },
                        label = { Text("None") },
                        leadingIcon = if (selectedGoalId == null) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .semantics {
                                contentDescription = "No goal linked"
                            }
                    )
                }

                // Goal options
                items(availableGoals, key = { it.id }) { goal ->
                    val isSelected = selectedGoalId == goal.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onGoalSelected(goal.id) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = goal.icon,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .semantics {
                                contentDescription = if (isSelected) {
                                    "Goal ${goal.name} selected"
                                } else {
                                    "Select goal ${goal.name}"
                                }
                            }
                    )
                }
            }
        }
    }
}
