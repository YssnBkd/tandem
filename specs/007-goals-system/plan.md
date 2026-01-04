# Implementation Plan: Goals System

**Branch**: `007-goals-system` | **Date**: 2026-01-04 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/007-goals-system/spec.md`

## Summary

Implement long-term goal tracking that spans multiple weeks, with three goal types (Weekly Habit, Recurring Task, Target Amount), progress visualization, and task linking for automatic progress updates. Users can view their partner's goals in read-only mode.

**Technical Approach**: SQLDelight for goal/progress persistence, kotlinx.datetime for week calculations, Koin DI, Jetpack Compose UI with Material Design 3, existing Partner infrastructure for partner goal visibility.

## Technical Context

**Language/Version**: Kotlin 2.1+ (Kotlin Multiplatform)
**Primary Dependencies**: Compose Multiplatform, Koin, SQLDelight, DataStore, kotlinx.datetime
**Storage**: SQLDelight (local), offline-first (sync via existing partner infrastructure for partner goal visibility)
**Testing**: Kotlin Test (unit), Android Instrumented Tests (UI)
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: 60 fps UI, <100ms UI response, <2s partner goal sync
**Constraints**: Offline-first, Material Design 3 compliance, max 10 active goals per user

**Feature-Specific Tech**:
- kotlinx.datetime for week boundary calculations
- Existing Partner System (Feature 006) for partner goal visibility (read-only)
- Emoji support for goal icons (standard Unicode emoji)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Post-Design Review (Phase 1 Complete)**: All checks verified. Design maintains constitution compliance.

### Core Principles Compliance

- [x] **Relationship-First Design**: Partners can see each other's goals (read-only), fostering awareness without control
- [x] **Weekly Rhythm**: Goals are tracked week-by-week; Weekly Habit resets each week; progress history is weekly
- [x] **Autonomous Partnership**: Each partner owns their own goals exclusively; partner goals are view-only
- [x] **Celebration Over Judgment**: Progress bars show advancement; "Completed" vs "Expired" (not "Failed"); positive framing
- [x] **Intentional Simplicity**: Three simple goal types; no complex hierarchies; no due dates within weeks

### Decision Framework

1. Does it strengthen the weekly rhythm? ✓ Goals track progress week-by-week; Weekly Habit resets weekly
2. Does it respect partner autonomy? ✓ Each user owns their goals; partner goals are read-only
3. Is it the simplest solution that works? ✓ Three goal types cover common use cases without overcomplication
4. Can it work offline? ✓ SQLDelight local storage; sync when online
5. Does it follow Material Design 3 patterns? ✓ Specified in tech stack

### Non-Negotiables Check

- [x] NO tracking of partner's incomplete tasks - Partner goals visible but read-only, no pressure mechanics
- [x] NO notifications for partner's task completions (default off) - Not applicable to goals
- [x] NO assigning tasks without acceptance workflow - Tasks can only link to user's own goals
- [x] NO shame language in UI copy - Use "Completed/Expired" not "Failed/Missed"
- [x] NO complex task hierarchies - Goals are flat; task-goal links are optional references

### Technical Compliance

- [x] Clean Architecture with MVI pattern
- [x] Domain layer is 100% shared code (Kotlin Multiplatform)
- [x] UI uses Jetpack Compose with Material Design 3
- [x] Offline-first architecture with SQLDelight
- [ ] Build validation: `:composeApp:compileDebugKotlinAndroid` succeeds (pending implementation)

## Project Structure

### Documentation (this feature)

```text
specs/007-goals-system/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── goals-api.md     # SQLDelight schema contracts
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/
│   │   ├── Goal.kt
│   │   ├── GoalType.kt
│   │   └── GoalProgress.kt
│   └── repository/
│       └── GoalRepository.kt
└── data/
    └── repository/
        └── GoalRepositoryImpl.kt

composeApp/src/commonMain/kotlin/org/epoque/tandem/
└── presentation/goals/
    ├── GoalsViewModel.kt
    ├── GoalsUiState.kt
    ├── GoalsEvent.kt
    └── GoalsSideEffect.kt

composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/goals/
│   ├── GoalsScreen.kt
│   ├── GoalCard.kt
│   ├── AddGoalSheet.kt
│   ├── GoalDetailScreen.kt
│   └── components/
│       ├── EmojiPicker.kt
│       ├── GoalProgressBar.kt
│       └── GoalTypeSelector.kt
├── ui/navigation/
│   └── GoalsNavGraph.kt
└── di/
    └── GoalsModule.kt
