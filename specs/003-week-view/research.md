# Research: Week View

**Feature Branch**: `003-week-view`
**Date**: 2026-01-02

## Overview

This document consolidates research findings for implementing the Week View feature. The feature builds on the existing Task Data Layer (Feature 002) and Core Infrastructure (Feature 001).

---

## Research Topics

### 1. Compose Material 3 SegmentedButton

**Decision**: Use `SegmentedButton` from Material 3 for segment navigation

**Rationale**:
- Native M3 component since Compose Material 3 1.2.0
- Built-in support for single/multi selection
- Handles accessibility automatically
- Consistent with Material Design 3 spec

**Implementation**:
```kotlin
SingleChoiceSegmentedButtonRow {
    Segment.entries.forEach { segment ->
        SegmentedButton(
            selected = selectedSegment == segment,
            onClick = { onSegmentSelected(segment) },
            shape = SegmentedButtonDefaults.itemShape(
                index = segment.ordinal,
                count = Segment.entries.size
            )
        ) {
            Text(segment.displayName)
        }
    }
}
```

**Alternatives Considered**:
- Custom Tab implementation - Rejected: reinventing the wheel
- FilterChip group - Rejected: not designed for mutually exclusive selection

---

### 2. ModalBottomSheet Best Practices

**Decision**: Use `ModalBottomSheet` with `rememberModalBottomSheetState()`

**Rationale**:
- M3 component with built-in drag-to-dismiss
- Handles system back gesture
- Proper scrim and elevation

**Implementation Notes**:
```kotlin
val sheetState = rememberModalBottomSheetState()

if (uiState.showDetailSheet) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.onEvent(DetailSheetDismissed) },
        sheetState = sheetState
    ) {
        TaskDetailContent(...)
    }
}
```

**Key Considerations**:
- Sheet state is separate from content visibility
- Use `LaunchedEffect` for programmatic show/hide
- Content should be scrollable if exceeds viewport

---

### 3. Haptic Feedback in Compose

**Decision**: Use `LocalHapticFeedback.current` with `HapticFeedbackType.LongPress`

**Rationale**:
- Built into Compose; no additional dependencies
- Works across devices; graceful degradation
- LongPress provides satisfying "thump" for completion

**Implementation**:
```kotlin
val hapticFeedback = LocalHapticFeedback.current

// In LaunchedEffect collecting side effects
hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
```

**Alternatives Considered**:
- `HapticFeedbackType.TextHandleMove` - Rejected: too subtle for completion feedback
- VibrationEffect API directly - Rejected: requires platform-specific code

---

### 4. LazyColumn Animation for Task Reordering

**Decision**: Use `Modifier.animateItem()` (Compose 1.7+)

**Rationale**:
- Built-in smooth animation for item insertions/removals/moves
- Works with `key` parameter to track items
- Minimal implementation effort

**Implementation**:
```kotlin
LazyColumn {
    items(
        items = tasks,
        key = { it.id }  // Critical for animation tracking
    ) { task ->
        TaskListItem(
            task = task,
            modifier = Modifier.animateItem(
                fadeInSpec = tween(durationMillis = 250),
                fadeOutSpec = tween(durationMillis = 250),
                placementSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        )
    }
}
```

**Fallback for older Compose**:
- Use deprecated `animateItemPlacement()` if targeting older versions

---

### 5. DataStore for Segment Preferences

**Decision**: Use Jetpack DataStore Preferences for segment persistence

**Rationale**:
- Already in project dependencies (Feature 001)
- Type-safe, Flow-based
- Small data footprint (single string key)

**Implementation**:
```kotlin
// Create DataStore instance
val Context.segmentDataStore by preferencesDataStore(name = "week_preferences")

// Key definition
val SELECTED_SEGMENT_KEY = stringPreferencesKey("selected_segment")
```

