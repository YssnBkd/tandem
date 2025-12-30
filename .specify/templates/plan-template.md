# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

**Language/Version**: Kotlin 2.1+ (Kotlin Multiplatform)
**Primary Dependencies**: Compose Multiplatform, Koin, SQLDelight, DataStore, WorkManager
**Storage**: SQLDelight (local), DataStore (preferences), offline-first with sync queue
**Testing**: Kotlin Test (unit), Android Instrumented Tests (UI)
**Target Platform**: Android 8.0+ (SDK 26), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: 60 fps UI, <100ms UI response, efficient battery usage
**Constraints**: Offline-first (core features work without network), Material Design 3 compliance
**Scale/Scope**: 2-person teams (couples), weekly task volumes (~10-50 tasks/week/person)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [ ] **Relationship-First Design**: Does this feature strengthen relationships without enabling surveillance/nagging/control?
- [ ] **Weekly Rhythm**: Is the feature centered on the weekly planning → execution → review cycle?
- [ ] **Autonomous Partnership**: Does the feature respect partner autonomy and require acceptance for requests?
- [ ] **Celebration Over Judgment**: Does the UI use positive framing (Tried/Skipped vs Failed/Abandoned)?
- [ ] **Intentional Simplicity**: Does the feature avoid due dates, priority levels, subtasks, and categories?

### Decision Framework

1. Does it strengthen the weekly rhythm?
2. Does it respect partner autonomy?
3. Is it the simplest solution that works?
4. Can it work offline?
5. Does it follow Material Design 3 patterns?

### Non-Negotiables Check

- [ ] NO tracking of partner's incomplete tasks
- [ ] NO notifications for partner's task completions (default off)
- [ ] NO assigning tasks without acceptance workflow
- [ ] NO shame language in UI copy
- [ ] NO complex task hierarchies

### Technical Compliance

- [ ] Clean Architecture with MVI pattern
- [ ] Domain layer is 100% shared code (Kotlin Multiplatform)
- [ ] UI uses Jetpack Compose with Material Design 3
- [ ] Offline-first architecture with SQLDelight
- [ ] Build validation: `:composeApp:compileDebugKotlinAndroid` succeeds

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

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
