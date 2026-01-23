package org.epoque.tandem.ui.legacy.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.presentation.goals.GoalSegment
import org.epoque.tandem.presentation.goals.GoalsEvent
import org.epoque.tandem.presentation.goals.GoalsSideEffect
import org.epoque.tandem.presentation.goals.GoalsUiState
import org.epoque.tandem.presentation.goals.GoalsViewModel
import org.epoque.tandem.ui.legacy.goals.components.EmptyGoalsState
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Goals screen with segment control for "Yours" / "Partner's" goals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = koinViewModel(),
    onNavigateToGoalDetail: (goalId: String, isPartnerGoal: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is GoalsSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is GoalsSideEffect.NavigateToDetail -> {
                    onNavigateToGoalDetail(effect.goalId, effect.isPartnerGoal)
                }
                is GoalsSideEffect.NavigateBack -> {
                    // Handled by navigation
                }
                is GoalsSideEffect.TriggerHapticFeedback -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    GoalsScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalsScreenContent(
    uiState: GoalsUiState,
    onEvent: (GoalsEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                actions = {
                    // Status filter toggle
                    IconButton(
                        onClick = { onEvent(GoalsEvent.ToggleStatusFilter) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = if (uiState.showActiveOnly) "Show all goals" else "Show active only",
                            tint = if (uiState.showActiveOnly) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    // Only show Add button on "Yours" segment
                    if (uiState.selectedSegment == GoalSegment.YOURS) {
                        IconButton(
                            onClick = { onEvent(GoalsEvent.AddGoalTapped) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Goal")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segment control
            SegmentedButtonRow(
                selectedSegment = uiState.selectedSegment,
                hasPartner = uiState.hasPartner,
                onSegmentSelected = { onEvent(GoalsEvent.SegmentSelected(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.showEmptyState) {
                EmptyGoalsState(
                    message = uiState.emptyStateMessage,
                    modifier = Modifier.weight(1f)
                )
            } else {
                GoalsList(
                    goals = uiState.displayedGoals,
                    isPartnerSegment = uiState.selectedSegment == GoalSegment.PARTNERS,
                    onGoalTapped = { goalId, isPartnerGoal ->
                        onEvent(GoalsEvent.GoalTapped(goalId, isPartnerGoal))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Show Add Goal Sheet when needed
    if (uiState.showAddGoalSheet) {
        AddGoalSheet(
            name = uiState.newGoalName,
            icon = uiState.newGoalIcon,
            type = uiState.newGoalType,
            durationWeeks = uiState.newGoalDuration,
            isCreating = uiState.isCreatingGoal,
            onNameChange = { onEvent(GoalsEvent.NewGoalNameChanged(it)) },
            onIconChange = { onEvent(GoalsEvent.NewGoalIconChanged(it)) },
            onTypeChange = { onEvent(GoalsEvent.NewGoalTypeChanged(it)) },
            onDurationChange = { onEvent(GoalsEvent.NewGoalDurationChanged(it)) },
            onCreate = { onEvent(GoalsEvent.CreateGoal) },
            onDismiss = { onEvent(GoalsEvent.DismissAddGoalSheet) }
        )
    }

    // Show Goal Detail Sheet when a goal is selected
    val selectedGoal = uiState.selectedGoal
    if (uiState.showGoalDetail && selectedGoal != null) {
        GoalDetailSheet(
            goal = selectedGoal,
            progressHistory = uiState.selectedGoalProgress,
            isPartnerGoal = uiState.isViewingPartnerGoal,
            isEditing = uiState.isEditingGoal,
            editName = uiState.editGoalName,
            editIcon = uiState.editGoalIcon,
            showDeleteConfirmation = uiState.showDeleteConfirmation,
            onEditTapped = { onEvent(GoalsEvent.EditGoalTapped) },
            onDeleteTapped = { onEvent(GoalsEvent.DeleteGoalTapped) },
            onConfirmDelete = { onEvent(GoalsEvent.ConfirmDeleteGoal) },
            onCancelDelete = { onEvent(GoalsEvent.CancelDeleteGoal) },
            onEditNameChange = { onEvent(GoalsEvent.EditGoalNameChanged(it)) },
            onEditIconChange = { onEvent(GoalsEvent.EditGoalIconChanged(it)) },
            onSaveEdit = { onEvent(GoalsEvent.SaveGoalEdit) },
            onCancelEdit = { onEvent(GoalsEvent.CancelGoalEdit) },
            onDismiss = { onEvent(GoalsEvent.DismissGoalDetail) }
        )
    }
}

@Composable
private fun SegmentedButtonRow(
    selectedSegment: GoalSegment,
    hasPartner: Boolean,
    onSegmentSelected: (GoalSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GoalSegment.entries.forEach { segment ->
            val enabled = segment != GoalSegment.PARTNERS || hasPartner

            FilterChip(
                selected = selectedSegment == segment,
                onClick = { if (enabled) onSegmentSelected(segment) },
                label = {
                    Text(
                        when (segment) {
                            GoalSegment.YOURS -> "Yours"
                            GoalSegment.PARTNERS -> "Partner's"
                        }
                    )
                },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GoalsList(
    goals: List<Goal>,
    isPartnerSegment: Boolean,
    onGoalTapped: (goalId: String, isPartnerGoal: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(goals, key = { it.id }) { goal ->
            GoalCard(
                goal = goal,
                onClick = { onGoalTapped(goal.id, isPartnerSegment) }
            )
        }
    }
}
