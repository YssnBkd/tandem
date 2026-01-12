package org.epoque.tandem.ui.screens.week

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.epoque.tandem.presentation.week.WeekEvent
import org.epoque.tandem.presentation.week.WeekSideEffect
import org.epoque.tandem.presentation.week.WeekViewModel
import org.epoque.tandem.presentation.week.model.CalendarDay
import org.epoque.tandem.presentation.week.model.Segment
import org.epoque.tandem.presentation.week.model.TaskSection
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.ui.components.week.CompletedSection
import org.epoque.tandem.ui.components.week.TaskRowItem
import org.epoque.tandem.ui.components.week.TaskSectionHeader
import org.epoque.tandem.ui.components.week.TaskUiItem
import org.epoque.tandem.ui.components.week.WeekDayItem
import org.epoque.tandem.ui.components.week.WeekDaySelector
import org.epoque.tandem.ui.components.week.WeekFab
import org.epoque.tandem.ui.components.week.WeekHeader
import org.epoque.tandem.ui.components.SegmentedControl
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Week View screen with Todoist-inspired UI redesign.
 * Connected to WeekViewModel for data and state management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    viewModel: WeekViewModel = koinViewModel(),
    onNavigateToPartnerInvite: () -> Unit = {},
    onNavigateToSeasons: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Task detail sheet state - use confirmValueChange to prevent dismiss when unsaved changes exist
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { targetValue ->
            // When trying to hide the sheet, check for unsaved text changes
            if (targetValue == androidx.compose.material3.SheetValue.Hidden) {
                val hasUnsavedChanges = uiState.taskDetailState?.hasUnsavedTextChanges == true
                if (hasUnsavedChanges) {
                    // Show the discard dialog instead of dismissing
                    viewModel.onEvent(WeekEvent.DetailSheetDismissRequested)
                    false // Prevent the sheet from hiding
                } else {
                    // No unsaved changes, allow dismiss
                    viewModel.onEvent(WeekEvent.DetailSheetDismissed)
                    true
                }
            } else {
                true // Allow other state changes (expanding, etc.)
            }
        }
    )

    // Add task modal state
    val addTaskSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is WeekSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is WeekSideEffect.TriggerHapticFeedback -> {
                    hapticFeedback.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                    )
                }
                is WeekSideEffect.NavigateToPartnerInvite -> {
                    onNavigateToPartnerInvite()
                }
                is WeekSideEffect.NavigateToRequestTask -> {
                    // Will be implemented in Partner feature
                }
                is WeekSideEffect.ClearFocus -> {
                    // Could use FocusManager here if needed
                }
                is WeekSideEffect.DismissKeyboard -> {
                    // Could use FocusManager here if needed
                }
                is WeekSideEffect.ScrollToSection -> {
                    // Could scroll to section using LazyListState
                }
                is WeekSideEffect.ShowDatePicker -> {
                    // Will be implemented in Modal conversation
                }
                is WeekSideEffect.ShowTimePicker -> {
                    // Will be implemented in Modal conversation
                }
                is WeekSideEffect.ShowPriorityPicker -> {
                    // Will be implemented in Modal conversation
                }
                is WeekSideEffect.ShowLabelsPicker -> {
                    // Will be implemented in Modal conversation
                }
                is WeekSideEffect.ShowGoalPicker -> {
                    // Will be implemented in Modal conversation
                }
            }
        }
    }

    // Map CalendarDay to WeekDayItem for the selector
    val calendarDays = uiState.calendarDays.map { it.toWeekDayItem() }

    // Map TaskUiModel to TaskUiItem for display
    val overdueTasks = uiState.overdueTasks.map { it.toTaskUiItem(isOverdue = true) }
    val todayTasks = uiState.todayTasks.map { it.toTaskUiItem() }
    val tomorrowTasks = uiState.tomorrowTasks.map { it.toTaskUiItem() }
    val laterTasks = uiState.laterThisWeekTasks.map { it.toTaskUiItem() }
    val unscheduledTasks = uiState.unscheduledTasks.map { it.toTaskUiItem() }
    val completedTasks = uiState.completedTasks.map { it.toTaskUiItem() }

    // Header info
    val headerTitle = if (uiState.weekInfo?.isCurrentWeek == true) "This Week" else "Week"
    val headerSubtitle = uiState.weekInfo?.let {
        "${it.dateRangeText.removePrefix("Week of ")} · ${uiState.totalCount} tasks"
    } ?: ""

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            WeekFab(onClick = { viewModel.onEvent(WeekEvent.AddTaskSheetRequested) })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Show loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Week Header
                item {
                    WeekHeader(
                        title = headerTitle,
                        subtitle = headerSubtitle,
                        seasonInfo = "\uD83C\uDF31 Q1 2026 · Week 3 of 12",
                        onSeasonClick = onNavigateToSeasons
                    )
                }

                // Day Selector
                item {
                    WeekDaySelector(
                        days = calendarDays,
                        onDaySelected = { index ->
                            val day = uiState.calendarDays.getOrNull(index)
                            day?.let { viewModel.onEvent(WeekEvent.CalendarDateTapped(it.date)) }
                        },
                        onPreviousWeek = { viewModel.onEvent(WeekEvent.PreviousWeekTapped) },
                        onNextWeek = { viewModel.onEvent(WeekEvent.NextWeekTapped) }
                    )
                }

                // Segment Control
                item {
                    SegmentedControl(
                        segments = Segment.entries.map { it.displayName },
                        selectedIndex = Segment.entries.indexOf(uiState.selectedSegment),
                        onSegmentSelected = { index ->
                            viewModel.onEvent(WeekEvent.SegmentSelected(Segment.entries[index]))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Overdue Section
                if (overdueTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(section = TaskSection.OVERDUE)
                    }
                    items(overdueTasks, key = { it.id }) { task ->
                        TaskRowItem(
                            task = task,
                            onCheckedChange = { viewModel.onEvent(WeekEvent.TaskCheckboxTapped(task.id)) },
                            onClick = { viewModel.onEvent(WeekEvent.TaskTapped(task.id)) }
                        )
                    }
                }

                // Today Section
                if (todayTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(section = TaskSection.TODAY)
                    }
                    items(todayTasks, key = { it.id }) { task ->
                        TaskRowItem(
                            task = task,
                            onCheckedChange = { viewModel.onEvent(WeekEvent.TaskCheckboxTapped(task.id)) },
                            onClick = { viewModel.onEvent(WeekEvent.TaskTapped(task.id)) }
                        )
                    }
                }

                // Tomorrow Section
                if (tomorrowTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(section = TaskSection.TOMORROW)
                    }
                    items(tomorrowTasks, key = { it.id }) { task ->
                        TaskRowItem(
                            task = task,
                            onCheckedChange = { viewModel.onEvent(WeekEvent.TaskCheckboxTapped(task.id)) },
                            onClick = { viewModel.onEvent(WeekEvent.TaskTapped(task.id)) }
                        )
                    }
                }

                // Later this week Section
                if (laterTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(section = TaskSection.LATER_THIS_WEEK)
                    }
                    items(laterTasks, key = { it.id }) { task ->
                        TaskRowItem(
                            task = task,
                            onCheckedChange = { viewModel.onEvent(WeekEvent.TaskCheckboxTapped(task.id)) },
                            onClick = { viewModel.onEvent(WeekEvent.TaskTapped(task.id)) }
                        )
                    }
                }

                // Unscheduled Section
                if (unscheduledTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(section = TaskSection.UNSCHEDULED)
                    }
                    items(unscheduledTasks, key = { it.id }) { task ->
                        TaskRowItem(
                            task = task,
                            onCheckedChange = { viewModel.onEvent(WeekEvent.TaskCheckboxTapped(task.id)) },
                            onClick = { viewModel.onEvent(WeekEvent.TaskTapped(task.id)) }
                        )
                    }
                }

                // Completed Section (collapsible)
                if (completedTasks.isNotEmpty()) {
                    item {
                        CompletedSection(
                            completedCount = completedTasks.size,
                            expanded = uiState.isCompletedSectionExpanded,
                            onExpandToggle = { viewModel.onEvent(WeekEvent.CompletedSectionToggled) }
                        ) {
                            completedTasks.forEach { task ->
                                TaskRowItem(
                                    task = task,
                                    onCheckedChange = { viewModel.onEvent(WeekEvent.TaskCheckboxTapped(task.id)) },
                                    onClick = { viewModel.onEvent(WeekEvent.TaskTapped(task.id)) }
                                )
                            }
                        }
                    }
                }

                // Empty state (when no tasks at all)
                if (!uiState.hasIncompleteTasks && completedTasks.isEmpty()) {
                    item {
                        EmptyStateContent(
                            message = uiState.emptyStateMessage,
                            actionText = uiState.emptyStateActionText,
                            onActionClick = {
                                when (uiState.selectedSegment) {
                                    Segment.PARTNER -> viewModel.onEvent(WeekEvent.InvitePartnerTapped)
                                    Segment.SHARED -> viewModel.onEvent(WeekEvent.AddTaskSheetRequested)
                                    else -> {}
                                }
                            }
                        )
                    }
                }

                // Bottom padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Task Detail Bottom Sheet - show when taskDetailState is not null
        // Note: Dismiss handling is done in sheetState.confirmValueChange above
        uiState.taskDetailState?.let { taskDetailState ->
            TaskDetailSheet(
                state = taskDetailState,
                availableGoals = uiState.availableGoals,
                onEvent = { viewModel.onEvent(it) },
                onDismiss = { /* Handled by confirmValueChange in sheetState */ },
                sheetState = sheetState
            )
        }

        // Discard Changes Dialog - show when trying to dismiss with unsaved text changes
        if (uiState.showDiscardChangesDialog) {
            DiscardChangesDialog(
                onDiscard = { viewModel.onEvent(WeekEvent.DiscardChangesConfirmed) },
                onCancel = { viewModel.onEvent(WeekEvent.DiscardChangesCancelled) }
            )
        }

        // Add Task Modal - show when showAddTaskSheet is true
        if (uiState.showAddTaskSheet) {
            AddTaskModal(
                state = uiState.addTaskForm,
                availableGoals = uiState.availableGoals,
                onEvent = { viewModel.onEvent(it) },
                onDismiss = { viewModel.onEvent(WeekEvent.AddTaskSheetDismissed) },
                sheetState = addTaskSheetState
            )
        }
    }
}

