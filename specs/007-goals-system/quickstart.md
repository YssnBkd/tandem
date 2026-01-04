# Quickstart: Goals System

**Feature**: 007-goals-system
**Date**: 2026-01-04

This guide provides copy-paste code snippets for implementing the Goals System feature.

## 1. Domain Models

### Goal.kt
```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Goal.kt
package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    val id: String,
    val name: String,
    val icon: String,
    val type: GoalType,
    val durationWeeks: Int?,
    val startWeekId: String,
    val ownerId: String,
    val currentProgress: Int,
    val currentWeekId: String,
    val status: GoalStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /** Target based on goal type */
    val target: Int get() = when (type) {
        is GoalType.WeeklyHabit -> type.targetPerWeek
        is GoalType.RecurringTask -> 1
        is GoalType.TargetAmount -> type.targetTotal
    }

    /** Progress fraction (0.0 to 1.0+) */
    val progressFraction: Float get() = if (target > 0) {
        currentProgress.toFloat() / target
    } else 0f

    /** Progress display text (e.g., "3/5" or "75/100") */
    val progressText: String get() = "$currentProgress/$target"

    /** Whether goal is still active */
    val isActive: Boolean get() = status == GoalStatus.ACTIVE

    /** Whether goal has met its target */
    val hasMetTarget: Boolean get() = currentProgress >= target

    /** Whether this is a weekly reset goal */
    val resetsWeekly: Boolean get() = type is GoalType.WeeklyHabit
}

@Serializable
sealed class GoalType {
    @Serializable
    data class WeeklyHabit(val targetPerWeek: Int) : GoalType()

    @Serializable
    data object RecurringTask : GoalType()

    @Serializable
    data class TargetAmount(val targetTotal: Int) : GoalType()
}

@Serializable
enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED
}
```

### GoalProgress.kt
```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/GoalProgress.kt
package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class GoalProgress(
    val id: String,
    val goalId: String,
    val weekId: String,
    val progressValue: Int,
    val targetValue: Int,
    val createdAt: Instant
) {
    val progressFraction: Float get() = if (targetValue > 0) {
        progressValue.toFloat() / targetValue
    } else 0f

    val progressText: String get() = "$progressValue/$targetValue"
}
```

---

## 2. Repository Interface

### GoalRepository.kt
```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/GoalRepository.kt
package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalStatus

interface GoalRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS - Own Goals (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    fun observeMyGoals(userId: String): Flow<List<Goal>>
    fun observeMyActiveGoals(userId: String): Flow<List<Goal>>
    fun observeGoal(goalId: String): Flow<Goal?>
    fun observeProgressHistory(goalId: String): Flow<List<GoalProgress>>

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS - Partner Goals (Read-Only)
    // ═══════════════════════════════════════════════════════════════════════════

    fun observePartnerGoals(partnerId: String): Flow<List<Goal>>
    suspend fun getPartnerGoalById(goalId: String): Goal?
    suspend fun getPartnerGoalsLastSyncTime(partnerId: String): kotlinx.datetime.Instant?

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun getGoalById(goalId: String): Goal?
    suspend fun getActiveGoalCount(userId: String): Int
    suspend fun getGoalsById(goalIds: List<String>): Map<String, Goal>

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS (Own Goals Only)
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun createGoal(goal: Goal): Goal
    suspend fun updateGoal(goalId: String, name: String, icon: String): Goal?
    suspend fun incrementProgress(goalId: String, amount: Int = 1): Goal?
    suspend fun updateStatus(goalId: String, status: GoalStatus): Goal?
    suspend fun recordWeeklyProgress(
        goalId: String,
        progressValue: Int,
        targetValue: Int,
        weekId: String
    )
    suspend fun resetWeeklyProgress(goalId: String, newWeekId: String)
    suspend fun deleteGoal(goalId: String): Boolean

    // ═══════════════════════════════════════════════════════════════════════════
    // SYNC OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun syncPartnerGoals(partnerId: String)
    suspend fun clearPartnerGoalCache(partnerId: String)

    // ═══════════════════════════════════════════════════════════════════════════
    // MAINTENANCE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun processWeeklyResets(currentWeekId: String)
    suspend fun checkGoalExpirations(currentWeekId: String)
}

sealed class GoalException(message: String) : Exception(message) {
    object LimitExceeded : GoalException("Maximum of 10 active goals reached")
    data class InvalidGoal(override val message: String) : GoalException(message)
    object NotFound : GoalException("Goal not found")
    object NotOwner : GoalException("Cannot modify goals you don't own")
}
```

