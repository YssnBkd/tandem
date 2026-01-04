# Implementation Plan: Week Review

**Branch**: `005-week-review` | **Date**: 2026-01-03 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-week-review/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Week review flow where users reflect on their week by rating their overall experience (5-point emoji scale), reviewing each task one-by-one (Done/Tried/Skipped), and seeing a completion summary with streak information. Implements a multi-step wizard using Jetpack Navigation Compose, DataStore for review progress persistence, and follows existing MVI patterns from Features 001-004. Review window: Friday 6PM - Sunday 11:59PM.

## How to Use This Plan

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[tasks.md](./tasks.md)** | Phased task list with dependencies and validation checkpoints | **Start here** - follow task sequence |
| [quickstart.md](./quickstart.md) | Complete code samples for UI components | Copy-paste reference during implementation |
| [data-model.md](./data-model.md) | State classes, events, DataStore schema | Copy implementations for presentation layer |
| [contracts/review-operations.md](./contracts/review-operations.md) | ViewModel method implementations | Copy all event handlers |
| [research.md](./research.md) | Design decisions and rationale | Understand "why" behind choices |
| [spec.md](./spec.md) | Requirements (FR-XXX) and acceptance criteria | Verify behavior correctness |

## Technical Context

**Language/Version**: Kotlin 2.1+ (Kotlin Multiplatform)
**Primary Dependencies**: Compose Multiplatform, Koin, SQLDelight, DataStore, Jetpack Navigation Compose, kotlinx.datetime
**Storage**: SQLDelight (via Feature 002 repositories), DataStore (review progress persistence), offline-first
**Testing**: Kotlin Test (unit), Android Instrumented Tests (UI flows)
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: 60 fps UI, <100ms UI response, <5 min review session completion
**Constraints**: Offline-first (progress saved locally), Material Design 3 compliance, existing Task/Week entities
**Scale/Scope**: 2-person teams (couples), weekly task volumes (~10-50 tasks/week/person)

### Key Dependencies (from existing codebase)

| Operation | Source | Purpose |
|-----------|--------|---------|
| `taskRepository.observeTasksForWeek(weekId, userId)` | Feature 002 | Get all tasks for review |
| `taskRepository.updateTaskStatus(taskId, status)` | Feature 002 | Mark task Done/Tried/Skipped |
| `taskRepository.updateTaskReviewNote(taskId, reviewNote)` | Feature 002 | Save review note per task |
| `weekRepository.getOrCreateCurrentWeek(userId)` | Feature 002 | Ensure week exists |
| `weekRepository.updateWeekReview(weekId, rating, note)` | Feature 002 | Save overall rating |
| `authRepository.authState` | Feature 001 | Get authenticated user |

### Existing Data Model Support

The data layer **already supports** all review operations:

| Entity | Field | Purpose |
|--------|-------|---------|
| Week | `overallRating: Int?` | 1-5 rating scale |
| Week | `reviewNote: String?` | Optional note with rating |
| Week | `reviewedAt: Instant?` | Timestamp when reviewed |
| Week | `isReviewed: Boolean` | Computed property |
| Task | `reviewNote: String?` | Per-task review note |
| Task | `status: TaskStatus` | COMPLETED, TRIED, SKIPPED |

### New Operations Needed

| Operation | Purpose | Implementation |
|-----------|---------|----------------|
| `calculateStreak(userId)` | Count consecutive reviewed weeks | New use case |
| `isReviewWindowOpen()` | Check if Friday 6PM - Sunday 11:59PM | Utility function |
| Review progress persistence | Save/restore progress across sessions | DataStore |

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [x] **Relationship-First Design**: Review is self-reflection, not surveillance. Partners review their own tasks. "Together" mode is collaborative, not evaluative. Reactions are supportive (emoji), not critical.
- [x] **Weekly Rhythm**: This IS the weekly review cycle. Completes the planning → execution → review loop. Triggered Friday evening, closes Sunday night.
- [x] **Autonomous Partnership**: Each partner reviews their own tasks. In Together mode, only task owner selects outcome. Observer can only add supportive reactions, not change outcomes.
- [x] **Celebration Over Judgment**: "Tried" not "Failed". "Skipped" not "Abandoned". Summary shows positive completion metrics. Streak encourages consistency without shaming breaks.
- [x] **Intentional Simplicity**: Three simple outcomes (Done/Tried/Skipped). Optional notes only. No task editing during review. No complex analytics.

### Decision Framework

1. **Strengthens weekly rhythm?** ✅ Core feature IS the weekly review ritual
2. **Respects partner autonomy?** ✅ Each person reviews only their own task outcomes
3. **Simplest solution?** ✅ 3-step wizard, card-based UI, minimal required fields
4. **Works offline?** ✅ Progress saved locally, final result syncs on completion
5. **Material Design 3 patterns?** ✅ Full-screen cards, 48dp touch targets, M3 components

### Non-Negotiables Check

- [x] **NO tracking of partner's incomplete tasks** - Review shows only user's own tasks
- [x] **NO notifications for partner's task completions** - No notifications in review feature
- [x] **NO assigning tasks without acceptance workflow** - Review doesn't create or assign tasks
- [x] **NO shame language in UI copy** - "Tried" (not "Failed"), "Skipped" (not "Abandoned")
- [x] **NO complex task hierarchies** - Flat task list review, no subtasks

