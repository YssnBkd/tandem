# Implementation Plan: Progress & Insights

**Branch**: `008-progress-insights` | **Date**: 2026-01-04 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/008-progress-insights/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Progress tab showing streak information, completion trends over past 8 weeks, and browsable past weeks with partner comparison. Uses existing Week, Task, and Partnership data models with computed completion percentages. Custom Canvas-based chart implementation for trends to maintain minimal dependencies.

## Technical Context

**Language/Version**: Kotlin 2.1+ (Kotlin Multiplatform)
**Primary Dependencies**: Compose Multiplatform, Koin, SQLDelight, DataStore, kotlinx.datetime
**Storage**: SQLDelight (local), DataStore (milestone preferences), offline-first
**Testing**: Kotlin Test (unit), Android Instrumented Tests (UI)
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: <2s Progress tab load, <1s past week list initial load, 60 fps charts
**Constraints**: Offline-first (all features work without network), Material Design 3 compliance
**Scale/Scope**: 2-person teams (couples), historical data spanning 8-52+ weeks

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [x] **Relationship-First Design**: Shows mutual progress data (both completed, not just user's), celebrates shared streaks, focuses on celebration not comparison. No "who did more" judgment.
- [x] **Weekly Rhythm**: Entirely centered on weekly reviews - streak counts weekly review completions, trends show weekly percentages, past weeks list weekly summaries.
- [x] **Autonomous Partnership**: Read-only historical view. No ability to affect partner's tasks. Partner data shown for mutual awareness, not control.
- [x] **Celebration Over Judgment**: Uses "Tried"/"Skipped" terminology, milestone celebrations at 5/10/20/50 weeks, positive framing ("3-week streak!" not "missed 2 weeks").
- [x] **Intentional Simplicity**: No complex analytics, no daily breakdowns, no task categories. Simple: streak count + completion bar + past weeks list.

### Decision Framework

1. Does it strengthen the weekly rhythm? ✅ Yes - encourages completing weekly reviews through streak mechanic
2. Does it respect partner autonomy? ✅ Yes - read-only visibility of mutual progress
3. Is it the simplest solution that works? ✅ Yes - Canvas charts, no external libraries, minimal entities
4. Can it work offline? ✅ Yes - all data from local SQLDelight, cached Week/Task data
5. Does it follow Material Design 3 patterns? ✅ Yes - Cards, horizontal bars, standard navigation

### Non-Negotiables Check

- [x] NO tracking of partner's incomplete tasks - Shows completion percentages only, not pending task lists
- [x] NO notifications for partner's task completions (default off) - No notifications in this feature at all
- [x] NO assigning tasks without acceptance workflow - Read-only feature, no task creation
- [x] NO shame language in UI copy - "Tried"/"Skipped" not "Failed", milestone celebrations, no "you're behind"
- [x] NO complex task hierarchies - Flat task counts, no subtask aggregation

### Technical Compliance

- [x] Clean Architecture with MVI pattern - ProgressViewModel with UiState, Events, SideEffects
- [x] Domain layer is 100% shared code (Kotlin Multiplatform) - Use cases in shared/commonMain
- [x] UI uses Jetpack Compose with Material Design 3 - ProgressScreen.kt in androidMain
- [x] Offline-first architecture with SQLDelight - Queries existing Week/Task tables
- [ ] Build validation: `:composeApp:compileDebugKotlinAndroid` succeeds - To be verified after implementation

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

**Tandem uses Kotlin Multiplatform with the following structure:**

```text
composeApp/
├── src/
│   ├── commonMain/kotlin/           # Shared code (domain, data, presentation)
│   │   ├── domain/                  # Use cases, entities, repository interfaces
│   │   ├── data/                    # Repository implementations, data sources
│   │   └── presentation/            # ViewModels, UI state
│   ├── androidMain/kotlin/          # Android-specific code
│   │   ├── ui/                      # Compose UI screens and components
│   │   ├── theme/                   # Material Design 3 theme
│   │   └── di/                      # Koin modules
│   └── iosMain/kotlin/              # iOS-specific code (future)
└── build.gradle.kts

shared/
├── src/
│   ├── commonMain/kotlin/           # Additional shared utilities
│   ├── androidMain/kotlin/
│   └── iosMain/kotlin/
└── build.gradle.kts

iosApp/                              # iOS application entry point (future)
└── iosApp/

# Testing (within composeApp/src/)
commonTest/kotlin/                   # Shared unit tests
androidInstrumentedTest/kotlin/      # Android UI tests
```

**Build Validation**: All features must pass `:composeApp:compileDebugKotlinAndroid`

**Structure Decision**: Tandem follows Kotlin Multiplatform Clean Architecture with MVI:
- Domain layer in `commonMain` (100% shared)
- Data layer in `commonMain` with platform-specific implementations when needed
- Presentation layer (ViewModels) in `commonMain`
- UI layer (Compose) in `androidMain` (platform-specific)

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations identified. Feature passes all constitution checks.

## Post-Design Constitution Re-Evaluation

*Completed: 2026-01-04*

After completing Phase 1 design artifacts (research.md, data-model.md, contracts/, quickstart.md), re-evaluated constitution compliance:

### Design Decisions Verified

1. **Custom Canvas Charts** - Maintains intentional simplicity (no external chart library)
2. **DataStore for Milestones** - Simple key-value, no new database tables
3. **Computed Aggregations** - All derived from existing Week/Task entities, no denormalization
4. **Partner Streak Logic** - Requires BOTH partners complete review, promoting relationship-first collaboration
5. **Mood Emoji Mapping** - Positive framing (1-5 rating → emojis, no negative labels)
6. **Task Status Display** - Uses "Done"/"Tried"/"Skipped" per Celebration Over Judgment principle

### No New Violations Introduced

- No new tracking of partner's pending/incomplete tasks
- No comparison language ("you did more than partner")
- No shame messaging ("you're falling behind")
- No complex analytics beyond simple completion percentages
- No daily or hourly breakdowns (weekly rhythm preserved)

**Status**: ✅ Design phase complete, ready for tasks.md generation via `/speckit.tasks`

## Generated Artifacts

| Artifact | Path | Status |
|----------|------|--------|
| Research | `specs/008-progress-insights/research.md` | ✅ Complete |
| Data Model | `specs/008-progress-insights/data-model.md` | ✅ Complete |
| Contracts | `specs/008-progress-insights/contracts/` | ✅ Complete |
| Quickstart | `specs/008-progress-insights/quickstart.md` | ✅ Complete |
| Tasks | `specs/008-progress-insights/tasks.md` | ✅ Complete |
