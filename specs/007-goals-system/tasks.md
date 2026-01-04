# Tasks: Goals System

**Input**: Design documents from `/specs/007-goals-system/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions (Kotlin Multiplatform)

```text
shared/src/commonMain/kotlin/org/epoque/tandem/           # Domain layer (100% shared)
composeApp/src/commonMain/kotlin/org/epoque/tandem/       # Presentation layer (shared ViewModels)
composeApp/src/androidMain/kotlin/org/epoque/tandem/      # UI layer (Android-specific)
shared/src/commonMain/sqldelight/org/epoque/tandem/       # SQLDelight schemas
```

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and SQLDelight schema setup

- [ ] T001 Create Goal.sq SQLDelight schema in `shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/Goal.sq`
- [ ] T002 [P] Create GoalProgress.sq SQLDelight schema in `shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/GoalProgress.sq`
- [ ] T003 Verify `Task.linkedGoalId` exists in `shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/Task.sq` (already present)
- [ ] T004 Run `./gradlew :shared:generateCommonMainTandemDatabaseInterface` to generate SQLDelight code

**Checkpoint**: SQLDelight schemas compile successfully

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain models and repository interface that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Domain Models

- [ ] T005 [P] Create GoalType sealed class in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/GoalType.kt`
- [ ] T006 [P] Create GoalStatus enum in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/GoalStatus.kt`
- [ ] T007 [P] Create Goal data class in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Goal.kt`
- [ ] T008 [P] Create GoalProgress data class in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/GoalProgress.kt`
- [ ] T009 Create WeekCalculator utility in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/util/WeekCalculator.kt`

### Repository Interface

- [ ] T010 Create GoalRepository interface in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/GoalRepository.kt`
- [ ] T011 Create GoalException sealed class in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/GoalException.kt`

### Repository Implementation

- [ ] T012 Create GoalRepositoryImpl in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/GoalRepositoryImpl.kt`
- [ ] T013 Add SQLDelight type adapters for GoalType and GoalStatus in `shared/src/commonMain/kotlin/org/epoque/tandem/data/local/adapter/GoalTypeAdapter.kt`
- [ ] T014 Register Goal tables in TandemDatabaseFactory in `shared/src/commonMain/kotlin/org/epoque/tandem/data/local/TandemDatabaseFactory.kt`

### DI Module

- [ ] T015 Create GoalsModule for Koin in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/GoalsModule.kt`
- [ ] T016 Register GoalsModule in AppModule in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/AppModule.kt`

### Build Verification

- [ ] T017 Run `./gradlew :composeApp:compileDebugKotlinAndroid` to verify foundation compiles

**Checkpoint**: Foundation ready - repository can create, read, update, delete goals. User story implementation can now begin.

---

## Phase 3: User Story 1 - View Personal and Shared Goals (Priority: P1) 🎯 MVP

**Goal**: Users can view their personal and shared goals in a dedicated Goals tab with segment control

**Independent Test**: Navigate to Goals tab → See segment control → Switch between "Yours" and "Shared" → Goals display correctly in each segment

### Presentation Layer for US1

- [ ] T018 [P] [US1] Create GoalSegment enum in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalSegment.kt`
- [ ] T019 [P] [US1] Create GoalsUiState data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsUiState.kt`
- [ ] T020 [P] [US1] Create GoalsEvent sealed interface in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsEvent.kt`
- [ ] T021 [P] [US1] Create GoalsSideEffect sealed interface in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsSideEffect.kt`
- [ ] T022 [US1] Create GoalsViewModel with CRITICAL init sequence (wait for auth, check partner, observe goals) in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsViewModel.kt`

### UI Components for US1

- [ ] T023 [P] [US1] Create GoalProgressBar composable (48dp touch target) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/GoalProgressBar.kt`
- [ ] T024 [P] [US1] Create GoalCard composable (icon, title, progress bar, progress text) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalCard.kt`
- [ ] T025 [P] [US1] Create EmptyGoalsState composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/EmptyGoalsState.kt`
- [ ] T026 [US1] Create GoalsScreen composable (segment control, goal list, empty state) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalsScreen.kt`

### Navigation for US1

- [ ] T027 [US1] Create Goals routes in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/Routes.kt`
- [ ] T028 [US1] Create GoalsNavGraph with stateProvider pattern in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/GoalsNavGraph.kt`
- [ ] T029 [US1] Integrate GoalsNavGraph into TandemNavHost in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/TandemNavHost.kt`
- [ ] T030 [US1] Update MainScreen to replace placeholder GoalsScreen in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/main/MainScreen.kt`

### Verification for US1

- [ ] T031 [US1] Run `./gradlew :composeApp:compileDebugKotlinAndroid` and verify build succeeds
- [ ] T032 [US1] Manual test: Navigate to Goals tab → Verify segment control displays → Verify empty state shows

