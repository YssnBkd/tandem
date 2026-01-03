# Quickstart: Week Planning (Feature 004)

**Date**: 2026-01-03
**Estimated Effort**: Medium (8-10 UI files, 3 presentation files, repository extensions)

## Prerequisites

Before implementing, ensure:

- [ ] Feature 002 (Task Data Layer) is complete - provides Task/Week models and repositories
- [ ] Feature 003 (Week View) is complete - provides WeekViewModel patterns and navigation
- [ ] Build passes: `./gradlew :composeApp:compileDebugKotlinAndroid`

## Implementation Order

### Phase 1: Repository Extensions (1 task)

1. **Extend TaskRepository & WeekRepository**
   - Add `observeIncompleteTasksForWeek()` to TaskRepository
   - Add `observeTasksByStatus()` to TaskRepository
   - Add `getPreviousWeekId()` to WeekRepository
   - Add SQLDelight queries to Task.sq

### Phase 2: Presentation Layer (3 tasks)

2. **Create DataStore Progress Persistence**
   - `PlanningProgress.kt` following SegmentPreferences pattern
   - `PlanningProgressState` data class

3. **Create ViewModel and State**
   - `PlanningViewModel.kt` with init sequence
   - `PlanningUiState.kt` with step-specific state
   - `PlanningEvent.kt` sealed class
   - `PlanningSideEffect.kt` sealed class

### Phase 3: Navigation (1 task)

4. **Setup Planning Navigation**
   - Add `Routes.Planning` sealed class to Routes.kt
   - Create `PlanningNavGraph.kt` with nested routes
   - Wire into MainNavGraph

### Phase 4: UI Components (4 tasks)

5. **Create Shared Components**
   - `PlanningCard.kt` - full-screen card with swipe + buttons
   - `ProgressDots.kt` - step indicator
   - `TaskInputField.kt` - text input with visible Add button

6. **Create Step Screens**
   - `RolloverStepScreen.kt` - card stack for incomplete tasks
   - `AddTasksStepScreen.kt` - input field + running list
   - `PartnerRequestsStepScreen.kt` - card stack for requests
   - `ConfirmationStepScreen.kt` - summary with checkmark

7. **Create Main Container**
   - `PlanningScreen.kt` - hosts NavHost, collects side effects

8. **Create Banner Component**
   - `PlanningBanner.kt` - for Week Tab integration
   - Update WeekScreen to show banner conditionally

### Phase 5: DI & Integration (1 task)

9. **Wire Up Dependency Injection**
   - Create `PlanningModule.kt` with Koin bindings
   - Register module in TandemApp
   - Add DataStore instance for planning preferences

---

## Key Files to Create

```
composeApp/src/
├── commonMain/kotlin/org/epoque/tandem/
│   └── presentation/planning/
│       ├── PlanningViewModel.kt
│       ├── PlanningUiState.kt
│       ├── PlanningEvent.kt
│       ├── PlanningSideEffect.kt
│       └── preferences/
│           └── PlanningProgress.kt
│
└── androidMain/kotlin/org/epoque/tandem/
    ├── ui/planning/
    │   ├── PlanningScreen.kt
    │   ├── RolloverStepScreen.kt
    │   ├── AddTasksStepScreen.kt
    │   ├── PartnerRequestsStepScreen.kt
    │   ├── ConfirmationStepScreen.kt
    │   ├── PlanningBanner.kt
    │   └── components/
    │       ├── PlanningCard.kt
    │       ├── ProgressDots.kt
    │       └── TaskInputField.kt
    ├── di/
    │   └── PlanningModule.kt
    └── ui/navigation/
        └── PlanningNavGraph.kt (or extend Routes.kt)
```

---

## Key Files to Modify

```
shared/src/
├── commonMain/kotlin/org/epoque/tandem/
│   ├── domain/repository/
│   │   ├── TaskRepository.kt          # Add 2 methods
│   │   └── WeekRepository.kt          # Add 1 method
│   └── data/repository/
│       ├── TaskRepositoryImpl.kt      # Implement new methods
│       └── WeekRepositoryImpl.kt      # Implement getPreviousWeekId
└── commonMain/sqldelight/org/epoque/tandem/data/local/
    └── Task.sq                        # Add 2 queries

composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/navigation/
│   ├── Routes.kt                      # Add Routes.Planning
│   └── MainNavGraph.kt                # Add planning navigation
├── ui/week/
│   └── WeekScreen.kt                  # Add PlanningBanner
└── TandemApp.kt                       # Register planningModule
```

---

## Critical Implementation Patterns

### 1. Auth-First Initialization

```kotlin
// ALWAYS wait for auth before data operations
init {
    viewModelScope.launch {
        val userId = authRepository.authState
            .filterIsInstance<AuthState.Authenticated>()
            .first()
            .user.id

        // Now safe to call repositories
    }
}
```

### 2. Single Side Effect Collector

```kotlin
// CORRECT: One LaunchedEffect for all effects
LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            is ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            is NavigateToStep -> navController.navigate(...)
            // ...
        }
    }
}
```

### 3. Visible Action Buttons

```kotlin
// CORRECT: Visible button (not keyboard-only)
OutlinedTextField(
    trailingIcon = {
        IconButton(onClick = onSubmit, enabled = text.isNotBlank()) {
            Icon(Icons.AutoMirrored.Filled.Send, "Add task")
        }
    }
)
```

### 4. Progress Save After Each Action

```kotlin
private fun handleRolloverTaskAdded(taskId: String) {
    viewModelScope.launch {
        // ... create task ...
        saveProgress()  // ALWAYS save after state change
    }
}
```

---

## Testing Checklist

### Unit Tests
- [ ] `PlanningViewModel` init sequence with auth wait
- [ ] `getPreviousWeekId()` year boundary handling
- [ ] Step skipping logic (no rollover → skip to ADD_TASKS)
- [ ] Progress save/restore cycle

### UI Tests
- [ ] Complete planning flow end-to-end
- [ ] Resume from interrupted session
- [ ] Banner visibility conditions (Sunday 6pm, planning incomplete)
- [ ] Back navigation between steps
- [ ] Empty state handling (0 rollover tasks, 0 partner requests)

### Accessibility Tests
- [ ] All buttons have 48dp+ touch targets
- [ ] Content descriptions on action buttons
- [ ] TalkBack navigation through card stack

---

## Build Verification

After implementation, verify:

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:testDebugUnitTest
```

---

## Reference Files

| Pattern | Reference File |
|---------|----------------|
| ViewModel structure | `composeApp/src/commonMain/.../presentation/week/WeekViewModel.kt` |
| UI State | `composeApp/src/commonMain/.../presentation/week/WeekUiState.kt` |
| Events | `composeApp/src/commonMain/.../presentation/week/WeekEvent.kt` |
| DataStore | `composeApp/src/commonMain/.../presentation/week/preferences/SegmentPreferences.kt` |
| Navigation | `composeApp/src/androidMain/.../ui/navigation/AuthNavGraph.kt` |
| Koin Module | `composeApp/src/androidMain/.../di/WeekModule.kt` |
| Bottom Sheet | `composeApp/src/androidMain/.../ui/week/TaskDetailSheet.kt` |
