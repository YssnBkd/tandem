# Implementation Plan: Week View

**Branch**: `003-week-view` | **Date**: 2026-01-02 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-week-view/spec.md`

## Summary

Implement the main Week tab UI screen where users view, add, and complete tasks for the current week. This feature builds the presentation layer (ViewModel + Compose UI) on top of the existing Task Data Layer (Feature 002), providing segment-based navigation (You/Partner/Shared), quick task addition, task completion with haptic feedback and animations, and real-time progress tracking.

**Technical Approach**: Jetpack Compose with Material Design 3, MVI pattern with WeekViewModel, reactive data binding via Flow from TaskRepository/WeekRepository.

## Technical Context

**Language/Version**: Kotlin 2.1+ (Kotlin Multiplatform)
**Primary Dependencies**: Compose Multiplatform, Koin, SQLDelight, DataStore, kotlinx.datetime
**Storage**: SQLDelight (via Feature 002), DataStore (segment preference persistence)
**Testing**: Kotlin Test (ViewModel unit tests), Compose UI Tests
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: 60 fps UI, <100ms UI response, efficient list rendering
**Constraints**: Offline-first (all data from local SQLite), Material Design 3 compliance

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [x] **Relationship-First Design**: Partner segment is read-only (no surveillance/control); "Request a Task" requires partner acceptance
- [x] **Weekly Rhythm**: Feature centers entirely on the weekly task view; week date range header, weekly progress
- [x] **Autonomous Partnership**: Partner tasks are view-only; requests go through acceptance workflow; each partner manages their own tasks
- [x] **Celebration Over Judgment**: Uses "Tried/Skipped" status (not Failed/Abandoned) per Feature 002 data model; completion provides positive feedback
- [x] **Intentional Simplicity**: No due dates, priority levels, subtasks, or categories; flat task list with simple status

### Decision Framework

1. ✅ Does it strengthen the weekly rhythm? Yes - centered on weekly task management
2. ✅ Does it respect partner autonomy? Yes - partner segment is read-only, requests require acceptance
3. ✅ Is it the simplest solution that works? Yes - flat task list, minimal UI, no complex features
4. ✅ Can it work offline? Yes - uses local SQLite via TaskRepository
5. ✅ Does it follow Material Design 3 patterns? Yes - SegmentedButton, ModalBottomSheet, Material components

### Non-Negotiables Check

- [x] NO tracking of partner's incomplete tasks - Partner segment is read-only view; no editing of partner task status
- [x] NO notifications for partner's task completions (default off) - Not implemented in this feature
- [x] NO assigning tasks without acceptance workflow - "Request a Task" button is v1.0 placeholder only; full implementation with acceptance workflow in Feature 006
- [x] NO shame language in UI copy - Uses positive framing (progress indicator, celebration)
- [x] NO complex task hierarchies - Flat task list, no subtasks/categories

### Technical Compliance

- [x] Clean Architecture with MVI pattern - WeekViewModel with UiState + Events
- [x] Domain layer is 100% shared code (Kotlin Multiplatform) - Reuses Feature 002 domain layer
- [x] UI uses Jetpack Compose with Material Design 3 - All composables use M3 components
- [x] Offline-first architecture with SQLDelight - Data from TaskRepository/WeekRepository
- [ ] Build validation: `:composeApp:compileDebugKotlinAndroid` succeeds - To be verified during implementation

## Project Structure

### Documentation (this feature)

```text
specs/003-week-view/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (presentation models)
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (ViewModel contract)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

**Files to create/modify for this feature:**

