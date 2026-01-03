# Implementation Tasks: Week Review

**Feature**: 005-week-review | **Generated**: 2026-01-03
**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Document Cross-Reference Guide

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **tasks.md** (this file) | Task sequence with dependencies | Primary implementation guide |
| [data-model.md](./data-model.md) | Complete code for state classes, events, DataStore | Copy-paste implementations |
| [contracts/review-operations.md](./contracts/review-operations.md) | ViewModel method implementations | All event handler code |
| [quickstart.md](./quickstart.md) | UI component code samples | Compose screen implementations |
| [research.md](./research.md) | Design decisions and rationale | Understand "why" behind choices |
| [spec.md](./spec.md) | Requirements (FR-XXX) and acceptance criteria | Verify behavior correctness |

## Task Legend

- `[P]` - Can run in parallel with other `[P]` tasks in same phase
- `[S]` - Sequential, must complete before next task
- `[V]` - Validation checkpoint
- `[D:X]` - Depends on task X completing first

## Phase 1: Shared Infrastructure

> Foundation use cases and domain logic in shared module. No UI yet.

### Task 1.1 [P] - Create CalculateStreakUseCase

**File**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/CalculateStreakUseCase.kt`

**Reference**: [data-model.md#6-new-use-cases](./data-model.md#6-new-use-cases)

**Implementation**:
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

**Acceptance**: Use case returns correct streak count for consecutive reviewed weeks.

**Scope**: Solo mode only. Couple-aware streak (FR-024, FR-025) deferred to Phase 7 (US5).

---

### Task 1.2 [P] - Create IsReviewWindowOpenUseCase

**File**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/IsReviewWindowOpenUseCase.kt`