### Technical Compliance

- [x] **Clean Architecture with MVI pattern** - ReviewViewModel with ReviewUiState/ReviewEvent following PlanningViewModel patterns
- [x] **Domain layer is 100% shared code** - Review state and use cases in commonMain, UI in androidMain
- [x] **UI uses Jetpack Compose with Material Design 3** - Full-screen cards, emoji buttons, progress bar
- [x] **Offline-first architecture with SQLDelight** - Existing Task/Week repositories, DataStore for progress
- [ ] **Build validation** - Will verify after implementation

## Project Structure

### Documentation (this feature)

```text
specs/005-week-review/
├── plan.md              # This file - overview and constitution check
├── quickstart.md        # Code samples for UI components (reference)
├── data-model.md        # Data structures, SQL, state classes
├── research.md          # Design decisions and rationale
├── contracts/           # ViewModel operation implementations
│   └── review-operations.md
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

> **Implementation Flow**: Start with [tasks.md](./tasks.md) for the task sequence. Reference [quickstart.md](./quickstart.md) for complete code samples when implementing UI components.

### Source Code (this feature)

```text
composeApp/src/
├── commonMain/kotlin/org/epoque/tandem/
│   ├── presentation/review/
│   │   ├── ReviewViewModel.kt           # Main ViewModel for review wizard
│   │   ├── ReviewUiState.kt             # UI state data class
│   │   ├── ReviewEvent.kt               # Sealed class for user events
│   │   └── ReviewSideEffect.kt          # One-time side effects
│   └── domain/usecase/review/
│       ├── CalculateStreakUseCase.kt    # Calculate consecutive reviewed weeks
│       └── IsReviewWindowOpenUseCase.kt # Check Friday 6PM - Sunday 11:59PM
│
└── androidMain/kotlin/org/epoque/tandem/
    ├── ui/review/
    │   ├── ReviewScreen.kt              # Main wizard container with NavHost
    │   ├── ReviewModeSelectionScreen.kt # Solo vs Together choice
    │   ├── OverallRatingStepScreen.kt   # Step 1: Rate week with emojis
    │   ├── TaskReviewStepScreen.kt      # Step 2: Review each task
    │   ├── ReviewSummaryScreen.kt       # Step 3: Completion summary
    │   ├── ReviewBanner.kt              # Banner component for Week Tab
    │   └── components/
    │       ├── EmojiRatingSelector.kt   # 5 emoji rating buttons
    │       ├── TaskOutcomeCard.kt       # Full-screen task card
    │       └── ReviewProgressDots.kt    # Step indicator dots
    ├── di/
    │   └── ReviewModule.kt              # Koin module for review feature
    └── ui/navigation/
        └── ReviewNavGraph.kt            # Nested navigation for review steps

# DataStore for progress persistence
composeApp/src/androidMain/kotlin/org/epoque/tandem/
└── data/preferences/
    └── ReviewProgressDataStore.kt       # Save/restore review progress
```

### Shared Code (existing - no changes needed)

The following already exist and fully support review operations:

```text
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/model/
│   ├── Task.kt                          # Has reviewNote, status (TRIED, SKIPPED)
│   ├── Week.kt                          # Has overallRating, reviewNote, reviewedAt
│   └── TaskStatus.kt                    # COMPLETED, TRIED, SKIPPED exist
├── domain/repository/
│   ├── TaskRepository.kt                # Has updateTaskStatus, updateTaskReviewNote
│   └── WeekRepository.kt                # Has updateWeekReview
└── domain/usecase/week/
    └── SaveWeekReviewUseCase.kt         # Already implemented
```

**Build Validation**: All features must pass `:composeApp:compileDebugKotlinAndroid`

## Complexity Tracking

> **No violations - all Constitution checks passed.**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |

---

## Post-Design Constitution Re-Check

*Completed after Phase 1 design artifacts (research.md, data-model.md, contracts/, quickstart.md)*

### Design Decisions Validated

| Decision | Constitution Alignment |
|----------|----------------------|
| Done/Tried/Skipped outcomes | Celebration Over Judgment - positive framing |
| 5-emoji rating scale | Intentional Simplicity - intuitive, no numbers |
| Review only own tasks | Autonomous Partnership - no partner surveillance |
| Optional notes | Intentional Simplicity - minimal required input |
| Quick Finish marks as Skipped | Celebration Over Judgment - "Skipped" not "Incomplete" |
| DataStore for progress (not database) | Intentional Simplicity - transient state |
| Friday 6PM - Sunday 11:59PM window | Weekly Rhythm - review at week's end |
| Streak resets (no grace period) | Intentional Simplicity - clear rules |

### Technical Decisions Validated

| Decision | Technical Principle |
|----------|---------------------|
| ReviewViewModel in commonMain | Domain layer shared code |
| UI screens in androidMain | Platform-specific UI |
| Single side effect channel | MVI pattern consistency |
| Auth-first initialization | Match WeekViewModel/PlanningViewModel pattern |
| Koin module per feature | DI module organization |
| Reuse existing SaveWeekReviewUseCase | No redundant code |

### Final Status

✅ **All Constitution checks pass post-design**
✅ **Ready for Phase 0 research and Phase 1 design artifacts**