```text
composeApp/src/
├── commonMain/kotlin/org/epoque/tandem/
│   ├── presentation/week/
│   │   ├── WeekViewModel.kt           # MVI ViewModel
│   │   ├── WeekUiState.kt             # UI State data class
│   │   ├── WeekEvent.kt               # User events sealed class
│   │   ├── WeekSideEffect.kt          # One-time effects (haptics)
│   │   └── model/
│   │       ├── TaskUiModel.kt         # UI-ready task model
│   │       └── Segment.kt             # Enum for segments
│   └── domain/usecase/                # Use cases if needed
│       ├── GetTasksForWeekUseCase.kt
│       └── CompleteTaskUseCase.kt
├── androidMain/kotlin/org/epoque/tandem/
│   ├── ui/week/
│   │   ├── WeekScreen.kt              # Main screen composable
│   │   ├── WeekHeader.kt              # Date range + progress
│   │   ├── SegmentedControl.kt        # You/Partner/Shared tabs
│   │   ├── TaskList.kt                # LazyColumn with tasks
│   │   ├── TaskListItem.kt            # Individual task row
│   │   ├── QuickAddField.kt           # Inline add field
│   │   ├── TaskDetailSheet.kt         # Modal for task details
│   │   ├── AddTaskSheet.kt            # Modal for adding tasks
│   │   └── EmptyState.kt              # Empty state composables
│   └── di/
│       └── WeekModule.kt              # Koin module for Week feature
└── commonTest/kotlin/org/epoque/tandem/
    └── presentation/week/
        └── WeekViewModelTest.kt       # ViewModel unit tests
```

**Build Validation**: All features must pass `:composeApp:compileDebugKotlinAndroid`

## Architecture Overview

### Data Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            Week Screen                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐ │
│  │ WeekHeader  │  │SegmentedCtl│  │ QuickAdd    │  │ TaskList        │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────┘ │
│                           │                                              │
│                           ▼                                              │
│                  ┌─────────────────┐                                     │
│                  │  WeekUiState    │ ◄── Compose collectAsState          │
│                  └─────────────────┘                                     │
│                           ▲                                              │
└───────────────────────────┼─────────────────────────────────────────────┘
                            │
                   ┌────────┴────────┐
                   │  WeekViewModel  │
                   │   (MVI + Flow)  │
                   └────────┬────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│  TaskRepository  │ │  WeekRepository  │ │    DataStore     │
│   (Feature 002)  │ │   (Feature 002)  │ │ (Segment Pref)   │
└──────────────────┘ └──────────────────┘ └──────────────────┘
```

### State Management

```kotlin
// WeekUiState.kt - Single source of truth
data class WeekUiState(
    val weekInfo: WeekInfo,           // Week dates, ID
    val selectedSegment: Segment,      // YOU, PARTNER, SHARED
    val tasks: List<TaskUiModel>,      // Filtered, sorted tasks
    val completedCount: Int,           // Progress numerator
    val totalCount: Int,               // Progress denominator
    val isLoading: Boolean,
    val quickAddText: String,          // Current input
    val quickAddError: String?,        // Validation error
    val selectedTask: TaskUiModel?,    // For detail sheet
    val showAddTaskSheet: Boolean,
    val showDetailSheet: Boolean,
    val error: String?
)

// WeekEvent.kt - User intentions
sealed class WeekEvent {
    data class SegmentSelected(val segment: Segment) : WeekEvent()
    data class TaskTapped(val taskId: String) : WeekEvent()
    data class TaskCheckboxTapped(val taskId: String) : WeekEvent()
    data class QuickAddTextChanged(val text: String) : WeekEvent()
    data object QuickAddSubmitted : WeekEvent()
    data class TaskUpdated(val taskId: String, val title: String, val notes: String?) : WeekEvent()
    data class TaskDeleted(val taskId: String) : WeekEvent()
    data class TaskMarkedComplete(val taskId: String) : WeekEvent()
    data object DetailSheetDismissed : WeekEvent()
    data object AddTaskSheetRequested : WeekEvent()
    data object AddTaskSheetDismissed : WeekEvent()
    data class AddTaskSubmitted(val title: String, val notes: String?, val ownerType: OwnerType) : WeekEvent()
    data object RefreshRequested : WeekEvent()
}

