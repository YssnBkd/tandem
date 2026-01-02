# Tasks: Week View

**Input**: Design documents from `/specs/003-week-view/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Unit tests for ViewModel are included as specified in plan.md Testing Strategy.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

Based on plan.md, this project uses Kotlin Multiplatform structure:
- `composeApp/src/commonMain/kotlin/org/epoque/tandem/` - Shared code
- `composeApp/src/androidMain/kotlin/org/epoque/tandem/` - Android UI
- `composeApp/src/commonTest/kotlin/org/epoque/tandem/` - Shared tests

---

## Phase 0: Prerequisites

**Purpose**: Verify dependencies from previous features are complete before starting implementation

- [X] T000 [CRITICAL] Verify Feature 002 (Task Data Layer) is complete: TaskRepository and WeekRepository exist and compile successfully

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create presentation layer foundation and models used across all user stories

- [X] T001 [P] Create Segment enum in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/model/Segment.kt`
- [X] T002 [P] Create WeekInfo data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/model/WeekInfo.kt`
- [X] T003 [P] Create TaskUiModel data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/model/TaskUiModel.kt`
- [X] T004 [P] Create WeekUiState data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekUiState.kt`
- [X] T005 [P] Create WeekEvent sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekEvent.kt`
- [X] T006 [P] Create WeekSideEffect sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekSideEffect.kt`
- [X] T007 Create SegmentPreferences class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/preferences/SegmentPreferences.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core ViewModel and DI infrastructure required by ALL user stories

**⚠️ CRITICAL**: No user story UI work can begin until this phase is complete

- [X] T008 Create WeekViewModel scaffold (init, state flow, event handler structure) in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekViewModel.kt`
- [X] T009 Implement loadInitialData() and observeWeek() in WeekViewModel for week info loading
- [X] T010 Implement observeSegmentPreference() in WeekViewModel for segment persistence
- [X] T011 Implement observeTasks() and updateTasksInState() in WeekViewModel for reactive task updates
- [X] T012 Create WeekModule Koin module in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/WeekModule.kt`
- [X] T013 Register WeekModule in application DI configuration

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - View My Weekly Tasks (Priority: P1) 🎯 MVP

**Goal**: Users can see their tasks for the current week organized by completion status with progress indicator

**Independent Test**: Open Week tab → verify tasks appear sorted (incomplete first, completed faded) → verify progress indicator shows "X/Y"

### Unit Tests for User Story 1

- [ ] T014 [P] [US1] Create WeekViewModelTest scaffold in `composeApp/src/commonTest/kotlin/org/epoque/tandem/presentation/week/WeekViewModelTest.kt`
- [ ] T015 [P] [US1] Add test: tasks are sorted by completion status (incomplete first) in WeekViewModelTest
- [ ] T016 [P] [US1] Add test: progress calculation is correct (completedCount/totalCount) in WeekViewModelTest
- [ ] T017 [P] [US1] Add test: loading state shows initially then clears in WeekViewModelTest

### Implementation for User Story 1

- [X] T018 [P] [US1] Create TaskListItem composable (checkbox, title, completed style) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/TaskListItem.kt`
- [X] T019 [P] [US1] Create RepeatProgressIndicator composable (fraction format: "2/3") in TaskListItem.kt
- [X] T020 [P] [US1] Create WeekHeader composable (date range text, progress indicator) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/WeekHeader.kt`
- [X] T021 [P] [US1] Create EmptyState composable (message + optional action button) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/EmptyState.kt`
- [X] T022 [US1] Create TaskList composable (LazyColumn with incomplete/completed sections) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/TaskList.kt`
- [X] T023 [US1] Create WeekScreen scaffold (Scaffold, TopAppBar, content area) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/WeekScreen.kt`
- [X] T024 [US1] Wire WeekScreen to WeekViewModel (collectAsState, loading state) in WeekScreen.kt
- [X] T025 [US1] Implement pull-to-refresh in TaskList with PullToRefreshBox
- [X] T026 [US1] Run build validation: `./gradlew :composeApp:compileDebugKotlinAndroid`