**Alternatives Considered**:
- SharedPreferences - Rejected: deprecated pattern, not reactive
- Room table - Rejected: overkill for single preference

---

### 6. Pull-to-Refresh Pattern

**Decision**: Use `pullToRefresh` modifier with `PullToRefreshBox` (M3 1.3+)

**Rationale**:
- Native M3 component
- Handles gesture and indicator automatically
- Integrates with LazyColumn

**Implementation**:
```kotlin
PullToRefreshBox(
    isRefreshing = uiState.isLoading,
    onRefresh = { viewModel.onEvent(RefreshRequested) }
) {
    LazyColumn {
        // Task items
    }
}
```

**Fallback**:
- Use `pullToRefresh` modifier from accompanist if targeting older M3

---

### 7. Checkbox Animation

**Decision**: Custom animated checkbox with scale spring animation

**Rationale**:
- Default Checkbox has minimal animation
- Scale "pop" provides satisfying feedback
- Spring animation feels natural

**Implementation**:
```kotlin
@Composable
fun AnimatedTaskCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    var animationTrigger by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animationTrigger) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    LaunchedEffect(checked) {
        if (checked) {
            animationTrigger = true
            delay(150)
            animationTrigger = false
        }
    }

    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = Modifier.scale(scale)
    )
}
```

---

### 8. OwnerType to Segment Mapping

**Decision**: Map domain OwnerType to presentation Segment enum

**Rationale**:
- Domain model uses `OwnerType.SELF/PARTNER/SHARED`
- UI uses `Segment.YOU/PARTNER/SHARED` for display
- Separation allows different labeling

**Mapping**:
```kotlin
enum class Segment(val displayName: String) {
    YOU("You"),
    PARTNER("Partner"),
    SHARED("Shared");

    fun toOwnerType(): OwnerType = when (this) {
        YOU -> OwnerType.SELF
        PARTNER -> OwnerType.PARTNER
        SHARED -> OwnerType.SHARED
    }
}

fun OwnerType.toSegment(): Segment = when (this) {
    OwnerType.SELF -> Segment.YOU
    OwnerType.PARTNER -> Segment.PARTNER
    OwnerType.SHARED -> Segment.SHARED
}
```

---

### 9. Week Date Formatting

**Decision**: Use `kotlinx.datetime` with custom formatting

**Rationale**:
- Already using kotlinx.datetime in project
- Week start/end dates available from Week entity
- Custom format: "Week of Dec 30 - Jan 5"

**Implementation**:
```kotlin
fun Week.formatDateRange(): String {
    val startMonth = startDate.month.name.take(3).lowercase()
        .replaceFirstChar { it.uppercase() }
    val endMonth = endDate.month.name.take(3).lowercase()
        .replaceFirstChar { it.uppercase() }

    return if (startDate.month == endDate.month) {
        "Week of $startMonth ${startDate.dayOfMonth} - ${endDate.dayOfMonth}"
    } else {
        "Week of $startMonth ${startDate.dayOfMonth} - $endMonth ${endDate.dayOfMonth}"
    }
}
```

---

### 10. Empty State Design

**Decision**: Contextual empty states per segment

**Rationale**:
- Different messages for different contexts
- Actionable guidance for users

**States**:
| Segment | Message | Action |
|---------|---------|--------|
| You | "No tasks yet. Add one above!" | Quick-add field visible |
| Partner | "No partner connected yet" | "Invite Partner" button |
| Shared | "No shared tasks yet" | "Add Shared Task" button |

---

## Resolved Clarifications

All technical decisions have been made based on research. No outstanding clarifications needed.

## Dependencies Confirmed

- ✅ TaskRepository exists (Feature 002)
- ✅ WeekRepository exists (Feature 002)
- ✅ Task, Week domain models available (Feature 002)
- ✅ Koin DI infrastructure (Feature 001)
- ✅ Navigation shell with Week destination (Feature 001)
- ✅ DataStore available (Feature 001)
