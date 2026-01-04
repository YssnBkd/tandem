package org.epoque.tandem.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalStatus
import org.epoque.tandem.domain.model.GoalType
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.GoalException
import org.epoque.tandem.domain.repository.GoalRepository
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.util.WeekCalculator

/**
 * ViewModel for the Goals screen.
 *
 * Manages goal display, creation, editing, and deletion.
 * Handles both personal goals and partner goal viewing (read-only).
 */
class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<GoalsSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<GoalsSideEffect> = _sideEffects.receiveAsFlow()

    private var currentUserId: String? = null

    init {
        initializeGoals()
    }

    private fun initializeGoals() {
        viewModelScope.launch {
            try {
                // Wait for authentication
                val userId = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                    .user.id

                currentUserId = userId

                // Process weekly resets and check expirations
                val currentWeekId = WeekCalculator.getWeekId()
                goalRepository.processWeeklyResets(currentWeekId)
                goalRepository.checkGoalExpirations(currentWeekId)

                // Observe own goals
                launch {
                    goalRepository.observeMyGoals(userId)
                        .collect { goals ->
                            _uiState.update { it.copy(myGoals = goals, isLoading = false) }
                        }
                }

                // Observe partner and their goals (read-only)
                launch {
                    partnerRepository.observePartner(userId)
                        .collect { partner ->
                            if (partner != null) {
                                _uiState.update { it.copy(hasPartner = true) }

                                // Sync and observe partner goals
                                goalRepository.syncPartnerGoals(partner.id)

                                launch {
                                    goalRepository.observePartnerGoals(partner.id)
                                        .collect { partnerGoals ->
                                            val lastSync = goalRepository.getPartnerGoalsLastSyncTime(partner.id)
                                            _uiState.update {
                                                it.copy(
                                                    partnerGoals = partnerGoals,
                                                    partnerGoalsLastSyncTime = lastSync
                                                )
                                            }
                                        }
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        hasPartner = false,
                                        partnerGoals = emptyList(),
                                        partnerGoalsLastSyncTime = null
                                    )
                                }
                            }
                        }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onEvent(event: GoalsEvent) {
        when (event) {
            is GoalsEvent.SegmentSelected -> handleSegmentSelected(event.segment)
            GoalsEvent.ToggleStatusFilter -> handleToggleStatusFilter()
            is GoalsEvent.GoalTapped -> handleGoalTapped(event.goalId, event.isPartnerGoal)
            GoalsEvent.AddGoalTapped -> handleAddGoalTapped()

            GoalsEvent.DismissAddGoalSheet -> dismissAddGoalSheet()
            is GoalsEvent.NewGoalNameChanged -> updateNewGoalName(event.name)
            is GoalsEvent.NewGoalIconChanged -> updateNewGoalIcon(event.icon)
            is GoalsEvent.NewGoalTypeChanged -> updateNewGoalType(event.type)
            is GoalsEvent.NewGoalDurationChanged -> updateNewGoalDuration(event.weeks)
            GoalsEvent.CreateGoal -> createGoal()

            GoalsEvent.DismissGoalDetail -> dismissGoalDetail()
            GoalsEvent.EditGoalTapped -> startEditGoal()
            GoalsEvent.DeleteGoalTapped -> showDeleteConfirmation()
            GoalsEvent.ConfirmDeleteGoal -> deleteGoal()
            GoalsEvent.CancelDeleteGoal -> hideDeleteConfirmation()

            is GoalsEvent.EditGoalNameChanged -> updateEditGoalName(event.name)
            is GoalsEvent.EditGoalIconChanged -> updateEditGoalIcon(event.icon)
            GoalsEvent.SaveGoalEdit -> saveGoalEdit()
            GoalsEvent.CancelGoalEdit -> cancelGoalEdit()

            GoalsEvent.DismissError -> dismissError()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Event Handlers
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleSegmentSelected(segment: GoalSegment) {
        _uiState.update { it.copy(selectedSegment = segment) }
    }

    private fun handleToggleStatusFilter() {
        _uiState.update { it.copy(showActiveOnly = !it.showActiveOnly) }
    }

    private fun handleGoalTapped(goalId: String, isPartnerGoal: Boolean) {
        viewModelScope.launch {
            val goal = if (isPartnerGoal) {
                goalRepository.getPartnerGoalById(goalId)
            } else {
                goalRepository.getGoalById(goalId)
            } ?: return@launch

            goalRepository.observeProgressHistory(goalId)
                .take(1)
                .collect { progress ->
                    _uiState.update {
                        it.copy(
                            selectedGoalId = goalId,
                            selectedGoal = goal,
                            selectedGoalProgress = progress,
                            showGoalDetail = true,
                            isViewingPartnerGoal = isPartnerGoal
                        )
                    }
                }
        }
    }

    private fun handleAddGoalTapped() {
        if (!_uiState.value.canCreateNewGoal) {
            viewModelScope.launch {
                _sideEffects.send(
                    GoalsSideEffect.ShowSnackbar(
                        "You can have up to 10 active goals. Complete or delete a goal to add a new one."
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                showAddGoalSheet = true,
                newGoalName = "",
                newGoalIcon = "\uD83C\uDFAF", // 🎯
                newGoalType = GoalType.WeeklyHabit(3),
                newGoalDuration = 4
            )
        }
    }

    private fun createGoal() {
        val state = _uiState.value
        val name = state.newGoalName.trim()

        if (name.isEmpty()) {
            viewModelScope.launch {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Please enter a goal name"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingGoal = true) }

            try {
                val userId = currentUserId ?: return@launch
                val currentWeekId = WeekCalculator.getWeekId()

                val goal = Goal(
                    id = "", // Generated by repository
                    name = name,
                    icon = state.newGoalIcon,
                    type = state.newGoalType,
                    durationWeeks = state.newGoalDuration,
                    startWeekId = currentWeekId,
                    ownerId = userId,
                    currentProgress = 0,
                    currentWeekId = currentWeekId,
                    status = GoalStatus.ACTIVE,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )

                goalRepository.createGoal(goal)

                _uiState.update {
                    it.copy(showAddGoalSheet = false, isCreatingGoal = false)
                }
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Goal created!"))
                _sideEffects.send(GoalsSideEffect.TriggerHapticFeedback)

            } catch (e: GoalException.LimitExceeded) {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar(e.message ?: "Goal limit reached"))
                _uiState.update { it.copy(isCreatingGoal = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Failed to create goal"))
                _uiState.update { it.copy(isCreatingGoal = false) }
            }
        }
    }

    private fun showDeleteConfirmation() {
        // Cannot delete partner's goals
        if (_uiState.value.isViewingPartnerGoal) {
            viewModelScope.launch {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Cannot delete partner's goal"))
            }
            return
        }
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    private fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    private fun deleteGoal() {
        val goalId = _uiState.value.selectedGoalId ?: return

        // Cannot delete partner's goals
        if (_uiState.value.isViewingPartnerGoal) {
            viewModelScope.launch {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Cannot delete partner's goal"))
            }
            return
        }

        viewModelScope.launch {
            try {
                goalRepository.deleteGoal(goalId)
                _uiState.update {
                    it.copy(
                        showGoalDetail = false,
                        showDeleteConfirmation = false,
                        selectedGoalId = null,
                        selectedGoal = null,
                        isViewingPartnerGoal = false
                    )
                }
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Goal deleted"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Failed to delete goal"))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI State Updates
    // ═══════════════════════════════════════════════════════════════════════════

    private fun dismissAddGoalSheet() {
        _uiState.update { it.copy(showAddGoalSheet = false) }
    }

    private fun updateNewGoalName(name: String) {
        _uiState.update { it.copy(newGoalName = name) }
    }

    private fun updateNewGoalIcon(icon: String) {
        _uiState.update { it.copy(newGoalIcon = icon) }
    }

    private fun updateNewGoalType(type: GoalType) {
        _uiState.update { it.copy(newGoalType = type) }
    }

    private fun updateNewGoalDuration(weeks: Int?) {
        _uiState.update { it.copy(newGoalDuration = weeks) }
    }

    private fun dismissGoalDetail() {
        _uiState.update {
            it.copy(
                showGoalDetail = false,
                selectedGoalId = null,
                selectedGoal = null,
                isEditingGoal = false,
                isViewingPartnerGoal = false,
                showDeleteConfirmation = false
            )
        }
    }

    private fun startEditGoal() {
        // Cannot edit partner's goals
        if (_uiState.value.isViewingPartnerGoal) {
            viewModelScope.launch {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Cannot edit partner's goal"))
            }
            return
        }

        val goal = _uiState.value.selectedGoal ?: return
        _uiState.update {
            it.copy(
                isEditingGoal = true,
                editGoalName = goal.name,
                editGoalIcon = goal.icon
            )
        }
    }

    private fun updateEditGoalName(name: String) {
        _uiState.update { it.copy(editGoalName = name) }
    }

    private fun updateEditGoalIcon(icon: String) {
        _uiState.update { it.copy(editGoalIcon = icon) }
    }

    private fun saveGoalEdit() {
        val goalId = _uiState.value.selectedGoalId ?: return
        val name = _uiState.value.editGoalName.trim()
        val icon = _uiState.value.editGoalIcon

        if (name.isEmpty()) {
            viewModelScope.launch {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Name cannot be empty"))
            }
            return
        }

        viewModelScope.launch {
            try {
                val updatedGoal = goalRepository.updateGoal(goalId, name, icon)
                _uiState.update {
                    it.copy(
                        isEditingGoal = false,
                        selectedGoal = updatedGoal
                    )
                }
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Goal updated"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(GoalsSideEffect.ShowSnackbar("Failed to update goal"))
            }
        }
    }

    private fun cancelGoalEdit() {
        _uiState.update { it.copy(isEditingGoal = false) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
