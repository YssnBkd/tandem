package org.epoque.tandem.ui.week

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.epoque.tandem.presentation.week.WeekEvent
import org.epoque.tandem.presentation.week.WeekSideEffect
import org.epoque.tandem.presentation.week.WeekViewModel
import org.epoque.tandem.presentation.week.model.Segment
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Week View screen.
 *
 * Following Android best practices:
 * - Uses Scaffold for Material 3 layout structure
 * - collectAsStateWithLifecycle for lifecycle-aware state collection
 * - PullToRefreshBox for Material 3 pull-to-refresh (v1.3.0+)
 * - Passes data down, events up (UDF pattern)
 * - Uses koinViewModel() for dependency injection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    viewModel: WeekViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    // Collect UI state with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Haptic feedback for tactile responses
    // Based on best practices from:
    // https://medium.com/@jpmtech/haptics-in-jetpack-compose-06ac8adaf985
    // https://www.sinasamaki.com/haptic-feedback-in-jetpack-compose/
    val hapticFeedback = LocalHapticFeedback.current

    // Snackbar host state for showing messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Single collector for all side effects (Channel can only be consumed once)
    // Following best practices: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is WeekSideEffect.TriggerHapticFeedback -> {
                    // Use LongPress for satisfying "thump" on task completion
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is WeekSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is WeekSideEffect.NavigateToPartnerInvite -> {
                    // TODO: Navigate to partner invite (future feature)
                }
                is WeekSideEffect.NavigateToRequestTask -> {
                    // TODO: Navigate to request task (future feature)
                }
                is WeekSideEffect.ClearFocus -> {
                    // Keyboard focus is cleared automatically by QuickAddField
                    // using LocalFocusManager.clearFocus()
                }
            }
        }
    }

    // Scroll behavior for collapsing toolbar (if needed in future)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            // FAB for adding tasks with details
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(WeekEvent.AddTaskSheetRequested)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task with details"
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.weekInfo != null) {
                        Text(
                            text = uiState.weekInfo!!.dateRangeText,
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else {
                        Text(
                            text = "Week View",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    // Progress indicator in top bar
                    Text(
                        text = uiState.progressText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        // Pull-to-refresh container
        // Following Material 3 best practices from:
        // https://developer.android.com/develop/ui/compose/components/pull-to-refresh
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                viewModel.onEvent(WeekEvent.RefreshRequested)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Error state
                uiState.error != null -> {
                    EmptyState(
                        message = uiState.error!!,
                        actionText = "Retry",
                        onActionClick = {
                            viewModel.onEvent(WeekEvent.RefreshRequested)
                        }
                    )
                }

                // Main content: always show segment control and quick add, with task list or empty state
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Segment control for switching between You/Partner/Shared
                        SegmentedControl(
                            selectedSegment = uiState.selectedSegment,
                            onSegmentSelected = { segment ->
                                viewModel.onEvent(WeekEvent.SegmentSelected(segment))
                            }
                        )

                        // Quick add field (hidden in Partner segment - read-only)
                        if (!uiState.isReadOnly) {
                            QuickAddField(
                                text = uiState.quickAddText,
                                onTextChange = { text ->
                                    viewModel.onEvent(WeekEvent.QuickAddTextChanged(text))
                                },
                                onSubmit = {
                                    viewModel.onEvent(WeekEvent.QuickAddSubmitted)
                                },
                                errorMessage = uiState.quickAddError
                            )
                        }

                        // "Request a Task" button for Partner segment
                        if (uiState.isReadOnly && uiState.hasPartner) {
                            RequestTaskButton(
                                onClick = {
                                    viewModel.onEvent(WeekEvent.RequestTaskFromPartnerTapped)
                                }
                            )
                        }

                        // Task list or empty state
                        if (uiState.showEmptyState) {
                            // Empty state shown inside the content area
                            EmptyState(
                                message = uiState.emptyStateMessage,
                                actionText = uiState.emptyStateActionText,
                                onActionClick = when (uiState.selectedSegment) {
                                    Segment.PARTNER -> {
                                        if (!uiState.hasPartner) {
                                            { viewModel.onEvent(WeekEvent.InvitePartnerTapped) }
                                        } else null
                                    }
                                    Segment.SHARED -> {
                                        { viewModel.onEvent(WeekEvent.AddTaskSheetRequested) }
                                    }
                                    else -> null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Task list
                            TaskList(
                                incompleteTasks = uiState.incompleteTasks,
                                completedTasks = uiState.completedTasks,
                                isReadOnly = uiState.isReadOnly,
                                onTaskClick = { taskId ->
                                    viewModel.onEvent(WeekEvent.TaskTapped(taskId))
                                },
                                onCheckboxClick = { taskId ->
                                    viewModel.onEvent(WeekEvent.TaskCheckboxTapped(taskId))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Task detail sheet
        if (uiState.showDetailSheet) {
            TaskDetailSheet(
                task = uiState.selectedTask,
                isReadOnly = uiState.isReadOnly,
                onDismiss = {
                    viewModel.onEvent(WeekEvent.DetailSheetDismissed)
                },
                onTitleChange = { title ->
                    viewModel.onEvent(WeekEvent.TaskTitleChanged(title))
                },
                onNotesChange = { notes ->
                    viewModel.onEvent(WeekEvent.TaskNotesChanged(notes))
                },
                onSaveRequested = {
                    viewModel.onEvent(WeekEvent.TaskSaveRequested)
                },
                onMarkCompleteRequested = {
                    viewModel.onEvent(WeekEvent.TaskMarkCompleteRequested)
                },
                onDeleteRequested = {
                    viewModel.onEvent(WeekEvent.TaskDeleteConfirmed)
                }
            )
        }

        // Add task sheet
        if (uiState.showAddTaskSheet) {
            AddTaskSheet(
                onDismiss = {
                    viewModel.onEvent(WeekEvent.AddTaskSheetDismissed)
                },
                onSubmit = { title, notes, ownerType ->
                    viewModel.onEvent(WeekEvent.AddTaskSubmitted(title, notes, ownerType))
                }
            )
        }
    }
}