**Checkpoint**: User Story 1 complete - Users can view Goals tab with segment control and empty state

---

## Phase 4: User Story 2 - Create a New Goal (Priority: P1)

**Goal**: Users can create new goals with name, icon, type, duration, and shared toggle

**Independent Test**: Tap Add button → Fill form → Save → Goal appears in list

### UI Components for US2

- [ ] T033 [P] [US2] Create EmojiPicker composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/EmojiPicker.kt`
- [ ] T034 [P] [US2] Create GoalTypeSelector composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/GoalTypeSelector.kt`
- [ ] T035 [P] [US2] Create DurationSelector composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/DurationSelector.kt`
- [ ] T036 [US2] Create AddGoalSheet composable (name, icon picker, type, duration, shared toggle) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/AddGoalSheet.kt`

### ViewModel Updates for US2

- [ ] T037 [US2] Add goal creation events to GoalsEvent in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsEvent.kt`
- [ ] T038 [US2] Add goal creation state to GoalsUiState in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsUiState.kt`
- [ ] T039 [US2] Implement createGoal handler with 10-goal limit check in GoalsViewModel in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsViewModel.kt`

### Screen Integration for US2

- [ ] T040 [US2] Update GoalsScreen to show Add button (48dp touch target) and AddGoalSheet in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalsScreen.kt`

### Verification for US2

- [ ] T041 [US2] Run `./gradlew :composeApp:compileDebugKotlinAndroid` and verify build succeeds
- [ ] T042 [US2] Manual test: Create Weekly Habit goal → Verify appears in list with correct progress display

**Checkpoint**: User Story 2 complete - Users can create goals of all three types

---

## Phase 5: User Story 3 - Track Goal Progress Visually (Priority: P1)

**Goal**: Users see visual progress through progress bars and completion fractions

**Independent Test**: View goal card → Verify progress bar reflects completion percentage → Verify progress text shows fraction

### Progress Calculation

- [ ] T043 [US3] Implement calculateProgress methods in Goal model in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Goal.kt`
- [ ] T044 [US3] Implement weekly reset logic in GoalRepositoryImpl (processWeeklyResets) in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/GoalRepositoryImpl.kt`

### UI Updates for US3

- [ ] T045 [US3] Update GoalCard to show type-specific progress indicators in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalCard.kt`
- [ ] T046 [US3] Add "This week" indicator for Weekly Habit and Recurring Task goals in GoalCard

### ViewModel Updates for US3

- [ ] T047 [US3] Call processWeeklyResets on GoalsViewModel init in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsViewModel.kt`
- [ ] T048 [US3] Call checkGoalExpirations on GoalsViewModel init for status transitions

### Verification for US3

- [ ] T049 [US3] Run `./gradlew :composeApp:compileDebugKotlinAndroid` and verify build succeeds
- [ ] T050 [US3] Manual test: Create goal → Verify progress bar and text display correctly

**Checkpoint**: User Story 3 complete - Progress visualization working for all goal types

---

## Phase 6: User Story 4 - Link Tasks to Goals (Priority: P2)

**Goal**: Users can link tasks to goals and completing tasks updates goal progress

**Independent Test**: Edit task → Select goal from picker → Complete task → Verify goal progress increments

### Repository Updates for US4

- [ ] T051 [US4] Add observeGoalsForTaskPicker method to GoalRepository in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/GoalRepository.kt`
- [ ] T052 [US4] Implement observeGoalsForTaskPicker in GoalRepositoryImpl in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/GoalRepositoryImpl.kt`

### Task Integration for US4

- [ ] T053 [US4] Create GoalPicker composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/GoalPicker.kt`
- [ ] T054 [US4] Update TaskDetailSheet to include GoalPicker in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/TaskDetailSheet.kt`
- [ ] T055 [US4] Create GoalBadge composable for task cards in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/components/GoalBadge.kt`
- [ ] T056 [US4] Update TaskListItem to display GoalBadge when linkedGoalId exists in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/week/TaskListItem.kt`

### Progress Auto-Update for US4

- [ ] T057 [US4] Update WeekViewModel handleTaskCheckboxTapped to increment goal progress in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/week/WeekViewModel.kt`
- [ ] T058 [US4] Add goalRepository dependency to WeekViewModel and update DI in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/WeekModule.kt`

### Verification for US4

- [ ] T059 [US4] Run `./gradlew :composeApp:compileDebugKotlinAndroid` and verify build succeeds
- [ ] T060 [US4] Manual test: Link task to goal → Complete task → Verify goal progress updates

**Checkpoint**: User Story 4 complete - Task-goal linking and automatic progress tracking working

---

## Phase 7: User Story 5 - View Goal Details and History (Priority: P2)