```

## Complexity Tracking

No constitution violations. Feature aligns with all principles.

## Integration Points

### From Feature 001 (Core Infrastructure)
| Method/API | Purpose | Preconditions |
|------------|---------|---------------|
| `authRepository.authState` | Get authenticated user | Wait for `AuthState.Authenticated` |
| `authRepository.currentUser` | User details for goal ownership | User authenticated |

### From Feature 002 (Task Data Layer)
| Method/API | Purpose | Preconditions |
|------------|---------|---------------|
| `Task.linkedGoalId` | Link tasks to goals | Field already exists in Task model |
| `taskRepository.observeTasksForWeek()` | Find linked tasks for goal | Week exists |

### From Feature 006 (Partner System)
| Method/API | Purpose | Preconditions |
|------------|---------|---------------|
| `partnerRepository.getPartner()` | Check if user has partner | User authenticated |
| `partnerRepository.observePartner()` | React to partner changes | User authenticated |

### New Repositories
| Repository | Purpose | Key Methods |
|------------|---------|-------------|
| `GoalRepository` | Goal lifecycle | `createGoal()`, `observeMyGoals()`, `observePartnerGoals()`, `updateGoal()`, `deleteGoal()`, `recordProgress()` |

## Initialization Sequence (CRITICAL)

```kotlin
// GoalsViewModel.init
viewModelScope.launch {
    // 1. Wait for authentication
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Observe own goals
    launch {
        goalRepository.observeMyGoals(userId)
            .collect { goals ->
                _uiState.update { it.copy(myGoals = goals) }
            }
    }

    // 3. Check for partner and observe their goals (read-only)
    launch {
        partnerRepository.observePartner()
            .collect { partner ->
                if (partner != null) {
                    goalRepository.observePartnerGoals(partner.id)
                        .collect { partnerGoals ->
                            _uiState.update {
                                it.copy(
                                    partnerGoals = partnerGoals,
                                    hasPartner = true
                                )
                            }
                        }
                } else {
                    _uiState.update {
                        it.copy(partnerGoals = emptyList(), hasPartner = false)
                    }
                }
            }
    }

    _uiState.update { it.copy(isLoading = false) }
}
```

## Week Boundary Handling

```kotlin
// WeekCalculator.kt
fun getWeekId(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String {
    // ISO 8601 week ID: "2026-W01"
    return "${date.year}-W${date.weekOfYear.toString().padStart(2, '0')}"
}

fun isNewWeek(lastWeekId: String): Boolean {
    return getWeekId() != lastWeekId
}

// Called on app launch and when resuming from background
fun checkWeeklyReset(goal: Goal) {
    if (goal.type == GoalType.WEEKLY_HABIT && isNewWeek(goal.lastProgressWeekId)) {
        // Reset weekly progress, archive previous week's progress
        goalRepository.recordWeeklyProgress(goal.id, goal.currentProgress)
        goalRepository.resetWeeklyProgress(goal.id)
    }
}
```

## Implementation Patterns

### Side Effect Channel - Single Collector Only
```kotlin
private val _sideEffects = Channel<GoalsSideEffect>(Channel.BUFFERED)
val sideEffects = _sideEffects.receiveAsFlow()

// In UI: Single LaunchedEffect collector
LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            is GoalsSideEffect.NavigateToDetail -> navController.navigate(...)
            is GoalsSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
        }
    }
}
```

### stateProvider Pattern for Navigation
```kotlin
// Avoid state capture issues in NavGraphBuilder
fun NavGraphBuilder.goalsNavGraph(
    navController: NavController,
    stateProvider: () -> GoalsUiState,
    onEvent: (GoalsEvent) -> Unit
) {
    composable<Routes.Goals.List> {
        val state = stateProvider()
        GoalsScreen(
            myGoals = state.myGoals,
            partnerGoals = state.partnerGoals,
            selectedSegment = state.selectedSegment,
            hasPartner = state.hasPartner,
            onSegmentSelected = { onEvent(GoalsEvent.SegmentSelected(it)) },
            onGoalTapped = { onEvent(GoalsEvent.GoalTapped(it)) },
            onAddGoal = { onEvent(GoalsEvent.AddGoalTapped) }
        )
    }
}
```

### UI Affordance - Always Visible Actions
All primary actions have visible buttons with 48dp minimum touch targets.
