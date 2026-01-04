# Tasks: Progress & Insights

**Input**: Design documents from `/specs/008-progress-insights/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Not explicitly requested - implementation tasks only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- **Domain models**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/`
- **Use cases**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/` *(create `progress/` dir)*
- **Presentation (ViewModel)**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/` *(create `progress/` dir)*
- **UI (Compose)**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/` *(create `progress/` dir)*
- **DI**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/`
- **Navigation**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/`
- **Preferences**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/` *(create `data/preferences/` dirs)*

**Note**: New directories will be created automatically when first file is written.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create base project structure and foundational models for Progress feature

- [ ] T001 [P] Create StreakResult domain model in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/StreakResult.kt`
- [ ] T002 [P] Create CompletionStats domain model in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/CompletionStats.kt`
- [ ] T003 [P] Create TrendDataPoint domain model in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/TrendDataPoint.kt`
- [ ] T004 [P] Create TrendChartData domain model in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/TrendChartData.kt`
- [ ] T005 [P] Create WeekSummary domain model in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/WeekSummary.kt`
- [ ] T006 [P] Create PastWeekDetail domain model (includes ReviewDetail, TaskOutcome) in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/PastWeekDetail.kt`
- [ ] T007 [P] Create PastWeeksResult domain model in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/PastWeeksResult.kt`
- [ ] T008 Create ProgressPreferences DataStore class in `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ProgressPreferences.kt`

**Checkpoint**: All domain models compile, DataStore preference keys defined

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core use cases and utilities that ALL user stories depend on

**WARNING**: No user story work can begin until this phase is complete

- [ ] T009 Create GetCompletionStatsUseCase in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/GetCompletionStatsUseCase.kt`
- [ ] T010 Create GetPendingMilestoneUseCase in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/GetPendingMilestoneUseCase.kt`
- [ ] T011 Create MarkMilestoneCelebratedUseCase in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/MarkMilestoneCelebratedUseCase.kt`
- [ ] T012 [P] Create MoodEmojis helper object in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/MoodEmojis.kt`
- [ ] T013 [P] Create TaskStatusDisplay helper object in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/TaskStatusDisplay.kt`
- [ ] T014 [P] Create StreakMilestones helper object in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/StreakMilestones.kt`
- [ ] T015 Add Progress routes to Routes.kt: add `sealed interface Progress : Routes` with `PastWeekDetail(weekId: String)` route (note: `Routes.Main.Progress` already exists for tab, this adds detail navigation) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/Routes.kt`

**Checkpoint**: Foundation ready - run `./gradlew :composeApp:compileDebugKotlinAndroid` to verify

---

## Phase 3: User Story 1 - View Streak with Partner (Priority: P1)

**Goal**: Display current review streak with partner and milestone celebrations

**Independent Test**: Complete weekly reviews with partner over multiple consecutive weeks, verify streak counter increments and milestone celebrations appear at 5/10/20/50 weeks

### Implementation for User Story 1