**Goal**: Users can view full goal details, progress history, and linked tasks

**Independent Test**: Tap goal card → Verify detail screen shows all info → Verify progress history displays

### UI Components for US5

- [ ] T061 [P] [US5] Create ProgressHistoryChart composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/ProgressHistoryChart.kt`
- [ ] T062 [P] [US5] Create LinkedTasksList composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/LinkedTasksList.kt`
- [ ] T063 [US5] Create GoalDetailScreen composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalDetailScreen.kt`

### Repository Updates for US5

- [ ] T064 [US5] Add observeLinkedTasks method to TaskRepository in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/TaskRepository.kt`
- [ ] T065 [US5] Implement observeLinkedTasks in TaskRepositoryImpl in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/TaskRepositoryImpl.kt`
- [ ] T066 [US5] Add getTasksByLinkedGoalId query to Task.sq in `shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/Task.sq`

### ViewModel Updates for US5

- [ ] T067 [US5] Add goal detail state to GoalsUiState in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsUiState.kt`
- [ ] T068 [US5] Add goal detail events to GoalsEvent in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsEvent.kt`
- [ ] T069 [US5] Implement handleGoalTapped to load detail with progress history and linked tasks in GoalsViewModel

### Navigation for US5

- [ ] T070 [US5] Add Goals.Detail route in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/Routes.kt`
- [ ] T071 [US5] Add GoalDetailScreen to GoalsNavGraph in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/GoalsNavGraph.kt`

### Verification for US5

- [ ] T072 [US5] Run `./gradlew :composeApp:compileDebugKotlinAndroid` and verify build succeeds
- [ ] T073 [US5] Manual test: Tap goal → Verify detail screen with history and linked tasks

**Checkpoint**: User Story 5 complete - Goal detail view with history working

---

## Phase 8: User Story 6 - Edit and Delete Goals (Priority: P3)

**Goal**: Users can edit goal properties and delete goals

**Independent Test**: Open goal detail → Edit name → Save → Verify update. Delete goal → Confirm → Verify removed.

### UI Components for US6

- [ ] T074 [US6] Create EditGoalSheet composable (name, icon editable) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/EditGoalSheet.kt`
- [ ] T075 [US6] Create DeleteGoalDialog composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/components/DeleteGoalDialog.kt`

### ViewModel Updates for US6

- [ ] T076 [US6] Add edit/delete events to GoalsEvent in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsEvent.kt`
- [ ] T077 [US6] Add edit state to GoalsUiState in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsUiState.kt`
- [ ] T078 [US6] Implement edit and delete handlers in GoalsViewModel in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/goals/GoalsViewModel.kt`

### Screen Integration for US6

- [ ] T079 [US6] Add Edit and Delete buttons (48dp touch targets) to GoalDetailScreen in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/goals/GoalDetailScreen.kt`
- [ ] T080 [US6] Integrate EditGoalSheet and DeleteGoalDialog into GoalDetailScreen

### Verification for US6

- [ ] T081 [US6] Run `./gradlew :composeApp:compileDebugKotlinAndroid` and verify build succeeds
- [ ] T082 [US6] Manual test: Edit goal name → Verify update. Delete goal → Verify removal.

**Checkpoint**: User Story 6 complete - Full CRUD operations for goals

---

## Phase 9: User Story 7 - Goal-Based Task Suggestions in Planning (Priority: P3)

**Goal**: During week planning, users see task suggestions based on active goals

**Independent Test**: Enter week planning → Verify "Based on your goals" section appears with suggestions

### Repository Updates for US7

- [ ] T083 [US7] Add getActiveGoalsForSuggestions method to GoalRepository in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/GoalRepository.kt`
- [ ] T084 [US7] Implement getActiveGoalsForSuggestions in GoalRepositoryImpl in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/GoalRepositoryImpl.kt`

### UI Components for US7

- [ ] T085 [US7] Create GoalSuggestionsCard composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/components/GoalSuggestionsCard.kt`

### ViewModel Updates for US7

- [ ] T086 [US7] Add goal suggestions state to PlanningUiState in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningUiState.kt`
- [ ] T087 [US7] Add goalRepository dependency to PlanningViewModel and load suggestions in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/planning/PlanningViewModel.kt`
- [ ] T088 [US7] Update PlanningModule to inject goalRepository in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/PlanningModule.kt`

### Screen Integration for US7

- [ ] T089 [US7] Add GoalSuggestionsCard to AddTasksStepScreen in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/planning/AddTasksStepScreen.kt`

### Verification for US7

- [ ] T090 [US7] Run `./gradlew :composeApp:compileDebugKotlinAndroid` and verify build succeeds
- [ ] T091 [US7] Manual test: Enter planning with active goals → Verify suggestions appear

**Checkpoint**: User Story 7 complete - Goal-based suggestions in planning

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Final integration, shared goal sync, and verification

