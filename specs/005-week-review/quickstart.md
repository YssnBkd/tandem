# Quickstart: Week Review Implementation

**Feature**: 005-week-review | **Date**: 2026-01-03

## Document Purpose

**This document provides complete code samples for UI components and screens.** Use it as a copy-paste reference when implementing tasks.

| For... | Use... |
|--------|--------|
| Task sequence and dependencies | [tasks.md](./tasks.md) ← **Start here** |
| Complete code samples | **quickstart.md** (this file) |
| State classes and DataStore | [data-model.md](./data-model.md) |
| ViewModel operations | [contracts/review-operations.md](./contracts/review-operations.md) |
| Design rationale | [research.md](./research.md) |

## Prerequisites

- Feature 001 (Core Infrastructure) implemented - AuthRepository available
- Feature 002 (Task Data Layer) implemented - TaskRepository, WeekRepository available
- Feature 004 (Week Planning) implemented - Navigation patterns established

## Code Samples Index

| Task | Description | Estimated Complexity |
|------|-------------|---------------------|
| 1 | [Create Use Cases](#task-1-create-use-cases) | Low |
| 2 | [Create UI State and Events](#task-2-create-ui-state-and-events) | Low |
| 3 | [Create DataStore for Progress](#task-3-create-datastore-for-progress) | Low |
| 4 | [Implement ReviewViewModel](#task-4-implement-reviewviewmodel) | Medium |
| 5 | [Create Review Banner Component](#task-5-create-review-banner-component) | Low |
| 6 | [Create Emoji Rating Selector](#task-6-create-emoji-rating-selector) | Low |
| 7 | [Create Task Outcome Card](#task-7-create-task-outcome-card) | Medium |
| 8 | [Create Review Screens](#task-8-create-review-screens) | Medium |
| 9 | [Create Navigation Graph](#task-9-create-navigation-graph) | Low |
| 10 | [Create Koin Module](#task-10-create-koin-module) | Low |
| 11 | [Integrate with Week View](#task-11-integrate-with-week-view) | Low |
| 12 | [Build Validation](#task-12-build-validation) | Low |

---

## Task 1: Create Use Cases

**Files to create:**
- `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/CalculateStreakUseCase.kt`
- `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/IsReviewWindowOpenUseCase.kt`
- `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/GetReviewStatsUseCase.kt`

**Reference:** [data-model.md → New Use Cases](./data-model.md#6-new-use-cases)

### CalculateStreakUseCase

```kotlin
package org.epoque.tandem.domain.usecase.review

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.repository.WeekRepository

class CalculateStreakUseCase(
    private val weekRepository: WeekRepository
) {
    suspend operator fun invoke(userId: String): Int {
        val weeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .sortedByDescending { it.startDate }

        var streak = 0
        for (week in weeks) {
            if (week.isReviewed) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
```

### IsReviewWindowOpenUseCase

```kotlin
package org.epoque.tandem.domain.usecase.review

import kotlinx.datetime.*

class IsReviewWindowOpenUseCase(
    private val clock: Clock = Clock.System
) {
    operator fun invoke(): Boolean {
        val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayOfWeek = now.dayOfWeek
        val hour = now.hour

        return when (dayOfWeek) {
            DayOfWeek.FRIDAY -> hour >= 18
            DayOfWeek.SATURDAY -> true
            DayOfWeek.SUNDAY -> true
            else -> false
        }
    }
}
```

**Verification:** Unit test streak calculation with mock weeks.

---

## Task 2: Create UI State and Events

**Files to create:**
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewUiState.kt`
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewEvent.kt`
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewSideEffect.kt`

**Reference:** [data-model.md → UI State Classes](./data-model.md#2-ui-state-classes)

### ReviewUiState.kt

```kotlin
package org.epoque.tandem.presentation.review

import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.model.Week

data class ReviewUiState(
    val reviewMode: ReviewMode = ReviewMode.SOLO,
    val currentStep: ReviewStep = ReviewStep.MODE_SELECT,
    val currentWeek: Week? = null,
    val isReviewWindowOpen: Boolean = false,
    val overallRating: Int? = null,
    val overallNote: String = "",
    val tasksToReview: List<Task> = emptyList(),
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, TaskStatus> = emptyMap(),
    val taskNotes: Map<String, String> = emptyMap(),
    val currentStreak: Int = 0,
    val completionPercentage: Int = 0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val hasIncompleteProgress: Boolean = false
) {
    val currentTask: Task? get() = tasksToReview.getOrNull(currentTaskIndex)
    val totalTasks: Int get() = tasksToReview.size
    val reviewedTaskCount: Int get() = taskOutcomes.size
    val canProceedFromRating: Boolean get() = overallRating != null
    val isLastTask: Boolean get() = currentTaskIndex >= tasksToReview.size - 1
    val doneCount: Int get() = taskOutcomes.count { it.value == TaskStatus.COMPLETED }
}

enum class ReviewMode { SOLO, TOGETHER }
enum class ReviewStep { MODE_SELECT, RATING, TASK_REVIEW, SUMMARY }
```

### ReviewEvent.kt

See [data-model.md → Event Classes](./data-model.md#3-event-classes) for full implementation.

### ReviewSideEffect.kt

See [data-model.md → Side Effect Classes](./data-model.md#4-side-effect-classes) for full implementation.

---

## Task 3: Create DataStore for Progress

**Files to create:**
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ReviewProgress.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ReviewProgressDataStore.kt`

**Reference:** [data-model.md → DataStore Progress Schema](./data-model.md#5-datastore-progress-schema)

### ReviewProgress.kt

```kotlin
package org.epoque.tandem.data.preferences

import kotlinx.serialization.Serializable
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.presentation.review.ReviewMode
import org.epoque.tandem.presentation.review.ReviewStep

@Serializable
data class ReviewProgress(
    val weekId: String,
    val reviewMode: String = "SOLO",
    val currentStep: String = "MODE_SELECT",
    val overallRating: Int? = null,
    val overallNote: String = "",
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, String> = emptyMap(),
    val taskNotes: Map<String, String> = emptyMap(),
    val lastUpdatedAt: Long = 0
) {
    fun toUiState(): Pair<ReviewMode, ReviewStep> = Pair(
        ReviewMode.valueOf(reviewMode),
        ReviewStep.valueOf(currentStep)
    )

    fun getTaskOutcomesAsStatus(): Map<String, TaskStatus> =
        taskOutcomes.mapValues { TaskStatus.valueOf(it.value) }
}
```

### ReviewProgressDataStore.kt

```kotlin
package org.epoque.tandem.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ReviewProgressDataStore(
    private val dataStore: DataStore<Preferences>
) {
    private val progressKey = stringPreferencesKey("review_progress")

    val progress: Flow<ReviewProgress?> = dataStore.data.map { prefs ->
        prefs[progressKey]?.let { json ->
            runCatching { Json.decodeFromString<ReviewProgress>(json) }.getOrNull()
        }
    }

    suspend fun saveProgress(progress: ReviewProgress) {
        dataStore.edit { prefs ->
            prefs[progressKey] = Json.encodeToString(progress)
        }
    }

    suspend fun clearProgress() {
        dataStore.edit { prefs ->
            prefs.remove(progressKey)
        }
    }
}
```

---

## Task 4: Implement ReviewViewModel

**File to create:**
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewViewModel.kt`

**Reference:** [contracts/review-operations.md](./contracts/review-operations.md) for all operations

### Key Implementation Points

1. **Initialization sequence** - Follow auth-first pattern from Feature 004
2. **Event handler dispatch** - Map all ReviewEvent types to handler methods
3. **Progress persistence** - Save after each state change
4. **Side effect channel** - Single collector pattern

```kotlin
package org.epoque.tandem.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.epoque.tandem.data.preferences.ReviewProgressDataStore
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.domain.usecase.review.CalculateStreakUseCase
import org.epoque.tandem.domain.usecase.review.GetReviewStatsUseCase
import org.epoque.tandem.domain.usecase.review.IsReviewWindowOpenUseCase

class ReviewViewModel(
    private val authRepository: AuthRepository,
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val isReviewWindowOpenUseCase: IsReviewWindowOpenUseCase,
    private val getReviewStatsUseCase: GetReviewStatsUseCase,
    private val reviewProgressDataStore: ReviewProgressDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ReviewSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<ReviewSideEffect> = _sideEffect.receiveAsFlow()

    init {
        // See contracts/review-operations.md for full initialization
    }

    fun onEvent(event: ReviewEvent) {
        // See contracts/review-operations.md for event dispatch
    }
}
```

**Verification:** Unit test ViewModel with mock repositories.

---

## Task 5: Create Review Banner Component

**File to create:**
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewBanner.kt`

### ReviewBanner.kt

```kotlin
package org.epoque.tandem.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReviewBanner(
    currentStreak: Int,
    onStartReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Time to review your week!",
                    style = MaterialTheme.typography.titleMedium
                )
                if (currentStreak > 0) {
                    Text(
                        text = "Current streak: $currentStreak weeks",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Button(
                onClick = onStartReview,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Review")
            }
        }
    }
}
```

---

## Task 6: Create Emoji Rating Selector

**File to create:**
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/components/EmojiRatingSelector.kt`

**Reference:** [research.md → Emoji Rating Scale Design](./research.md#4-emoji-rating-scale-design)

```kotlin
package org.epoque.tandem.ui.review.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RATINGS = listOf(
    Triple(1, "😫", "Terrible week"),
    Triple(2, "😕", "Bad week"),
    Triple(3, "😐", "Okay week"),
    Triple(4, "🙂", "Good week"),
    Triple(5, "🎉", "Great week")
)

@Composable
fun EmojiRatingSelector(
    selectedRating: Int?,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        RATINGS.forEach { (value, emoji, description) ->
            EmojiButton(
                emoji = emoji,
                contentDescription = description,
                isSelected = selectedRating == value,
                onClick = { onRatingSelected(value) }
            )
        }
    }
}

@Composable
private fun EmojiButton(
    emoji: String,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
        }
    }
}
```

---

## Task 7: Create Task Outcome Card

**File to create:**
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/components/TaskOutcomeCard.kt`

```kotlin
package org.epoque.tandem.ui.review.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus

@Composable
fun TaskOutcomeCard(
    task: Task,
    currentOutcome: TaskStatus?,
    note: String,
    onOutcomeSelected: (TaskStatus) -> Unit,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Outcome buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutcomeButton(
                    text = "Done",
                    icon = "✓",
                    isSelected = currentOutcome == TaskStatus.COMPLETED,
                    onClick = { onOutcomeSelected(TaskStatus.COMPLETED) }
                )
                OutcomeButton(
                    text = "Tried",
                    icon = "~",
                    isSelected = currentOutcome == TaskStatus.TRIED,
                    onClick = { onOutcomeSelected(TaskStatus.TRIED) }
                )
                OutcomeButton(
                    text = "Skipped",
                    icon = "○",
                    isSelected = currentOutcome == TaskStatus.SKIPPED,
                    onClick = { onOutcomeSelected(TaskStatus.SKIPPED) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional note field
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                label = { Text("Quick note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OutcomeButton(
    text: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .heightIn(min = 56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text("$icon  $text")
    }
}
```

---

## Task 8: Create Review Screens

**Files to create:**
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewScreen.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewModeSelectionScreen.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/OverallRatingStepScreen.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/TaskReviewStepScreen.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewSummaryScreen.kt`

### ReviewScreen.kt (Container)

```kotlin
package org.epoque.tandem.ui.review

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import org.epoque.tandem.presentation.review.ReviewSideEffect
import org.epoque.tandem.presentation.review.ReviewViewModel
import org.epoque.tandem.ui.navigation.ReviewNavGraph
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReviewScreen(
    onClose: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    viewModel: ReviewViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ReviewSideEffect.CloseReview -> onClose()
                ReviewSideEffect.NavigateToPlanning -> onNavigateToPlanning()
                is ReviewSideEffect.NavigateToTask ->
                    navController.navigate("review/task/${effect.index}")
                ReviewSideEffect.NavigateToRating ->
                    navController.navigate("review/rating")
                ReviewSideEffect.NavigateToSummary ->
                    navController.navigate("review/summary")
                ReviewSideEffect.NavigateBack ->
                    navController.popBackStack()
                is ReviewSideEffect.ShowError -> { /* Show snackbar */ }
                ReviewSideEffect.ShowReviewComplete -> { /* Show celebration */ }
                ReviewSideEffect.ShowPassToPartnerDialog -> { /* Show dialog */ }
            }
        }
    }

    ReviewNavGraph(
        navController = navController,
        state = state,
        onEvent = viewModel::onEvent
    )
}
```

### Other Screens

Implement `ReviewModeSelectionScreen`, `OverallRatingStepScreen`, `TaskReviewStepScreen`, and `ReviewSummaryScreen` following the patterns from Feature 004's planning screens.

**Key points:**
- Mode selection: Two buttons (Solo/Together) + streak display
- Rating: EmojiRatingSelector + optional note + Continue/Quick Finish buttons
- Task review: TaskOutcomeCard + progress dots + Next/Back navigation
- Summary: Progress bar + completion percentage + streak + Done/Start Next Week buttons

---

## Task 9: Create Navigation Graph

**File to create:**
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/ReviewNavGraph.kt`

**Reference:** [research.md → Navigation Flow Design](./research.md#8-navigation-flow-design)

```kotlin
package org.epoque.tandem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.epoque.tandem.presentation.review.ReviewEvent
import org.epoque.tandem.presentation.review.ReviewUiState
import org.epoque.tandem.ui.review.*

sealed class ReviewRoute(val route: String) {
    object ModeSelection : ReviewRoute("review/mode")
    object Rating : ReviewRoute("review/rating")
    object TaskReview : ReviewRoute("review/task/{index}") {
        fun createRoute(index: Int) = "review/task/$index"
    }
    object Summary : ReviewRoute("review/summary")
}

@Composable
fun ReviewNavGraph(
    navController: NavHostController,
    state: ReviewUiState,
    onEvent: (ReviewEvent) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = ReviewRoute.ModeSelection.route
    ) {
        composable(ReviewRoute.ModeSelection.route) {
            ReviewModeSelectionScreen(
                currentStreak = state.currentStreak,
                onSoloSelected = { onEvent(ReviewEvent.SelectMode(ReviewMode.SOLO)) },
                onTogetherSelected = { onEvent(ReviewEvent.SelectMode(ReviewMode.TOGETHER)) }
            )
        }

        composable(ReviewRoute.Rating.route) {
            OverallRatingStepScreen(
                selectedRating = state.overallRating,
                note = state.overallNote,
                onRatingSelected = { onEvent(ReviewEvent.SelectRating(it)) },
                onNoteChanged = { onEvent(ReviewEvent.UpdateRatingNote(it)) },
                onContinue = { onEvent(ReviewEvent.ContinueToTasks) },
                onQuickFinish = { onEvent(ReviewEvent.QuickFinish) }
            )
        }

        composable(
            route = ReviewRoute.TaskReview.route,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val task = state.tasksToReview.getOrNull(index)

            if (task != null) {
                TaskReviewStepScreen(
                    task = task,
                    taskIndex = index,
                    totalTasks = state.totalTasks,
                    currentOutcome = state.taskOutcomes[task.id],
                    note = state.taskNotes[task.id] ?: "",
                    onOutcomeSelected = { status ->
                        onEvent(ReviewEvent.SelectTaskOutcome(task.id, status))
                    },
                    onNoteChanged = { note ->
                        onEvent(ReviewEvent.UpdateTaskNote(task.id, note))
                    },
                    onNext = { onEvent(ReviewEvent.NextTask) },
                    onPrevious = { onEvent(ReviewEvent.PreviousTask) }
                )
            }
        }

        composable(ReviewRoute.Summary.route) {
            ReviewSummaryScreen(
                completionPercentage = state.completionPercentage,
                doneCount = state.doneCount,
                totalTasks = state.totalTasks,
                currentStreak = state.currentStreak,
                onStartNextWeek = { onEvent(ReviewEvent.StartNextWeek) },
                onDone = { onEvent(ReviewEvent.Done) }
            )
        }
    }
}
```

---

## Task 10: Create Koin Module

**File to create:**
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/ReviewModule.kt`

```kotlin
package org.epoque.tandem.di

import org.epoque.tandem.data.preferences.ReviewProgressDataStore
import org.epoque.tandem.domain.usecase.review.CalculateStreakUseCase
import org.epoque.tandem.domain.usecase.review.GetReviewStatsUseCase
import org.epoque.tandem.domain.usecase.review.IsReviewWindowOpenUseCase
import org.epoque.tandem.presentation.review.ReviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val reviewModule = module {
    // Use cases
    factory { CalculateStreakUseCase(get()) }
    factory { IsReviewWindowOpenUseCase() }
    factory { GetReviewStatsUseCase() }

    // DataStore
    single { ReviewProgressDataStore(get()) }

    // ViewModel
    viewModel {
        ReviewViewModel(
            authRepository = get(),
            weekRepository = get(),
            taskRepository = get(),
            calculateStreakUseCase = get(),
            isReviewWindowOpenUseCase = get(),
            getReviewStatsUseCase = get(),
            reviewProgressDataStore = get()
        )
    }
}
```

**Don't forget:** Add `reviewModule` to the Koin app modules list.

---

## Task 11: Integrate with Week View

**Files to modify:**
- Week view/tab to show ReviewBanner when `isReviewWindowOpen && !week.isReviewed`
- Main navigation to include ReviewScreen route

### Banner Integration

In Week view composable:

```kotlin
@Composable
fun WeekTab(
    weekState: WeekUiState,
    onStartReview: () -> Unit
) {
    // Check if banner should be shown
    val showReviewBanner = weekState.isReviewWindowOpen &&
                           weekState.currentWeek?.isReviewed == false

    Column {
        if (showReviewBanner) {
            ReviewBanner(
                currentStreak = weekState.currentStreak,
                onStartReview = onStartReview
            )
        }

        // ... rest of week view
    }
}
```

---

## Task 12: Build Validation

Run build validation to ensure feature compiles:

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

**Expected:** Build succeeds with no errors.

Run unit tests:

```bash
./gradlew :composeApp:testDebugUnitTest
```

---

## Summary Checklist

- [ ] Task 1: Use cases created (CalculateStreak, IsReviewWindowOpen)
- [ ] Task 2: UI state and events created
- [ ] Task 3: DataStore for progress created
- [ ] Task 4: ReviewViewModel implemented
- [ ] Task 5: ReviewBanner component created
- [ ] Task 6: EmojiRatingSelector component created
- [ ] Task 7: TaskOutcomeCard component created
- [ ] Task 8: All review screens created
- [ ] Task 9: Navigation graph created
- [ ] Task 10: Koin module created and registered
- [ ] Task 11: Integrated with Week view
- [ ] Task 12: Build validation passed

## Next Steps

After completing all tasks from [tasks.md](./tasks.md):
1. Implement Together mode (P5) in v1.1
2. Add UI tests for critical flows
3. Run `/speckit.analyze` for cross-artifact consistency check