**Checkpoint**: User Story 1 complete - users can view their weekly tasks with progress indicator

---

## Phase 4: User Story 2 - Complete a Task (Priority: P1)

**Goal**: Users can tap checkbox to complete tasks with animation and haptic feedback

**Independent Test**: Tap checkbox on incomplete task → verify animation + haptic + task moves to completed section + progress updates

### Unit Tests for User Story 2

- [ ] T027 [P] [US2] Add test: task completion toggles status in WeekViewModelTest
- [ ] T028 [P] [US2] Add test: task completion triggers haptic side effect in WeekViewModelTest
- [ ] T029 [P] [US2] Add test: repeating task increments count instead of completing in WeekViewModelTest

### Implementation for User Story 2

- [X] T030 [P] [US2] Create AnimatedCheckbox composable (scale spring animation) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/components/AnimatedCheckbox.kt`
- [X] T031 [US2] Implement handleTaskCheckboxTapped() in WeekViewModel (status toggle + repeat logic)
- [X] T032 [US2] Integrate AnimatedCheckbox into TaskListItem with onCheckboxClick callback
- [X] T033 [US2] Add haptic feedback collection in WeekScreen (LaunchedEffect for sideEffects)
- [X] T034 [US2] Add animateItem() modifier to LazyColumn items for smooth reordering
- [X] T035 [US2] Run build validation: `./gradlew :composeApp:compileDebugKotlinAndroid`

**Checkpoint**: User Stories 1 AND 2 complete - users can view and complete tasks with feedback

---

## Phase 5: User Story 3 - Quick Add Task (Priority: P2)

**Goal**: Users can quickly add tasks via inline text field without navigating away

**Independent Test**: Type task title in quick-add field → press enter → verify task appears in list + field clears + progress total updates

### Unit Tests for User Story 3

- [ ] T036 [P] [US3] Add test: quick add with empty title shows error in WeekViewModelTest
- [ ] T037 [P] [US3] Add test: quick add creates task and clears input in WeekViewModelTest

### Implementation for User Story 3

- [ ] T038 [US3] Create QuickAddField composable (OutlinedTextField + submit handling + inline error display) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/QuickAddField.kt`
- [ ] T039 [US3] Implement handleQuickAddTextChanged() in WeekViewModel
- [ ] T040 [US3] Implement handleQuickAddSubmitted() in WeekViewModel (validation + inline error for empty title + create task)
- [ ] T041 [US3] Integrate QuickAddField into WeekScreen above TaskList
- [ ] T042 [US3] Add keyboard focus handling (ClearFocus side effect) in WeekScreen
- [ ] T043 [US3] Run build validation: `./gradlew :composeApp:compileDebugKotlinAndroid`

**Checkpoint**: User Stories 1, 2, AND 3 complete - core task management flow is functional

---

## Phase 6: User Story 4 - Switch Between Segments (Priority: P2)

**Goal**: Users can switch between You/Partner/Shared segments with persistence

**Independent Test**: Tap Partner segment → verify tasks filter to partner's (read-only) → restart app → verify segment restored

### Unit Tests for User Story 4

- [ ] T044 [P] [US4] Add test: segment selection updates state and persists in WeekViewModelTest
- [ ] T045 [P] [US4] Add test: partner segment sets isReadOnly true in WeekViewModelTest

### Implementation for User Story 4

