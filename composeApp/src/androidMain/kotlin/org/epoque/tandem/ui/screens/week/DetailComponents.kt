package org.epoque.tandem.ui.screens.week

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSizing
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.CoralPrimary
import org.epoque.tandem.ui.theme.ScheduleGreen
import org.epoque.tandem.ui.components.week.LargePriorityCheckbox
import org.epoque.tandem.ui.components.week.toColor

/**
 * Reusable detail row with icon and content.
 */
@Composable
fun DetailRow(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(vertical = TandemSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.xl),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(TandemSpacing.md))
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

/**
 * Task title row with large priority checkbox.
 */
@Composable
fun TaskTitleRow(
    title: String,
    priority: TaskPriority,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTitleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TandemSpacing.md),
        verticalAlignment = Alignment.Top
    ) {
        LargePriorityCheckbox(
            checked = isCompleted,
            priority = priority,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(TandemSpacing.md))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (isCompleted) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onTitleClick)
        )
    }
}

/**
 * Goal progress row showing goal name with progress dots.
 */
@Composable
fun GoalProgressRow(
    goalName: String,
    goalIcon: String?,
    progress: Int, // 0-100 percentage or steps completed
    totalSteps: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DetailRow(
        icon = Icons.Outlined.Flag,
        iconTint = CoralPrimary,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (goalIcon != null) {
                    Text(
                        text = goalIcon,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(TandemSpacing.xs))
                }
                Text(
                    text = goalName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = CoralPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress dots or percentage
            if (totalSteps != null && totalSteps > 0) {
                ProgressDots(
                    completed = progress,
                    total = totalSteps
                )
            }
        }
    }
}

/**
 * Progress dots indicator.
 */
@Composable
private fun ProgressDots(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xxs)
    ) {
        repeat(total.coerceAtMost(5)) { index ->
            Box(
                modifier = Modifier
                    .size(TandemSizing.Indicator.badge)
                    .clip(CircleShape)
                    .background(
                        if (index < completed) CoralPrimary
                        else CoralPrimary.copy(alpha = 0.2f)
                    )
            )
        }
        if (total > 5) {
            Text(
                text = "+${total - 5}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Option chips row for deadline, reminders, etc.
 */
@Composable
fun OptionChipsRow(
    hasDeadline: Boolean,
    hasReminders: Boolean,
    hasRepeat: Boolean,
    repeatText: String?,
    onDeadlineClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onRepeatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TandemSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs)
    ) {
        // Deadline chip
        OptionChip(
            icon = Icons.Outlined.CalendarMonth,
            label = "Deadline",
            isActive = hasDeadline,
            onClick = onDeadlineClick
        )

        // Reminders chip
        OptionChip(
            icon = Icons.Outlined.Notifications,
            label = "Reminders",
            isActive = hasReminders,
            onClick = onRemindersClick
        )

        // Repeat chip
        OptionChip(
            icon = Icons.Outlined.Repeat,
            label = repeatText ?: "Repeat",
            isActive = hasRepeat,
            onClick = onRepeatClick
        )
    }
}

/**
 * Individual option chip.
 */
@Composable
private fun OptionChip(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(TandemShapes.lg)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = TandemSpacing.Chip.horizontalPadding,
                vertical = TandemSpacing.Chip.verticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.md),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(TandemSpacing.xxs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * Subtasks section with list of subtasks.
 */
@Composable
fun SubtasksSection(
    subtasks: List<SubtaskItem>,
    onSubtaskChecked: (String, Boolean) -> Unit,
    onAddSubtask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TandemSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtasks",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${subtasks.count { it.isCompleted }}/${subtasks.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Subtask list
        subtasks.forEach { subtask ->
            SubtaskRow(
                subtask = subtask,
                onCheckedChange = { onSubtaskChecked(subtask.id, it) }
            )
        }

        // Add subtask button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TandemShapes.sm)
                .clickable(onClick = onAddSubtask)
                .padding(vertical = TandemSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add subtask",
                modifier = Modifier.size(TandemSizing.Icon.lg),
                tint = CoralPrimary
            )
            Spacer(modifier = Modifier.width(TandemSpacing.sm))
            Text(
                text = "Add subtask",
                style = MaterialTheme.typography.bodyMedium,
                color = CoralPrimary
            )
        }
    }
}

/**
 * Subtask data class.
 */
data class SubtaskItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

/**
 * Individual subtask row.
 */
@Composable
private fun SubtaskRow(
    subtask: SubtaskItem,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TandemSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small checkbox
        Box(
            modifier = Modifier
                .size(TandemSizing.Checkbox.visualSize)
                .clip(CircleShape)
                .border(
                    width = TandemSizing.Border.standard,
                    color = if (subtask.isCompleted) ScheduleGreen else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .background(
                    if (subtask.isCompleted) ScheduleGreen else Color.Transparent
                )
                .clickable { onCheckedChange(!subtask.isCompleted) },
            contentAlignment = Alignment.Center
        ) {
            if (subtask.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    modifier = Modifier.size(TandemSizing.Icon.sm),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(TandemSpacing.sm))

        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (subtask.isCompleted) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Action buttons for task detail (Complete, Skip).
 */
@Composable
fun TaskActionButtons(
    isCompleted: Boolean,
    onCompleteClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.sm)
    ) {
        // Complete button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(TandemShapes.md)
                .background(
                    if (isCompleted) ScheduleGreen.copy(alpha = 0.1f)
                    else CoralPrimary
                )
                .clickable(onClick = onCompleteClick)
                .padding(vertical = TandemSpacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(TandemSizing.Icon.lg),
                    tint = if (isCompleted) ScheduleGreen else Color.White
                )
                Spacer(modifier = Modifier.width(TandemSpacing.xs))
                Text(
                    text = if (isCompleted) "Completed" else "Complete",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isCompleted) ScheduleGreen else Color.White
                )
            }
        }

        // Skip button (only for incomplete tasks)
        if (!isCompleted) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(TandemShapes.md)
                    .border(
                        width = TandemSizing.Border.hairline,
                        color = MaterialTheme.colorScheme.outline,
                        shape = TandemShapes.md
                    )
                    .clickable(onClick = onSkipClick)
                    .padding(vertical = TandemSpacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