---

## 3. Presentation Layer

### GoalsUiState.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsUiState.kt
package org.epoque.tandem.presentation.goals

import kotlinx.datetime.Instant
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalType

data class GoalsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Goal lists
    val myGoals: List<Goal> = emptyList(),
    val partnerGoals: List<Goal> = emptyList(),

    // Segment selection
    val selectedSegment: GoalSegment = GoalSegment.YOURS,

    // Add goal sheet
    val showAddGoalSheet: Boolean = false,
    val newGoalName: String = "",
    val newGoalIcon: String = "🎯",
    val newGoalType: GoalType = GoalType.WeeklyHabit(3),
    val newGoalDuration: Int? = 4,
    val isCreatingGoal: Boolean = false,

    // Goal detail
    val selectedGoalId: String? = null,
    val selectedGoal: Goal? = null,
    val selectedGoalProgress: List<GoalProgress> = emptyList(),
    val showGoalDetail: Boolean = false,
    val isViewingPartnerGoal: Boolean = false,

    // Edit goal
    val isEditingGoal: Boolean = false,
    val editGoalName: String = "",
    val editGoalIcon: String = "",

    // Partner state
    val hasPartner: Boolean = false,
    val partnerGoalsLastSyncTime: Instant? = null
) {
    val displayedGoals: List<Goal> get() = when (selectedSegment) {
        GoalSegment.YOURS -> myGoals
        GoalSegment.PARTNERS -> partnerGoals
    }

    val showEmptyState: Boolean get() = !isLoading && displayedGoals.isEmpty()

    val emptyStateMessage: String get() = when (selectedSegment) {
        GoalSegment.YOURS -> "No goals yet.\nTap + to create your first goal!"
        GoalSegment.PARTNERS -> if (hasPartner) {
            "Your partner hasn't created any goals yet."
        } else {
            "Connect with a partner to see their goals."
        }
    }

    val activeGoalCount: Int get() = myGoals.count { it.isActive }

    val canCreateNewGoal: Boolean get() = activeGoalCount < 10

    /** Whether the currently selected goal can be edited (own goals only) */
    val canEditSelectedGoal: Boolean get() = !isViewingPartnerGoal
}

enum class GoalSegment {
    YOURS,
    PARTNERS
}
```

### GoalsEvent.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsEvent.kt
package org.epoque.tandem.presentation.goals

import org.epoque.tandem.domain.model.GoalType

sealed interface GoalsEvent {
    // Segment
    data class SegmentSelected(val segment: GoalSegment) : GoalsEvent

    // Goal list
    data class GoalTapped(val goalId: String, val isPartnerGoal: Boolean = false) : GoalsEvent
    object AddGoalTapped : GoalsEvent

    // Add goal sheet
    object DismissAddGoalSheet : GoalsEvent
    data class NewGoalNameChanged(val name: String) : GoalsEvent
    data class NewGoalIconChanged(val icon: String) : GoalsEvent
    data class NewGoalTypeChanged(val type: GoalType) : GoalsEvent
    data class NewGoalDurationChanged(val weeks: Int?) : GoalsEvent
    object CreateGoal : GoalsEvent

    // Goal detail
    object DismissGoalDetail : GoalsEvent
    object EditGoalTapped : GoalsEvent
    object DeleteGoalTapped : GoalsEvent
    object ConfirmDeleteGoal : GoalsEvent

    // Edit goal
    data class EditGoalNameChanged(val name: String) : GoalsEvent
    data class EditGoalIconChanged(val icon: String) : GoalsEvent
    object SaveGoalEdit : GoalsEvent
    object CancelGoalEdit : GoalsEvent

    // Error handling
    object DismissError : GoalsEvent
}
```

