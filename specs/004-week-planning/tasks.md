# Tasks: Week Planning (Feature 004)

**Input**: Design documents from `/specs/004-week-planning/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/planning-operations.md ✅

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1-US5)
- Include exact file paths in descriptions

## User Stories Summary (from spec.md)

| ID | Story | Priority | Core Deliverable |
|----|-------|----------|------------------|
| US1 | Complete Weekly Planning Flow | P1 (MVP) | Full wizard flow from banner to confirmation |
| US2 | Roll Over Incomplete Tasks | P2 | Review and carry forward tasks from previous week |
| US3 | Add New Tasks During Planning | P2 | Create new tasks in Step 2 |
| US4 | Review Partner Task Requests | P3 | Accept/discuss partner requests in Step 3 |
| US5 | View Planning Summary | P3 | Confirmation screen with task count |

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project structure and verification

- [ ] T001 Verify Feature 002 (Task Data Layer) is complete - repositories exist at `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/`
- [ ] T002 Verify Feature 003 (Week View) is complete - WeekViewModel pattern exists at `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/`
- [ ] T003 Run build verification: `./gradlew :composeApp:compileDebugKotlinAndroid`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Repository extensions and DataStore that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Directory Structure

- [ ] T004 Create planning feature directory structure:
  - `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/`
  - `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/preferences/`
  - `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/`
  - `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/`

### Data Layer Extensions

- [ ] T005 Add SQL queries to `shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/Task.sq`:
  - `getIncompleteTasksByWeekId` (weekId, ownerId, status='PENDING')
  - `getTasksByStatusAndOwnerId` (status, ownerId)
  - See [data-model.md#new-queries-sqldelight]
- [ ] T006 [P] Add `observeIncompleteTasksForWeek(weekId, userId)` to interface in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/TaskRepository.kt`
- [ ] T007 [P] Add `observeTasksByStatus(status, userId)` to interface in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/TaskRepository.kt`
- [ ] T008 Implement `observeIncompleteTasksForWeek()` in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/TaskRepositoryImpl.kt` (depends on T005, T006) - see [contracts/planning-operations.md#observeincompletetasksforweek]
- [ ] T009 Implement `observeTasksByStatus()` in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/TaskRepositoryImpl.kt` (depends on T005, T007) - see [contracts/planning-operations.md#observetasksbystatus]
- [ ] T010 Add `getPreviousWeekId(currentWeekId)` to interface in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/WeekRepository.kt`
- [ ] T011 Implement `getPreviousWeekId()` with year boundary handling (W01 → previous year W52/W53) in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/WeekRepositoryImpl.kt` (depends on T010) - see [research.md#2-iso-8601-week-id-calculation---previous-week], reference existing `calculateWeekBoundaries()` at lines 110-134

### Presentation Layer State Models

- [ ] T012 [P] Create `PlanningStep` enum in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningStep.kt` - see [data-model.md#planningstep-enum]
- [ ] T013 [P] Create `PlanningUiState` data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningUiState.kt` - see [data-model.md#planninguistate]. Note: Import `TaskUiModel` from `org.epoque.tandem.presentation.week.model.TaskUiModel`
- [ ] T014 [P] Create `PlanningEvent` sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningEvent.kt` - see [data-model.md#planningevent-sealed-class]
- [ ] T015 [P] Create `PlanningSideEffect` sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningSideEffect.kt` - see [data-model.md#planningsideeffect]

### DataStore Progress Persistence

- [ ] T016 Create `PlanningProgress.kt` in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/preferences/PlanningProgress.kt` with:
  - `PlanningProgressState` data class - see [data-model.md#planningprogressstate]
  - `PlanningProgress` class with `planningProgress: Flow`, `saveProgress()`, `clearProgress()`
  - Stale week handling (discard if weekId != current)
  - See [contracts/planning-operations.md#datastore-operations], pattern in `SegmentPreferences.kt`

### ViewModel Core Structure

- [ ] T017 Create `PlanningViewModel` skeleton in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningViewModel.kt` with:
  - Constructor dependencies: `taskRepository: TaskRepository`, `weekRepository: WeekRepository`, `authRepository: AuthRepository`, `planningProgress: PlanningProgress`
  - `private val _uiState = MutableStateFlow(PlanningUiState())`
  - `val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()`
  - `private val _sideEffects = Channel<PlanningSideEffect>(Channel.BUFFERED)`
  - `val sideEffects: Flow<PlanningSideEffect> = _sideEffects.receiveAsFlow()`
  - `private val currentUserId: String? get() = authRepository.currentUser?.id`
  - See WeekViewModel.kt:43-54, 476-477 for pattern
- [ ] T018 Implement ViewModel init sequence in `PlanningViewModel.kt` with **CRITICAL ORDER**:
  1. Wait for `AuthState.Authenticated` using `filterIsInstance<AuthState.Authenticated>().first()`
  2. Extract `userId` from `authState.user.id` and **store as ViewModel property** for event handlers
  3. Call `getOrCreateCurrentWeek(userId)` and **store `currentWeek` as ViewModel property**
  4. Check saved progress via `planningProgress.planningProgress.first()`, clear if `weekId != currentWeek.id`
  5. Calculate `previousWeekId` via `getPreviousWeekId(currentWeek.id)`
  6. Query rollover tasks via `observeIncompleteTasksForWeek(previousWeekId, userId).first()`
  7. Query partner requests via `observeTasksByStatus(PENDING_ACCEPTANCE, userId).first()`
  8. Initialize UI state with all collected data
  - **Required imports**: `filterIsInstance`, `first` from `kotlinx.coroutines.flow`
  - See [contracts/planning-operations.md#init]

### Navigation Setup

- [ ] T019 Add `Routes.Planning` sealed interface to `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/Routes.kt`:
  - `@Serializable data object Start : Planning`
  - `@Serializable data class Wizard(val stepIndex: Int = 0) : Planning`
  - See [research.md#1-jetpack-navigation-compose---nested-wizard-flow]
- [ ] T020 Create `PlanningNavGraph.kt` in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/PlanningNavGraph.kt` with nested NavHost for step screens (depends on T019) - see pattern in `AuthNavGraph.kt`
- [ ] T021 Wire up planning navigation in `MainNavGraph.kt` at `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/MainNavGraph.kt` (depends on T020)

### Dependency Injection

- [ ] T022 Create `PlanningModule.kt` in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/PlanningModule.kt` with:
  - DataStore for planning preferences (`preferencesDataStore(name = "planning_preferences")`)
  - `single { PlanningProgress(get(named("planning"))) }`
  - `viewModel { PlanningViewModel(...) }`
  - See [quickstart.md#task-9], pattern in `WeekModule.kt`
- [ ] T023 Register `planningModule` in `TandemApp.kt` at `composeApp/src/androidMain/kotlin/org/epoque/tandem/TandemApp.kt` - add to `modules()` list

**CHECKPOINT**: Run `./gradlew :composeApp:compileDebugKotlinAndroid` - build MUST pass before proceeding

---

## Phase 3: User Story 1 - Complete Weekly Planning Flow (Priority: P1) 🎯 MVP

**Goal**: Users can complete the full 4-step planning wizard from banner to confirmation

**Independent Test**: Start planning from banner → navigate through all steps → complete planning → banner dismissed

### ViewModel Event Handlers for US1

- [ ] T024 [US1] Implement `onEvent()` dispatcher in `PlanningViewModel.kt` - route all `PlanningEvent` types to handlers
- [ ] T025 [US1] Implement `handleBackPressed()` with `NavigateBack` side effect in `PlanningViewModel.kt`
- [ ] T026 [US1] Implement `handleExitRequested()` with progress save via `planningProgress.saveProgress()` in `PlanningViewModel.kt`
- [ ] T027 [US1] Implement `handlePlanningCompleted()` in `PlanningViewModel.kt`:
  - Call `weekRepository.markPlanningCompleted(weekId)`
  - Call `planningProgress.clearProgress()`
  - Calculate summary totals
  - Send `ExitPlanning` side effect
  - See [contracts/planning-operations.md#handleplanningcompleted]
- [ ] T028 [US1] Implement step skipping logic in `PlanningViewModel.kt`:
  - `getInitialStep()` - skip ROLLOVER if no rollover tasks
  - `getNextStep()` - skip PARTNER_REQUESTS if no partner requests
  - See [contracts/planning-operations.md#step-skipping-logic]

### UI Components for US1

- [ ] T029 [P] [US1] Create `ProgressDots.kt` composable (step indicator 1-4) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/ProgressDots.kt`
- [ ] T030 [US1] Create `PlanningScreen.kt` main container in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/PlanningScreen.kt` with:
  - NavHost routing to: `RolloverStepScreen` (step 0), `AddTasksStepScreen` (step 1), `PartnerRequestsStepScreen` (step 2), `ConfirmationStepScreen` (step 3)
  - **CRITICAL: Single LaunchedEffect** collecting `viewModel.sideEffects` (see WeekScreen.kt:55-79 for pattern)
  - Handle all `PlanningSideEffect` types: `ShowSnackbar`, `NavigateToStep`, `NavigateBack`, `ExitPlanning`, `TriggerHapticFeedback`, `ClearFocus`
  - See [contracts/planning-operations.md], [data-model.md#planningsideeffect]
- [ ] T031 [US1] Create `ConfirmationStepScreen.kt` in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/ConfirmationStepScreen.kt` with:
  - Checkmark success icon
  - "X tasks planned" summary text
  - "Done" button emitting `PlanningCompleted` event
    - **48dp minimum touch target** (FR-023)
    - **Content description**: "Complete planning and return to week view" (FR-024)

### Banner Integration for US1

- [ ] T032 [US1] Create `PlanningBanner.kt` composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/PlanningBanner.kt` with:
  - "Start" button that navigates to `Routes.Planning.Wizard(0)`
  - Private helper function `shouldShowPlanningBanner(week: Week): Boolean`:
    - Return false if `week.planningCompletedAt != null`
    - Check if Sunday >= 6pm using `Clock.System.now()` and `kotlinx.datetime`
    - See [quickstart.md#task-8] for time calculation logic
- [ ] T033 [US1] Integrate `PlanningBanner` into `WeekScreen.kt` at `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/WeekScreen.kt`:
  - Add banner above content when `shouldShowPlanningBanner()` returns true
  - Pass navigation callback to banner

**CHECKPOINT**: E2E test - Start planning → reach confirmation → complete → banner dismissed

---

## Phase 4: User Story 2 - Roll Over Incomplete Tasks (Priority: P2)

**Goal**: Users can review incomplete tasks from last week and add or skip them

**Independent Test**: Have incomplete tasks from previous week → start planning → see each as full-screen card → add/skip → tasks appear in current week

### ViewModel Event Handlers for US2

- [ ] T034 [US2] Implement `handleRolloverTaskAdded(taskId)` in `PlanningViewModel.kt`:
  - Get `userId` from stored ViewModel property (set in T018 init)
  - Get `currentWeekId` via `weekRepository.getCurrentWeekId()`
  - Get original task via `taskRepository.getTaskById(taskId)`
  - Create new task with `rolledFromWeekId = originalTask.weekId`, `weekId = currentWeekId`, `ownerId = userId` (see [data-model.md#rollover-task-creation-logic])
  - Call `taskRepository.createTask(newTask)`
  - Update state: add to `addedTasks` via `TaskUiModel.fromTask()`, increment `currentRolloverIndex`, `rolloverTasksAdded`
  - **Call `saveProgress()`** after state update
  - Send `TriggerHapticFeedback` side effect
  - Import `TaskUiModel` from `org.epoque.tandem.presentation.week.model`
  - See [contracts/planning-operations.md#handlerollovertaskadded]
- [ ] T035 [US2] Implement `handleRolloverTaskSkipped(taskId)` in `PlanningViewModel.kt`:
  - Update state: increment `currentRolloverIndex`, `processedRolloverCount`
  - **Call `saveProgress()`** after state update
  - No database changes
  - See [contracts/planning-operations.md#handlerollovertaskskipped]
- [ ] T036 [US2] Implement `handleRolloverStepComplete()` in `PlanningViewModel.kt`:
  - Call `getNextStep(ROLLOVER)` to determine next step
  - Send `NavigateToStep` side effect

### UI Components for US2

- [ ] T037 [P] [US2] Create `PlanningCard.kt` composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/PlanningCard.kt` with:
  - Full-screen card using `Surface` with `RoundedCornerShape(16.dp)`
  - Optional swipe gestures (right = add, left = skip, threshold 100dp)
  - **Visible action buttons** with 48dp touch targets (FR-023)
  - Content descriptions for accessibility (FR-024)
  - See [research.md#4-material-design-3-full-screen-cards]
- [ ] T038 [US2] Create `RolloverStepScreen.kt` in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/RolloverStepScreen.kt` with:
  - `PlanningCard` showing current rollover task (title, notes)
  - "Add to This Week" button → emits `RolloverTaskAdded(taskId)`
    - **48dp minimum touch target** (FR-023)
    - **Content description**: "Add task to this week" (FR-024)
  - "Skip" button → emits `RolloverTaskSkipped(taskId)`
    - **48dp minimum touch target** (FR-023)
    - **Content description**: "Skip this task" (FR-024)
  - `ProgressDots` showing `currentRolloverIndex / rolloverTasks.size`
  - Auto-emit `RolloverStepComplete` when all tasks processed

**CHECKPOINT**: E2E test - Start planning with incomplete tasks → Add one → Skip one → tasks correctly rolled over

---

## Phase 5: User Story 3 - Add New Tasks During Planning (Priority: P2)

**Goal**: Users can add new tasks during Step 2 with a visible submit button

**Independent Test**: Enter Step 2 → type task title → tap Add button → task appears in list → tap Done

### ViewModel Event Handlers for US3

- [ ] T039 [US3] Implement `handleNewTaskTextChanged(text)` in `PlanningViewModel.kt`:
  - Update `_uiState` with new `newTaskText`, clear `newTaskError`
- [ ] T040 [US3] Implement `handleNewTaskSubmitted()` in `PlanningViewModel.kt`:
  - Get `userId` from stored ViewModel property (set in T018 init)
  - Get `currentWeekId` via `weekRepository.getCurrentWeekId()`
  - Validate title not empty (set `newTaskError` if empty, return early)
  - Create task with `ownerId = userId`, `weekId = currentWeekId`, `OwnerType.SELF`, `status = PENDING`, `rolledFromWeekId = null`
  - Call `taskRepository.createTask(task)`
  - Update state: clear `newTaskText`, add to `addedTasks` via `TaskUiModel.fromTask()`, increment `newTasksCreated`
  - **Call `saveProgress()`** after state update
  - Send `ClearFocus` side effect
  - Handle `CancellationException` (re-throw), other exceptions (show snackbar)
  - See [contracts/planning-operations.md#handlenewtasksubmitted]
- [ ] T041 [US3] Implement `handleDoneAddingTasks()` in `PlanningViewModel.kt`:
  - Call `getNextStep(ADD_TASKS)` to determine next step
  - Send `NavigateToStep` side effect

### UI Components for US3

- [ ] T042 [P] [US3] Create `TaskInputField.kt` composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/TaskInputField.kt` with:
  - `OutlinedTextField` for task title
  - **VISIBLE Add button** as `trailingIcon` using `IconButton` with `Modifier.size(48.dp)` (FR-010, FR-023)
  - **NOT keyboard-only submission** - button must be tappable
  - Inline error display below field when `error != null`
  - Content description: "Add task"
  - See [quickstart.md#task-5], spec FR-010
- [ ] T043 [US3] Create `AddTasksStepScreen.kt` in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/AddTasksStepScreen.kt` with:
  - `TaskInputField` at top
  - `LazyColumn` showing `addedTasks` list (running list of tasks added this session)
  - "Done Adding Tasks" button → emits `DoneAddingTasks` event
    - **48dp minimum touch target** (FR-023)
    - **Content description**: "Finish adding tasks and continue" (FR-024)
  - Pass `newTaskText`, `newTaskError` from state

**CHECKPOINT**: E2E test - Add 3 new tasks → see them in list → tap Done → proceed to next step

---

## Phase 6: User Story 4 - Review Partner Task Requests (Priority: P3)

**Goal**: Users can accept or discuss partner requests in Step 3

**Independent Test**: Have pending partner requests → see each as full-screen card → accept one → status changes to PENDING

### ViewModel Event Handlers for US4

- [ ] T044 [US4] Implement `handlePartnerRequestAccepted(taskId)` in `PlanningViewModel.kt`:
  - Call `taskRepository.updateTaskStatus(taskId, TaskStatus.PENDING)`
  - Update state: increment `currentRequestIndex`, `partnerRequestsAccepted`
  - **Call `saveProgress()`** after state update
  - Send `TriggerHapticFeedback` side effect
  - See [contracts/planning-operations.md#handlepartnerrequestaccepted]
- [ ] T045 [US4] Implement `handlePartnerRequestDiscussed(taskId)` in `PlanningViewModel.kt`:
  - Send `ShowSnackbar("Discuss feature coming soon")` side effect
  - Update state: increment `currentRequestIndex`, `processedRequestCount`
  - **Call `saveProgress()`** after state update
  - See [contracts/planning-operations.md#handlepartnerrequestdiscussed]
- [ ] T046 [US4] Implement `handlePartnerRequestsStepComplete()` in `PlanningViewModel.kt`:
  - Send `NavigateToStep(CONFIRMATION)` side effect

### UI Components for US4

- [ ] T047 [US4] Create `PartnerRequestsStepScreen.kt` in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/PartnerRequestsStepScreen.kt` with:
  - `PlanningCard` showing current partner request (reuse from T037)
  - Display task title and optional note from partner (FR-018)
  - "Accept" button → emits `PartnerRequestAccepted(taskId)`
    - **48dp minimum touch target** (FR-023)
    - **Content description**: "Accept this task request" (FR-024)
  - "Discuss" button → emits `PartnerRequestDiscussed(taskId)`
    - **48dp minimum touch target** (FR-023)
    - **Content description**: "Discuss this request with partner" (FR-024)
  - `ProgressDots` showing `currentRequestIndex / partnerRequests.size`
  - Auto-emit `PartnerRequestsStepComplete` when all requests processed

**CHECKPOINT**: E2E test - Accept partner request → status changes to PENDING in database

---

## Phase 7: User Story 5 - View Planning Summary (Priority: P3)

**Goal**: Users see a summary screen showing what they planned

**Independent Test**: Complete all steps → see confirmation with correct task count breakdown

### Implementation for US5

- [ ] T048 [US5] Enhance `PlanningUiState.kt` with computed summary:
  - Ensure `totalTasksPlanned = rolloverTasksAdded + newTasksCreated + partnerRequestsAccepted`
  - Add any missing fields for breakdown display
- [ ] T049 [US5] Enhance `ConfirmationStepScreen.kt` with detailed breakdown:
  - Show "X rolled over from last week"
  - Show "Y new tasks added"
  - Show "Z partner requests accepted"
  - Total: "X tasks planned for this week"
- [ ] T050 [US5] Add "See Partner's Week" button to `ConfirmationStepScreen.kt`:
  - Placeholder for future feature (FR-022)
  - Show "Coming soon" snackbar on tap
  - **48dp minimum touch target** (FR-023)
  - **Content description**: "View partner's week" (FR-024)

**CHECKPOINT**: E2E test - Complete planning → see accurate summary of tasks by category

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T051 [P] Verify all touch targets are 48dp+ (FR-023) across: `PlanningCard.kt`, `TaskInputField.kt`, `RolloverStepScreen.kt`, `AddTasksStepScreen.kt`, `PartnerRequestsStepScreen.kt`, `ConfirmationStepScreen.kt`
- [ ] T052 [P] Verify all action buttons have content descriptions (FR-024) across all step screens
- [ ] T053 [P] Test empty state handling:
  - No rollover tasks → skip directly to ADD_TASKS step
  - No partner requests → skip directly to CONFIRMATION step
  - Zero tasks total → can still complete planning
- [ ] T054 [P] Test progress persistence:
  - Exit mid-planning → reopen app → resume from same position
  - Switch weeks → stale progress discarded
- [ ] T055 [P] Test year boundary: `getPreviousWeekId("2026-W01")` → returns `"2025-W52"` or `"2025-W53"`
- [ ] T056 Run final build verification: `./gradlew :composeApp:compileDebugKotlinAndroid`
- [ ] T057 Run unit tests: `./gradlew :composeApp:testDebugUnitTest`

### E2E Database Verification Commands

For manual E2E testing, use these ADB commands to verify database state:

```bash
# E2E-2: Verify rollover task created with reference to previous week
adb shell "run-as org.epoque.tandem cat databases/tandem.db" | sqlite3 :memory: \
  "SELECT id, title, rolled_from_week_id FROM Task WHERE rolled_from_week_id IS NOT NULL"

# E2E-3: Verify new tasks created in current week
adb shell "run-as org.epoque.tandem cat databases/tandem.db" | sqlite3 :memory: \
  "SELECT COUNT(*) as task_count FROM Task WHERE week_id = '2026-W01'"

# E2E-4: Verify partner request status changed to PENDING
adb shell "run-as org.epoque.tandem cat databases/tandem.db" | sqlite3 :memory: \
  "SELECT id, status FROM Task WHERE status = 'PENDING'"

# E2E-1/5: Verify planning completion timestamp set
adb shell "run-as org.epoque.tandem cat databases/tandem.db" | sqlite3 :memory: \
  "SELECT id, planning_completed_at FROM Week WHERE planning_completed_at IS NOT NULL"
```

**E2E Test → Required Tasks → Verification**:
| E2E Test | Required Tasks | Verification Method |
|----------|---------------|---------------------|
| Start → Confirm → Banner dismissed | T018-T033 | Visual: Banner not visible |
| Rollover add/skip | T034-T038 | DB: `rolled_from_week_id IS NOT NULL` |
| Add 3 new tasks | T039-T043 | DB: `COUNT(*) WHERE week_id = current` |
| Accept partner request | T044-T047 | DB: `status = 'PENDING'` |
| Summary shows counts | T048-T050 | Visual: UI breakdown matches DB |

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - can start immediately
- **Phase 2 (Foundational)**: Depends on Setup - **BLOCKS all user stories**
- **Phase 3-7 (User Stories)**: All depend on Phase 2 completion
- **Phase 8 (Polish)**: Depends on all user stories being complete

### User Story Dependencies

| Story | Depends On | Shared Components Used |
|-------|------------|----------------------|
| US1 (P1) | Phase 2 only | ProgressDots, PlanningScreen, ConfirmationStepScreen, PlanningBanner |
| US2 (P2) | Phase 2 only | PlanningCard (shared with US4), RolloverStepScreen |
| US3 (P2) | Phase 2 only | TaskInputField, AddTasksStepScreen |
| US4 (P3) | Phase 2, T037 (PlanningCard) | PlanningCard (from US2), PartnerRequestsStepScreen |
| US5 (P3) | US1 (uses ConfirmationStepScreen) | ConfirmationStepScreen enhancements |

### Task Dependencies Within Phase 2

```
T004 (directories) ─┐
                    ├─→ T005 (SQL) ─→ T008, T009 (impl)
T006, T007 (interface) ─┘
T010 (interface) ─→ T011 (impl)
T012-T015 (state models) ─→ T016 (DataStore) ─→ T017-T018 (ViewModel)
T019 (Routes) ─→ T020 (NavGraph) ─→ T021 (MainNavGraph wire-up)
T017-T018, T020 ─→ T022-T023 (DI)
```

### Parallel Opportunities

```text
Phase 2 parallel groups:
  - T006 + T007 (different interface methods)
  - T012 + T013 + T014 + T015 (state models in different files)

Phase 3 parallel:
  - T029 (ProgressDots) can run with T024-T028 (ViewModel handlers)

Phase 4 parallel:
  - T037 (PlanningCard) can run with T034-T036 (ViewModel handlers)

Phase 5 parallel:
  - T042 (TaskInputField) can run with T039-T041 (ViewModel handlers)

Cross-phase parallel (after Phase 2):
  - US1, US2, US3 can proceed in parallel if team capacity allows
  - US4 must wait for T037 (PlanningCard) from US2
```

---

## Parallel Example: Phase 2 State Models

```bash
# Launch all state model tasks in parallel:
Task: "T012 - Create PlanningStep enum"
Task: "T013 - Create PlanningUiState data class"
Task: "T014 - Create PlanningEvent sealed class"
Task: "T015 - Create PlanningSideEffect sealed class"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup verification
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (banner + navigation + confirmation)
4. **STOP and VALIDATE**: Can start planning and reach confirmation
5. User can complete planning even with 0 tasks

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 → Basic flow works → **MVP Ready**
3. Add US2 → Rollover tasks functional
4. Add US3 → New task creation works
5. Add US4 → Partner requests handled
6. Add US5 → Summary enhanced
7. Each story adds value without breaking previous stories

### Single Developer Strategy

Execute in this order:
1. Phase 1 (Setup) - 3 tasks
2. Phase 2 (Foundational) - 20 tasks, execute in dependency order above
3. Phase 3 (US1) - 10 tasks, enables basic flow testing
4. Phase 4 (US2) - 5 tasks
5. Phase 5 (US3) - 5 tasks
6. Phase 6 (US4) - 4 tasks
7. Phase 7 (US5) - 3 tasks
8. Phase 8 (Polish) - 7 tasks

**Total: 57 tasks**

---

## Critical Implementation Notes

**From user input and plan.md - MUST follow**:

1. **ViewModel init MUST wait for AuthState.Authenticated** before calling repositories - see T018
2. **ViewModel init MUST call `getOrCreateCurrentWeek(userId)`** before any week operations - see T018
3. **Side effects Channel MUST have only ONE collector** in the UI - see T030
4. **All submit actions MUST have VISIBLE buttons** (not keyboard-only) - see T042
5. **All event handlers that modify progress MUST call `saveProgress()`** - see T034, T035, T040, T044, T045

---

## Key Imports Reference

| Import | Used In |
|--------|---------|
| `org.epoque.tandem.domain.repository.AuthState` | PlanningViewModel (T018) |
| `org.epoque.tandem.presentation.week.model.TaskUiModel` | PlanningUiState (T013), PlanningViewModel (T034, T040) |
| `kotlinx.datetime.Clock` | PlanningBanner (T032) |
| `kotlinx.coroutines.channels.Channel` | PlanningViewModel (T017) |
| `kotlinx.coroutines.flow.filterIsInstance` | PlanningViewModel (T018) - for auth state filtering |
| `kotlinx.coroutines.flow.first` | PlanningViewModel (T018) - for one-shot collection |
| `kotlinx.coroutines.flow.receiveAsFlow` | PlanningViewModel (T017) - for side effects |
| `org.epoque.tandem.domain.model.TaskStatus` | PlanningViewModel (T044) - for PENDING status |
| `org.epoque.tandem.domain.model.OwnerType` | PlanningViewModel (T034, T040) - for SELF type |

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Explicit dependencies noted with "(depends on TXXX)"
