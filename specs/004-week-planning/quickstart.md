# Quickstart: Week Planning (Feature 004)

**Date**: 2026-01-03
**Estimated Effort**: Medium (8-10 UI files, 3 presentation files, repository extensions)

## Prerequisites

Before implementing, ensure:

- [ ] Feature 002 (Task Data Layer) is complete - provides Task/Week models and repositories
- [ ] Feature 003 (Week View) is complete - provides WeekViewModel patterns and navigation
- [ ] Build passes: `./gradlew :composeApp:compileDebugKotlinAndroid`

---

## Implementation Tasks

### Task 1: Extend Repositories with New Queries

**Goal**: Add repository methods for querying rollover candidates and partner requests.

**Files to modify**:
- `shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/Task.sq`
- `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/TaskRepository.kt`
- `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/TaskRepositoryImpl.kt`
- `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/WeekRepository.kt`
- `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/WeekRepositoryImpl.kt`

**Implementation details**:
| What | Where to Find |
|------|---------------|
| SQL queries to add | [data-model.md → New Queries (SQLDelight)](./data-model.md#new-queries-sqldelight) |
| Repository interface signatures | [data-model.md → Repository Interface Additions](./data-model.md#repository-interface-additions) |
| `getPreviousWeekId()` implementation | [research.md → ISO 8601 Week ID Calculation](./research.md#2-iso-8601-week-id-calculation---previous-week) |
| Contract for `observeIncompleteTasksForWeek` | [contracts/planning-operations.md → observeIncompleteTasksForWeek](./contracts/planning-operations.md#observeincompletetasksforweek) |
| Contract for `observeTasksByStatus` | [contracts/planning-operations.md → observeTasksByStatus](./contracts/planning-operations.md#observetasksbystatus) |

**Reference pattern**: `TaskRepositoryImpl.kt:observeTasksForWeek()` (lines 45-55)

**Verification**: Build passes after adding interface methods and implementations.

---

### Task 2: Create DataStore Progress Persistence

**Goal**: Persist wizard progress for resume capability (survives app kill).

**Files to create**:
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/preferences/PlanningProgress.kt`

**Implementation details**:
| What | Where to Find |
|------|---------------|
| `PlanningProgressState` data class | [data-model.md → Planning Progress (DataStore)](./data-model.md#new-data-model-planning-progress-datastore) |
| `PlanningProgress` class structure | [research.md → DataStore for Planning Progress](./research.md#3-datastore-for-planning-progress-persistence) |
| `saveProgress()` / `clearProgress()` methods | [contracts/planning-operations.md → DataStore Operations](./contracts/planning-operations.md#datastore-operations) |
| Stale progress handling | [data-model.md → Stale Progress Handling](./data-model.md#new-data-model-planning-progress-datastore) |

**Reference pattern**: `SegmentPreferences.kt` - copy structure, adapt keys

**Verification**: Unit test that saves/restores progress state.

---

### Task 3: Create ViewModel and State Classes

**Goal**: Implement PlanningViewModel with MVI pattern following WeekViewModel.

**Files to create**:
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningViewModel.kt`
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningUiState.kt`
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningEvent.kt`
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningSideEffect.kt`

**Implementation details**:
| What | Where to Find |
|------|---------------|
| `PlanningUiState` full definition | [data-model.md → PlanningUiState](./data-model.md#planninguistate) |
| `PlanningEvent` sealed class | [data-model.md → PlanningEvent](./data-model.md#planningevent-sealed-class) |
| `PlanningSideEffect` sealed class | [data-model.md → PlanningSideEffect](./data-model.md#planningsideeffect) |
| `PlanningStep` enum | [data-model.md → PlanningStep Enum](./data-model.md#planningstep-enum) |
| **ViewModel init sequence (CRITICAL)** | [contracts/planning-operations.md → init](./contracts/planning-operations.md#init) |
| `handleRolloverTaskAdded()` | [contracts/planning-operations.md → handleRolloverTaskAdded](./contracts/planning-operations.md#handlerollovertaskadded) |
| `handleRolloverTaskSkipped()` | [contracts/planning-operations.md → handleRolloverTaskSkipped](./contracts/planning-operations.md#handlerollovertaskskipped) |
| `handleNewTaskSubmitted()` | [contracts/planning-operations.md → handleNewTaskSubmitted](./contracts/planning-operations.md#handlenewtasksubmitted) |
| `handlePartnerRequestAccepted()` | [contracts/planning-operations.md → handlePartnerRequestAccepted](./contracts/planning-operations.md#handlepartnerrequestaccepted) |
| `handlePartnerRequestDiscussed()` | [contracts/planning-operations.md → handlePartnerRequestDiscussed](./contracts/planning-operations.md#handlepartnerrequestdiscussed) |
| `handlePlanningCompleted()` | [contracts/planning-operations.md → handlePlanningCompleted](./contracts/planning-operations.md#handleplanningcompleted) |
| Step skipping logic | [contracts/planning-operations.md → Step Skipping Logic](./contracts/planning-operations.md#step-skipping-logic) |
| Rollover task creation | [data-model.md → Rollover Task Creation Logic](./data-model.md#rollover-task-creation-logic) |

**Reference pattern**: `WeekViewModel.kt` - copy structure, auth-first init, channel for side effects

**Critical pattern** (from research.md):
```kotlin
// Auth-first initialization - NEVER skip this
val userId = authRepository.authState
    .filterIsInstance<AuthState.Authenticated>()
    .first()
    .user.id
```

**Verification**: Unit tests for init sequence and event handlers.

---

### Task 4: Setup Planning Navigation

**Goal**: Add nested navigation graph for wizard steps.

**Files to modify**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/Routes.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/MainNavGraph.kt`

**Files to create**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/PlanningNavGraph.kt`

**Implementation details**:
| What | Where to Find |
|------|---------------|
| `Routes.Planning` sealed interface | [research.md → Jetpack Navigation Compose](./research.md#1-jetpack-navigation-compose---nested-wizard-flow) |
| Navigation pattern with step parameter | [research.md → Pattern](./research.md#1-jetpack-navigation-compose---nested-wizard-flow) |

**Reference pattern**: `AuthNavGraph.kt` - nested navigation structure

**Verification**: Can navigate to planning screen from Week Tab.

---

### Task 5: Create Shared UI Components

**Goal**: Build reusable components for wizard steps.

**Files to create**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/PlanningCard.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/ProgressDots.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/TaskInputField.kt`

**Implementation details**:
| What | Where to Find |
|------|---------------|
| Full-screen card with swipe + buttons | [research.md → Material Design 3 Full-Screen Cards](./research.md#4-material-design-3-full-screen-cards) |
| Swipe behavior (thresholds, snap back) | [research.md → Swipe Behavior](./research.md#4-material-design-3-full-screen-cards) |
| Visible action button pattern | See "Critical pattern" below |

**Critical pattern** (from spec FR-010, FR-023):
```kotlin
// CORRECT: Visible button with 48dp touch target
OutlinedTextField(
    trailingIcon = {
        IconButton(
            onClick = onSubmit,
            enabled = text.isNotBlank(),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Add task")
        }
    }
)
```

**Reference pattern**: `TaskDetailSheet.kt` for M3 Surface patterns

**Verification**: Preview renders correctly, touch targets are 48dp+.

---

### Task 6: Create Step Screens

**Goal**: Implement the 4 wizard step screens.

**Files to create**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/RolloverStepScreen.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/AddTasksStepScreen.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/PartnerRequestsStepScreen.kt`
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/ConfirmationStepScreen.kt`

**Implementation details**:
| Screen | UI Elements | Events to Emit |
|--------|-------------|----------------|
| RolloverStepScreen | PlanningCard stack, progress dots | `RolloverTaskAdded`, `RolloverTaskSkipped`, `RolloverStepComplete` |
| AddTasksStepScreen | TaskInputField, running list of added tasks | `NewTaskTextChanged`, `NewTaskSubmitted`, `DoneAddingTasks` |
| PartnerRequestsStepScreen | PlanningCard stack (Accept/Discuss buttons) | `PartnerRequestAccepted`, `PartnerRequestDiscussed` |
| ConfirmationStepScreen | Checkmark, "X tasks planned" summary, Done button | `PlanningCompleted` |

**State to consume** (from PlanningUiState):
| Screen | State Fields |
|--------|--------------|
| RolloverStepScreen | `rolloverTasks`, `currentRolloverIndex`, `processedRolloverCount` |
| AddTasksStepScreen | `newTaskText`, `newTaskError`, `addedTasks` |
| PartnerRequestsStepScreen | `partnerRequests`, `currentRequestIndex`, `processedRequestCount` |
| ConfirmationStepScreen | `totalTasksPlanned`, `rolloverTasksAdded`, `newTasksCreated`, `partnerRequestsAccepted` |

**Reference pattern**: Existing screens in `ui/week/` for Compose structure

---

### Task 7: Create Main Planning Container

**Goal**: Host the wizard NavHost and collect side effects.

**Files to create**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/PlanningScreen.kt`

**Implementation details**:
| What | Where to Find |
|------|---------------|
| Single side effect collector pattern | See "Critical pattern" below |
| NavHost for step screens | Task 4 navigation setup |

**Critical pattern** (NEVER use multiple collectors):
```kotlin
// CORRECT: One LaunchedEffect for ALL side effects
LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            is PlanningSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            is PlanningSideEffect.NavigateToStep -> navController.navigate(...)
            is PlanningSideEffect.NavigateBack -> navController.popBackStack()
            is PlanningSideEffect.ExitPlanning -> onPlanningComplete()
            is PlanningSideEffect.TriggerHapticFeedback -> hapticFeedback.performHapticFeedback(...)
            is PlanningSideEffect.ClearFocus -> focusManager.clearFocus()
        }
    }
}
```

**Reference pattern**: `WeekScreen.kt` side effect collection

---

### Task 8: Create Planning Banner

**Goal**: Show banner on Week Tab when planning is needed.

**Files to create**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/PlanningBanner.kt`

**Files to modify**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/WeekScreen.kt`

**Implementation details**:
| What | Logic |
|------|-------|
| Banner visibility | Show if `week.planningCompletedAt == null` AND current time is Sunday >= 6pm |
| Banner persistence | Persists until planning complete OR next Sunday 6pm (spec FR-001) |
| Banner action | "Start" button navigates to `Routes.Planning.Wizard(0)` |

**Time calculation**:
```kotlin
fun shouldShowPlanningBanner(week: Week): Boolean {
    if (week.planningCompletedAt != null) return false

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val isSundayEvening = now.dayOfWeek == DayOfWeek.SUNDAY && now.hour >= 18
    // Also show Mon-Sat if planning wasn't completed last Sunday
    return isSundayEvening || !week.isPlanningComplete
}
```

---

### Task 9: Wire Up Dependency Injection

**Goal**: Register all planning dependencies with Koin.

**Files to create**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/PlanningModule.kt`

**Files to modify**:
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/TandemApp.kt`

**Implementation details**:
```kotlin
// PlanningModule.kt
private val Context.planningPreferencesDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "planning_preferences")

val planningModule = module {
    single<DataStore<Preferences>>(named("planning")) {
        androidContext().planningPreferencesDataStore
    }
    single { PlanningProgress(get(named("planning"))) }
    viewModel {
        PlanningViewModel(
            taskRepository = get(),
            weekRepository = get(),
            authRepository = get(),
            planningProgress = get()
        )
    }
}

// TandemApp.kt - add to startKoin
modules(appModule, authModule, taskModule, weekModule, planningModule)
```

**Reference pattern**: `WeekModule.kt` for DataStore + ViewModel setup

---

## Verification Checklist

After completing all tasks:

```bash
# Build verification
./gradlew :composeApp:compileDebugKotlinAndroid

# Unit tests
./gradlew :composeApp:testDebugUnitTest
```

### Unit Tests
- [ ] `PlanningViewModel` init sequence with auth wait
- [ ] `getPreviousWeekId()` year boundary handling (2026-W01 → 2025-W52)
- [ ] Step skipping logic (no rollover → skip to ADD_TASKS)
- [ ] Progress save/restore cycle

### UI Tests
- [ ] Complete planning flow end-to-end
- [ ] Resume from interrupted session
- [ ] Banner visibility conditions
- [ ] Back navigation between steps
- [ ] Empty state handling (0 rollover tasks, 0 partner requests)

### Accessibility Tests
- [ ] All buttons have 48dp+ touch targets (spec FR-023)
- [ ] Content descriptions on action buttons (spec FR-024)
- [ ] TalkBack navigation through card stack

---

## Quick Reference: Codebase Patterns

| Pattern | Reference File |
|---------|----------------|
| ViewModel structure | `composeApp/src/commonMain/.../presentation/week/WeekViewModel.kt` |
| UI State data class | `composeApp/src/commonMain/.../presentation/week/WeekUiState.kt` |
| Event sealed class | `composeApp/src/commonMain/.../presentation/week/WeekEvent.kt` |
| DataStore preferences | `composeApp/src/commonMain/.../presentation/week/preferences/SegmentPreferences.kt` |
| Navigation graphs | `composeApp/src/androidMain/.../ui/navigation/AuthNavGraph.kt` |
| Koin module | `composeApp/src/androidMain/.../di/WeekModule.kt` |
| Bottom sheet / Surface | `composeApp/src/androidMain/.../ui/week/TaskDetailSheet.kt` |
| ISO 8601 week calculation | `shared/src/commonMain/.../data/repository/WeekRepositoryImpl.kt:83-134` |