### GoalsSideEffect.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsSideEffect.kt
package org.epoque.tandem.presentation.goals

sealed interface GoalsSideEffect {
    data class ShowSnackbar(val message: String) : GoalsSideEffect
    data class NavigateToDetail(val goalId: String) : GoalsSideEffect
    object NavigateBack : GoalsSideEffect
    object TriggerHapticFeedback : GoalsSideEffect
}
```

### GoalsViewModel.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsViewModel.kt
package org.epoque.tandem.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.epoque.tandem.domain.model.*
import org.epoque.tandem.domain.repository.*
import org.epoque.tandem.domain.util.WeekCalculator
import kotlin.coroutines.cancellation.CancellationException

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = Channel<GoalsSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

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

                // Process weekly resets
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
                    partnerRepository.observePartner()
                        .collect { partner ->
                            if (partner != null) {
                                _uiState.update { it.copy(hasPartner = true) }

                                // Sync and observe partner goals
                                goalRepository.syncPartnerGoals(partner.id)
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
            GoalsEvent.DeleteGoalTapped -> { /* Show confirmation dialog */ }
            GoalsEvent.ConfirmDeleteGoal -> deleteGoal()

            is GoalsEvent.EditGoalNameChanged -> updateEditGoalName(event.name)
            is GoalsEvent.EditGoalIconChanged -> updateEditGoalIcon(event.icon)
            GoalsEvent.SaveGoalEdit -> saveGoalEdit()
            GoalsEvent.CancelGoalEdit -> cancelGoalEdit()

            GoalsEvent.DismissError -> dismissError()
        }
    }

    private fun handleSegmentSelected(segment: GoalSegment) {
        _uiState.update { it.copy(selectedSegment = segment) }
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
                newGoalIcon = "🎯",
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
                val userId = authRepository.currentUser?.id ?: return@launch
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

    // Helper methods
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
                isViewingPartnerGoal = false
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
                goalRepository.updateGoal(goalId, name, icon)
                _uiState.update { it.copy(isEditingGoal = false) }
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
```

---

## 4. UI Components

