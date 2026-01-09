package org.epoque.tandem.ui.legacy.planning

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.epoque.tandem.presentation.planning.PlanningEvent
import org.epoque.tandem.presentation.planning.PlanningSideEffect
import org.epoque.tandem.presentation.planning.PlanningStep
import org.epoque.tandem.presentation.planning.PlanningViewModel
import org.epoque.tandem.ui.legacy.planning.components.ProgressDots
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main container screen for the weekly planning wizard.
 *
 * Following Android best practices:
 * - Uses Scaffold for Material 3 layout structure
 * - collectAsStateWithLifecycle for lifecycle-aware state collection
 * - Single LaunchedEffect for side effects (Channel can only be consumed once)
 * - UDF pattern: data flows down, events flow up
 *
 * @param onNavigateBack Callback when user wants to exit planning
 * @param viewModel PlanningViewModel instance from Koin
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlanningViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Single collector for all side effects
    // Following best practices: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is PlanningSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is PlanningSideEffect.NavigateToStep -> {
                    // Step navigation is handled by updating uiState.currentStep
                    // The UI automatically renders the correct step screen
                }
                is PlanningSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is PlanningSideEffect.ExitPlanning -> {
                    onNavigateBack()
                }
                is PlanningSideEffect.TriggerHapticFeedback -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is PlanningSideEffect.ClearFocus -> {
                    focusManager.clearFocus()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = getStepTitle(uiState.currentStep),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.onEvent(PlanningEvent.BackPressed) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    ProgressDots(
                        totalSteps = 4,
                        currentStep = uiState.currentStep.ordinal,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // Render the appropriate step screen based on current step
                    when (uiState.currentStep) {
                        PlanningStep.ROLLOVER -> {
                            RolloverStepScreen(
                                currentTask = uiState.rolloverTasks.getOrNull(uiState.currentRolloverIndex),
                                currentIndex = uiState.currentRolloverIndex,
                                totalTasks = uiState.rolloverTasks.size,
                                onAddToWeek = {
                                    val taskId = uiState.rolloverTasks.getOrNull(uiState.currentRolloverIndex)?.id
                                    if (taskId != null) {
                                        viewModel.onEvent(PlanningEvent.RolloverTaskAdded(taskId))
                                    }
                                },
                                onSkip = {
                                    val taskId = uiState.rolloverTasks.getOrNull(uiState.currentRolloverIndex)?.id
                                    if (taskId != null) {
                                        viewModel.onEvent(PlanningEvent.RolloverTaskSkipped(taskId))
                                    }
                                },
                                onStepComplete = {
                                    viewModel.onEvent(PlanningEvent.RolloverStepComplete)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        PlanningStep.ADD_TASKS -> {
                            AddTasksStepScreen(
                                taskText = uiState.newTaskText,
                                taskError = uiState.newTaskError,
                                addedTasks = uiState.addedTasks,
                                goalSuggestions = uiState.goalSuggestions,
                                selectedGoal = uiState.selectedGoalForNewTask,
                                onTextChange = { text ->
                                    viewModel.onEvent(PlanningEvent.NewTaskTextChanged(text))
                                },
                                onAddTask = {
                                    viewModel.onEvent(PlanningEvent.NewTaskSubmitted)
                                },
                                onGoalSelected = { goal ->
                                    viewModel.onEvent(PlanningEvent.GoalSuggestionSelected(goal.id))
                                },
                                onClearGoal = {
                                    viewModel.onEvent(PlanningEvent.ClearSelectedGoal)
                                },
                                onDone = {
                                    viewModel.onEvent(PlanningEvent.DoneAddingTasks)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        PlanningStep.PARTNER_REQUESTS -> {
                            PartnerRequestsStepScreen(
                                currentRequest = uiState.partnerRequests.getOrNull(uiState.currentRequestIndex),
                                currentIndex = uiState.currentRequestIndex,
                                totalRequests = uiState.partnerRequests.size,
                                onAccept = {
                                    val taskId = uiState.partnerRequests.getOrNull(uiState.currentRequestIndex)?.id
                                    if (taskId != null) {
                                        viewModel.onEvent(PlanningEvent.PartnerRequestAccepted(taskId))
                                    }
                                },
                                onDiscuss = {
                                    val taskId = uiState.partnerRequests.getOrNull(uiState.currentRequestIndex)?.id
                                    if (taskId != null) {
                                        viewModel.onEvent(PlanningEvent.PartnerRequestDiscussed(taskId))
                                    }
                                },
                                onStepComplete = {
                                    viewModel.onEvent(PlanningEvent.PartnerRequestsStepComplete)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        PlanningStep.CONFIRMATION -> {
                            ConfirmationStepScreen(
                                totalTasksPlanned = uiState.totalTasksPlanned,
                                rolloverTasksAdded = uiState.rolloverTasksAdded,
                                newTasksCreated = uiState.newTasksCreated,
                                partnerRequestsAccepted = uiState.partnerRequestsAccepted,
                                onDone = {
                                    viewModel.onEvent(PlanningEvent.PlanningCompleted)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Get the title for the current planning step.
 */
private fun getStepTitle(step: PlanningStep): String {
    return when (step) {
        PlanningStep.ROLLOVER -> "Review Last Week"
        PlanningStep.ADD_TASKS -> "Add Tasks"
        PlanningStep.PARTNER_REQUESTS -> "Partner Requests"
        PlanningStep.CONFIRMATION -> "You're All Set!"
    }
}