- [ ] T016 [US1] Create CalculatePartnerStreakUseCase in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/CalculatePartnerStreakUseCase.kt`
  - **Dependencies**: `WeekRepository` (F005), `PartnerRepository` (F006), `ProgressPreferences` (T008)
  - **Logic**: Solo streak (user reviewed weeks) vs partner streak (BOTH reviewed same week)
  - Reference existing `CalculateStreakUseCase` in `domain/usecase/review/` for week query patterns
- [ ] T017 [US1] Create ProgressUiState data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressUiState.kt`
- [ ] T018 [US1] Create ProgressEvent sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressEvent.kt`
- [ ] T019 [US1] Create ProgressSideEffect sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressSideEffect.kt`
- [ ] T020 [US1] Create ProgressViewModel in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressViewModel.kt`
  - **Required imports**: `filterIsInstance`, `first` from kotlinx.coroutines.flow
  - **Init sequence** (see quickstart.md:176-233):
    1. Wait for `authRepository.authState.filterIsInstance<AuthState.Authenticated>().first().user.id`
    2. Get partner via `partnerRepository.getPartner(userId)`
    3. Calculate streak via `calculateStreakUseCase(userId)`
    4. Update UI state with streak data
    5. Trigger `TriggerMilestoneHaptic` side effect if `pendingMilestone != null`
  - **Error handling**: Re-throw `CancellationException`, catch others and set `error` state
  - **Events**: `DismissMilestone`, `Retry`, `ScreenVisible` (US2-4 will add more)
- [ ] T021 [P] [US1] Create StreakCard composable (streak count, partner indicator, milestone celebration overlay) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/StreakCard.kt`
- [ ] T022 [P] [US1] Create MilestoneCelebration composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/MilestoneCelebration.kt`
  - Auto-dismiss after 3s via `LaunchedEffect` with `delay(3000)`
  - Include visible dismiss button (IconButton with close icon, ≥48dp touch target)
  - Trigger haptic feedback via `LocalHapticFeedback.current.performHapticFeedback()`
  - Content description: "Dismiss milestone celebration"
- [ ] T023 [US1] Create ProgressScreen composable shell (LazyColumn with StreakCard) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressScreen.kt`
- [ ] T024 [US1] Create ProgressModule with use cases and ViewModel in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/ProgressModule.kt`
- [ ] T025 [US1] Register ProgressModule in AppModule in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/AppModule.kt`
- [ ] T026 [US1] Wire ProgressScreen to MainScreen Progress tab content in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/main/MainScreen.kt`

**Checkpoint US1**: Run app, navigate to Progress tab, verify streak displays. Test milestone by reaching 5-week streak.

---

## Phase 4: User Story 2 - View Completion Trends (Priority: P2)

**Goal**: Display task completion rate over time with line chart comparing user and partner

**Independent Test**: View completion percentages over past weeks for both user and partner in chart and bars

### Implementation for User Story 2

- [ ] T027 [US2] Create GetCompletionTrendsUseCase (8 weeks, user + partner percentages) in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/GetCompletionTrendsUseCase.kt`
- [ ] T028 [US2] Create GetMonthlyCompletionUseCase (current calendar month aggregation) in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/GetMonthlyCompletionUseCase.kt`
- [ ] T029 [US2] Update ProgressViewModel to load trend data and monthly completion in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressViewModel.kt`
  - Add to init sequence (after streak calculation):
    4. `val trends = getCompletionTrendsUseCase(userId)`
    5. `val monthlyUser = getMonthlyCompletionUseCase(userId)`
    6. `val monthlyPartner = partner?.let { getMonthlyCompletionUseCase(it.id) }`
  - Update UiState with `trendData`, `showTrendChart`, `userMonthlyCompletion`, `partnerMonthlyCompletion`
- [ ] T030 [P] [US2] Create TrendChart composable (Canvas-based line chart, user + partner lines) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/TrendChart.kt`
- [ ] T031 [P] [US2] Create TrendChartEmptyState composable (<4 weeks message) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/TrendChartEmptyState.kt`
- [ ] T032 [P] [US2] Create CompletionBars composable (horizontal bars with percentages for user and partner) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/CompletionBars.kt`
- [ ] T033 [US2] Update ProgressScreen to include CompletionBars and TrendChart sections in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressScreen.kt`
- [ ] T034 [US2] Update ProgressModule with new use cases in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/ProgressModule.kt`

**Checkpoint US2**: Run app, verify trend chart shows 8 weeks of data, completion bars show monthly stats

---

## Phase 5: User Story 3 - Browse Past Weeks (Priority: P3)

**Goal**: Display paginated list of past weeks with summary stats (completion, mood emojis)

**Independent Test**: Scroll through past weeks list, verify summary stats display correctly, pagination loads more

### Implementation for User Story 3

- [ ] T035 [US3] Create GetPastWeeksUseCase (offset pagination, 10 per page) in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/GetPastWeeksUseCase.kt`
- [ ] T036 [US3] Create WeekSummaryUiModel data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/WeekSummaryUiModel.kt`
- [ ] T037 [US3] Update ProgressUiState with pastWeeks list, hasMoreWeeks, isLoadingMoreWeeks in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressUiState.kt`
- [ ] T038 [US3] Update ProgressEvent with LoadMoreWeeks event in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressEvent.kt`
- [ ] T039 [US3] Update ProgressViewModel with loadPastWeeks and loadMoreWeeks functions in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressViewModel.kt`
  - Add to init sequence (after monthly completion):
    7. `val pastWeeks = getPastWeeksUseCase(userId, offset = 0, limit = 10)`
  - Add `loadMoreWeeks()` function with offset tracking
  - Handle `LoadMoreWeeks` event in `onEvent()`