### Shared Goals Sync

- [ ] T092 Add shared goals sync to PartnerViewModel using existing Supabase Realtime in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/partner/PartnerViewModel.kt`
- [ ] T093 Create Supabase goals table migration (for shared goal sync) in `supabase/migrations/`

### Edge Cases

- [ ] T094 Implement goal expiration check (COMPLETED/EXPIRED status) in GoalRepositoryImpl
- [ ] T095 Handle partner disconnection: Convert shared goals to personal in PartnerRepository
- [ ] T096 Clear linkedGoalId when goal is deleted in GoalRepositoryImpl

### Final Verification

- [ ] T097 Run full build: `./gradlew :composeApp:compileDebugKotlinAndroid`
- [ ] T098 E2E Test: Create goal → Appears in list with progress display
- [ ] T099 E2E Test: Link task to goal → Complete task → Verify progress updates
- [ ] T100 E2E Test: Create shared goal → Verify visible to partner (requires two test accounts)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **US1-US3 (Phase 3-5)**: Depend on Foundational - These are all P1 priority and build core functionality
- **US4-US5 (Phase 6-7)**: Depend on Foundational - P2 priority, can start after US1
- **US6-US7 (Phase 8-9)**: Depend on Foundational - P3 priority, can start after US1
- **Polish (Phase 10)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Foundation only - View goals (no data required)
- **US2 (P1)**: Foundation only - Create goals (enables US1 to show data)
- **US3 (P1)**: Foundation + US2 - Progress visualization (requires goals to exist)
- **US4 (P2)**: Foundation only - Task linking (can work without goals existing)
- **US5 (P2)**: Foundation + US2 - Goal detail (requires goals to exist)
- **US6 (P3)**: US5 - Edit/Delete (requires detail screen)
- **US7 (P3)**: Foundation + US2 - Planning suggestions (requires goals to exist)

### Within Each User Story

- ViewModel/State before UI components
- Repository before ViewModel (if new methods needed)
- Core implementation before integration

### Parallel Opportunities

All tasks marked [P] within a phase can run in parallel. Additionally:
- US1, US2, US3 share many foundation components - implement in order
- US4 and US5 can proceed in parallel after Foundation
- US6 requires US5 to be complete
- US7 can proceed independently after Foundation

---

## Parallel Example: Foundation Phase

```bash
# Launch all domain models in parallel:
Task: "Create GoalType sealed class in shared/.../domain/model/GoalType.kt"
Task: "Create GoalStatus enum in shared/.../domain/model/GoalStatus.kt"
Task: "Create Goal data class in shared/.../domain/model/Goal.kt"
Task: "Create GoalProgress data class in shared/.../domain/model/GoalProgress.kt"
```

## Parallel Example: User Story 1 UI

```bash
# Launch all UI components in parallel:
Task: "Create GoalProgressBar composable in composeApp/.../ui/goals/components/GoalProgressBar.kt"
Task: "Create GoalCard composable in composeApp/.../ui/goals/GoalCard.kt"
Task: "Create EmptyGoalsState composable in composeApp/.../ui/goals/components/EmptyGoalsState.kt"
```

---

## Implementation Strategy

### MVP First (User Stories 1-3 Only)

1. Complete Phase 1: Setup (SQLDelight schemas)
2. Complete Phase 2: Foundational (domain models, repository, DI)
3. Complete Phase 3: US1 - View Goals (segment control, goal cards)
4. Complete Phase 4: US2 - Create Goals (add sheet with all options)
5. Complete Phase 5: US3 - Progress Display (visualization, weekly reset)
6. **STOP and VALIDATE**: Test US1-3 independently - can create and view goals with progress
7. Deploy/demo if ready

### Incremental Delivery

1. Foundation → US1 → US2 → US3 → **MVP Demo** (P1 complete)
2. Add US4 (Task Linking) → Test → Demo
3. Add US5 (Detail View) → Test → Demo
4. Add US6 (Edit/Delete) → Test → Demo
5. Add US7 (Planning Suggestions) → Test → Demo
6. Polish Phase → Final Release

---

## Critical Implementation Notes

1. **ViewModel Init MUST Wait for Auth**: Use `authRepository.authState.filterIsInstance<AuthState.Authenticated>().first()`
2. **Check Partner Before Shared Goals**: Call `partnerRepository.getPartner(userId)` before loading shared goals
3. **Goal Progress Must Be Reactive**: When task completes, increment goal progress immediately
4. **All Buttons 48dp Minimum**: Touch targets per Material Design 3 accessibility guidelines
5. **No Shame Language**: Use "Completed/Expired" not "Failed/Missed"
6. **Max 10 Active Goals**: Enforce limit in createGoal handler with user-friendly message

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All file paths are exact - copy-paste ready
