# Tasks: Task Data Layer

**Input**: Design documents from `/specs/002-task-data-layer/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/task-repository.md, contracts/week-repository.md

**Tests**: Tests are included as this is a data layer requiring validation of persistence and CRUD operations.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4, US5)
- Includes exact file paths in descriptions

## Path Conventions

This is a Kotlin Multiplatform project with the following structure:
- `shared/src/commonMain/kotlin/` - Shared code (domain, data)
- `shared/src/commonTest/kotlin/` - Shared tests
- `shared/src/androidMain/kotlin/` - Android-specific implementations
- `shared/src/iosMain/kotlin/` - iOS-specific implementations
- `composeApp/src/androidMain/kotlin/` - Android app code (DI modules)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add SQLDelight dependencies and configure build

- [x] T001 Add SQLDelight version and dependencies to gradle/libs.versions.toml
- [x] T002 Add SQLDelight plugin and dependencies to shared/build.gradle.kts
- [x] T003 Configure SQLDelight database in shared/build.gradle.kts (packageName: org.epoque.tandem.data.local, database: TandemDatabase)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain models, enums, type adapters, and SQLDelight schema that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 [P] Create OwnerType enum in shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/OwnerType.kt
- [x] T005 [P] Create TaskStatus enum in shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/TaskStatus.kt
- [x] T006 [P] Create Task data class in shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Task.kt
- [x] T007 [P] Create Week data class in shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Week.kt
- [x] T008 [P] Create InstantAdapter for kotlinx.datetime.Instant in shared/src/commonMain/kotlin/org/epoque/tandem/data/local/adapter/InstantAdapter.kt
- [x] T009 [P] Create LocalDateAdapter for kotlinx.datetime.LocalDate in shared/src/commonMain/kotlin/org/epoque/tandem/data/local/adapter/LocalDateAdapter.kt
- [x] T010 Create Task.sq with table schema and queries in shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/Task.sq
- [x] T011 Create Week.sq with table schema and queries in shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/Week.sq
- [x] T012 Create DatabaseFactory.kt with createDatabase() function in shared/src/commonMain/kotlin/org/epoque/tandem/data/local/DatabaseFactory.kt
- [x] T013 Create DatabaseDriverFactory interface in shared/src/commonMain/kotlin/org/epoque/tandem/data/local/DatabaseDriverFactory.kt
- [x] T014 [P] Create AndroidDatabaseDriverFactory in shared/src/androidMain/kotlin/org/epoque/tandem/data/local/DatabaseDriverFactory.kt
- [x] T015 [P] Create IOSDatabaseDriverFactory in shared/src/iosMain/kotlin/org/epoque/tandem/data/local/DatabaseDriverFactory.kt
- [x] T016 Run ./gradlew :shared:generateCommonMainTandemDatabaseInterface to verify schema compiles

**Checkpoint**: Foundation ready - SQLDelight generates code, domain models defined

---

## Phase 3: User Story 1+2 - Task Persistence & CRUD (Priority: P1) 🎯 MVP

**Goal**: Tasks persist across app restarts and users can create, read, update, delete tasks

**Independent Test**: Create a task, close the app, reopen it, verify the task is still there. Perform all CRUD operations and verify they work correctly.

### Tests for User Story 1+2

- [x] T017 [P] [US1] Create TaskRepositoryTest in shared/src/commonTest/kotlin/org/epoque/tandem/data/repository/TaskRepositoryTest.kt
- [x] T018 [P] [US2] Create CreateTaskUseCaseTest in shared/src/commonTest/kotlin/org/epoque/tandem/domain/usecase/task/CreateTaskUseCaseTest.kt

### Implementation for User Story 1+2

- [x] T019 [US1] Create TaskRepository interface in shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/TaskRepository.kt
- [x] T020 [US1] Create TaskRepositoryImpl with observeAllTasks, getTaskById, createTask, updateTask, updateTaskStatus, deleteTask in shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/TaskRepositoryImpl.kt
- [x] T021 [US2] Implement Task-to-domain mapper extension in TaskRepositoryImpl (convert SQLDelight generated Task to domain Task)
- [x] T022 [US2] Implement validation in TaskRepositoryImpl: empty title check, week ID format check
- [x] T023 [P] [US2] Create CreateTaskUseCase in shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/task/CreateTaskUseCase.kt
- [x] T024 [P] [US2] Create UpdateTaskStatusUseCase in shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/task/UpdateTaskStatusUseCase.kt
- [x] T025 [P] [US2] Create DeleteTaskUseCase in shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/task/DeleteTaskUseCase.kt
- [x] T026 [US2] Add incrementRepeatCount method to TaskRepositoryImpl for repeating tasks
- [x] T027 [US1] Run tests: ./gradlew :shared:testDebugUnitTest --tests "*TaskRepository*"

**Checkpoint**: Tasks persist locally, all CRUD operations work - MVP complete

---

## Phase 4: User Story 3 - Week-Based Task Filtering (Priority: P2)

**Goal**: Users can view tasks filtered by week and manage week entities

**Independent Test**: Create tasks for different weeks, filter by week ID, verify only matching tasks are returned

### Tests for User Story 3

- [x] T028 [P] [US3] Create WeekRepositoryTest in shared/src/commonTest/kotlin/org/epoque/tandem/data/repository/WeekRepositoryTest.kt
- [x] T029 [P] [US3] Create GetCurrentWeekUseCaseTest in shared/src/commonTest/kotlin/org/epoque/tandem/domain/usecase/week/GetCurrentWeekUseCaseTest.kt

### Implementation for User Story 3

- [x] T030 [US3] Create WeekRepository interface in shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/WeekRepository.kt
- [x] T031 [US3] Create WeekRepositoryImpl with observeWeek, getWeekById, getOrCreateCurrentWeek, getCurrentWeekId, saveWeek, observePastWeeks in shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/WeekRepositoryImpl.kt
- [x] T032 [US3] Implement ISO 8601 week ID calculation in WeekRepositoryImpl.getCurrentWeekId()
- [x] T033 [US3] Implement week date boundary calculation (Monday start, Sunday end) in WeekRepositoryImpl
- [x] T034 [US3] Add observeTasksForWeek to TaskRepository interface and implementation
- [x] T035 [P] [US3] Create GetCurrentWeekUseCase in shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/week/GetCurrentWeekUseCase.kt
- [x] T036 [P] [US3] Create GetTasksForWeekUseCase in shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/task/GetTasksForWeekUseCase.kt
- [x] T037 [US3] Run tests: ./gradlew :shared:testDebugUnitTest --tests "*WeekRepository*"

**Checkpoint**: Week filtering works, current week auto-creates

---

## Phase 5: User Story 4 - Task Owner Filtering (Priority: P2)

**Goal**: Users can view tasks filtered by owner type (SELF, PARTNER, SHARED)

**Independent Test**: Create tasks with different owner types, filter by each type, verify correct filtering

### Tests for User Story 4

- [x] T038 [P] [US4] Add owner type filtering tests to TaskRepositoryTest

### Implementation for User Story 4

- [x] T039 [US4] Add observeTasksByOwnerType to TaskRepository interface
- [x] T040 [US4] Implement observeTasksByOwnerType in TaskRepositoryImpl
- [x] T041 [US4] Add observeTasksByWeekAndOwnerType for combined filtering to TaskRepository interface
- [x] T042 [US4] Implement observeTasksByWeekAndOwnerType in TaskRepositoryImpl
- [x] T043 [US4] Run tests: ./gradlew :shared:testDebugUnitTest --tests "*TaskRepository*ownerType*"

**Checkpoint**: Owner type filtering works for all three types

---

## Phase 6: User Story 5 - Week Review Management (Priority: P3)

**Goal**: Users can complete week reviews with rating and notes

**Independent Test**: Complete a week review with rating 1-5 and notes, verify data is saved with timestamp

### Tests for User Story 5

- [x] T044 [P] [US5] Create SaveWeekReviewUseCaseTest in shared/src/commonTest/kotlin/org/epoque/tandem/domain/usecase/week/SaveWeekReviewUseCaseTest.kt

### Implementation for User Story 5

- [x] T045 [US5] Add updateWeekReview and markPlanningCompleted to WeekRepository interface
- [x] T046 [US5] Implement updateWeekReview in WeekRepositoryImpl with rating validation (1-5)
- [x] T047 [US5] Implement markPlanningCompleted in WeekRepositoryImpl
- [x] T048 [P] [US5] Create SaveWeekReviewUseCase in shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/week/SaveWeekReviewUseCase.kt
- [x] T049 [US5] Add updateTaskReviewNote to TaskRepository for task-level review notes
- [x] T050 [US5] Run tests: ./gradlew :shared:testDebugUnitTest --tests "*WeekReview*"

**Checkpoint**: Week reviews can be saved and retrieved

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: DI integration, build validation, final cleanup

- [x] T051 Create TaskModule Koin DI module in composeApp/src/androidMain/kotlin/org/epoque/tandem/di/TaskModule.kt
- [x] T052 Register TaskModule in main Koin application modules
- [x] T053 Run full test suite: ./gradlew :shared:testDebugUnitTest
- [x] T054 Run build validation: ./gradlew :composeApp:compileDebugKotlinAndroid
- [x] T055 Verify all repository methods have KDoc documentation
- [x] T056 Run quickstart.md validation checklist

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **US1+US2 (Phase 3)**: Depends on Foundational - MVP milestone
- **US3 (Phase 4)**: Depends on Foundational only - can run parallel with US1+US2
- **US4 (Phase 5)**: Depends on US1+US2 (needs TaskRepository base)
- **US5 (Phase 6)**: Depends on US3 (needs WeekRepository base)
- **Polish (Phase 7)**: Depends on all user stories complete

### User Story Dependencies

```
Setup → Foundational → US1+US2 (MVP)
                    ↘         ↘
                      US3      US4
                        ↘
                          US5
                            ↘
                              Polish