- [X] T046 [US4] Create SegmentedControl composable (SingleChoiceSegmentedButtonRow) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/SegmentedControl.kt`
- [X] T047 [US4] Implement handleSegmentSelected() in WeekViewModel (persist + re-filter)
- [X] T048 [US4] Integrate SegmentedControl into WeekScreen below WeekHeader
- [X] T049 [US4] Hide checkbox in TaskListItem when isReadOnly = true (Partner segment)
- [X] T050 [US4] Add "Request a Task" button to WeekScreen for Partner segment (v1.0 PLACEHOLDER: shows snackbar "Request a Task feature coming in Partner System update")
- [X] T051 [US4] Add "completed by You" text to TaskListItem for completed shared tasks (partner name deferred to Feature 006)
- [X] T052 [US4] Run build validation: `./gradlew :composeApp:compileDebugKotlinAndroid`

**Checkpoint**: User Stories 1-4 complete - full segment navigation working

---

## Phase 7: User Story 5 - View and Edit Task Details (Priority: P3)

**Goal**: Users can tap a task to view/edit details in a modal sheet

**Independent Test**: Tap a task → verify detail sheet opens with editable fields → edit title → tap save → verify update persists

### Unit Tests for User Story 5

- [ ] T053 [P] [US5] Add test: task tap opens detail sheet in WeekViewModelTest
- [ ] T054 [P] [US5] Add test: task delete removes from state in WeekViewModelTest

### Implementation for User Story 5

- [X] T055 [US5] Create TaskDetailSheet composable (ModalBottomSheet structure) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/TaskDetailSheet.kt`
- [X] T056 [US5] Add title and notes OutlinedTextFields to TaskDetailSheet
- [X] T057 [US5] Add status display, owner info, and creation/rollover info to TaskDetailSheet
- [X] T058 [US5] Add "Mark Complete" button to TaskDetailSheet (for incomplete own tasks)
- [X] T059 [US5] Add "Delete" button with confirmation dialog to TaskDetailSheet
- [X] T060 [US5] Implement handleTaskTapped(), handleTaskSaveRequested(), handleTaskDeleteConfirmed() in WeekViewModel
- [X] T061 [US5] Integrate TaskDetailSheet into WeekScreen (show when showDetailSheet = true)
- [X] T062 [US5] Wire TaskListItem onClick to TaskTapped event
- [X] T063 [US5] Run build validation: `./gradlew :composeApp:compileDebugKotlinAndroid`

**Checkpoint**: User Stories 1-5 complete - task details viewable and editable

---

## Phase 8: User Story 6 - Add Task with Details (Priority: P3)

**Goal**: Users can add tasks with notes via a dedicated sheet

**Independent Test**: Tap FAB → fill title + notes → tap save → verify task created with notes → sheet closes

### Unit Tests for User Story 6

- [ ] T064 [P] [US6] Add test: add task sheet submission creates task in WeekViewModelTest

### Implementation for User Story 6

