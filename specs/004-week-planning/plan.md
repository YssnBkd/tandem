# Implementation Plan: Week Planning

**Branch**: `004-week-planning` | **Date**: 2026-01-03 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-week-planning/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Weekly planning flow (Sunday Setup) where users review incomplete tasks from the previous week, add new tasks, review partner requests, and complete planning with a confirmation summary. Implements a 4-step wizard using Jetpack Navigation Compose with nested navigation, DataStore for planning progress persistence, and follows existing MVI patterns from Features 001-003.

## Technical Context

**Language/Version**: Kotlin 2.1+ (Kotlin Multiplatform)
**Primary Dependencies**: Compose Multiplatform, Koin, SQLDelight, DataStore, Jetpack Navigation Compose, kotlinx.datetime
**Storage**: SQLDelight (via Feature 002 repositories), DataStore (planning progress persistence), offline-first
**Testing**: Kotlin Test (unit), Android Instrumented Tests (UI flows)
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: 60 fps UI, <100ms UI response, <5 min planning session completion
**Constraints**: Offline-first (progress saved locally), Material Design 3 compliance, existing Task/Week entities
**Scale/Scope**: 2-person teams (couples), weekly task volumes (~10-50 tasks/week/person)

### Key Dependencies (from existing codebase)
- **TaskRepository**: `observeTasksForWeek()`, `createTask()`, `updateTaskStatus()` - all operations exist
- **WeekRepository**: `getOrCreateCurrentWeek()`, `markPlanningCompleted()`, `getCurrentWeekId()` - all operations exist
- **AuthRepository**: `authState` Flow with `AuthState.Authenticated` - pattern established
- **Week.planningCompletedAt**: Already in domain model (Instant?)
- **Task.rolledFromWeekId**: Already in domain model (String?)
- **Task.status**: Includes `PENDING_ACCEPTANCE` for partner requests

### Missing Operations (NEEDS IMPLEMENTATION)
- **getIncompleteTasksForWeek(weekId)**: Query tasks with status != COMPLETED && status != SKIPPED && status != TRIED
- **getPreviousWeekId(currentWeekId)**: Calculate previous ISO 8601 week ID
- **getTasksByStatus(status, userId)**: Query pending partner requests

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [x] **Relationship-First Design**: Strengthens relationships through intentional weekly check-in. Partner requests require acceptance (FR-016). "Discuss" option respects autonomy over immediate obligation. No surveillance of partner's planning progress.
- [x] **Weekly Rhythm**: This IS the weekly planning cycle. Triggered Sunday 6pm, centered on weekly task commitment. The wizard flow embodies the planning → execution cadence.
- [x] **Autonomous Partnership**: Partner requests REQUIRE acceptance (FR-016). User can Accept or Discuss - no forced task assignment. Original tasks remain unchanged in previous week when rolled over.
- [x] **Celebration Over Judgment**: "Skip" language (FR-008), not "Abandon" or "Fail". Confirmation screen shows positive "X tasks planned" summary. No shame language in wizard steps.
- [x] **Intentional Simplicity**: No due dates, priority levels, subtasks, or categories in planning. Simple swipe/tap card flow. Minimal wizard steps (4). "Add" creates flat tasks with title only (notes optional).

### Decision Framework

1. **Strengthens weekly rhythm?** ✅ Core feature IS the weekly planning ritual
2. **Respects partner autonomy?** ✅ Requests require acceptance, no forced assignments
3. **Simplest solution?** ✅ 4-step wizard, card-based UI, minimal required fields
4. **Works offline?** ✅ FR-004a mandates offline-first with local save
5. **Material Design 3 patterns?** ✅ Full-screen cards, 48dp touch targets, M3 components

### Non-Negotiables Check