### GoalsScreen.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalsScreen.kt
package org.epoque.tandem.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.presentation.goals.GoalSegment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goals: List<Goal>,
    selectedSegment: GoalSegment,
    showEmptyState: Boolean,
    emptyStateMessage: String,
    hasPartner: Boolean,
    onSegmentSelected: (GoalSegment) -> Unit,
    onGoalTapped: (goalId: String, isPartnerGoal: Boolean) -> Unit,
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                actions = {
                    // Only show Add button on "Yours" segment
                    if (selectedSegment == GoalSegment.YOURS) {
                        IconButton(onClick = onAddGoal) {
                            Icon(Icons.Default.Add, contentDescription = "Add Goal")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segment control
            SegmentedButtonRow(
                selectedSegment = selectedSegment,
                hasPartner = hasPartner,
                onSegmentSelected = onSegmentSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (showEmptyState) {
                EmptyGoalsState(
                    message = emptyStateMessage,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = {
                                onGoalTapped(goal.id, selectedSegment == GoalSegment.PARTNERS)
                            }
                        )
                    }
                }
            }
        }
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
private fun EmptyGoalsState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}
```

### GoalCard.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalCard.kt
package org.epoque.tandem.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = goal.icon,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { goal.progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress text and type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = goal.progressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = when (goal.type) {
                            is GoalType.WeeklyHabit -> "This week"
                            is GoalType.RecurringTask -> if (goal.currentProgress > 0) "Done" else "This week"
                            is GoalType.TargetAmount -> "Total"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

### AddGoalSheet.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/AddGoalSheet.kt
package org.epoque.tandem.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.GoalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalSheet(
    name: String,
    icon: String,
    type: GoalType,
    durationWeeks: Int?,
    isCreating: Boolean,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onTypeChange: (GoalType) -> Unit,
    onDurationChange: (Int?) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Goal",
                style = MaterialTheme.typography.headlineSmall
            )

            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Goal name") },
                placeholder = { Text("e.g., Exercise regularly") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Icon picker (simplified)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("🎯", "💪", "📚", "🏃", "🧘", "💼", "🎨", "🎵").forEach { emoji ->
                    FilterChip(
                        selected = icon == emoji,
                        onClick = { onIconChange(emoji) },
                        label = { Text(emoji) }
                    )
                }
            }

            // Goal type selector
            GoalTypeSelector(
                selectedType = type,
                onTypeSelected = onTypeChange
            )

            // Duration selector
            DurationSelector(
                selectedDuration = durationWeeks,
                onDurationSelected = onDurationChange
            )

            // Create button
            Button(
                onClick = onCreate,
                enabled = name.isNotBlank() && !isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Goal")
                }
            }
        }
    }
}

@Composable
private fun GoalTypeSelector(
    selectedType: GoalType,
    onTypeSelected: (GoalType) -> Unit
) {
    Column {
        Text(
            text = "Goal type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoalTypeChip(
                label = "Weekly",
                selected = selectedType is GoalType.WeeklyHabit,
                onClick = { onTypeSelected(GoalType.WeeklyHabit(3)) }
            )
            GoalTypeChip(
                label = "Recurring",
                selected = selectedType is GoalType.RecurringTask,
                onClick = { onTypeSelected(GoalType.RecurringTask) }
            )
            GoalTypeChip(
                label = "Target",
                selected = selectedType is GoalType.TargetAmount,
                onClick = { onTypeSelected(GoalType.TargetAmount(10)) }
            )
        }

        // Target input for applicable types
        when (selectedType) {
            is GoalType.WeeklyHabit -> {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedType.targetPerWeek.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { target ->
                            if (target in 1..99) {
                                onTypeSelected(GoalType.WeeklyHabit(target))
                            }
                        }
                    },
                    label = { Text("Times per week") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(150.dp)
                )
            }
            is GoalType.TargetAmount -> {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedType.targetTotal.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { target ->
                            if (target in 1..9999) {
                                onTypeSelected(GoalType.TargetAmount(target))
                            }
                        }
                    },
                    label = { Text("Total target") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(150.dp)
                )
            }
            is GoalType.RecurringTask -> { /* No additional input */ }
        }
    }
}

@Composable
private fun GoalTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun DurationSelector(
    selectedDuration: Int?,
    onDurationSelected: (Int?) -> Unit
) {
    Column {
        Text(
            text = "Duration",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4 to "4 weeks", 8 to "8 weeks", 12 to "12 weeks", null to "Ongoing").forEach { (weeks, label) ->
                FilterChip(
                    selected = selectedDuration == weeks,
                    onClick = { onDurationSelected(weeks) },
                    label = { Text(label) }
                )
            }
        }
    }
}
```

---

## 5. Navigation

### GoalsNavGraph.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/GoalsNavGraph.kt
package org.epoque.tandem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.epoque.tandem.presentation.goals.GoalSegment
import org.epoque.tandem.presentation.goals.GoalsEvent
import org.epoque.tandem.presentation.goals.GoalsSideEffect
import org.epoque.tandem.presentation.goals.GoalsUiState
import org.epoque.tandem.ui.goals.*

/**
 * Navigation graph for goals screens.
 */
