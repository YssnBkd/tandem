# Mockup to Compose UI Workflow

## Architecture: Separating New from Legacy UI

### Current Package Structure (After Reorganization)
```
ui/
  components/                # NEW: Reusable design system
    SegmentedControl.kt      # Pill-style segment selector (generic)
    chips/                   # (empty - to be populated)
    goals/
      GoalCard.kt            # Goal card with progress bar
    popovers/
      OwnerSelectorPopover.kt
      DateSelectorPopover.kt
      PrioritySelectorPopover.kt
    selectors/
      GoalSelectorSheet.kt
      LabelSelectorSheet.kt
    week/                    # Week-specific components
      AnimatedCheckbox.kt
      CompletedSection.kt
      GoalBadge.kt
      PlanReviewBanner.kt
      PriorityCheckbox.kt
      TaskRowItem.kt
      TaskSectionHeader.kt
      WeekDaySelector.kt
      WeekFab.kt
      WeekHeader.kt

  screens/                   # NEW: Mockup-based screens
    goals/
      GoalsScreen.kt         # Goals list with Active/Completed tabs
      GoalUiModel.kt         # Data models for goals UI
    week/
      WeekScreen.kt
      OwnerSegment.kt        # Enum for You/Partner/Together
      TaskDetailSheet.kt
      TaskDetailModels.kt
      DetailComponents.kt
      AddTaskModal.kt
    seasons/
      SeasonsScreen.kt

  legacy/                    # OLD: Frozen, do not modify or reference
    auth/
    goals/
    partner/
    planning/
    progress/
    review/

  main/                      # Main scaffold (keep)
    MainScreen.kt
    WeekScreen.kt            # Stub that calls screens/week/WeekScreen
    GoalsScreen.kt

  navigation/                # Routing (keep)
    *.kt

  theme/                     # Design tokens (keep)
    Color.kt
    Type.kt
```

### Import Rules
```kotlin
// NEW screens and components - USE THESE
import org.epoque.tandem.ui.screens.week.*
import org.epoque.tandem.ui.components.popovers.*
import org.epoque.tandem.ui.components.selectors.*
import org.epoque.tandem.ui.components.week.*

// LEGACY - NEVER import from here in new code
// import org.epoque.tandem.ui.legacy.*  // FORBIDDEN
```

### Migration Strategy
1. **NEVER import from `ui/legacy/`** in new code
2. Create new screens in `ui/screens/[feature]/`
3. Extract reusable components to `ui/components/`
4. Update navigation to point to new screens
5. Delete legacy packages once all features are migrated

---

## Quickstart Prompt

Copy this prompt to start a new mockup-to-Compose session:

```
# Convert HTML Mockup to Compose UI

## Target
Mockup: `mockups/screens/[SCREEN_NAME].html`
Output: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/screens/[feature]/[ScreenName].kt`

## CRITICAL: Legacy Code Rules
**THE `ui/legacy/` DIRECTORY IS OFF-LIMITS. THIS IS NON-NEGOTIABLE.**

1. **NEVER search for files in `ui/legacy/`** - Do not use Glob/Grep patterns that match legacy paths
2. **NEVER read files from `ui/legacy/`** - Even if a search returns legacy files, do not open them
3. **NEVER reference or copy patterns from legacy code** - If you see a legacy path in search results, skip it entirely
4. **If a component seems to exist in legacy/**, create it fresh from the mockup instead

When searching for existing components, use these constrained patterns:
```
# CORRECT - only searches new design system
**/ui/components/**/*.kt
**/ui/screens/**/*.kt

