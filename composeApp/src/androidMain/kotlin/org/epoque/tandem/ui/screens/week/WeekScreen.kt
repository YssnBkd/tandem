package org.epoque.tandem.ui.screens.week

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.ui.components.week.CompletedSection
import org.epoque.tandem.ui.components.week.TaskRowItem
import org.epoque.tandem.ui.components.week.TaskSection
import org.epoque.tandem.ui.components.week.TaskSectionHeader
import org.epoque.tandem.ui.components.week.TaskUiItem
import org.epoque.tandem.ui.components.week.WeekDayItem
import org.epoque.tandem.ui.components.week.WeekDaySelector
import org.epoque.tandem.ui.components.week.WeekFab
import org.epoque.tandem.ui.components.week.WeekHeader
import org.epoque.tandem.ui.screens.week.AddTaskModal
import org.epoque.tandem.ui.screens.week.GoalProgressUiModel
import org.epoque.tandem.ui.screens.week.LabelUiModel
import org.epoque.tandem.ui.screens.week.SubtaskUiModel
import org.epoque.tandem.ui.screens.week.TaskDetailSheet
import org.epoque.tandem.ui.screens.week.TaskDetailUiModel

/**
 * Main Week View screen with Todoist-inspired UI redesign.
 * Uses mock data for visual iteration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    onNavigateToPlanning: () -> Unit = {},
    onNavigateToReview: () -> Unit = {},
    onNavigateToPartnerInvite: () -> Unit = {},
    onNavigateToPartnerSettings: () -> Unit = {},
    onNavigateToSeasons: () -> Unit = {},
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Local state
    var selectedDayIndex by remember { mutableIntStateOf(2) } // Tuesday selected
    var selectedSegment by remember { mutableStateOf(OwnerSegment.YOU) }
    var completedExpanded by remember { mutableStateOf(true) }

    // Task detail sheet state
    var showTaskDetail by remember { mutableStateOf(false) }
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    // Add task modal state
    var showAddTaskModal by remember { mutableStateOf(false) }
    val addTaskSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Mock task detail data (matching mockup)
    val mockTaskDetail = TaskDetailUiModel(
        id = "2",
        title = "Morning workout routine",
        description = "30 minutes cardio + 15 minutes stretching. Remember to warm up properly!",
        priority = TaskPriority.P1,
        owner = "Me",
        dueDate = "Today",
        project = "This Week",
        labels = listOf(LabelUiModel("Health", Color(0xFFD1453B))),
        goal = GoalProgressUiModel(
            emoji = "\uD83D\uDCAA",
            name = "Get fit together",
            current = 2,
            total = 3
        ),
        subtasks = listOf(
            SubtaskUiModel("st1", "Warm up (5 min)", isCompleted = true),
            SubtaskUiModel("st2", "Cardio session", isCompleted = false)
        )
    )

    // Mock data matching the mockup exactly
    val mockDays = listOf(
        WeekDayItem("SUN", 5, isToday = false, isSelected = false, hasTasks = true),
        WeekDayItem("MON", 6, isToday = false, isSelected = false, hasTasks = true),
        WeekDayItem("TUE", 7, isToday = true, isSelected = true, hasTasks = true),
        WeekDayItem("WED", 8, isToday = false, isSelected = false, hasTasks = true),
        WeekDayItem("THU", 9, isToday = false, isSelected = false, hasTasks = false),
        WeekDayItem("FRI", 10, isToday = false, isSelected = false, hasTasks = true),
        WeekDayItem("SAT", 11, isToday = false, isSelected = false, hasTasks = false)
    )

    val overdueTasks = listOf(
        TaskUiItem(
            id = "1",
            title = "Submit expense report",
            priority = TaskPriority.P1,
            schedule = "Yesterday",
            isOverdue = true,
            projectOrGoal = "Work"
        )
    )

    val todayTasks = listOf(
        TaskUiItem(
            id = "2",
            title = "Do 30 minutes of yoga \uD83E\uDDD8",
            priority = TaskPriority.P1,
            schedule = "7:30 AM",
            isRecurring = true,
            projectOrGoal = "Fitness"
        ),
        TaskUiItem(
            id = "3",
            title = "Review monthly budget",
            priority = TaskPriority.P2,
            schedule = "10:00 AM",
            projectOrGoal = "Finance"
        ),
        TaskUiItem(
            id = "4",
            title = "Call dentist to reschedule",
            priority = TaskPriority.P4,
            projectOrGoal = "Appointments"
        )
    )

    val tomorrowTasks = listOf(
        TaskUiItem(
            id = "5",
            title = "Grocery shopping \uD83D\uDED2",
            priority = TaskPriority.P2,
            subtaskCount = 3,
            projectOrGoal = "Groceries"
        ),
        TaskUiItem(
            id = "6",
            title = "Read chapter 5 \uD83D\uDCDA",
            priority = TaskPriority.P4,
            projectOrGoal = "Learning"
        )
    )

    val laterTasks = listOf(
        TaskUiItem(
            id = "7",
            title = "Clean apartment",
            priority = TaskPriority.P4,
            schedule = "Sat",
            projectOrGoal = "Home"
        )
    )

    val completedTasks = listOf(
        TaskUiItem(
            id = "8",
            title = "Call mom",
            priority = TaskPriority.P4,
            isCompleted = true
        ),
        TaskUiItem(
            id = "9",
            title = "Meal prep for week \uD83E\uDD57",
            priority = TaskPriority.P4,
            isCompleted = true,
            projectOrGoal = "Healthy eating"
        ),
        TaskUiItem(
            id = "10",
            title = "Schedule dentist appointment",
            priority = TaskPriority.P4,
            isCompleted = true,
            projectOrGoal = "Appointments"
        )
    )

    // Update day selection state
    val daysWithSelection = mockDays.mapIndexed { index, day ->
        day.copy(isSelected = index == selectedDayIndex)
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            WeekFab(onClick = { showAddTaskModal = true })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Week Header
            item {
                WeekHeader(
                    title = "This Week",
                    subtitle = "Jan 5 - 11 · 7 tasks",
                    seasonInfo = "\uD83C\uDF31 Q1 2026 · Week 3 of 12",
                    onSeasonClick = onNavigateToSeasons
                )
            }

            // Day Selector
            item {
                WeekDaySelector(
                    days = daysWithSelection,
                    onDaySelected = { index -> selectedDayIndex = index },
                    onPreviousWeek = { /* TODO */ },
                    onNextWeek = { /* TODO */ }
                )
            }

            // Segment Control
            item {
                SegmentedControl(
                    selectedSegment = selectedSegment,
                    onSegmentSelected = { selectedSegment = it }
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
                        onCheckedChange = { /* TODO */ },
                        onClick = {
                            selectedTaskId = task.id
                            showTaskDetail = true
                        }
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
                        onCheckedChange = { /* TODO */ },
                        onClick = {
                            selectedTaskId = task.id
                            showTaskDetail = true
                        }
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
                        onCheckedChange = { /* TODO */ },
                        onClick = {
                            selectedTaskId = task.id
                            showTaskDetail = true
                        }
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
                        onCheckedChange = { /* TODO */ },
                        onClick = {
                            selectedTaskId = task.id
                            showTaskDetail = true
                        }
                    )
                }
            }

            // Completed Section (collapsible)
            if (completedTasks.isNotEmpty()) {
                item {
                    CompletedSection(
                        completedCount = completedTasks.size,
                        expanded = completedExpanded,
                        onExpandToggle = { completedExpanded = !completedExpanded }
                    ) {
                        completedTasks.forEach { task ->
                            TaskRowItem(
                                task = task,
                                onCheckedChange = { /* TODO */ },
                                onClick = {
                            selectedTaskId = task.id
                            showTaskDetail = true
                        }
                            )
                        }
                    }
                }
            }

            // Bottom padding for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Task Detail Bottom Sheet
        if (showTaskDetail) {
            TaskDetailSheet(
                task = mockTaskDetail,
                sheetState = sheetState,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showTaskDetail = false
                    }
                },
                onComplete = {
                    scope.launch {
                        sheetState.hide()
                        showTaskDetail = false
                    }
                },
                onSkip = {
                    scope.launch {
                        sheetState.hide()
                        showTaskDetail = false
                    }
                }
            )
        }

        // Add Task Modal
        if (showAddTaskModal) {
            AddTaskModal(
                sheetState = addTaskSheetState,
                onDismiss = {
                    scope.launch {
                        addTaskSheetState.hide()
                        showAddTaskModal = false
                    }
                },
                onTaskCreated = { title, description, date, priority, project ->
                    // TODO: Actually create the task
                    scope.launch {
                        addTaskSheetState.hide()
                        showAddTaskModal = false
                    }
                }
            )
        }
    }
}

/**
 * Helper function to show task detail sheet.
 */
private fun showTaskDetailSheet(
    taskId: String,
    onShow: (String) -> Unit
) {
    onShow(taskId)
}
