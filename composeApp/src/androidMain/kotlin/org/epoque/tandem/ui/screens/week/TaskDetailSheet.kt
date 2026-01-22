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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSizing
import org.epoque.tandem.ui.theme.TandemSpacing
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.presentation.week.WeekEvent
import org.epoque.tandem.presentation.week.model.SubtaskUiModel
import org.epoque.tandem.presentation.week.model.TaskDetailState
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.components.week.LargePriorityCheckbox
import org.epoque.tandem.ui.components.popovers.DateSelectorPopover
import org.epoque.tandem.ui.components.popovers.OwnerSelectorPopover
import org.epoque.tandem.ui.components.popovers.PrioritySelectorPopover
import org.epoque.tandem.ui.components.popovers.QuickDate
import org.epoque.tandem.ui.components.selectors.GoalOption
import org.epoque.tandem.ui.components.selectors.GoalSelectorSheet
import org.epoque.tandem.ui.components.selectors.LabelSelectorSheet
import org.epoque.tandem.ui.components.selectors.mockLabels
import org.epoque.tandem.ui.components.popovers.OwnerType as PopoverOwnerType

// Colors
private val DragHandleColor = Color(0xFFE0E0E0)
private val ChipBackground = Color(0xFFF5F5F5)
private val SuccessGreen = Color(0xFF4CAF50)
private val DividerColor = Color(0xFFE8E8E8)

/**
 * Task Detail Bottom Sheet connected to ViewModel state.
 * Draggable modal that shows task details, subtasks, and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    state: TaskDetailState,
    availableGoals: List<Goal>,
    onEvent: (WeekEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = TandemShapes.Sheet.bottomSheet,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { DragHandle() }
    ) {
        TaskDetailContent(
            state = state,
            availableGoals = availableGoals,
            onEvent = onEvent
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
            .padding(vertical = TandemSpacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(TandemShapes.xs)
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
    state: TaskDetailState,
    availableGoals: List<Goal>,
    onEvent: (WeekEvent) -> Unit
) {
    // Bottom sheet visibility states (for nested selectors)
    var showGoalSelector by remember { mutableStateOf(false) }
    var showLabelSelector by remember { mutableStateOf(false) }

    // Map domain OwnerType to popover OwnerType
    val selectedOwner = state.ownerType.toPopoverOwnerType()

    // Format date for display
    val dateDisplayText = state.scheduledDate?.let { date ->
        formatDateForDisplay(date)
    } ?: "No date"

    // Option chip states (local for now - could be moved to state)
    var deadlineSet by remember { mutableStateOf(state.deadline != null) }
    var reminderSet by remember { mutableStateOf(false) }
    var locationSet by remember { mutableStateOf(false) }
    var repeatSet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TandemSpacing.Sheet.horizontalPadding)
    ) {
        // Save button row - shows when there are unsaved text changes
        if (state.hasUnsavedTextChanges) {
            SaveButtonRow(
                onSave = { onEvent(WeekEvent.SaveTextChangesRequested) }
            )
        }

        // Primary content that should be visible in partial mode
        PrimaryContent(
            state = state,
            selectedOwner = selectedOwner,
            dateDisplayText = dateDisplayText,
            onTitleChanged = { onEvent(WeekEvent.TaskTitleChanged(it)) },
            onDescriptionChanged = { onEvent(WeekEvent.TaskDescriptionChanged(it)) },
            onOwnerSelected = { popoverOwner ->
                onEvent(WeekEvent.TaskOwnerChanged(popoverOwner.toDomainOwnerType()))
            },
            onDateSelected = { quickDate ->
                val date = quickDate.toLocalDate()
                onEvent(WeekEvent.TaskDateChanged(date))
            },
            onPrioritySelected = { onEvent(WeekEvent.TaskPriorityChanged(it)) },
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
            onComplete = { onEvent(WeekEvent.TaskCompleteRequested) }
        )

        // Subtasks, comments, and action buttons (visible when scrolled/expanded)
        ExpandedContent(
            state = state,
            onSubtaskCheckboxTapped = { subtaskId -> onEvent(WeekEvent.SubtaskCheckboxTapped(subtaskId)) },
            onNewSubtaskTitleChanged = { title -> onEvent(WeekEvent.NewSubtaskTitleChanged(title)) },
            onAddSubtaskSubmitted = { onEvent(WeekEvent.AddSubtaskSubmitted) },
            onSubtaskDeleted = { subtaskId -> onEvent(WeekEvent.SubtaskDeleted(subtaskId)) },
            onCommentTextChanged = { text -> onEvent(WeekEvent.CommentTextChanged(text)) },
            onCommentSubmitted = { onEvent(WeekEvent.CommentSubmitted) },
            onComplete = { onEvent(WeekEvent.TaskCompleteRequested) }
        )
    }

    // Goal Selector Sheet
    if (showGoalSelector) {
        // Map domain Goals to GoalOption for the selector
        val goalOptions = buildList {
            // Add "No goal" option first
            add(GoalOption(id = "none", emoji = "\u2796", name = "No goal", progress = null))
            // Add actual goals
            availableGoals.forEach { goal ->
                add(GoalOption(
                    id = goal.id,
                    emoji = goal.icon,
                    name = goal.name,
                    progress = goal.progressText
                ))
            }
        }

        GoalSelectorSheet(
            goals = goalOptions,
            selectedGoalId = state.linkedGoalId,
            onGoalSelected = { goalId ->
                onEvent(WeekEvent.TaskGoalChanged(goalId))
                showGoalSelector = false
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
                onEvent(WeekEvent.TaskLabelsChanged(newLabels))
            },
            onDismiss = { showLabelSelector = false }
        )
    }
}

// Helper functions for type conversion
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
 * Save button row - appears when there are unsaved text changes.
 */