// ============================================
// MAPPING EXTENSIONS
// ============================================

/**
 * Convert CalendarDay to WeekDayItem for UI component.
 */
private fun CalendarDay.toWeekDayItem(): WeekDayItem {
    return WeekDayItem(
        dayName = dayOfWeekLabel,
        dayNumber = dayNumber,
        isToday = isToday,
        isSelected = isSelected,
        hasTasks = hasTasks
    )
}

/**
 * Convert TaskUiModel to TaskUiItem for UI component.
 */
private fun TaskUiModel.toTaskUiItem(isOverdue: Boolean = false): TaskUiItem {
    // Format schedule text
    val scheduleText = when {
        scheduledTime != null -> {
            val hour = scheduledTime.hour
            val minute = scheduledTime.minute
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val minuteStr = if (minute > 0) ":${minute.toString().padStart(2, '0')}" else ""
            "$displayHour$minuteStr $amPm"
        }
        subtitleText != null -> subtitleText
        else -> null
    }

    return TaskUiItem(
        id = id,
        title = title,
        priority = priority,
        isCompleted = isCompleted,
        schedule = scheduleText,
        isOverdue = isOverdue,
        isRecurring = isRepeating,
        subtaskCount = if (subtaskCount > 0) subtaskCount else null,
        projectOrGoal = linkedGoalName ?: primaryLabelText
    )
}

// ============================================
// EMPTY STATE COMPONENT
// ============================================

@Composable
private fun EmptyStateContent(
    message: String,
    actionText: String?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (actionText != null) {
                androidx.compose.material3.TextButton(onClick = onActionClick) {
                    androidx.compose.material3.Text(actionText)
                }
            }
        }
    }
}

// ============================================
// DISCARD CHANGES DIALOG
// ============================================

/**
 * Dialog shown when user tries to dismiss task detail sheet with unsaved text changes.
 */
@Composable
private fun DiscardChangesDialog(
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Discard changes?") },
        text = { Text("You have unsaved changes to the task title or description. Are you sure you want to discard them?") },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text("Discard")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