**Reference**: [research.md#1-review-window-time-calculation](./research.md#1-review-window-time-calculation)

**Implementation**:
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

**Acceptance**: Returns true only during Friday 6PM - Sunday 11:59PM.

**Edge case handling (FR-017a)**:
- When `invoke()` returns `false` (outside Friday 6PM - Sunday 11:59PM):
  - Banner is not shown (handled in Task 4.3)
  - If user had incomplete progress, it remains in DataStore but week is not marked reviewed
  - Streak will break on next review if this week was missed
- **Note**: Deadline enforcement is passive - when window closes, banner disappears and user cannot start new review. No explicit "missed" notification in v1.

---

### Task 1.3 [P] - Create GetReviewStatsUseCase

**File**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/GetReviewStatsUseCase.kt`

**Reference**: [data-model.md#6-new-use-cases](./data-model.md#6-new-use-cases)

**Implementation**:
```kotlin
package org.epoque.tandem.domain.usecase.review

import org.epoque.tandem.domain.model.TaskStatus

data class ReviewStats(
    val totalTasks: Int,
    val doneCount: Int,
    val triedCount: Int,
    val skippedCount: Int,
    val completionPercentage: Int
)

class GetReviewStatsUseCase {
    operator fun invoke(taskOutcomes: Map<String, TaskStatus>): ReviewStats {
        val doneCount = taskOutcomes.count { it.value == TaskStatus.COMPLETED }
        val triedCount = taskOutcomes.count { it.value == TaskStatus.TRIED }
        val skippedCount = taskOutcomes.count { it.value == TaskStatus.SKIPPED }
        val totalTasks = taskOutcomes.size

        val completionPercentage = if (totalTasks > 0) {
            (doneCount * 100) / totalTasks
        } else {
            0
        }

        return ReviewStats(
            totalTasks = totalTasks,
            doneCount = doneCount,
            triedCount = triedCount,
            skippedCount = skippedCount,
            completionPercentage = completionPercentage
        )
    }
}
```

**Acceptance**: Completion percentage = Done only (Tried/Skipped = incomplete per FR-026).

---

### Task 1.4 [V] - Phase 1 Validation

**Command**: `./gradlew :shared:compileKotlinMetadata`

**Verify**:
- [ ] All use cases compile without errors
- [ ] No missing imports from domain/repository
- [ ] Unit tests pass for streak calculation edge cases

---

## Phase 2: Presentation Layer Foundation

> UI state, events, side effects, DataStore, and ViewModel in commonMain/androidMain.

### Task 2.1 [P] - Create ReviewUiState

**File**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewUiState.kt`

**References**:
- **Implementation**: [data-model.md#reviewuistate](./data-model.md#reviewuistate) - Copy complete data class
- **Why these fields**: [research.md#3-review-progress-persistence-strategy](./research.md#3-review-progress-persistence-strategy)

**Copy from data-model.md**:
- `ReviewUiState` data class with all fields
- `ReviewMode` enum (SOLO, TOGETHER)
- `ReviewStep` enum (MODE_SELECT, RATING, TASK_REVIEW, SUMMARY)
- All computed properties (`currentTask`, `totalTasks`, `canProceedFromRating`, etc.)

**Acceptance**: State class compiles with all computed properties.

---

### Task 2.2 [P] - Create ReviewEvent

**File**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewEvent.kt`

**References**:
- **Implementation**: [data-model.md#3-event-classes](./data-model.md#3-event-classes) - Copy complete sealed interface
- **Handlers for each event**: [contracts/review-operations.md#event-handler-dispatch](./contracts/review-operations.md#event-handler-dispatch)

**Copy from data-model.md**: Complete `ReviewEvent` sealed interface with all event types.

**Acceptance**: All events from spec are represented. Each event maps to a handler in contracts/review-operations.md.

---

### Task 2.3 [P] - Create ReviewSideEffect

**File**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewSideEffect.kt`

**References**:
- **Implementation**: [data-model.md#4-side-effect-classes](./data-model.md#4-side-effect-classes) - Copy complete sealed interface
- **How side effects are consumed**: [quickstart.md#reviewscreenkt-container](./quickstart.md#reviewscreenkt-container)

**Copy from data-model.md**: Complete `ReviewSideEffect` sealed interface.

**Acceptance**: All navigation and feedback side effects represented.

---

### Task 2.4 [S] [D:2.1-2.3] - Create ReviewProgress DataStore

**Files**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ReviewProgress.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ReviewProgressDataStore.kt`

**Reference**: [data-model.md#5-datastore-progress-schema](./data-model.md#5-datastore-progress-schema)

**Key points**:
- `@Serializable` data class with `weekId`, step, rating, outcomes, notes
- `toUiState()` and `getTaskOutcomesAsStatus()` helper methods
- DataStore wrapper with `progress: Flow`, `saveProgress()`, `clearProgress()`

**Acceptance**: Progress can be saved, restored, and cleared.

---

### Task 2.5 [S] [D:2.4] - Implement ReviewViewModel

**File**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewViewModel.kt`

**References by operation group**:

| Operation | Contract Section |
|-----------|------------------|
| Class structure + init | [contracts/review-operations.md#1-viewmodel-initialization](./contracts/review-operations.md#1-viewmodel-initialization) |
| `onSelectMode` | [contracts/review-operations.md#2-mode-selection-operations](./contracts/review-operations.md#2-mode-selection-operations) |
| `onSelectRating`, `onUpdateRatingNote`, `onContinueToTasks` | [contracts/review-operations.md#3-rating-operations](./contracts/review-operations.md#3-rating-operations) |
| `onSelectTaskOutcome`, `onUpdateTaskNote`, `onNextTask`, `onPreviousTask` | [contracts/review-operations.md#4-task-review-operations](./contracts/review-operations.md#4-task-review-operations) |
| `onQuickFinish` | [contracts/review-operations.md#5-quick-finish-operation](./contracts/review-operations.md#5-quick-finish-operation) |
| `completeReview`, `onDone`, `onStartNextWeek` | [contracts/review-operations.md#6-complete-review-operation](./contracts/review-operations.md#6-complete-review-operation) |
| `saveProgress`, `onResumeProgress`, `onDiscardProgress` | [contracts/review-operations.md#7-progress-persistence-operations](./contracts/review-operations.md#7-progress-persistence-operations) |
| `onEvent` dispatch | [contracts/review-operations.md#event-handler-dispatch](./contracts/review-operations.md#event-handler-dispatch) |

**Dependencies**: Inject `GetReviewStatsUseCase` (from Task 1.3) and use it in `calculateAndSetStats()`:
```kotlin
private val getReviewStatsUseCase: GetReviewStatsUseCase

private fun calculateAndSetStats() {
    val state = _uiState.value
    val stats = getReviewStatsUseCase(state.taskOutcomes)
    _uiState.update { it.copy(completionPercentage = stats.completionPercentage) }
}
```

**CRITICAL - Initialization Sequence** (from contracts §1):
1. Wait for `AuthState.Authenticated` (NEVER skip)
2. Check `isReviewWindowOpenUseCase()`
3. `weekRepository.getOrCreateCurrentWeek(userId)`
4. Check `reviewProgressDataStore.progress` for incomplete
5. Load tasks via `taskRepository.observeTasksForWeek()`
6. `prepareTasksForReview()` - pending first, completed last
7. `calculateStreakUseCase(userId)`
8. Update `_uiState`

**Acceptance**: ViewModel initializes correctly, handles all events per contracts, persists progress.

---

### Task 2.6 [V] - Phase 2 Validation

**Command**: `./gradlew :composeApp:compileDebugKotlinAndroid`

**Verify**:
- [ ] ViewModel compiles with all dependencies
- [ ] No circular dependencies between state/events/ViewModel
- [ ] DataStore integration works

---

## Phase 3: US1 - Solo Week Review (P1)

> Core review flow: rating → task review → summary.

### Task 3.1 [P] - Create EmojiRatingSelector Component

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/components/EmojiRatingSelector.kt`

**Reference**: [research.md#4-emoji-rating-scale-design](./research.md#4-emoji-rating-scale-design)

**UI requirements**:
- 5 emojis: 😫 😕 😐 🙂 🎉 (maps to 1-5)
- Minimum 48dp touch targets (accessibility)
- Selected state with visual highlight
- Content descriptions for screen readers

**Acceptance**: Emojis selectable, touch targets ≥48dp.

---

### Task 3.2 [P] - Create TaskOutcomeCard Component

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/components/TaskOutcomeCard.kt`

**Reference**: [quickstart.md#task-7-create-task-outcome-card](./quickstart.md#task-7-create-task-outcome-card)

**UI requirements**:
- Full-screen card display
- Task title prominently displayed
- Three outcome buttons: Done (✓), Tried (~), Skipped (○)
- Optional quick note field
- 56dp minimum button height

**Acceptance**: Task card fills screen, outcomes selectable, note field works.

---

### Task 3.3 [P] - Create ReviewProgressDots Component

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/components/ReviewProgressDots.kt`

**References**:
- **Pattern source**: Look at Feature 004 planning wizard for similar step indicators
- **Why progress dots**: Users need visual feedback on position in task sequence

**Signature**:
```kotlin
@Composable
fun ReviewProgressDots(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
)
```

**Implementation notes**:
- Row of dots, filled for completed, outlined for remaining
- Current dot highlighted with different color/size
- Touch targets not needed (display only)

**Acceptance**: Shows current position in task sequence with visual distinction.

---

### Task 3.4 [S] [D:3.1] - Create OverallRatingStepScreen

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/OverallRatingStepScreen.kt`

**References**:
- **Screen pattern**: [quickstart.md#task-8-create-review-screens](./quickstart.md#task-8-create-review-screens) - Key points section
- **Why emoji scale**: [research.md#4-emoji-rating-scale-design](./research.md#4-emoji-rating-scale-design)
- **Requirements**: [spec.md#rating](./spec.md#rating) - FR-005, FR-006, FR-007

**UI requirements**:
- "How was your week?" title
- `EmojiRatingSelector` component (from Task 3.1)
- Optional note text field (FR-007)
- "Continue" button (enabled when rating selected per FR-006)
- "Quick Finish" text button

**Acceptance**: Rating required before Continue enabled (FR-006). Note field optional (FR-007).

---

### Task 3.5 [S] [D:3.2,3.3] - Create TaskReviewStepScreen

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/TaskReviewStepScreen.kt`

**References**:
- **Screen pattern**: [quickstart.md#task-8-create-review-screens](./quickstart.md#task-8-create-review-screens) - Key points section
- **Why task order**: [research.md#5-task-review-order-strategy](./research.md#5-task-review-order-strategy)
- **Requirements**: [spec.md#task-review](./spec.md#task-review) - FR-009 through FR-014

**UI requirements**:
- `TaskOutcomeCard` centered (from Task 3.2)
- `ReviewProgressDots` at top (from Task 3.3)
- Navigation: Back button, Next/Done button
- Pre-fill outcome for already-completed tasks (FR-014)
- **Empty state**: If `tasksToReview.isEmpty()`, show message "No tasks this week" with "Continue to Summary" button

**Wire to ViewModel**:
- `onOutcomeSelected` → `ReviewEvent.SelectTaskOutcome`
- `onNoteChanged` → `ReviewEvent.UpdateTaskNote`
- `onNext` → `ReviewEvent.NextTask`
- `onPrevious` → `ReviewEvent.PreviousTask`

**Acceptance**: Tasks reviewed one at a time per FR-010. Pre-filled outcomes per FR-014.

---

### Task 3.6 [S] [D:3.4,3.5] - Create ReviewSummaryScreen

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewSummaryScreen.kt`

**References**:
- **Screen pattern**: [quickstart.md#task-8-create-review-screens](./quickstart.md#task-8-create-review-screens) - Key points section
- **Completion calculation**: [spec.md#clarifications](./spec.md#clarifications) - Done only = complete
- **Requirements**: [spec.md#summary](./spec.md#summary) - FR-026, FR-027, FR-028, FR-029

**UI requirements**:
- Completion percentage with progress bar (FR-026) - Done tasks only
- Stats breakdown: X Done, Y Tried, Z Skipped
- Current streak with encouraging message (FR-027)
- "Start Next Week" button → Planning (FR-028)
- "Done" button → Close review

**Wire to ViewModel**:
- `onStartNextWeek` → `ReviewEvent.StartNextWeek`
- `onDone` → `ReviewEvent.Done`

**Acceptance**: Summary shows correct stats per FR-026. Navigation works per FR-028.

---

### Task 3.7 [V] - US1 Validation

**Verify**:
- [ ] Complete solo review flow from start to finish
- [ ] Rating required before task review
- [ ] All tasks can be marked Done/Tried/Skipped
- [ ] Summary shows correct completion percentage
- [ ] Exit and resume preserves progress

---

## Phase 4: US2 - Review Trigger and Mode Selection (P2)

> Banner display and mode selection screen.

### Task 4.0 [S] - Extend WeekViewModel for Review Integration

**Files to modify**:
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekUiState.kt`
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekViewModel.kt`

**Reference**: [research.md#1-review-window-time-calculation](./research.md#1-review-window-time-calculation)

**Add to WeekUiState**:
```kotlin
// Add these fields to WeekUiState data class:
val isReviewWindowOpen: Boolean = false,
val currentStreak: Int = 0
```

**Add to WeekViewModel**:
```kotlin
// Add dependencies to constructor:
private val isReviewWindowOpenUseCase: IsReviewWindowOpenUseCase,
private val calculateStreakUseCase: CalculateStreakUseCase

// Add to loadInitialData() after week is loaded:
val isWindowOpen = isReviewWindowOpenUseCase()
val streak = calculateStreakUseCase(userId)
_uiState.update { state ->
    state.copy(
        isReviewWindowOpen = isWindowOpen,
        currentStreak = streak
    )
}
```

**Update WeekModule.kt**: Add new use case dependencies to WeekViewModel instantiation.

**Acceptance**: WeekUiState exposes `isReviewWindowOpen` and `currentStreak` for banner visibility logic.

---

### Task 4.1 [S] [D:4.0] - Create ReviewBanner Component

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewBanner.kt`

**Reference**: [quickstart.md#task-5-create-review-banner-component](./quickstart.md#task-5-create-review-banner-component)

**UI requirements**:
- Card with primary container color
- "Time to review your week!" message
- Current streak display (if > 0)
- "Review" button with 48dp min height

**Acceptance**: Banner displays correctly with streak.

---

### Task 4.2 [S] [D:4.1] - Create ReviewModeSelectionScreen

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewModeSelectionScreen.kt`

**UI requirements**:
- Title: "How do you want to review?"
- Two large buttons: "Review Solo" and "Review Together"
- Current streak display
- Together option can be visually de-emphasized (P5 priority)

**Acceptance**: Mode selection navigates to rating step.

---

### Task 4.3 [S] [D:4.0,4.1,4.2] - Integrate Banner with Week View

**File to modify**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/WeekScreen.kt` (or equivalent Week tab composable - verify path in codebase)

**References**:
- **Integration pattern**: [quickstart.md#task-11-integrate-with-week-view](./quickstart.md#task-11-integrate-with-week-view)
- **Requirements**: [spec.md#review-trigger](./spec.md#review-trigger) - FR-015, FR-016

**Note**: Task 4.0 adds `isReviewWindowOpen` and `currentStreak` to WeekUiState. This task consumes those values.

**Integration**:
```kotlin
val showReviewBanner = weekState.isReviewWindowOpen &&
                       weekState.currentWeek?.isReviewed == false

if (showReviewBanner) {
    ReviewBanner(
        currentStreak = weekState.currentStreak,
        onStartReview = { /* Navigate to review */ }
    )
}
```

**Acceptance**: Banner shows Friday 6PM+ (FR-015), hides after review complete (FR-016).

---

### Task 4.4 [V] - US2 Validation

**Verify**:
- [ ] Banner appears on Friday 6PM (test with mock clock)
- [ ] Banner disappears after review completion
- [ ] Mode selection shows current streak
- [ ] Both mode buttons navigate to rating

---

## Phase 5: US3 - Quick Finish Review (P3)

> Escape hatch for busy users.

### Task 5.1 [S] - Add Quick Finish to OverallRatingStepScreen

**File to modify**: `OverallRatingStepScreen.kt`

**UI requirements**:
- "Quick Finish" text button below Continue
- Confirms all tasks will be marked Skipped
- Navigates directly to summary

**Acceptance**: Quick Finish marks remaining tasks as Skipped (FR-004).

---

### Task 5.2 [S] [D:5.1] - Implement onQuickFinish in ViewModel

**Reference**: [contracts/review-operations.md#5-quick-finish-operation](./contracts/review-operations.md#5-quick-finish-operation)

**Implementation**:
- Get unreviewed tasks (not in taskOutcomes map)
- Mark all as TaskStatus.SKIPPED via taskRepository
- Update local state
- Navigate to summary

**Acceptance**: Partial reviews can be completed quickly.

---

### Task 5.3 [V] - US3 Validation

**Verify**:
- [ ] Quick Finish available on rating screen
- [ ] Quick Finish available during task review
- [ ] Only unreviewed tasks marked Skipped
- [ ] Completion percentage reflects skipped tasks

---

## Phase 6: US4 - Review Summary and Streak (P4)

> Gamification and motivation.

### Task 6.1 [S] - Enhance ReviewSummaryScreen with Streak

**File to modify**: `ReviewSummaryScreen.kt`

**UI requirements**:
- Streak count with fire emoji 🔥
- Encouraging messages:
  - 0 weeks: "Start your streak!"
  - 1-3 weeks: "Keep it going!"
  - 4+ weeks: "Amazing consistency!"
- Progress bar animation

**Acceptance**: Streak displayed with appropriate message.

---

### Task 6.2 [S] [D:6.1] - Add Navigation to Planning

**File to modify**: `ReviewSummaryScreen.kt` and main navigation

**Implementation**:
- "Start Next Week" button emits `StartNextWeek` event
- ViewModel sends `NavigateToPlanning` side effect
- Main navigation handles transition to PlanningScreen

**Acceptance**: Can start planning from review summary (FR-028).

---

### Task 6.3 [V] - US4 Validation

**Verify**:
- [ ] Streak count accurate after multiple reviews
- [ ] Appropriate encouragement message displays
- [ ] "Start Next Week" navigates to planning
- [ ] "Done" closes review and returns to week view

---

## Phase 7: US5 - Together Review Mode (P5 - Deferred)

> Couples review experience. **Deferred to v1.1.**
>
> **Deferred requirements from earlier phases**:
> - FR-024: Couple streak reset logic (currently solo-only in Task 1.1)
> - FR-025: Shared streak display (currently solo-only)
> - FR-022: Reaction storage schema

### Task 7.1 [S] - Create TogetherReviewState

**File**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/TogetherReviewState.kt`

**Reference**: [data-model.md#togetherreviewstate-for-us5---p5](./data-model.md#togetherreviewstate-for-us5---p5)

**Note**: Basic structure only - full implementation deferred.

---

### Task 7.2 [S] - Add PassToPartner Dialog

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/PassToPartnerDialog.kt`

**Implementation**: Simple dialog with "Ready to pass to partner" message.

---

### Task 7.3 [S] - Add Reaction Buttons (Stub)

**File to modify**: `TaskOutcomeCard.kt`

**Implementation**: Observer can tap emoji reactions (👏 ❤️ 💪). Store locally only for now.

---

### Task 7.4 [V] - US5 Validation (Deferred)

**Note**: Full validation deferred to v1.1. Basic flow should not crash.

---

## Phase 8: US6 - Task Review Notes (P6)

> Optional notes per task. **Note**: Core implementation already done in Phase 2-3. This phase is validation and verification only.

### Task 8.1 [S] - Verify Note Field Implementation

**Already implemented in**:
- UI: Task 3.2 (`TaskOutcomeCard`) - note field in card
- ViewModel: Task 2.5 (`onUpdateTaskNote` handler)
- Contracts: [contracts/review-operations.md#updatetasknote](./contracts/review-operations.md#updatetasknote)

**Verify**:
- [ ] Note field appears in `TaskOutcomeCard`
- [ ] Field is clearly labeled as optional
- [ ] Debounce works (500ms delay before save)

---

### Task 8.2 [S] - Verify Note Persistence

**References**:
- **Handler**: [contracts/review-operations.md#updatetasknote](./contracts/review-operations.md#updatetasknote)
- **Repository method**: [data-model.md#existing-repository-methods-used](./data-model.md#existing-repository-methods-used) - `updateTaskReviewNote`

**Verify**:
- [ ] `taskRepository.updateTaskReviewNote(taskId, note)` is called
- [ ] Notes persist after review completion
- [ ] Notes visible when viewing task elsewhere (if UI exists)

---

### Task 8.3 [V] - US6 Validation

**Requirements**: [spec.md](./spec.md) - User Story 6

**Verify**:
- [ ] Note field is optional - can proceed without entering note
- [ ] Notes persist after review completion
- [ ] Empty notes don't cause errors

---

## Phase 9: Integration and Navigation

> Wire everything together.

### Task 9.1 [S] - Create ReviewNavGraph

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/ReviewNavGraph.kt`

**Reference**: [research.md#8-navigation-flow-design](./research.md#8-navigation-flow-design)

**Routes**:
- `review/mode` - Mode selection
- `review/rating` - Overall rating
- `review/task/{index}` - Task review
- `review/summary` - Completion summary

**Acceptance**: All routes navigable, back stack correct.

---

### Task 9.2 [S] [D:9.1] - Create ReviewScreen Container

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/review/ReviewScreen.kt`

**References**:
- **Full implementation**: [quickstart.md#reviewscreenkt-container](./quickstart.md#reviewscreenkt-container) - Copy LaunchedEffect pattern
- **Side effect handling**: [quickstart.md#task-8-create-review-screens](./quickstart.md#task-8-create-review-screens)

**Implementation**:
- Hosts `ReviewNavGraph` with `rememberNavController()`
- Collects side effects via `LaunchedEffect` and handles navigation
- Maps each `ReviewSideEffect` to navigation action
- Provides `onClose` and `onNavigateToPlanning` callbacks to parent

**Key pattern** (from quickstart.md):
```kotlin
LaunchedEffect(Unit) {
    viewModel.sideEffect.collect { effect ->
        when (effect) {
            ReviewSideEffect.CloseReview -> onClose()
            ReviewSideEffect.NavigateToPlanning -> onNavigateToPlanning()
            // ... handle all side effects
        }
    }
}
```

---

### Task 9.3 [S] [D:9.2] - Integrate with Main Navigation

**File to modify**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/AppNavGraph.kt` (or equivalent - verify path in codebase)

**References**:
- **Pattern source**: Look at how PlanningScreen is integrated in Feature 004
- **Navigation patterns**: [research.md#8-navigation-flow-design](./research.md#8-navigation-flow-design)

**Implementation**:
1. Add review route to main `NavHost`:
   ```kotlin
   composable("review") {
       ReviewScreen(
           onClose = { navController.popBackStack() },
           onNavigateToPlanning = { navController.navigate("planning") }
       )
   }
   ```
2. Update Week view banner's `onStartReview` to navigate to review route
3. Ensure back navigation from review returns to week view

---

### Task 9.4 [V] - Integration Validation

**Verify**:
- [ ] Review flow accessible from Week view
- [ ] All navigation transitions smooth
- [ ] Back button behavior correct at each step
- [ ] Deep linking works (optional)

---

## Phase 10: DI and Final Validation

> Dependency injection and build verification.

### Task 10.1 [S] - Create ReviewModule

**File**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/ReviewModule.kt`

**Reference**: [quickstart.md#task-10-create-koin-module](./quickstart.md#task-10-create-koin-module)

**Register**:
- `CalculateStreakUseCase` (factory)
- `IsReviewWindowOpenUseCase` (factory)
- `GetReviewStatsUseCase` (factory)
- `ReviewProgressDataStore` (single)
- `ReviewViewModel` (viewModel)

---

### Task 10.2 [S] [D:10.1] - Register Module in App

**File to modify**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/TandemApplication.kt` or `di/AppModule.kt` (verify path in codebase - look for where other feature modules are registered)

**References**:
- **Pattern source**: Look at how `planningModule` (Feature 004) is registered

**Implementation**:
1. Find the Koin module aggregation (likely in Application class `startKoin` block)
2. Add `reviewModule` to the modules list:
   ```kotlin
   startKoin {
       modules(
           // ... existing modules
           reviewModule
       )
   }
   ```
3. Verify module loads without runtime errors

---

### Task 10.3 [V] - Build Validation

**Commands**:
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:testDebugUnitTest
```

**Verify**:
- [ ] No compilation errors
- [ ] All unit tests pass
- [ ] No Koin dependency resolution errors at runtime

---

### Task 10.4 [V] - E2E Acceptance Testing

**Test scenarios from spec.md**:

1. **Solo review flow** (US1):
   - [ ] Start review → Rate week → Review each task → See summary
   - [ ] Exit mid-review → Resume with progress intact
   - [ ] Complete review → Banner disappears

2. **Review trigger** (US2):
   - [ ] Banner appears Friday 6PM+
   - [ ] Banner hidden before Friday 6PM
   - [ ] Banner hidden after review complete

3. **Quick finish** (US3):
   - [ ] Quick Finish from rating → All tasks Skipped
   - [ ] Quick Finish mid-task → Remaining tasks Skipped

4. **Summary and streak** (US4):
   - [ ] Correct completion percentage
   - [ ] Streak increments on completion
   - [ ] Navigate to planning works

5. **Task notes** (US6):
   - [ ] Add note to task → Note persists
   - [ ] Skip note → No error

**Database Verification (via App Inspection or ADB)**:

For US4 (streak verification):
```bash
# Verify week was marked as reviewed
adb shell run-as org.epoque.tandem cat databases/tandem.db | sqlite3 :memory: \
  "SELECT id, reviewed_at, overall_rating FROM weeks ORDER BY start_date DESC LIMIT 1"
```

For US6 (task notes persistence):
```bash
# Verify review note was saved on task
adb shell run-as org.epoque.tandem cat databases/tandem.db | sqlite3 :memory: \
  "SELECT id, title, review_note FROM tasks WHERE review_note IS NOT NULL"
```

**Alternative**: Use Android Studio's App Inspection → Database Inspector for visual verification.

---

## Summary

| Phase | User Story | Priority | Tasks |
|-------|------------|----------|-------|
| 1 | Shared Infrastructure | - | 1.1-1.4 |
| 2 | Presentation Foundation | - | 2.1-2.6 |
| 3 | US1: Solo Week Review | P1 | 3.1-3.7 |
| 4 | US2: Review Trigger | P2 | 4.0-4.4 |
| 5 | US3: Quick Finish | P3 | 5.1-5.3 |
| 6 | US4: Summary & Streak | P4 | 6.1-6.3 |
| 7 | US5: Together Mode | P5 (Deferred) | 7.1-7.4 |
| 8 | US6: Task Notes | P6 | 8.1-8.3 |
| 9 | Integration | - | 9.1-9.4 |
| 10 | DI & Validation | - | 10.1-10.4 |

**Total**: 36 tasks across 10 phases

**Critical Path**: Phase 1 → Phase 2 → Phase 3 → Phase 4 (4.0) → Phase 9 → Phase 10