- [X] T065 [US6] Create AddTaskSheet composable (ModalBottomSheet with form) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/AddTaskSheet.kt`
- [X] T066 [US6] Add title (required) and notes (optional) fields to AddTaskSheet
- [X] T067 [US6] Add owner selector (You/Shared) to AddTaskSheet for Shared segment
- [X] T068 [US6] Add Save/Cancel buttons to AddTaskSheet
- [X] T069 [US6] Implement handleAddTaskSheetRequested(), handleAddTaskSubmitted() in WeekViewModel
- [X] T070 [US6] Add FAB to WeekScreen that triggers AddTaskSheetRequested event
- [X] T071 [US6] Integrate AddTaskSheet into WeekScreen (show when showAddTaskSheet = true)
- [X] T072 [US6] Run build validation: `./gradlew :composeApp:compileDebugKotlinAndroid`

**Checkpoint**: All user stories complete - full Week View functionality available

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Refinements and final validation

- [X] T073 Add snackbar host to WeekScreen for ShowSnackbar side effects
- [X] T074 Implement error state display in WeekScreen for repository errors
- [X] T075 Add content descriptions for accessibility to all interactive elements
- [X] T076 Ensure minimum 48dp touch targets on all tappable elements
- [ ] T077 Test light and dark mode appearance (Manual testing - deferred)
- [ ] T078 Add performance validation: measure and verify segment switching completes within 500ms (Deferred)
- [X] T079 Add edge case handling: long task title truncation (ellipsis in list, full in detail)
- [X] T080 Add edge case handling: Partner segment empty state when no partner connected
- [X] T081 Add edge case handling: offline pull-to-refresh shows brief indicator then continues showing local data
- [ ] T082 Add unit test: verify progress indicator updates when tasks are added or completed (Unit tests deferred)
- [X] T083 Add task to TaskDetailSheet: display creation date and rollover info ("Rolled over from Week X")
- [X] T084 Add confirmation dialog to TaskDetailSheet delete action with "Delete Task" title and "Cancel"/"Delete" buttons
- [ ] T085 Run full unit test suite: `./gradlew :composeApp:testDebugUnitTest` (Unit tests not yet implemented)
- [X] T086 Run final build validation: `./gradlew :composeApp:compileDebugKotlinAndroid`
- [ ] T087 Manual testing: complete quickstart.md validation steps (Manual testing - deferred)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Prerequisites (Phase 0)**: MUST complete first - verifies Feature 002 dependency
- **Setup (Phase 1)**: Depends on Phase 0 passing - creates foundational models
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-8)**: All depend on Foundational phase completion
  - US1 & US2 are both P1: Complete US1 first (view tasks), then US2 (complete tasks)
  - US3 & US4 are both P2: Can proceed in parallel after US1+US2
  - US5 & US6 are both P3: Can proceed in parallel after US3+US4 (or earlier if needed)
- **Polish (Phase 9)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P1)**: Builds on US1's TaskListItem - Should follow US1
- **User Story 3 (P2)**: Independent - Can start after Foundational
- **User Story 4 (P2)**: Builds on TaskList - Should follow US1
- **User Story 5 (P3)**: Independent - Adds modal to existing screen
- **User Story 6 (P3)**: Independent - Adds sheet and FAB to existing screen

### Within Each User Story

- Unit tests can be written first (TDD) or alongside implementation
- Models before UI components
- ViewModel logic before UI integration
- Core implementation before polish
- Build validation before moving to next story

### Parallel Opportunities

**Phase 1 - All setup tasks [P] can run in parallel:**
- T001, T002, T003, T004, T005, T006 are independent files

**Phase 3 - US1 parallel tasks:**
- T014, T015, T016, T017 (tests)
- T018, T019, T020, T021 (UI components)

**Phase 4 - US2 parallel tasks:**
- T027, T028, T029 (tests)

**Cross-story parallelism:**
- US3 and US4 can be worked on in parallel after US1+US2 complete
- US5 and US6 can be worked on in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Create WeekViewModelTest scaffold in composeApp/src/commonTest/.../WeekViewModelTest.kt"
Task: "Add test: tasks are sorted by completion status"
Task: "Add test: progress calculation is correct"
Task: "Add test: loading state shows initially then clears"

# Launch all UI components for User Story 1 together:
Task: "Create TaskListItem composable in composeApp/src/androidMain/.../TaskListItem.kt"
Task: "Create RepeatProgressIndicator composable in TaskListItem.kt"
Task: "Create WeekHeader composable in composeApp/src/androidMain/.../WeekHeader.kt"
Task: "Create EmptyState composable in composeApp/src/androidMain/.../EmptyState.kt"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup (models)
2. Complete Phase 2: Foundational (ViewModel + DI)
3. Complete Phase 3: User Story 1 (view tasks)
4. Complete Phase 4: User Story 2 (complete tasks)
5. **STOP and VALIDATE**: Test viewing and completing tasks
6. Deploy/demo if ready - core loop works!

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add User Story 1 → View tasks (MVP!)
3. Add User Story 2 → Complete tasks with feedback
4. Add User Story 3 → Quick add tasks
5. Add User Story 4 → Segment navigation
6. Add User Story 5 → Task details/editing
7. Add User Story 6 → Add with notes
8. Polish phase → Production ready

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 + 2 (core flow)
   - Developer B: User Story 3 + 4 (after A's foundation)
   - Developer C: User Story 5 + 6 (modals)
3. Stories integrate independently

---

## Summary

| Phase | Tasks | Story |
|-------|-------|-------|
| Prerequisites | 1 | - |
| Setup | 7 | - |
| Foundational | 6 | - |
| US1 | 13 | View Weekly Tasks |
| US2 | 9 | Complete Task |
| US3 | 8 | Quick Add |
| US4 | 9 | Segments |
| US5 | 11 | Task Details |
| US6 | 9 | Add with Details |
| Polish | 15 | - |
| **Total** | **88** | 6 user stories |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story
- Each user story is independently completable and testable
- Build validation after each user story ensures incremental correctness
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