# WRONG - might return legacy files
**/GoalCard.kt  # Could match ui/legacy/goals/GoalCard.kt
**/*.kt         # Too broad, will include legacy
```

## Other Rules
- **ONLY reference** components in `ui/components/` and `ui/screens/` (the new design system)
- Use mock data defined locally in the screen file
- Match the HTML mockup pixel-perfectly
- Read `docs/MOCKUP_TO_COMPOSE.md` for the current package structure and patterns

## Workflow

### Phase 1: Analyze Mockup
1. Open HTML mockup in Chrome browser (use tabs_context_mcp, navigate)
2. Set browser window to 412x892 (Pixel 6 dimensions) using resize_window
3. Take screenshot and save to `mockups/screenshots/[SCREEN_NAME].png`
4. Analyze: colors (extract hex values), spacing (in dp), typography, component hierarchy

### Phase 2: Build Compose Screen
1. Check `ui/components/` for existing reusable components
2. Create screen file with mock data
3. Follow these patterns from the codebase:
   - Bottom sheets: `ModalBottomSheet` with `RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)`
   - Chips: `RoundedCornerShape(16.dp)` for metadata, `RoundedCornerShape(20.dp)` for options
   - Popovers: Wrap chip+DropdownMenu in `Box` for proper anchoring
   - Keep keyboard open: Add `.focusProperties { canFocus = false }` before `.clickable()`
   - Drag handle: 36x4dp, `Color(0xFFE0E0E0)`, 2dp corner radius

### Phase 3: Visual Iteration Loop
```bash
# Build and install
./gradlew installDebug

# Launch specific screen (adjust activity/route as needed)
adb shell am start -n org.epoque.tandem/.MainActivity

# Capture screenshot
adb exec-out screencap -p > /tmp/app_screen.png
```

1. Compare ADB screenshot with mockup screenshot side-by-side
2. Fix discrepancies (spacing, colors, alignment, typography)
3. Rebuild and repeat until match is < 5% visual difference

### Phase 4: Component Extraction
After screen is complete:
1. Identify patterns used 2+ times
2. Extract to `ui/components/[category]/`
3. Update screen to use extracted components
4. Document component in this file's Component Library section

## Technical Constraints
- Kotlin Multiplatform + Compose Multiplatform
- Material 3 (`androidx.compose.material3`)
- No hardcoded strings (use constants or resources)
- Colors in `ui/theme/Color.kt`

## Start
Begin by opening the HTML mockup in Chrome and analyzing its visual structure.
```

---

## Design Tokens (Reference)

### Theme Colors (Warm Terracotta Palette)
```kotlin
// Primary - use MaterialTheme.colorScheme.primary
val TandemPrimary = Color(0xFFD97757)        // Warm terracotta

// Backgrounds
val TandemBackground = Color(0xFFFFFBF7)     // Soft eggshell
val TandemSurface = Color(0xFFFFFFFF)        // White for cards
val TandemSurfaceVariant = Color(0xFFF0EBE6) // Light warm gray

// Text - use MaterialTheme.colorScheme.onSurface/onSurfaceVariant
val TandemOnSurface = Color(0xFF4A4238)      // Warm charcoal (main text)
val TandemOnSurfaceVariant = Color(0xFF9C9488) // Warm muted (secondary text)

// Outline
val TandemOutline = Color(0xFFE0DCD6)        // Warm outline/borders
```

### Component Colors
```kotlin
// Backgrounds
val ChipBackground = Color(0xFFF5F5F5)
val DragHandleColor = Color(0xFFE0E0E0)
val DividerColor = Color(0xFFE8E8E8)

// Text
val PlaceholderColor = Color(0xFFB0B0B0)

// Priority
val PriorityP1 = Color(0xFFD1453B)  // Red
val PriorityP2 = Color(0xFFEB8909)  // Orange
val PriorityP3 = Color(0xFF246FE0)  // Blue
val PriorityP4 = Color(0xFF79747E)  // Gray

// Goals
val GoalPrimary = Color(0xFFD97757)          // Same as TandemPrimary
val GoalIconBackground = Color(0xFFFDF2EF)   // Soft coral
val GoalProgressBackground = Color(0xFFF0EBE6)
val GoalTextMuted = Color(0xFF9C9488)
```

### Spacing
```kotlin
val SpacingXs = 4.dp
val SpacingSm = 8.dp
val SpacingMd = 12.dp
val SpacingLg = 16.dp
val SpacingXl = 20.dp
val SpacingXxl = 24.dp
```

### Corner Radius
```kotlin
val RadiusChip = 16.dp
val RadiusOptionChip = 20.dp
val RadiusSheet = 20.dp
val RadiusPopover = 12.dp
val RadiusDragHandle = 2.dp
```

### Typography
```kotlin
// Task title: 18.sp, FontWeight.Medium
// Description: 14.sp, FontWeight.Normal, lineHeight 20.sp
// Chip label: labelMedium (MaterialTheme)
// Section header: bodySmall, FontWeight.SemiBold, uppercase
```

---

## Component Library

### Already Implemented

| Component | Location | Usage |
|-----------|----------|-------|
| `SegmentedControl` | components/ | Pill-style segment selector (generic, reusable) |
| `GoalCard` | components/goals/ | Goal card with icon, progress bar, ownership label |
| `MetadataChip` | TaskDetailModal, AddTaskModal | Icon + text chip (Owner, Date, Priority) |
| `MetadataChipWithEmoji` | TaskDetailModal, AddTaskModal | Emoji + text chip |
| `LabelChip` | TaskDetailModal, AddTaskModal | Colored dot + label name |
| `OptionChip` | TaskDetailModal, AddTaskModal | Deadline, Reminders, Location, Repeat |
| `OwnerSelectorPopover` | popovers/ | Me/Partner/Together selector |
| `DateSelectorPopover` | popovers/ | Today/Tomorrow/Next week/Pick date |
| `PrioritySelectorPopover` | popovers/ | P1/P2/P3/P4 with colored flags |
| `GoalSelectorSheet` | selectors/ | Bottom sheet with goal list |
| `LabelSelectorSheet` | selectors/ | Multi-select bottom sheet for labels |
| `DragHandle` | TaskDetailModal, AddTaskModal | Standard sheet drag indicator |

### Patterns to Reuse

**SegmentedControl Usage**
```kotlin
SegmentedControl(
    segments = listOf("Active", "Completed"),
    selectedIndex = selectedIndex,
    onSegmentSelected = { selectedIndex = it },
    modifier = Modifier.padding(horizontal = 16.dp),
    selectedColor = MaterialTheme.colorScheme.primary,  // Optional
    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant  // Optional
)
```

**GoalCard Usage**
```kotlin
GoalCard(
    goal = GoalUiModel(
        id = "1",
        icon = "🧘‍♀️",
        name = "Morning Yoga",
        ownershipLabel = "Together",
        ownershipType = OwnershipType.TOGETHER,
        progressFraction = 0.66f,
        progressText = "2 of 3 this week",
        goalTypeLabel = "Habit"
    ),
    onClick = { /* navigate to goal detail */ }
)
```

**Popover Anchoring**
```kotlin
Box {
    MetadataChip(
        text = "Today",
        onClick = { showPopover = true }
    )
    DateSelectorPopover(
        expanded = showPopover,
        onDismiss = { showPopover = false },
        onDateSelected = { ... }
    )
}
```

**Keyboard-Safe Clickable**
```kotlin
Row(
    modifier = Modifier
        .focusProperties { canFocus = false }  // Prevents keyboard dismiss
        .clickable(onClick = onClick)
)
```

**Selection Highlight in Popovers**
```kotlin
val backgroundColor = if (isSelected) {
    MaterialTheme.colorScheme.primaryContainer
} else {
    Color.Transparent
}
val contentColor = if (isSelected) {
    MaterialTheme.colorScheme.onPrimaryContainer
} else {
    MaterialTheme.colorScheme.onSurface
}
```

---

## Checklist for New Screens

- [ ] HTML mockup analyzed and screenshot saved
- [ ] Screen created in `ui/screens/[feature]/`
- [ ] Mock data defined in screen file
- [ ] Existing components from `ui/components/` reused
- [ ] New reusable components extracted
- [ ] Visual comparison passes (< 5% difference)
- [ ] Keyboard behavior correct (stays open when needed)
- [ ] Popovers anchor correctly to their triggers
- [ ] Colors match design tokens
- [ ] No references to `ui/legacy/`
