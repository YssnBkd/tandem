# Implementation Plan: Task Data Layer

**Branch**: `002-task-data-layer` | **Date**: 2026-01-01 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-task-data-layer/spec.md`

## Summary

Implement the core task data model, local database, and repository for managing tasks in Tandem. This feature provides the foundational data layer using SQLDelight for type-safe local persistence, supporting CRUD operations for tasks and weeks, with filtering by week and owner type. The implementation follows Clean Architecture with repository pattern, exposing reactive Flows for UI consumption.

## Technical Context

**Language/Version**: Kotlin 2.3.0 (Kotlin Multiplatform)
**Primary Dependencies**: SQLDelight 2.0+, Kotlin Coroutines Flow, kotlinx.datetime
**Storage**: SQLDelight (local), offline-first (no sync in this feature)
**Testing**: Kotlin Test (unit), in-memory SQLDelight driver for repository tests
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: <1 second query response for 1000 tasks, efficient battery usage
**Constraints**: Offline-first (all operations work without network), data layer only (no UI)
**Scale/Scope**: 2-person teams (couples), weekly task volumes (~10-50 tasks/week/person)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [x] **Relationship-First Design**: Data layer stores owner type (SELF/PARTNER/SHARED) without enabling surveillance. Partner tasks require PENDING_ACCEPTANCE status.
- [x] **Weekly Rhythm**: All tasks are associated with a week ID (ISO 8601 format). Week entity is a first-class citizen.
- [x] **Autonomous Partnership**: PENDING_ACCEPTANCE status enables acceptance workflow. Data model doesn't force task assignment.
- [x] **Celebration Over Judgment**: TaskStatus uses positive terminology (TRIED, SKIPPED instead of FAILED, ABANDONED).
- [x] **Intentional Simplicity**: No due dates within weeks, no priority levels, no subtasks, no categories in data model.

### Decision Framework

1. Does it strengthen the weekly rhythm? **YES** - Week is the organizing unit for all tasks
2. Does it respect partner autonomy? **YES** - Tasks have owner type, acceptance status supported
3. Is it the simplest solution that works? **YES** - Flat task structure, no hierarchies
4. Can it work offline? **YES** - SQLDelight local database, no network required
5. Does it follow Material Design 3 patterns? **N/A** - Data layer only, no UI

### Non-Negotiables Check

- [x] NO tracking of partner's incomplete tasks - Data layer stores data neutrally; UI layer controls visibility
- [x] NO notifications for partner's task completions - Data layer doesn't handle notifications
- [x] NO assigning tasks without acceptance workflow - PENDING_ACCEPTANCE status enables this
- [x] NO shame language in UI copy - Data layer uses TRIED/SKIPPED terminology
- [x] NO complex task hierarchies - Flat task structure, only optional linkedGoalId

### Technical Compliance

- [x] Clean Architecture with MVI pattern - Repository pattern, domain interfaces
- [x] Domain layer is 100% shared code (Kotlin Multiplatform) - Models and interfaces in shared/commonMain
- [ ] UI uses Jetpack Compose with Material Design 3 - N/A (data layer only)
- [x] Offline-first architecture with SQLDelight - Local database, no network dependency
- [ ] Build validation: `:composeApp:compileDebugKotlinAndroid` succeeds - To verify after implementation

## Project Structure

### Documentation (this feature)

```text
specs/002-task-data-layer/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (repository interfaces)
│   ├── task-repository.md
│   └── week-repository.md
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

**Implementation follows user-specified module structure:**

```text
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/
│   │   ├── Task.kt              # Task domain entity
│   │   ├── Week.kt              # Week domain entity
│   │   ├── OwnerType.kt         # SELF, PARTNER, SHARED enum
│   │   └── TaskStatus.kt        # Task lifecycle states
│   ├── repository/
│   │   ├── TaskRepository.kt    # Task repository interface
│   │   └── WeekRepository.kt    # Week repository interface
│   └── usecase/
│       ├── task/
│       │   ├── CreateTaskUseCase.kt
│       │   ├── GetTasksForWeekUseCase.kt
│       │   ├── UpdateTaskStatusUseCase.kt
│       │   └── DeleteTaskUseCase.kt
│       └── week/
│           ├── GetCurrentWeekUseCase.kt
│           └── SaveWeekReviewUseCase.kt
└── data/
    ├── local/
    │   └── sqldelight/
    │       └── org/epoque/tandem/
    │           ├── TandemDatabase.sq    # Database definition
    │           ├── Task.sq              # Task queries
    │           └── Week.sq              # Week queries
    └── repository/
        ├── TaskRepositoryImpl.kt
        └── WeekRepositoryImpl.kt

composeApp/src/androidMain/kotlin/org/epoque/tandem/
└── di/
    └── TaskModule.kt            # Koin DI module for task feature
```

**Build Validation**: `:composeApp:compileDebugKotlinAndroid` must succeed

## Complexity Tracking

> No violations identified. Implementation follows constitution principles.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| *None* | - | - |
