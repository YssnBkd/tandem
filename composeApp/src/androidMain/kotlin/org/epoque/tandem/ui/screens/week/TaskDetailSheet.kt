package org.epoque.tandem.ui.screens.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.components.week.LargePriorityCheckbox
import org.epoque.tandem.ui.components.popovers.DateSelectorPopover
import org.epoque.tandem.ui.components.popovers.OwnerSelectorPopover
import org.epoque.tandem.ui.components.popovers.OwnerType
import org.epoque.tandem.ui.components.popovers.PrioritySelectorPopover
import org.epoque.tandem.ui.components.popovers.QuickDate
import org.epoque.tandem.ui.components.selectors.GoalSelectorSheet
import org.epoque.tandem.ui.components.selectors.LabelSelectorSheet
import org.epoque.tandem.ui.components.selectors.mockGoals
import org.epoque.tandem.ui.components.selectors.mockLabels

// Colors
private val DragHandleColor = Color(0xFFE0E0E0)
private val ChipBackground = Color(0xFFF5F5F5)
private val SuccessGreen = Color(0xFF4CAF50)
private val DividerColor = Color(0xFFE8E8E8)

/**
 * Task Detail Bottom Sheet matching the Todoist-inspired mockup.
 * Draggable modal that shows task details, subtasks, and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    task: TaskDetailUiModel,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle() }
    ) {
        TaskDetailContent(
            task = task,
            onComplete = onComplete,
            onSkip = onSkip
        )
    }
}

/**
 * Drag handle - gray pill at top of sheet.
 */
@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DragHandleColor)
        )
    }
}

/**
 * Main content of the task detail sheet.
 * Structured so partial expansion shows content up to options chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailContent(
    task: TaskDetailUiModel,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    // Bottom sheet visibility states (popovers are now in CompactMetadataRow)
    var showGoalSelector by remember { mutableStateOf(false) }
    var showLabelSelector by remember { mutableStateOf(false) }

    // Editable values (initialized from task, mock data for now)
    var selectedOwner by remember { mutableStateOf(OwnerType.ME) }
    var selectedDate by remember { mutableStateOf(task.dueDate ?: "Today") }
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var selectedLabels by remember { mutableStateOf(task.labels) }
    var selectedGoalId by remember { mutableStateOf(task.goal?.let { "fitness" }) }

    // Option chip states
    var deadlineSet by remember { mutableStateOf(false) }
    var reminderSet by remember { mutableStateOf(false) }
    var locationSet by remember { mutableStateOf(false) }
    var repeatSet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Primary content that should be visible in partial mode
        PrimaryContent(
            task = task,
            selectedOwner = selectedOwner,
            selectedDate = selectedDate,
            selectedPriority = selectedPriority,
            selectedLabels = selectedLabels,
            onOwnerSelected = { selectedOwner = it },
            onDateSelected = { quickDate ->
                if (quickDate !is QuickDate.PickDate) {
                    selectedDate = quickDate.label
                }
                // TODO: Open date picker for PickDate
            },
            onPrioritySelected = { selectedPriority = it },
            onLabelsClick = { showLabelSelector = true },
            onGoalClick = { showGoalSelector = true },
            onDeadlineClick = { deadlineSet = !deadlineSet },
            onRemindersClick = { reminderSet = !reminderSet },
            onLocationClick = { locationSet = !locationSet },
            onRepeatClick = { repeatSet = !repeatSet },
            deadlineSet = deadlineSet,
            reminderSet = reminderSet,
            locationSet = locationSet,
            repeatSet = repeatSet,
            onComplete = onComplete
        )

        // Subtasks, comments, and action buttons (visible when scrolled/expanded)
        ExpandedContent(
            task = task,
            onComplete = onComplete,
            onSkip = onSkip
        )
    }

    // Goal Selector Sheet
    if (showGoalSelector) {
        GoalSelectorSheet(
            goals = mockGoals,
            selectedGoalId = selectedGoalId,
            onGoalSelected = { selectedGoalId = it },
            onDismiss = { showGoalSelector = false }
        )
    }

    // Label Selector Sheet
    if (showLabelSelector) {
        LabelSelectorSheet(
            availableLabels = mockLabels,
            selectedLabels = selectedLabels.map { it.name },
            onLabelToggled = { labelName ->
                val currentNames = selectedLabels.map { it.name }
                selectedLabels = if (labelName in currentNames) {
                    selectedLabels.filter { it.name != labelName }
                } else {
                    val newLabel = mockLabels.find { it.name == labelName }
                    if (newLabel != null) selectedLabels + newLabel else selectedLabels
                }
            },
            onDismiss = { showLabelSelector = false }
        )
    }
}

/**
 * Primary content visible in partial expansion.
 * Shows header through options chips.
 */
