package org.epoque.tandem.ui.components.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import kotlinx.datetime.LocalDate
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.ui.theme.ScheduleGreen
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemPrimaryContainer

/**
 * Simple data class for goal display info.
 */
data class GoalDisplayInfo(
    val icon: String,
    val name: String
)

/**
 * Expandable week card for the timeline.
 * Shows week label, date range, completion stats, and expandable task list.
 */
@Composable
fun TimelineWeekCard(
    week: Week,
    tasks: List<Task>,
    totalTasks: Int,
    completedTasks: Int,
    isCurrentWeek: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onViewFullWeek: (String) -> Unit,
    modifier: Modifier = Modifier,
    goalsByTaskId: Map<String, GoalDisplayInfo> = emptyMap(),
    partnerNamesByTaskId: Map<String, String> = emptyMap()
) {
    val completionRatio = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val completionPercentage = (completionRatio * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(
                if (isCurrentWeek) {
                    Modifier.border(
                        width = 2.dp,
                        color = TandemPrimary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Apply gradient or solid background
        val backgroundModifier = if (isCurrentWeek) {
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(TandemPrimaryContainer, MaterialTheme.colorScheme.surface)
                )
            )
        } else {
            Modifier.background(MaterialTheme.colorScheme.surface)
        }
        Column(
            modifier = backgroundModifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header row: Week label, stats, chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Week label with current indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatWeekLabel(week.startDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrentWeek) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TandemPrimary)
                                )
                                Text(
                                    text = "THIS WEEK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TandemPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Date range
                    Text(
                        text = formatDateRange(week.startDate, week.endDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Completion stats with progress bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (totalTasks > 0) {
                        // Custom progress bar (40dp wide, 4dp tall)
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(completionRatio)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ScheduleGreen)
                            )
                        }

                        Text(
                            text = "$completedTasks/$totalTasks",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded content: Task list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Divider before task list
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (tasks.isEmpty() && totalTasks > 0) {
                        Text(
                            text = "Loading tasks...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (tasks.isEmpty()) {
                        Text(
                            text = "No tasks for this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Show up to 5 tasks
                        tasks.take(5).forEach { task ->
                            val goalInfo = goalsByTaskId[task.id]
                            val partnerName = partnerNamesByTaskId[task.id]
                            TimelineTaskRow(
                                task = task,
                                modifier = Modifier.padding(vertical = 8.dp),
                                goalName = goalInfo?.name,
                                goalIcon = goalInfo?.icon,
                                partnerName = partnerName
                            )
                        }

                        if (tasks.size > 5) {
                            Text(
                                text = "+${tasks.size - 5} more tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Divider before button
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )

                        // View full week button - centered pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { onViewFullWeek(week.id) },
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TandemPrimaryContainer,
                                    contentColor = TandemPrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp
                                )
                            ) {
                                Text(
                                    text = "View full week",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatWeekLabel(startDate: LocalDate): String {
    // Format: "Week of Jan 20"
    val month = startDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "Week of $month ${startDate.dayOfMonth}"
}

private fun formatDateRange(startDate: LocalDate, endDate: LocalDate): String {
    // Format: "Mon 20 – Sun 26"
    val startDayName = startDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val endDayName = endDate.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$startDayName ${startDate.dayOfMonth} – $endDayName ${endDate.dayOfMonth}"
}