- [ ] T040 [P] [US3] Create PastWeekItem composable (dateRange, completion text, mood emojis, 48dp touch target) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/PastWeekItem.kt`
- [ ] T041 [P] [US3] Create PastWeeksList composable (LazyColumn with pagination trigger) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/PastWeeksList.kt`
- [ ] T042 [US3] Update ProgressScreen to include Past Weeks header and PastWeeksList in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressScreen.kt`
- [ ] T043 [US3] Update ProgressModule with GetPastWeeksUseCase in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/ProgressModule.kt`

**Checkpoint US3**: Run app, verify past weeks list shows 10 items, scroll to load more

---

## Phase 6: User Story 4 - View Past Week Detail (Priority: P4)

**Goal**: Tap past week to see full review details with side-by-side partner summaries and task outcomes

**Independent Test**: Tap any past week, verify detail view shows user/partner review notes, task list with outcomes

### Implementation for User Story 4

- [ ] T044 [US4] Create GetPastWeekDetailUseCase in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/progress/GetPastWeekDetailUseCase.kt`
- [ ] T045 [US4] Create PastWeekDetailUiState data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/PastWeekDetailUiState.kt`
- [ ] T046 [US4] Create ReviewSummaryUiModel data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ReviewSummaryUiModel.kt`
- [ ] T047 [US4] Create TaskOutcomeUiModel data class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/TaskOutcomeUiModel.kt`
- [ ] T048 [US4] Create PastWeekDetailEvent sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/PastWeekDetailEvent.kt`
- [ ] T049 [US4] Create PastWeekDetailSideEffect sealed class in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/PastWeekDetailSideEffect.kt`
- [ ] T050 [US4] Create PastWeekDetailViewModel (load week detail, handle back navigation) in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/PastWeekDetailViewModel.kt`
- [ ] T051 [P] [US4] Create ReviewSummaryCard composable (name, mood emoji, completion text, note) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ReviewSummaryCard.kt`
- [ ] T052 [P] [US4] Create ReviewSummaryCards composable (side-by-side user and partner) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ReviewSummaryCards.kt`
- [ ] T053 [P] [US4] Create TaskOutcomeItem composable (title, user status, partner status) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/TaskOutcomeItem.kt`
- [ ] T054 [P] [US4] Create TaskOutcomesList composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/TaskOutcomesList.kt`
- [ ] T055 [US4] Create PastWeekDetailScreen composable (TopAppBar, ReviewSummaryCards, TaskOutcomesList) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/PastWeekDetailScreen.kt`
- [ ] T056 [US4] Create ProgressNavGraph with Progress routes and PastWeekDetail navigation in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/ProgressNavGraph.kt`
- [ ] T057 [US4] Update TandemNavHost to include progressNavGraph in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/TandemNavHost.kt`
- [ ] T058 [US4] Update ProgressViewModel to handle PastWeekTapped event with NavigateToWeekDetail side effect in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressViewModel.kt`
- [ ] T059 [US4] Update ProgressScreen to collect side effects and trigger navigation in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressScreen.kt`
- [ ] T060 [US4] Update ProgressModule with PastWeekDetailViewModel in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/ProgressModule.kt`

**Checkpoint US4**: Run app, tap past week, verify navigation to detail, verify back navigation preserves scroll position

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Empty states, error handling, accessibility, and final touches

- [ ] T061 [P] Create ProgressEmptyState composable (no history, encourage first week) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressEmptyState.kt`
- [ ] T062 [P] Create ProgressErrorState composable (error message, retry button) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressErrorState.kt`
- [ ] T063 Update ProgressScreen to show ProgressEmptyState when showEmptyState is true in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressScreen.kt`
- [ ] T064 Update ProgressScreen to show ProgressErrorState when error is not null in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/ProgressScreen.kt`
- [ ] T065 Verify all interactive elements have minimum 48dp touch targets:
  - `PastWeekItem` clickable row (T040)
  - `MilestoneCelebration` dismiss button (T022)
  - `ProgressErrorState` retry button (T062)
  - `PastWeekDetailScreen` back button in TopAppBar (T055)
  - Implementation: Use `Modifier.heightIn(min = 48.dp)` or `Modifier.size(48.dp)`
- [ ] T066 Add content descriptions for accessibility:
  - `TrendChart`: "Completion trend chart showing {weekCount} weeks of data"
  - `CompletionBars`: "Your completion: {percentage}%, Partner completion: {percentage}%"
  - `StreakCard`: "{count}-week streak {with partner/solo}"
  - `PastWeekItem`: "Week of {dateRange}, {userCompletion} completed"
  - `MilestoneCelebration` dismiss: "Dismiss milestone celebration"
- [ ] T067 Update PastWeeksList to disable "Load More" when offline (check network connectivity) in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/progress/PastWeeksList.kt`
- [ ] T068 Run `./gradlew :composeApp:compileDebugKotlinAndroid` to verify build succeeds
- [ ] T069 Run security advisors check via Supabase MCP to verify no RLS issues

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
  - User stories can proceed sequentially in priority order (P1 -> P2 -> P3 -> P4)
  - US2-4 can technically start in parallel but have ViewModel/Screen dependencies on US1 base
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational (Phase 2) - Creates base ProgressViewModel and ProgressScreen
- **US2 (P2)**: Depends on US1 completing ProgressViewModel base - Adds trend and completion bar logic
- **US3 (P3)**: Depends on US1 completing ProgressScreen base - Adds past weeks list
- **US4 (P4)**: Depends on US3 completing past weeks list - Adds detail navigation and screen

