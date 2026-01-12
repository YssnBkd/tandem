package org.epoque.tandem.ui.screens.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.presentation.week.WeekEvent
import org.epoque.tandem.presentation.week.model.AddTaskFormState
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.components.popovers.DateSelectorPopover
import org.epoque.tandem.ui.components.popovers.OwnerSelectorPopover
import org.epoque.tandem.ui.components.popovers.PrioritySelectorPopover
import org.epoque.tandem.ui.components.popovers.QuickDate
import org.epoque.tandem.ui.components.selectors.GoalOption
import org.epoque.tandem.ui.components.selectors.GoalSelectorSheet
import org.epoque.tandem.ui.components.selectors.LabelSelectorSheet
import org.epoque.tandem.ui.components.selectors.mockLabels
import org.epoque.tandem.ui.components.popovers.OwnerType as PopoverOwnerType

// Colors - matching TaskDetailSheet
private val ChipBackground = Color(0xFFF5F5F5)
private val PlaceholderColor = Color(0xFFB0B0B0)
private val DividerColor = Color(0xFFE8E8E8)
private val DragHandleColor = Color(0xFFE0E0E0)

/**
 * Add Task Modal - Todoist-style compact task creation sheet.
 * Connected to ViewModel state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskModal(
    state: AddTaskFormState,
    availableGoals: List<Goal>,
    onEvent: (WeekEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets.ime },
        dragHandle = { DragHandle() }
    ) {
        AddTaskContent(
            state = state,
            availableGoals = availableGoals,
            onEvent = onEvent
        )
    }
}

/**
 * Drag handle - matching TaskDetailSheet.
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskContent(
    state: AddTaskFormState,
    availableGoals: List<Goal>,
    onEvent: (WeekEvent) -> Unit
) {
    // Convert domain OwnerType to popover OwnerType
    val selectedOwner = state.ownerType.toPopoverOwnerType()

    // Format date for display
    val dateDisplayText = state.scheduledDate?.let { formatDateForDisplay(it) } ?: "Today"

    // Option chip states (local UI state - not persisted to ViewModel yet)
    var deadlineSet by remember { mutableStateOf(state.deadline != null) }
    var reminderSet by remember { mutableStateOf(false) }
    var locationSet by remember { mutableStateOf(false) }
    var repeatSet by remember { mutableStateOf(false) }

    // Selector sheet states (local UI state)
    var showGoalSelector by remember { mutableStateOf(false) }
    var showLabelSelector by remember { mutableStateOf(false) }

    // Find selected goal from available goals
    val selectedGoal = state.linkedGoalId?.let { goalId ->
        availableGoals.find { it.id == goalId }?.let { goal ->
            GoalOption(
                id = goal.id,
                emoji = goal.icon,
                name = goal.name,
                progress = goal.progressText
            )
        }
    }

    // Selected labels as LabelUiModel
    val selectedLabels = state.labels.map { labelName ->
        mockLabels.find { it.name == labelName } ?: LabelUiModel(labelName, Color.Gray)
    }

    // Focus requester for auto-focus on task name
    val focusRequester = remember { FocusRequester() }

    // Auto-focus task name when sheet opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        // Project selector header (placeholder)
        ProjectSelectorRow(
            selectedProject = "Inbox",
            onProjectClick = { /* TODO: Open project picker */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Task name input
        TaskNameInput(
            value = state.title,
            onValueChange = { onEvent(WeekEvent.AddTaskTitleChanged(it)) },
            focusRequester = focusRequester,
            error = state.titleError
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description input
        DescriptionInput(
            value = state.description,
            onValueChange = { onEvent(WeekEvent.AddTaskDescriptionChanged(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Compact metadata row
        CompactMetadataRow(
            selectedOwner = selectedOwner,
            onOwnerSelected = { popoverType ->
                onEvent(WeekEvent.AddTaskOwnerChanged(popoverType.toDomainOwnerType()))
            },
            dateDisplayText = dateDisplayText,
            onDateSelected = { quickDate ->
                val newDate = quickDate.toLocalDate()
                onEvent(WeekEvent.AddTaskDateChanged(newDate))
            },
            selectedPriority = state.priority,
            onPrioritySelected = { onEvent(WeekEvent.AddTaskPriorityChanged(it)) },
            selectedLabels = selectedLabels,
            onLabelsClick = { showLabelSelector = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Goal row
        GoalRow(
            selectedGoal = selectedGoal,
            onClick = { showGoalSelector = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Options chips row
        OptionsChipRow(
            onDeadlineClick = { deadlineSet = !deadlineSet },
            onRemindersClick = { reminderSet = !reminderSet },
            onLocationClick = { locationSet = !locationSet },
            onRepeatClick = { repeatSet = !repeatSet },
            deadlineSet = deadlineSet,
            reminderSet = reminderSet,
            locationSet = locationSet,
            repeatSet = repeatSet
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit button
        Button(
            onClick = { onEvent(WeekEvent.AddTaskSubmitted) },
            enabled = state.isValid,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Add Task",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    // Goal Selector Sheet
    if (showGoalSelector) {
        val goalOptions = availableGoals.map { goal ->
            GoalOption(
                id = goal.id,
                emoji = goal.icon,
                name = goal.name,
                progress = goal.progressText
            )
        }
        GoalSelectorSheet(
            goals = goalOptions,
            selectedGoalId = state.linkedGoalId,
            onGoalSelected = { goalId ->
                onEvent(WeekEvent.AddTaskGoalChanged(goalId))
            },
            onDismiss = { showGoalSelector = false }
        )
    }

    // Label Selector Sheet
    if (showLabelSelector) {
        LabelSelectorSheet(
            availableLabels = mockLabels,
            selectedLabels = state.labels,
            onLabelToggled = { labelName ->
                val newLabels = if (labelName in state.labels) {
                    state.labels - labelName
                } else {
                    state.labels + labelName
                }
                onEvent(WeekEvent.AddTaskLabelsChanged(newLabels))
            },
            onDismiss = { showLabelSelector = false }
        )
    }
}

// ============================================
// TYPE CONVERSION HELPERS
// ============================================

private fun OwnerType.toPopoverOwnerType(): PopoverOwnerType = when (this) {
    OwnerType.SELF -> PopoverOwnerType.ME
    OwnerType.PARTNER -> PopoverOwnerType.PARTNER
    OwnerType.SHARED -> PopoverOwnerType.TOGETHER
}

private fun PopoverOwnerType.toDomainOwnerType(): OwnerType = when (this) {
    PopoverOwnerType.ME -> OwnerType.SELF
    PopoverOwnerType.PARTNER -> OwnerType.PARTNER
    PopoverOwnerType.TOGETHER -> OwnerType.SHARED
}

private fun formatDateForDisplay(date: LocalDate): String {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    return when (date) {
        today -> "Today"
        tomorrow -> "Tomorrow"
        else -> "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}"
    }
}

private fun QuickDate.toLocalDate(): LocalDate? {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (this) {
        is QuickDate.Today -> today
        is QuickDate.Tomorrow -> today.plus(1, DateTimeUnit.DAY)
        is QuickDate.NextWeek -> today.plus(7, DateTimeUnit.DAY)
        is QuickDate.PickDate -> null // User will pick specific date
    }
}

/**
 * Project selector row - matching TaskDetailSheet header style.
 */
@Composable
private fun ProjectSelectorRow(
    selectedProject: String,
    onProjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .focusProperties { canFocus = false }
            .clickable(onClick = onProjectClick),
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
            text = selectedProject,
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
}

/**
 * Task name text field - larger, prominent.
 */
@Composable
private fun TaskNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "Task name",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = PlaceholderColor
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Description text field - smaller, secondary.
 */
@Composable
private fun DescriptionInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = false,
        maxLines = 3,
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = "Description",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = PlaceholderColor
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * Compact metadata row - Owner, Date, Priority, Labels.
 * Matches TaskDetailSheet CompactMetadataRow.
 */
@Composable
private fun CompactMetadataRow(
    selectedOwner: PopoverOwnerType,
    onOwnerSelected: (PopoverOwnerType) -> Unit,
    dateDisplayText: String,
    onDateSelected: (QuickDate) -> Unit,
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    selectedLabels: List<LabelUiModel>,
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
                text = dateDisplayText,
                onClick = { showDatePopover = true }
            )
            DateSelectorPopover(
                expanded = showDatePopover,
                selectedDate = dateDisplayText,
                onDateSelected = { quickDate ->
                    onDateSelected(quickDate)
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
                iconTint = selectedPriority.toColor(),
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

        // Labels - show existing or "Labels" chip
        if (selectedLabels.isNotEmpty()) {
            selectedLabels.forEach { label ->
                LabelChip(label = label, onClick = onLabelsClick)
            }
        } else {
            MetadataChip(
                icon = Icons.Outlined.MoreHoriz,
                text = "Labels",
                onClick = onLabelsClick
            )
        }
    }
}

/**
 * Metadata chip with icon - matching TaskDetailSheet.
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
            .focusProperties { canFocus = false }
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
 * Metadata chip with emoji - matching TaskDetailSheet.
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
            .focusProperties { canFocus = false }
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
 * Label chip with colored dot - matching TaskDetailSheet.
 */
@Composable
private fun LabelChip(
    label: LabelUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBackground)
            .focusProperties { canFocus = false }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(label.color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Options chips row - matching TaskDetailSheet.
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
        // Divider
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
                label = if (deadlineSet) "10:00 AM" else "Deadline",
                icon = Icons.Outlined.AccessTime,
                isActive = deadlineSet,
                onClick = onDeadlineClick
            )
            OptionChip(
                label = if (reminderSet) "Reminder set" else "Reminders",
                icon = Icons.Outlined.Notifications,
                isActive = reminderSet,
                onClick = onRemindersClick
            )
            OptionChip(
                label = if (locationSet) "Home" else "Location",
                icon = Icons.Outlined.LocationOn,
                isActive = locationSet,
                onClick = onLocationClick
            )
            OptionChip(
                label = if (repeatSet) "Daily" else "Repeat",
                icon = Icons.Outlined.Replay,
                isActive = repeatSet,
                onClick = onRepeatClick
            )
        }
    }
}

/**
 * Option chip - matching TaskDetailSheet.
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
            .focusProperties { canFocus = false }
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
 * Goal row - shows selected goal or "Add goal" placeholder.
 * Matches TaskDetailSheet's GoalProgressRow style.
 */
@Composable
private fun GoalRow(
    selectedGoal: GoalOption?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .focusProperties { canFocus = false }
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedGoal != null) {
            // Show selected goal
            Text(
                text = selectedGoal.emoji,
                fontSize = 18.sp,
                modifier = Modifier.width(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedGoal.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (selectedGoal.progress != null) {
                    Text(
                        text = selectedGoal.progress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Show placeholder
            Text(
                text = "\uD83C\uDFAF", // 🎯 target emoji
                fontSize = 18.sp,
                modifier = Modifier.width(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add goal",
                style = MaterialTheme.typography.bodyMedium,
                color = PlaceholderColor,
                modifier = Modifier.weight(1f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Extension to get color for priority.
 */
private fun TaskPriority.toColor(): Color = when (this) {
    TaskPriority.P1 -> PriorityP1
    TaskPriority.P2 -> Color(0xFFEB8909)
    TaskPriority.P3 -> Color(0xFF246FE0)
    TaskPriority.P4 -> Color(0xFF79747E)
}