// WeekSideEffect.kt - One-time effects
sealed class WeekSideEffect {
    data object TriggerHapticFeedback : WeekSideEffect()
    data class ShowSnackbar(val message: String) : WeekSideEffect()
}
```

## Component Design

### WeekScreen (Scaffold)

```
┌──────────────────────────────────────┐
│ Week of Dec 30 - Jan 5         5/8   │  <- TopAppBar
├──────────────────────────────────────┤
│  [  You  ] [ Partner ] [ Shared ]    │  <- SegmentedButtonRow
├──────────────────────────────────────┤
│ ┌──────────────────────────────────┐ │
│ │ + Add a task...                  │ │  <- QuickAddField
│ └──────────────────────────────────┘ │
├──────────────────────────────────────┤
│ ☐ Grocery shopping                   │  <- TaskListItem
│ ☐ Call mom                           │
│ ☐ Gym 3x                      2/3    │  <- Repeat indicator
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │  <- Divider
│ ☑ Pay bills                   ░░░░░  │  <- Completed (faded)
│ ☑ Book dentist                ░░░░░  │
└──────────────────────────────────────┘
         [+]  <- FAB for AddTaskSheet
```

### TaskListItem Composable

```kotlin
@Composable
fun TaskListItem(
    task: TaskUiModel,
    isReadOnly: Boolean,           // Partner segment
    onCheckboxClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Visual States**:
- Incomplete: Normal text, unchecked checkbox
- Completed: Strikethrough text, checked checkbox, 50% alpha
- Repeating: Shows fraction format (2/3)

### ModalBottomSheets

**TaskDetailSheet**:
- OutlinedTextField for title (editable if own task)
- OutlinedTextField for notes (optional)
- Status display (Completed/Pending/etc.)
- Owner info + creation date
- "Mark Complete" button (if incomplete)
- "Delete" text button (with confirmation dialog)

**AddTaskSheet**:
- OutlinedTextField for title (auto-focus)
- OutlinedTextField for notes (optional)
- SegmentedButton for owner (You/Shared) - only on Shared segment
- Save/Cancel buttons

## Haptic Feedback

```kotlin
// In WeekScreen composable
val hapticFeedback = LocalHapticFeedback.current

LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            is WeekSideEffect.TriggerHapticFeedback -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            // ...
        }
    }
}
```

## Animations

### Task Completion Animation

```kotlin
@Composable
fun AnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { /* Return to 1f */ }
    )

    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.scale(scale)
    )
}
```

### List Item Reordering

```kotlin
LazyColumn {
    items(
        items = tasks,
        key = { it.id }
    ) { task ->
        TaskListItem(
            task = task,
            modifier = Modifier.animateItem() // Compose 1.7+ or animateItemPlacement
        )
    }
}
```

## Segment Persistence

Use DataStore to persist last selected segment:

```kotlin
// SegmentPreferences.kt (in commonMain)
class SegmentPreferences(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val SELECTED_SEGMENT = stringPreferencesKey("selected_segment")
    }

    val selectedSegment: Flow<Segment> = dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_SEGMENT]?.let { Segment.valueOf(it) } ?: Segment.YOU
    }

    suspend fun setSelectedSegment(segment: Segment) {
        dataStore.edit { prefs ->
            prefs[Keys.SELECTED_SEGMENT] = segment.name
        }
    }
}
```

## Dependencies

### Existing (from Feature 001 & 002)
- TaskRepository, WeekRepository
- Task, Week, TaskStatus, OwnerType domain models
- Koin DI setup
- TandemDatabase
- Navigation shell with Week tab destination

### New for this feature
- DataStore dependency for segment preferences
- No new external libraries needed

## Complexity Tracking

> No Constitution Check violations. All decisions align with core principles.

| Decision | Justification |
|----------|---------------|
| Inline QuickAdd (not FAB-only) | Simpler interaction for most common action; reduces friction |
| ModalBottomSheet for details | Material 3 pattern; simpler than full-screen navigation |
| Combined ViewModel for all segments | Segments share most state; avoids code duplication |

## Testing Strategy

### Unit Tests (WeekViewModelTest)
- Task filtering by segment
- Progress calculation
- Quick add validation
- Task completion updates state
- Segment persistence

### UI Tests
- Quick add task flow
- Complete task interaction
- Segment switching
- Empty state display
- Detail sheet opens on tap

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Animation performance | Medium | Use `key` in LazyColumn; profile with systrace |
| Haptic feedback not available | Low | Graceful degradation; no crash |
| Partner segment with no partner | Low | Empty state with invite CTA (UI placeholder) |
| Large task list performance | Medium | Lazy loading; limit visible items |