### Within Each User Story

- Use cases before ViewModel logic
- UI state models before ViewModel
- ViewModel before UI components
- UI components before screen integration
- DI updates after components exist

### Parallel Opportunities

**Phase 1 (All [P] tasks can run in parallel)**:
```
T001, T002, T003, T004, T005, T006, T007, T008
```

**Phase 2 (Foundation)**:
```
T012, T013, T014 can run in parallel
```

**US1 (Phase 3)**:
```
T021, T022 can run in parallel (UI components)
```

**US2 (Phase 4)**:
```
T030, T031, T032 can run in parallel (UI components)
```

**US3 (Phase 5)**:
```
T040, T041 can run in parallel (UI components)
```

**US4 (Phase 6)**:
```
T051, T052, T053, T054 can run in parallel (UI components)
```

**Phase 7 (Polish)**:
```
T061, T062 can run in parallel
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (domain models)
2. Complete Phase 2: Foundational (use cases, helpers, routes)
3. Complete Phase 3: User Story 1 (streak display)
4. **STOP and VALIDATE**: Test streak displays correctly
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational -> Foundation ready
2. Add User Story 1 -> Test streak -> Deploy (MVP!)
3. Add User Story 2 -> Test trends/bars -> Deploy
4. Add User Story 3 -> Test past weeks list -> Deploy
5. Add User Story 4 -> Test detail view -> Deploy
6. Add Polish -> Test empty/error states -> Deploy

### Critical Implementation Notes

1. **ViewModel init MUST wait for AuthState.Authenticated** - Use `authRepository.authState.filterIsInstance<AuthState.Authenticated>().first()` before data operations
2. **Partner may be null** - Handle gracefully by hiding partner-specific UI (no comparison shown)
3. **Pagination must work correctly** - Use offset-based pagination with automatic load on scroll-to-bottom
4. **All interactive elements MUST have VISIBLE touch targets >= 48dp** - Required for accessibility
5. **CancellationException must be re-thrown** - Never consume coroutine cancellation exceptions

---

## E2E Verification Checklist

After all phases complete, verify:

- [ ] E2E-001: Open Progress tab -> Streak displays with correct count
- [ ] E2E-002: View Progress tab with 4+ weeks history -> Trend chart shows data with lines
- [ ] E2E-003: View Progress tab -> Past weeks list loads 10 items
- [ ] E2E-004: Scroll past weeks to bottom -> More weeks load (if available)
- [ ] E2E-005: Tap past week -> Detail screen shows with user/partner summaries
- [ ] E2E-006: Tap back from detail -> Returns to Progress tab at same scroll position
- [ ] E2E-007: Reach 5-week streak -> Milestone celebration shows once then auto-dismisses
- [ ] E2E-008: View Progress without partner -> Shows solo streak, hides partner comparison
- [ ] E2E-009: View Progress with no history -> Shows empty state with encouragement
- [ ] E2E-010: View Progress offline -> Displays cached data correctly, "Load More" disabled

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