```

- **US1+US2**: Foundation only - No dependencies on other stories
- **US3**: Foundation only - Week operations are independent of task CRUD
- **US4**: US1+US2 - Extends TaskRepository with owner filtering
- **US5**: US3 - Extends WeekRepository with review features

### Parallel Opportunities

```bash
# Phase 2 - Launch all domain models in parallel:
Task: "Create OwnerType enum" [T004]
Task: "Create TaskStatus enum" [T005]
Task: "Create Task data class" [T006]
Task: "Create Week data class" [T007]
Task: "Create InstantAdapter" [T008]
Task: "Create LocalDateAdapter" [T009]

# Phase 2 - Launch platform drivers in parallel:
Task: "Create AndroidDatabaseDriverFactory" [T014]
Task: "Create IOSDatabaseDriverFactory" [T015]

# Phase 3 - Launch tests and use cases in parallel:
Task: "Create TaskRepositoryTest" [T017]
Task: "Create CreateTaskUseCaseTest" [T018]

# Phase 3 - Launch independent use cases in parallel:
Task: "Create CreateTaskUseCase" [T023]
Task: "Create UpdateTaskStatusUseCase" [T024]
Task: "Create DeleteTaskUseCase" [T025]

# Phase 4 & Phase 3 can run in parallel after Foundational completes
```

---

## Implementation Strategy

### MVP First (User Stories 1+2 Only)

1. Complete Phase 1: Setup (T001-T003)
2. Complete Phase 2: Foundational (T004-T016)
3. Complete Phase 3: US1+US2 - Task Persistence & CRUD (T017-T027)
4. **STOP and VALIDATE**: Run tests, verify tasks persist across restarts
5. Deploy/demo if ready - **This is MVP!**

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1+US2 → Test → **MVP Complete**
3. Add US3 → Week filtering works
4. Add US4 → Owner type filtering works
5. Add US5 → Week reviews work
6. Polish → Production ready

### Task Count Summary

| Phase | Story | Task Count |
|-------|-------|------------|
| Phase 1 | Setup | 3 |
| Phase 2 | Foundational | 13 |
| Phase 3 | US1+US2 (P1) | 11 |
| Phase 4 | US3 (P2) | 10 |
| Phase 5 | US4 (P2) | 6 |
| Phase 6 | US5 (P3) | 7 |
| Phase 7 | Polish | 6 |
| **Total** | | **56** |

---

## Notes

- [P] tasks = different files, no dependencies - can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Run `./gradlew :shared:testDebugUnitTest` frequently to catch regressions
