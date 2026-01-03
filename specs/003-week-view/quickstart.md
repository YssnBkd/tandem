# Quickstart: Week View Implementation

**Feature Branch**: `003-week-view`
**Date**: 2026-01-02

## Prerequisites

Before implementing this feature, ensure:

1. **Feature 001 (Core Infrastructure)** is complete:
   - Navigation shell with Week tab destination
   - Koin DI setup
   - DataStore for preferences
   - User authentication/provider

2. **Feature 002 (Task Data Layer)** is complete:
   - TaskRepository and WeekRepository implementations
   - Task, Week domain models
   - SQLDelight database setup

## Quick Start Steps

### 1. Add Presentation Models (5 min)

Create `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/model/`:

```kotlin
// Segment.kt
enum class Segment(val displayName: String) {
    YOU("You"),
    PARTNER("Partner"),
    SHARED("Shared")
}

// TaskUiModel.kt - see data-model.md for full implementation
// WeekInfo.kt - see data-model.md for full implementation
```

### 2. Add UI State & Events (5 min)

Create `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/`:

```kotlin
// WeekUiState.kt - see data-model.md
// WeekEvent.kt - see data-model.md
// WeekSideEffect.kt - see data-model.md
```

### 3. Add Segment Preferences (5 min)

Create `composeApp/src/commonMain/kotlin/org/epoque/tandem/data/preferences/`:

```kotlin
// SegmentPreferences.kt
class SegmentPreferences(private val dataStore: DataStore<Preferences>) {
    private val key = stringPreferencesKey("selected_segment")

    val selectedSegment: Flow<Segment> = dataStore.data.map { prefs ->
        prefs[key]?.let { Segment.valueOf(it) } ?: Segment.YOU
    }

    suspend fun setSelectedSegment(segment: Segment) {
        dataStore.edit { it[key] = segment.name }
    }
}
```

### 4. Implement WeekViewModel (30 min)

Create `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekViewModel.kt`:

See `contracts/week-viewmodel.md` for full implementation details.

Key methods:
- `observeTasks()` - Reactive data binding
- `handleTaskCheckboxTapped()` - Toggle completion
- `handleQuickAddSubmitted()` - Create task
- `handleSegmentSelected()` - Switch segments

### 5. Add Koin Module (5 min)

Create `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/WeekModule.kt`:

```kotlin
val weekModule = module {
    single { SegmentPreferences(get()) }
    viewModel { WeekViewModel(get(), get(), get(), get()) }
}
```

Add to `AppModule.kt`:
```kotlin
modules(coreModule, dataModule, weekModule)
```

### 6. Implement UI Composables (45 min)

Create `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/`:

**Order of implementation:**

1. `TaskListItem.kt` - Individual task row
2. `QuickAddField.kt` - Inline text input
3. `TaskList.kt` - LazyColumn wrapper
4. `SegmentedControl.kt` - Tab navigation
5. `WeekHeader.kt` - Date range + progress
6. `EmptyState.kt` - Empty state variants
7. `TaskDetailSheet.kt` - Edit/complete modal
8. `AddTaskSheet.kt` - Create task modal
9. `WeekScreen.kt` - Main scaffold

### 7. Wire Up Navigation (5 min)

In your navigation graph, add the Week screen:

```kotlin
composable(route = "week") {
    WeekScreen(
        viewModel = koinViewModel()
    )
}
```

### 8. Run Build Validation

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

### 9. Test on Device

1. Open app
2. Navigate to Week tab
3. Verify:
   - Week header shows current date range
   - Segmented control switches views
   - Quick add creates tasks
   - Checkbox completes with haptic
   - Progress updates in real-time

## File Checklist

```
composeApp/src/
├── commonMain/kotlin/org/epoque/tandem/
│   ├── presentation/week/
│   │   ├── WeekViewModel.kt          ☐
│   │   ├── WeekUiState.kt            ☐
│   │   ├── WeekEvent.kt              ☐
│   │   ├── WeekSideEffect.kt         ☐
│   │   └── model/
│   │       ├── TaskUiModel.kt        ☐
│   │       ├── Segment.kt            ☐
│   │       └── WeekInfo.kt           ☐
│   └── data/preferences/
│       └── SegmentPreferences.kt     ☐
├── androidMain/kotlin/org/epoque/tandem/
│   ├── ui/week/
│   │   ├── WeekScreen.kt             ☐
│   │   ├── WeekHeader.kt             ☐
│   │   ├── SegmentedControl.kt       ☐
│   │   ├── TaskList.kt               ☐
│   │   ├── TaskListItem.kt           ☐
│   │   ├── QuickAddField.kt          ☐
│   │   ├── TaskDetailSheet.kt        ☐
│   │   ├── AddTaskSheet.kt           ☐
│   │   └── EmptyState.kt             ☐
│   └── di/
│       └── WeekModule.kt             ☐
└── commonTest/kotlin/org/epoque/tandem/
    └── presentation/week/
        └── WeekViewModelTest.kt      ☐
```

## Common Issues

### Issue: Segment not persisting
- Check DataStore is properly injected
- Verify preference key is consistent

### Issue: Tasks not updating
- Ensure Flow is being collected with `collectAsState()`
- Verify repository returns Flow (not suspend)

### Issue: Haptics not working
- Device may not support haptics
- Graceful degradation is expected

### Issue: Animation janky
- Add `key = { it.id }` to `items()` block
- Profile with Android Profiler

## Next Steps

After implementation:
1. Run unit tests: `./gradlew :composeApp:testDebugUnitTest`
2. Manual testing on device
3. Code review
4. Merge to main