@Composable
private fun PrimaryContent(
    task: TaskDetailUiModel,
    selectedOwner: OwnerType,
    selectedDate: String,
    selectedPriority: TaskPriority,
    selectedLabels: List<LabelUiModel>,
    onOwnerSelected: (OwnerType) -> Unit,
    onDateSelected: (QuickDate) -> Unit,
    onPrioritySelected: (TaskPriority) -> Unit,
    onLabelsClick: () -> Unit,
    onGoalClick: () -> Unit,
    onDeadlineClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onLocationClick: () -> Unit,
    onRepeatClick: () -> Unit,
    deadlineSet: Boolean,
    reminderSet: Boolean,
    locationSet: Boolean,
    repeatSet: Boolean,
    onComplete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header: Project selector + More button
        TaskDetailHeader(project = task.project)

        Spacer(modifier = Modifier.height(8.dp))

        // Task title row
        TaskTitleRow(
            title = task.title,
            priority = selectedPriority,
            isCompleted = false,
            onCheckedChange = { onComplete() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        if (task.description != null) {
            DetailRow(
                icon = Icons.AutoMirrored.Outlined.Subject,
                content = task.description
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Compact metadata row: Owner, Date, Priority, Labels - all horizontal with anchored popovers
        CompactMetadataRow(
            selectedOwner = selectedOwner,
            selectedDate = selectedDate,
            selectedPriority = selectedPriority,
            selectedLabels = selectedLabels,
            onOwnerSelected = onOwnerSelected,
            onDateSelected = onDateSelected,
            onPrioritySelected = onPrioritySelected,
            onLabelsClick = onLabelsClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Goal Progress
        if (task.goal != null) {
            GoalProgressRow(goal = task.goal, onClick = onGoalClick)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Options chips (horizontal scroll)
        OptionsChipRow(
            onDeadlineClick = onDeadlineClick,
            onRemindersClick = onRemindersClick,
            onLocationClick = onLocationClick,
            onRepeatClick = onRepeatClick,
            deadlineSet = deadlineSet,
            reminderSet = reminderSet,
            locationSet = locationSet,
            repeatSet = repeatSet
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Expanded content revealed when sheet is fully expanded.
 * Contains subtasks, comments, and action buttons.
 */
@Composable
private fun ExpandedContent(
    task: TaskDetailUiModel,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Subtasks section
        if (task.subtasks.isNotEmpty()) {
            SubtasksSection(subtasks = task.subtasks)
        }

        // Comment input
        CommentInput()

        // Action buttons
        ActionButtons(
            onComplete = onComplete,
            onSkip = onSkip
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Header with project selector and more button.
 */
@Composable
private fun TaskDetailHeader(
    project: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Project selector
        Row(
            modifier = Modifier.clickable { /* TODO: Open project picker */ },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = project,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        // More button
        IconButton(onClick = { /* TODO: Show menu */ }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Task title row with priority checkbox.
 */
@Composable
private fun TaskTitleRow(
    title: String,
    priority: TaskPriority,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        LargePriorityCheckbox(
            checked = isCompleted,
            priority = priority,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Generic detail row with icon and content.
 */
@Composable
private fun DetailRow(
    icon: ImageVector,
    content: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* TODO: Edit */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
}

/**
 * Compact horizontal row showing Owner, Date, Priority, and Labels.
 * Each chip has its popover anchored directly to it.
 */
@Composable
private fun CompactMetadataRow(
    selectedOwner: OwnerType,
    selectedDate: String,
    selectedPriority: TaskPriority,
    selectedLabels: List<LabelUiModel>,
    onOwnerSelected: (OwnerType) -> Unit,
    onDateSelected: (QuickDate) -> Unit,
    onPrioritySelected: (TaskPriority) -> Unit,
    onLabelsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local popover states
    var showOwnerPopover by remember { mutableStateOf(false) }
    var showDatePopover by remember { mutableStateOf(false) }
    var showPriorityPopover by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Owner chip with anchored popover
        Box {
            MetadataChipWithEmoji(
                emoji = selectedOwner.emoji,
                text = selectedOwner.displayName,
                onClick = { showOwnerPopover = true }
            )
            OwnerSelectorPopover(
                expanded = showOwnerPopover,
                selectedOwner = selectedOwner,
                onOwnerSelected = {
                    onOwnerSelected(it)
                    showOwnerPopover = false
                },
                onDismiss = { showOwnerPopover = false }
            )
        }

        // Date chip with anchored popover
        Box {
            MetadataChip(
                icon = Icons.Outlined.CalendarToday,
                text = selectedDate,
                onClick = { showDatePopover = true }
            )
            DateSelectorPopover(
                expanded = showDatePopover,
                selectedDate = selectedDate,
                onDateSelected = {
                    onDateSelected(it)
                    showDatePopover = false
                },
                onDismiss = { showDatePopover = false }
            )
        }

        // Priority chip with anchored popover
        Box {
            MetadataChip(
                icon = Icons.Outlined.Flag,
                text = "P${selectedPriority.ordinal + 1}",
                iconTint = selectedPriority.toDetailColor(),
                onClick = { showPriorityPopover = true }
            )
            PrioritySelectorPopover(
                expanded = showPriorityPopover,
                selectedPriority = selectedPriority,
                onPrioritySelected = {
                    onPrioritySelected(it)
                    showPriorityPopover = false
                },
                onDismiss = { showPriorityPopover = false }
            )
        }

        // Labels - opens bottom sheet (handled externally)
        selectedLabels.forEach { label ->
            LabelChip(label = label, onClick = onLabelsClick)
        }
    }
}

/**
 * Small metadata chip with icon and text.
 */
@Composable
private fun MetadataChip(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = iconTint
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Metadata chip with emoji instead of icon.
 */
@Composable
private fun MetadataChipWithEmoji(
    emoji: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 12.sp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Labels row with colored chip dots.
 */
@Composable
private fun LabelRow(
    labels: List<LabelUiModel>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* TODO: Edit labels */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Label,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEach { label ->
                LabelChip(label = label)
            }
        }
    }
}

/**
 * Single label chip with colored dot.
 */
@Composable
private fun LabelChip(
    label: LabelUiModel,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(label.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Goal progress row with dots indicator.
 */
@Composable
private fun GoalProgressRow(
    goal: GoalProgressUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji
        Text(
            text = goal.emoji,
            fontSize = 18.sp,
            modifier = Modifier.width(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Goal info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = goal.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Weekly: ${goal.current} of ${goal.total}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Progress dots
        ProgressDots(current = goal.current, total = goal.total)

        Spacer(modifier = Modifier.width(8.dp))

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Progress dots indicator.
 */
@Composable
private fun ProgressDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(total) { index ->
            val isFilled = index < current
            val isCurrent = index == current - 1

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .then(
                        if (isCurrent) {
                            Modifier.shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        } else Modifier
                    )
                    .background(
                        color = if (isFilled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Horizontal scrollable options chips.
 */
@Composable
private fun OptionsChipRow(
    onDeadlineClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onLocationClick: () -> Unit,
    onRepeatClick: () -> Unit,
    deadlineSet: Boolean,
    reminderSet: Boolean,
    locationSet: Boolean,
    repeatSet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OptionChip(
                label = if (deadlineSet) "10:00 AM" else TaskOption.DEADLINE.label,
                icon = Icons.Outlined.AccessTime,
                isActive = deadlineSet,
                onClick = onDeadlineClick
            )
            OptionChip(
                label = if (reminderSet) "Reminder set" else TaskOption.REMINDERS.label,
                icon = Icons.Outlined.Notifications,
                isActive = reminderSet,
                onClick = onRemindersClick
            )
            OptionChip(
                label = if (locationSet) "Home" else TaskOption.LOCATION.label,
                icon = Icons.Outlined.LocationOn,
                isActive = locationSet,
                onClick = onLocationClick
            )
            OptionChip(
                label = if (repeatSet) "Daily" else TaskOption.REPEAT.label,
                icon = Icons.Outlined.Replay,
                isActive = repeatSet,
                onClick = onRepeatClick
            )
        }
    }
}

/**
 * Single option chip.
 */
@Composable
private fun OptionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        ChipBackground
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    }
}

/**
 * Subtasks section with collapsible list.
 */
@Composable
private fun SubtasksSection(
    subtasks: List<SubtaskUiModel>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val completedCount = subtasks.count { it.isCompleted }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sub-tasks",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$completedCount/${subtasks.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Subtask items
        if (expanded) {
            subtasks.forEach { subtask ->
                SubtaskItem(subtask = subtask)
            }

            // Add subtask
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Add subtask */ }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Add sub-task",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Single subtask item.
 */
@Composable
private fun SubtaskItem(
    subtask: SubtaskUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (subtask.isCompleted) SuccessGreen
                    else Color.Transparent
                )
                .then(
                    if (!subtask.isCompleted) {
                        Modifier.background(
                            Color.Transparent,
                            CircleShape
                        )
                    } else Modifier
                )
                .clickable { /* TODO: Toggle */ },
            contentAlignment = Alignment.Center
        ) {
            if (subtask.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    modifier = Modifier.size(12.dp),
                    tint = Color.White
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Transparent, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (subtask.isCompleted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Comment input field.
 */
@Composable
private fun CommentInput(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChipBackground, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add a comment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = "Attach",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Action buttons - Complete and Skip.
 */
@Composable
private fun ActionButtons(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Complete button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(SuccessGreen, RoundedCornerShape(12.dp))
                    .clickable { onComplete() }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }

            // Skip button
            Box(
                modifier = Modifier
                    .background(ChipBackground, RoundedCornerShape(12.dp))
                    .clickable { onSkip() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Extension to get detail row color for priority.
 */
private fun TaskPriority.toDetailColor(): Color = when (this) {
    TaskPriority.P1 -> PriorityP1
    TaskPriority.P2 -> Color(0xFFEB8909)
    TaskPriority.P3 -> Color(0xFF246FE0)
    TaskPriority.P4 -> Color(0xFF79747E)
}
