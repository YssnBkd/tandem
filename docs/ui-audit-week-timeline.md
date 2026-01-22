# UI Audit: Week and Timeline Screens

Audit against iOS Human Interface Guidelines.

## Executive Summary

The current implementation uses **Material Design 3** patterns which differ from iOS HIG in several areas. The main issues are:

1. **No centralized design tokens** - Spacing/sizing values are hardcoded throughout components
2. **Typography doesn't match iOS text styles** - Using MD3 scale instead of iOS scale
3. **Inconsistent spacing** - Not following 8pt grid consistently
4. **Touch target issues** - Some interactive elements below 44pt minimum

---

## Typography Issues

### Current State (`Type.kt`)

| Style | Current Size | iOS Equivalent | iOS Size |
|-------|-------------|----------------|----------|
| bodyLarge | 16sp | Body | 17pt |
| bodyMedium | 14sp | Subheadline | 15pt |
| bodySmall | 12sp | Caption 1 | 12pt |
| titleLarge | 22sp | Title 2 | 22pt |
| titleMedium | 16sp | Callout | 16pt |
| titleSmall | 14sp | - | - |
| labelLarge | 14sp | - | 14pt |
| labelMedium | 12sp | Caption 1 | 12pt |
| labelSmall | 11sp | Caption 2 | 11pt |
| headlineMedium | 28sp | Title 1 | 28pt |

### Issues Found

1. **Task title** (`TaskRowItem.kt:81-82`): Uses `bodyLarge.copy(fontSize = 15.sp)` - inconsistent override
2. **Week header title** (`WeekHeader.kt:49-51`): Uses `headlineMedium.copy(fontSize = 24.sp)` - should be Large Title (34pt) or use a consistent style
3. **Section headers** (`TaskSectionHeader.kt:40-43`): Uses `labelLarge.copy(fontSize = 14.sp)` - iOS uses 13pt Footnote for section headers typically
4. **Metadata text** (`TaskRowItem.kt:163`): Uses `labelSmall.copy(fontSize = 12.sp)` - redundant override
5. **Day selector** (`WeekDaySelector.kt:171`): Uses 10.sp for day names - matches iOS tab bar minimum

### Recommendations

Create iOS-aligned text styles:

```kotlin
// Recommended iOS text styles
val TandemTypography = Typography(
    // Large Title - 34pt Regular
    displayLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Normal, lineHeight = 41.sp),

    // Title 1 - 28pt Regular
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Normal, lineHeight = 34.sp),

    // Title 2 - 22pt Regular
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal, lineHeight = 28.sp),

    // Title 3 - 20pt Regular
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal),

    // Headline - 17pt Semibold
    titleLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),

    // Body - 17pt Regular
    bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),

    // Callout - 16pt Regular
    bodyMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),

    // Subheadline - 15pt Regular
    bodySmall = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),

    // Footnote - 13pt Regular
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal),

    // Caption 1 - 12pt Regular
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),

    // Caption 2 - 11pt Regular
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, lineHeight = 13.sp),
)
```

---

## Spacing Issues

### Current State

No centralized spacing tokens. Values found scattered across components:

| Value | Occurrences | Context |
|-------|-------------|---------|
| 2.dp | 5 | Spacer heights, internal padding |
| 4.dp | 15+ | Icon gaps, small spacers, task indicator dots |
| 6.dp | 8 | Chip padding, arrangement spacing |
| 8.dp | 12+ | Standard gaps, vertical padding |
| 12.dp | 20+ | Common padding, icon sizes, row spacing |
| 16.dp | 30+ | Horizontal margins, card padding |
| 20.dp | 10+ | Icon sizes, sheet corners |
| 32.dp | 5 | Empty state padding, larger spacers |

### Issues Found

1. **Margins inconsistent**: Most use 16.dp (correct for iPhone) but some use 8.dp
2. **Non-8pt values**: 2.dp, 6.dp don't follow 8pt grid
3. **No semantic tokens**: `12.dp` used for both icon sizes and padding (different purposes)

### Recommendations

Create spacing tokens:

```kotlin
object TandemSpacing {
    // 8pt grid
    val xxxs = 2.dp   // Exception: tight internal spacing
    val xxs = 4.dp    // Icon gaps, indicator dots
    val xs = 8.dp     // Related item spacing
    val sm = 12.dp    // Internal component padding
    val md = 16.dp    // Standard margins, card padding
    val lg = 24.dp    // Section spacing
    val xl = 32.dp    // Large section breaks
    val xxl = 40.dp   // Page-level spacing

    // Semantic aliases
    val screenHorizontalPadding = md  // 16.dp for iPhone
    val listItemPadding = sm          // 12.dp
    val sectionSpacing = md           // 16.dp
    val cardPadding = md              // 16.dp
}
```

---

## Touch Target Issues

### Current State

| Component | Current Size | Min Required | Status |
|-----------|-------------|--------------|--------|
| PriorityCheckbox | 20.dp | 44.dp | **FAIL** |
| WeekDaySelector arrows | 32.dp | 44.dp | **FAIL** |
| SeasonContextChip | ~30.dp height | 44.dp | **FAIL** |
| TimelineFilterBar chip | ~32.dp height | 44.dp | **FAIL** |
| TaskRowItem | Full width, 12.dp vertical padding | 44.dp | **OK** (row height ~48dp+) |
| TimelineWeekCard | Full width, 16.dp padding | 44.dp | **OK** |

### Recommendations

1. **PriorityCheckbox** (`PriorityCheckbox.kt:56`): Keep visual at 20.dp but add 44.dp touch target
2. **IconButton navigation** (`WeekDaySelector.kt:72,98`): Change from 32.dp to 44.dp
3. **Chips**: Ensure minimum 44.dp height for all tappable chips