@Composable
private fun SaveButtonRow(
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = TandemSpacing.xs),
        horizontalArrangement = Arrangement.End
    ) {
        Row(
            modifier = Modifier
                .clip(TandemShapes.lg)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onSave)
                .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xxs)
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(TandemSizing.Icon.md),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Save",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Primary content visible in partial expansion.
 * Shows header through options chips.
 */
@Composable
private fun PrimaryContent(
    state: TaskDetailState,
    selectedOwner: PopoverOwnerType,
    dateDisplayText: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onOwnerSelected: (PopoverOwnerType) -> Unit,
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
        TaskDetailHeader(project = "This Week")

        Spacer(modifier = Modifier.height(TandemSpacing.xs))

        // Task title row (editable)
        EditableTaskTitleRow(
            title = state.title,
            priority = state.priority,
            onTitleChanged = onTitleChanged,
            onCheckedChange = { onComplete() }
        )

        Spacer(modifier = Modifier.height(TandemSpacing.xs))

        // Description (editable)
        EditableDescriptionRow(
            description = state.description,
            onDescriptionChanged = onDescriptionChanged
        )

        Spacer(modifier = Modifier.height(TandemSpacing.xs))

        // Compact metadata row: Owner, Date, Priority, Labels - all horizontal with anchored popovers
        CompactMetadataRow(
            selectedOwner = selectedOwner,
            selectedDate = dateDisplayText,
            selectedPriority = state.priority,
            selectedLabels = state.labels.map { LabelUiModel(it, Color.Gray) }, // Map to UI model
            onOwnerSelected = onOwnerSelected,
            onDateSelected = onDateSelected,
            onPrioritySelected = onPrioritySelected,
            onLabelsClick = onLabelsClick
        )

        Spacer(modifier = Modifier.height(TandemSpacing.xs))

        // Goal Progress (if linked)
        if (state.linkedGoalId != null && state.linkedGoalName != null) {
            GoalProgressRow(
                goal = GoalProgressUiModel(
                    emoji = state.linkedGoalIcon ?: "🎯",
                    name = state.linkedGoalName,
                    current = (state.linkedGoalProgressFraction * 3).toInt(), // Approximate
                    total = 3
                ),
                onClick = onGoalClick
            )
        } else {
            // Show "Add goal" placeholder
            AddGoalRow(onClick = onGoalClick)
        }

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
    }
}

/**
 * Editable task title row with priority checkbox.
 */