- [x] **NO tracking of partner's incomplete tasks** - Only shows partner REQUESTS to user, not partner's personal incomplete tasks
- [x] **NO notifications for partner's task completions** - No notifications in this feature
- [x] **NO assigning tasks without acceptance workflow** - FR-016 explicitly requires Accept action
- [x] **NO shame language in UI copy** - "Skip" (not "Abandon"), "Planned" (not "Committed/Failed")
- [x] **NO complex task hierarchies** - Flat task list, no subtasks, no categories

### Technical Compliance

- [x] **Clean Architecture with MVI pattern** - PlanningViewModel with PlanningUiState/PlanningEvent following WeekViewModel patterns
- [x] **Domain layer is 100% shared code** - Planning preferences and state in commonMain, UI in androidMain
- [x] **UI uses Jetpack Compose with Material Design 3** - Full-screen cards, Surface, Button, IconButton
- [x] **Offline-first architecture with SQLDelight** - Existing Task/Week repositories, DataStore for progress
- [ ] **Build validation** - Will verify after implementation

## Project Structure

### Documentation (this feature)

```text
specs/004-week-planning/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (internal operations, no REST API)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (this feature)

```text
composeApp/src/
├── commonMain/kotlin/org/epoque/tandem/
│   └── presentation/planning/
│       ├── PlanningViewModel.kt         # Main ViewModel for wizard flow
│       ├── PlanningUiState.kt           # UI state data class
│       ├── PlanningEvent.kt             # Sealed class for user events
│       ├── PlanningSideEffect.kt        # One-time side effects
│       └── preferences/
│           └── PlanningProgress.kt      # DataStore for resume capability
│
└── androidMain/kotlin/org/epoque/tandem/
    ├── ui/planning/
    │   ├── PlanningScreen.kt            # Main wizard container with NavHost
    │   ├── RolloverStepScreen.kt        # Step 1: Review incomplete tasks
    │   ├── AddTasksStepScreen.kt        # Step 2: Add new tasks
    │   ├── PartnerRequestsStepScreen.kt # Step 3: Accept/discuss requests
    │   ├── ConfirmationStepScreen.kt    # Step 4: Summary
    │   ├── PlanningBanner.kt            # Banner component for Week Tab
    │   └── components/
    │       ├── PlanningCard.kt          # Full-screen card for rollover/requests
    │       ├── ProgressDots.kt          # Step indicator dots
    │       └── TaskInputField.kt        # Reusable text input with Add button
    ├── di/
    │   └── PlanningModule.kt            # Koin module for planning feature
    └── ui/navigation/
        └── PlanningNavGraph.kt          # Nested navigation for wizard steps
```

### Shared Code (existing - to extend)

```text
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/repository/
│   └── TaskRepository.kt              # Add: observeIncompleteTasksForWeek(), observeTasksByStatus()
└── data/repository/
    └── TaskRepositoryImpl.kt          # Add: implementations for new queries

shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/
└── Task.sq                            # Add: getIncompleteTasksByWeekId, getTasksByStatus queries
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
| Rollover creates new task, original unchanged | Autonomous Partnership - no modification of past data |
| Partner requests require Accept tap | Autonomous Partnership - explicit consent |
| "Discuss" shows "Coming soon" vs. force action | Relationship-First - no pressure to accept immediately |
| "Skip" button label | Celebration Over Judgment - no shame language |
| 4-step wizard (no additional complexity) | Intentional Simplicity - minimal viable flow |
| DataStore for progress (not database) | Intentional Simplicity - transient state doesn't need schema |
| No notifications added | Non-Negotiable compliance |

### Technical Decisions Validated

| Decision | Technical Principle |
|----------|---------------------|
| PlanningViewModel in commonMain | Domain layer shared code |
| UI screens in androidMain | Platform-specific UI |
| Single side effect channel | MVI pattern consistency |
| Auth-first initialization | Match WeekViewModel pattern |
| Koin module per feature | DI module organization |

### Final Status

✅ **All Constitution checks pass post-design**
✅ **Ready for `/speckit.tasks` command**