---

## Component-Specific Findings

### WeekScreen.kt

| Line | Issue | Recommendation |
|------|-------|----------------|
| 221 | `padding(horizontal = 16.dp, vertical = 8.dp)` | Use spacing tokens |
| 333 | `Spacer(Modifier.height(80.dp))` | Use semantic FAB spacing constant |
| 438 | `Arrangement.spacedBy(16.dp)` | Use spacing tokens |

### TimelineScreen.kt

| Line | Issue | Recommendation |
|------|-------|----------------|
| 158 | `PaddingValues(bottom = 80.dp)` | Use semantic FAB spacing constant |
| 219 | `Spacer(Modifier.height(16.dp))` | Use spacing tokens |
| 240-241 | `padding(horizontal = 16.dp, vertical = 8.dp)` | Use spacing tokens |
| 259 | `padding(horizontal = 12.dp, vertical = 6.dp)` | Touch target too small (6.dp vertical) |

### TaskRowItem.kt

| Line | Issue | Recommendation |
|------|-------|----------------|
| 61 | `padding(horizontal = 16.dp, vertical = 12.dp)` | Use spacing tokens |
| 81-82 | `fontSize = 15.sp` override | Use consistent text style |
| 115-116 | `Modifier.size(12.dp)` for icon | Use icon size token |
| 163 | `fontSize = 12.sp` override | Redundant, remove override |

### WeekHeader.kt

| Line | Issue | Recommendation |
|------|-------|----------------|
| 44 | `padding(horizontal = 16.dp, vertical = 12.dp)` | Use spacing tokens |
| 49-52 | `fontSize = 24.sp` override | Should use Large Title (34sp) or Title 1 (28sp) |
| 93 | `padding(horizontal = 12.dp, vertical: 4.dp)` | Touch target too small |

### TimelineWeekCard.kt

| Line | Issue | Recommendation |
|------|-------|----------------|
| 70 | `padding(horizontal: 16.dp, vertical: 4.dp)` | Card vertical spacing inconsistent |
| 76-82 | Border widths 2.dp and 1.dp | Consider using border tokens |
| 86 | `RoundedCornerShape(12.dp)` | Use corner radius token |
| 125 | `padding(horizontal: 6.dp, vertical: 2.dp)` | Touch target for badge |

---

## Architecture Recommendations

### 1. Create Design System Module

```
ui/
  theme/
    Color.kt          (existing)
    Type.kt           (update with iOS styles)
    Spacing.kt        (NEW)
    Sizing.kt         (NEW - icon sizes, touch targets)
    Shape.kt          (NEW - corner radii)
    Theme.kt          (existing)
```

### 2. Spacing.kt

```kotlin
object TandemSpacing {
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp

    object Screen {
        val horizontalPadding = md
        val topPadding = sm
    }

    object List {
        val itemVerticalPadding = sm
        val itemHorizontalPadding = md
        val sectionHeaderTop = md
        val sectionHeaderBottom = xs
    }

    object Card {
        val padding = md
        val spacing = xs
    }
}
```

### 3. Sizing.kt

```kotlin
object TandemSizing {
    // Touch targets
    val minTouchTarget = 44.dp

    // Icons
    object Icon {
        val xs = 12.dp    // Metadata icons
        val sm = 16.dp    // Inline icons
        val md = 20.dp    // Standard icons
        val lg = 24.dp    // Navigation icons
        val xl = 32.dp    // Feature icons
    }

    // Components
    object Component {
        val checkboxVisual = 20.dp
        val checkboxTouchTarget = 44.dp
        val progressBarHeight = 4.dp
        val progressBarWidth = 40.dp
        val taskIndicatorDot = 4.dp
    }

    // Navigation
    object Navigation {
        val statusBarHeight = 54.dp  // iPhone with notch
        val navBarHeight = 44.dp
        val tabBarHeight = 49.dp
        val fabBottomSpacing = 80.dp
    }
}
```

### 4. Shape.kt

```kotlin
object TandemShapes {
    val none = RoundedCornerShape(0.dp)
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(50)

    object Card {
        val default = md
    }

    object Chip {
        val default = pill
    }

    object Sheet {
        val top = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    }
}
```

---

## Migration Priority

### High Priority (Touch Targets - Accessibility)
1. Fix PriorityCheckbox touch target
2. Fix WeekDaySelector navigation arrow sizes
3. Fix filter chip touch targets

### Medium Priority (Visual Consistency)
1. Create spacing tokens and migrate components
2. Align typography with iOS text styles
3. Create reusable size tokens

### Low Priority (Polish)
1. Review letter spacing values
2. Audit line heights
3. Add Dynamic Type support

---

## Files to Modify

| File | Changes Required |
|------|------------------|
| `ui/theme/Type.kt` | Update to iOS text styles |
| `ui/theme/Spacing.kt` | Create new file |
| `ui/theme/Sizing.kt` | Create new file |
| `ui/theme/Shape.kt` | Create new file |
| `ui/components/week/TaskRowItem.kt` | Use tokens, fix overrides |
| `ui/components/week/WeekHeader.kt` | Use tokens, fix title size |
| `ui/components/week/WeekDaySelector.kt` | Fix touch targets |
| `ui/components/week/TaskSectionHeader.kt` | Use tokens |
| `ui/components/week/PriorityCheckbox.kt` | Fix touch target |
| `ui/components/timeline/TimelineWeekCard.kt` | Use tokens |
| `ui/components/timeline/TimelineFilterBar` | Fix touch target |
| `ui/screens/week/WeekScreen.kt` | Use tokens |
| `ui/screens/timeline/TimelineScreen.kt` | Use tokens |