fun NavGraphBuilder.goalsNavGraph(
    navController: NavController,
    stateProvider: () -> GoalsUiState,
    onEvent: (GoalsEvent) -> Unit,
    sideEffects: kotlinx.coroutines.flow.Flow<GoalsSideEffect>
) {
    composable<Routes.Goals.List> {
        val state = stateProvider()

        GoalsScreen(
            goals = state.displayedGoals,
            selectedSegment = state.selectedSegment,
            showEmptyState = state.showEmptyState,
            emptyStateMessage = state.emptyStateMessage,
            hasPartner = state.hasPartner,
            onSegmentSelected = { onEvent(GoalsEvent.SegmentSelected(it)) },
            onGoalTapped = { goalId, isPartnerGoal ->
                onEvent(GoalsEvent.GoalTapped(goalId, isPartnerGoal))
            },
            onAddGoal = { onEvent(GoalsEvent.AddGoalTapped) }
        )

        if (state.showAddGoalSheet) {
            AddGoalSheet(
                name = state.newGoalName,
                icon = state.newGoalIcon,
                type = state.newGoalType,
                durationWeeks = state.newGoalDuration,
                isCreating = state.isCreatingGoal,
                onNameChange = { onEvent(GoalsEvent.NewGoalNameChanged(it)) },
                onIconChange = { onEvent(GoalsEvent.NewGoalIconChanged(it)) },
                onTypeChange = { onEvent(GoalsEvent.NewGoalTypeChanged(it)) },
                onDurationChange = { onEvent(GoalsEvent.NewGoalDurationChanged(it)) },
                onCreate = { onEvent(GoalsEvent.CreateGoal) },
                onDismiss = { onEvent(GoalsEvent.DismissAddGoalSheet) }
            )
        }
    }

    composable<Routes.Goals.Detail> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Goals.Detail>()
        val state = stateProvider()

        LaunchedEffect(route.goalId) {
            onEvent(GoalsEvent.GoalTapped(route.goalId, route.isPartnerGoal))
        }

        state.selectedGoal?.let { goal ->
            GoalDetailScreen(
                goal = goal,
                progressHistory = state.selectedGoalProgress,
                isEditing = state.isEditingGoal,
                canEdit = state.canEditSelectedGoal,
                editName = state.editGoalName,
                editIcon = state.editGoalIcon,
                onEditNameChange = { onEvent(GoalsEvent.EditGoalNameChanged(it)) },
                onEditIconChange = { onEvent(GoalsEvent.EditGoalIconChanged(it)) },
                onEditTapped = { onEvent(GoalsEvent.EditGoalTapped) },
                onSaveEdit = { onEvent(GoalsEvent.SaveGoalEdit) },
                onCancelEdit = { onEvent(GoalsEvent.CancelGoalEdit) },
                onDeleteTapped = { onEvent(GoalsEvent.DeleteGoalTapped) },
                onConfirmDelete = { onEvent(GoalsEvent.ConfirmDeleteGoal) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// Routes extension
sealed interface Goals : Routes {
    @Serializable
    data object List : Goals

    @Serializable
    data class Detail(val goalId: String, val isPartnerGoal: Boolean = false) : Goals
}
```

---

## 6. Koin Module

### GoalsModule.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/di/GoalsModule.kt
package org.epoque.tandem.di

import org.epoque.tandem.data.repository.GoalRepositoryImpl
import org.epoque.tandem.domain.repository.GoalRepository
import org.epoque.tandem.presentation.goals.GoalsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val goalsModule = module {
    // Repository
    single<GoalRepository> { GoalRepositoryImpl(get(), get()) }

    // ViewModel
    viewModel {
        GoalsViewModel(
            goalRepository = get(),
            authRepository = get(),
            partnerRepository = get()
        )
    }
}
```

---

## Summary

This quickstart provides the essential code for:

1. **Domain Models**: Goal, GoalType, GoalProgress, GoalStatus (no shared goals)
2. **Repository Interface**: GoalRepository with separate own/partner goal operations
3. **ViewModel**: Complete MVI implementation with partner goal visibility (read-only)
4. **UI Screens**: GoalsScreen, GoalCard, AddGoalSheet with "Yours" / "Partner's" segments
5. **Navigation**: Type-safe routes with partner goal flag
6. **DI**: Koin module configuration

**Key Changes from Original Design**:
- Removed `isShared` field from Goal
- Segment control changed from "Yours" / "Shared" to "Yours" / "Partner's"
- Partner goals are read-only (no edit/delete/link tasks)
- Removed shared goal toggle from AddGoalSheet
- Added `isViewingPartnerGoal` state for detail screen behavior

Refer to `data-model.md` for schema details and `contracts/goals-api.md` for SQLDelight setup.