@Composable
private fun EditableTaskTitleRow(
    title: String,
    priority: TaskPriority,
    onTitleChanged: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        LargePriorityCheckbox(
            checked = false,
            priority = priority,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

        BasicTextField(
            value = title,
            onValueChange = onTitleChanged,
            modifier = Modifier
                .weight(1f)
                .padding(top = TandemSpacing.xxs),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (title.isEmpty()) {
                        Text(
                            text = "Task title",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

/**
 * Editable description row.
 */
@Composable
private fun EditableDescriptionRow(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TandemSpacing.xs),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Subject,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.lg),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

        BasicTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = false,
            maxLines = 5,
            decorationBox = { innerTextField ->
                Box {
                    if (description.isEmpty()) {
                        Text(
                            text = "Add description",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

/**
 * Add goal row placeholder.
 */
@Composable
private fun AddGoalRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = TandemSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🎯",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(TandemSizing.Icon.lg)
        )
        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))
        Text(
            text = "Add goal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.md),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Connected subtasks section with event callbacks.
 */
@Composable
private fun SubtasksSectionConnected(
    subtasks: List<SubtaskUiModel>,
    newSubtaskTitle: String,
    onSubtaskCheckboxTapped: (String) -> Unit,
    onNewSubtaskTitleChanged: (String) -> Unit,
    onAddSubtaskSubmitted: () -> Unit,
    onSubtaskDeleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val completedCount = subtasks.count { it.isCompleted }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TandemSizing.Border.hairline)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(TandemSpacing.md))

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
                Spacer(modifier = Modifier.width(TandemSpacing.xs))
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
                SubtaskItemConnected(
                    subtask = subtask,
                    onCheckboxTapped = { onSubtaskCheckboxTapped(subtask.id) },
                    onDelete = { onSubtaskDeleted(subtask.id) }
                )
            }

            // Add subtask input
            AddSubtaskInput(
                value = newSubtaskTitle,
                onValueChange = onNewSubtaskTitleChanged,
                onSubmit = onAddSubtaskSubmitted
            )
        }
    }
}

/**
 * Single subtask item with connected callbacks.
 */
@Composable
private fun SubtaskItemConnected(
    subtask: SubtaskUiModel,
    onCheckboxTapped: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TandemSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(TandemSizing.Checkbox.visualSize)
                .clip(CircleShape)
                .background(
                    if (subtask.isCompleted) SuccessGreen else Color.Transparent
                )
                .then(
                    if (!subtask.isCompleted) {
                        Modifier.background(Color.Transparent, CircleShape)
                    } else Modifier
                )
                .clickable(onClick = onCheckboxTapped),
            contentAlignment = Alignment.Center
        ) {
            if (subtask.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    modifier = Modifier.size(TandemSizing.Icon.xs),
                    tint = Color.White
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(TandemSizing.Checkbox.visualSize - TandemSpacing.xxxs)
                        .background(MaterialTheme.colorScheme.outline, CircleShape)
                        .padding(TandemSpacing.xxxs)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (subtask.isCompleted) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(TandemSizing.minTouchTarget)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete subtask",
                modifier = Modifier.size(TandemSizing.Icon.md),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Add subtask input field.
 */
@Composable
private fun AddSubtaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TandemSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.lg),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "Add sub-task",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (value.isNotEmpty()) {
            IconButton(
                onClick = onSubmit,
                modifier = Modifier.size(TandemSizing.minTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Add",
                    modifier = Modifier.size(TandemSizing.Icon.lg),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Connected comment input with callbacks.
 */
@Composable
private fun CommentInputConnected(
    commentText: String,
    onCommentTextChanged: (String) -> Unit,
    onCommentSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TandemSizing.Border.hairline)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(TandemSpacing.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChipBackground, TandemShapes.xl)
                .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = commentText,
                onValueChange = onCommentTextChanged,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (commentText.isEmpty()) {
                            Text(
                                text = "Add a comment",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (commentText.isNotEmpty()) {
                IconButton(
                    onClick = onCommentSubmitted,
                    modifier = Modifier.size(TandemSizing.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Submit",
                        modifier = Modifier.size(TandemSizing.Icon.lg),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = "Attach",
                    modifier = Modifier.size(TandemSizing.Icon.lg),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Expanded content revealed when sheet is fully expanded.
 * Contains subtasks, comments, and action buttons.
 */
@Composable
private fun ExpandedContent(
    state: TaskDetailState,
    onSubtaskCheckboxTapped: (String) -> Unit,
    onNewSubtaskTitleChanged: (String) -> Unit,
    onAddSubtaskSubmitted: () -> Unit,
    onSubtaskDeleted: (String) -> Unit,
    onCommentTextChanged: (String) -> Unit,
    onCommentSubmitted: () -> Unit,
    onComplete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Subtasks section
        SubtasksSectionConnected(
            subtasks = state.subtasks,
            newSubtaskTitle = state.newSubtaskTitle,
            onSubtaskCheckboxTapped = onSubtaskCheckboxTapped,
            onNewSubtaskTitleChanged = onNewSubtaskTitleChanged,
            onAddSubtaskSubmitted = onAddSubtaskSubmitted,
            onSubtaskDeleted = onSubtaskDeleted
        )

        // Comment input
        CommentInputConnected(
            commentText = state.commentText,
            onCommentTextChanged = onCommentTextChanged,
            onCommentSubmitted = onCommentSubmitted
        )

        // Action buttons
        ActionButtons(
            onComplete = onComplete
        )

        Spacer(modifier = Modifier.height(TandemSpacing.md))
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
                modifier = Modifier.size(TandemSizing.Icon.md),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(TandemSpacing.xs))
            Text(
                text = project,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(TandemSizing.Icon.md),
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

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = TandemSpacing.xxs)
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
            .padding(vertical = TandemSpacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.lg),
            tint = iconTint
        )

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Compact horizontal row showing Owner, Date, Priority, and Labels.
 * Each chip has its popover anchored directly to it.
 */
@Composable
private fun CompactMetadataRow(
    selectedOwner: PopoverOwnerType,
    selectedDate: String,
    selectedPriority: TaskPriority,
    selectedLabels: List<LabelUiModel>,
    onOwnerSelected: (PopoverOwnerType) -> Unit,
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
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.sm),
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
            .clip(TandemShapes.lg)
            .background(ChipBackground)
            .clickable(onClick = onClick)
            .padding(
                horizontal = TandemSpacing.Chip.horizontalPadding,
                vertical = TandemSpacing.Chip.verticalPaddingCompact
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xxs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.sm),
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
            .clip(TandemShapes.lg)
            .background(ChipBackground)
            .clickable(onClick = onClick)
            .padding(
                horizontal = TandemSpacing.Chip.horizontalPadding,
                vertical = TandemSpacing.Chip.verticalPaddingCompact
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xxs)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelSmall
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
            .padding(vertical = TandemSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Label,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.lg),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

        Row(horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs)) {
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
            .clip(TandemShapes.lg)
            .background(ChipBackground)
            .clickable(onClick = onClick)
            .padding(
                horizontal = TandemSpacing.Chip.horizontalPadding,
                vertical = TandemSpacing.Chip.verticalPaddingCompact
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(TandemSizing.Indicator.badge)
                .background(label.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(TandemSpacing.Inline.iconTextGap))
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
            .padding(vertical = TandemSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji
        Text(
            text = goal.emoji,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(TandemSizing.Icon.lg)
        )

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

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

        Spacer(modifier = Modifier.width(TandemSpacing.xs))

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(TandemSizing.Icon.md),
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
        horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xxs)
    ) {
        repeat(total) { index ->
            val isFilled = index < current
            val isCurrent = index == current - 1

            Box(
                modifier = Modifier
                    .size(TandemSizing.Indicator.badge + TandemSpacing.xxxs)
                    .then(
                        if (isCurrent) {
                            Modifier.shadow(
                                elevation = TandemSpacing.xxs,
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
                .height(TandemSizing.Border.hairline)
                .background(DividerColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = TandemSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(TandemSpacing.xs)
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
            .clip(TandemShapes.xl)
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
            modifier = Modifier.size(TandemSizing.Icon.sm),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(TandemSpacing.Inline.iconTextGap))
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
                .height(TandemSizing.Border.hairline)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(TandemSpacing.md))

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
                Spacer(modifier = Modifier.width(TandemSpacing.xs))
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
                    .padding(vertical = TandemSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(TandemSizing.Icon.lg),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))
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
            .padding(vertical = TandemSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(TandemSizing.Checkbox.visualSize)
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
                    modifier = Modifier.size(TandemSizing.Icon.xs),
                    tint = Color.White
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(TandemSizing.Checkbox.visualSize)
                        .background(Color.Transparent, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .padding(TandemSpacing.xxxs)
                        .background(
                            MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        .padding(TandemSpacing.xxxs)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(TandemSpacing.Inline.checkboxGap))

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
                .height(TandemSizing.Border.hairline)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(TandemSpacing.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChipBackground, TandemShapes.xl)
                .padding(horizontal = TandemSpacing.md, vertical = TandemSpacing.sm),
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
                modifier = Modifier.size(TandemSizing.Icon.lg),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Action button - Complete.
 */
@Composable
private fun ActionButtons(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TandemSizing.Border.hairline)
                .background(DividerColor)
        )

        Spacer(modifier = Modifier.height(TandemSpacing.md))

        // Complete button (full width)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SuccessGreen, TandemShapes.md)
                .clickable { onComplete() }
                .padding(vertical = TandemSpacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(TandemSizing.Icon.lg),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(TandemSpacing.xs))
            Text(
                text = "Complete",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
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
