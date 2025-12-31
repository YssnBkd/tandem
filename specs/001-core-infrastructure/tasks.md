# Tasks: Core Infrastructure

**Input**: Design documents from `/specs/001-core-infrastructure/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Unit tests for auth use cases are explicitly requested. Test tasks are included.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

**Kotlin Multiplatform Structure:**
- **shared/src/commonMain/kotlin/org/epoque/tandem/**: Domain models, repository interfaces
- **composeApp/src/commonMain/kotlin/org/epoque/tandem/**: Shared presentation (state, events)
- **composeApp/src/androidMain/kotlin/org/epoque/tandem/**: Android UI, DI, navigation
- **composeApp/src/commonTest/kotlin/**: Unit tests

---

## Phase 1: Setup (Project Dependencies & Configuration)

**Purpose**: Add required dependencies and configure build system

- [ ] T001 Add Supabase, Koin, Navigation, DataStore, and Serialization versions to `gradle/libs.versions.toml`
- [ ] T002 Add Supabase library declarations (bom, auth, compose-auth) to `gradle/libs.versions.toml`
- [ ] T003 [P] Add Koin library declarations (core, android, compose, compose-viewmodel) to `gradle/libs.versions.toml`
- [ ] T004 [P] Add Navigation and DataStore library declarations to `gradle/libs.versions.toml`
- [ ] T005 [P] Add Ktor client (Android) and kotlinx-serialization library declarations to `gradle/libs.versions.toml`
- [ ] T006 Add kotlinSerialization plugin declaration to `gradle/libs.versions.toml`
- [ ] T007 Update `composeApp/build.gradle.kts` to apply serialization plugin
- [ ] T008 Update `composeApp/build.gradle.kts` androidMain dependencies (Supabase, Koin, Navigation, DataStore)
- [ ] T009 Update `composeApp/build.gradle.kts` commonMain dependencies (Koin core, kotlinx-serialization)
- [ ] T010 Add BuildConfig generation for SUPABASE_URL, SUPABASE_KEY, GOOGLE_WEB_CLIENT_ID in `composeApp/build.gradle.kts`
- [ ] T011 Create `local.properties.example` with placeholder values for secrets documentation
- [ ] T012 Verify build succeeds with `:composeApp:compileDebugKotlinAndroid`

**Checkpoint**: Build configuration complete - dependencies available

---

## Phase 2: Foundational (Domain Layer & Core Infrastructure)

**Purpose**: Core models, repository interfaces, and DI setup that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

### Domain Models (shared/commonMain)

- [ ] T013 Create `AuthProvider` enum in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/AuthProvider.kt`
- [ ] T014 [P] Create `User` data class in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/User.kt`
- [ ] T015 [P] Create `AuthError` sealed class in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/AuthError.kt`
- [ ] T016 [P] Create `ValidationResult` sealed interface in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/ValidationResult.kt`

### Repository Interface (shared/commonMain)

- [ ] T017 Create `AuthRepository` interface in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/AuthRepository.kt`

### Presentation State (composeApp/commonMain)

- [ ] T018 [P] Create `AuthUiState` sealed interface in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/auth/AuthUiState.kt`
- [ ] T019 [P] Create `AuthEvent` sealed interface in `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/auth/AuthEvent.kt`

### Validators (shared/commonMain)

- [ ] T020 [P] Create `EmailValidator` object in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/validation/EmailValidator.kt`
- [ ] T021 [P] Create `PasswordValidator` object in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/validation/PasswordValidator.kt`
- [ ] T022 [P] Create `DisplayNameValidator` object in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/validation/DisplayNameValidator.kt`

### Repository Implementation (composeApp/androidMain)

- [ ] T023 Create `AuthRepositoryImpl` class in `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/repository/AuthRepositoryImpl.kt`

### Dependency Injection (composeApp/androidMain)

- [ ] T024 Create `AppModule` Koin module in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/AppModule.kt`
- [ ] T025 [P] Create `AuthModule` Koin module in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/AuthModule.kt`
- [ ] T026 Create `TandemApp` Application class with Koin init in `composeApp/src/androidMain/kotlin/org/epoque/tandem/TandemApp.kt`
- [ ] T027 Update `AndroidManifest.xml` to use TandemApp as application class

### Navigation Foundation (composeApp/androidMain)

- [ ] T028 Create `Routes` sealed classes with @Serializable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/Routes.kt`

### Unit Tests for Validators

- [ ] T029 [P] Create `EmailValidatorTest` in `composeApp/src/commonTest/kotlin/org/epoque/tandem/domain/validation/EmailValidatorTest.kt`
- [ ] T030 [P] Create `PasswordValidatorTest` in `composeApp/src/commonTest/kotlin/org/epoque/tandem/domain/validation/PasswordValidatorTest.kt`
- [ ] T031 [P] Create `DisplayNameValidatorTest` in `composeApp/src/commonTest/kotlin/org/epoque/tandem/domain/validation/DisplayNameValidatorTest.kt`

- [ ] T032 Verify build and tests pass with `:composeApp:compileDebugKotlinAndroid` and `:composeApp:testDebugUnitTest`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - First-Time Registration (Priority: P1) MVP

**Goal**: New users can create an account via email/password or Google Sign-In and see the welcome screen

**Independent Test**: Launch app on fresh install, create account with valid credentials, verify account creation success

### Unit Tests for User Story 1

- [ ] T033 [P] [US1] Create `AuthViewModelTest` with registration test cases in `composeApp/src/commonTest/kotlin/org/epoque/tandem/presentation/auth/AuthViewModelTest.kt`

### Implementation for User Story 1

- [ ] T034 [US1] Create `AuthViewModel` in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/auth/AuthViewModel.kt`
- [ ] T035 [P] [US1] Create `Color.kt` with brand colors and light/dark palettes in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/theme/Color.kt`
- [ ] T036 [P] [US1] Create `Type.kt` with typography scale in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/theme/Type.kt`
- [ ] T037 [US1] Create `Theme.kt` with MD3 theme and dynamic colors in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/theme/Theme.kt`
- [ ] T038 [US1] Create `WelcomeScreen` composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/auth/WelcomeScreen.kt`
- [ ] T039 [US1] Create `RegisterScreen` composable with form validation in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/auth/RegisterScreen.kt`
- [ ] T040 [US1] Create `AuthNavGraph` navigation extension in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/AuthNavGraph.kt`
- [ ] T041 [US1] Create `TandemNavHost` main NavHost in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/TandemNavHost.kt`
- [ ] T042 [US1] Update `MainActivity` to use TandemNavHost with TandemTheme in `composeApp/src/androidMain/kotlin/org/epoque/tandem/MainActivity.kt`
- [ ] T043 [US1] Implement Google Sign-In button with ComposeAuth in `WelcomeScreen`
- [ ] T044 [US1] Add input validation error display to `RegisterScreen`
- [ ] T045 [US1] Add loading state and error handling to auth screens

- [ ] T046 Verify build passes and test User Story 1 manually on device

**Checkpoint**: User Story 1 complete - new users can register. MVP achieved.

---

## Phase 4: User Story 2 - Returning User Authentication (Priority: P2)

**Goal**: Returning users are auto-authenticated or can sign in; users can sign out

**Independent Test**: Create account, sign out, sign back in with valid credentials; close/reopen app to verify session persistence

### Unit Tests for User Story 2

- [ ] T047 [P] [US2] Add sign-in and sign-out test cases to `AuthViewModelTest`

### Implementation for User Story 2

- [ ] T048 [US2] Create `SignInScreen` composable in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/auth/SignInScreen.kt`
- [ ] T049 [US2] Add SignIn route to `AuthNavGraph`
- [ ] T050 [US2] Implement session auto-restore in `AuthRepositoryImpl` using Supabase sessionStatus flow
- [ ] T051 [US2] Add sign-out functionality to `AuthViewModel`
- [ ] T052 [US2] Update `TandemNavHost` to observe auth state and navigate conditionally
- [ ] T053 [US2] Add "Already have an account? Sign In" link to `WelcomeScreen`
- [ ] T054 [US2] Add "Don't have an account? Register" link to `SignInScreen`
- [ ] T055 [US2] Handle invalid credentials error display in `SignInScreen`
- [ ] T056 [US2] Add network error handling with retry capability

- [ ] T057 Verify build passes and test User Story 2 manually

**Checkpoint**: User Stories 1 AND 2 complete - full auth flow working

---

## Phase 5: User Story 3 - App Navigation (Priority: P3)

**Goal**: Authenticated users see bottom navigation with Week, Progress, Goals tabs

**Independent Test**: Sign in, verify bottom nav appears with 3 tabs, tap each tab to verify switching, verify Week is default

### Implementation for User Story 3

- [ ] T058 [P] [US3] Create `NavigationTab` sealed class in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/NavigationTab.kt`
- [ ] T059 [P] [US3] Create `WeekScreen` placeholder in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/main/WeekScreen.kt`
- [ ] T060 [P] [US3] Create `ProgressScreen` placeholder in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/main/ProgressScreen.kt`
- [ ] T061 [P] [US3] Create `GoalsScreen` placeholder in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/main/GoalsScreen.kt`
- [ ] T062 [US3] Create `MainScreen` with BottomNavigation scaffold in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/main/MainScreen.kt`
- [ ] T063 [US3] Create `MainNavGraph` navigation extension in `composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/MainNavGraph.kt`
- [ ] T064 [US3] Add Main route to `TandemNavHost` for authenticated users
- [ ] T065 [US3] Implement tab state preservation across configuration changes
- [ ] T066 [US3] Add user display name and sign-out option to MainScreen (e.g., in TopAppBar dropdown)
- [ ] T067 [US3] Ensure Week tab is default destination when entering MainScreen

- [ ] T068 Verify build passes and test User Story 3 manually

**Checkpoint**: User Stories 1, 2, AND 3 complete - full navigation working

---

## Phase 6: User Story 4 - Theme Adaptation (Priority: P3)

**Goal**: App respects system light/dark mode preference and updates automatically

**Independent Test**: Change device theme setting, verify app theme updates immediately

### Implementation for User Story 4

- [ ] T069 [US4] Update `Theme.kt` to use `isSystemInDarkTheme()` for automatic theme detection
- [ ] T070 [US4] Implement dynamic colors for Android 12+ in `Theme.kt`
- [ ] T071 [US4] Apply TandemTheme to all screens (verify wrapping in `TandemNavHost`)
- [ ] T072 [US4] Test theme consistency across all auth screens (Welcome, SignIn, Register)
- [ ] T073 [US4] Test theme consistency across all main screens (Week, Progress, Goals, MainScreen)
- [ ] T074 [US4] Verify brand accent color is visible in both light and dark modes

- [ ] T075 Verify build passes and test User Story 4 manually

**Checkpoint**: All user stories complete - full feature ready

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements, cleanup, and validation

- [ ] T076 [P] Add KDoc comments to all public classes and functions
- [ ] T077 [P] Add content descriptions for accessibility (TalkBack support) to all interactive elements
- [ ] T078 Ensure minimum touch target 48dp for all buttons
- [ ] T079 Run all unit tests and verify they pass
- [ ] T080 Run `:composeApp:compileDebugKotlinAndroid` build validation
- [ ] T081 Test complete user flow: fresh install → register → navigate tabs → sign out → sign in → theme switch
- [ ] T082 Remove any unused imports and dead code
- [ ] T083 Verify no `!!` operators used (null safety per constitution)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - MVP deliverable
- **User Story 2 (Phase 4)**: Depends on Foundational (can parallelize with US1 if desired)
- **User Story 3 (Phase 5)**: Depends on Foundational (can parallelize with US1/US2 if desired)
- **User Story 4 (Phase 6)**: Depends on Theme.kt from US1, can be done after US1
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational - Integrates with US1 auth flow but independently testable
- **User Story 3 (P3)**: Can start after Foundational - Requires auth but independently testable post-login
- **User Story 4 (P3)**: Can start after Theme.kt created in US1 - Independently testable

### Within Each Phase

- Models/enums can run in parallel [P]
- Validators can run in parallel [P]
- Theme files (Color, Type) can run in parallel [P]
- Placeholder screens can run in parallel [P]
- Services depend on models
- ViewModels depend on repositories
- UI screens depend on ViewModels

---

## Parallel Execution Examples

### Phase 2 (Foundational) - Models in Parallel

```bash
# Launch these in parallel:
Task: "T014 Create User data class"
Task: "T015 Create AuthError sealed class"
Task: "T016 Create ValidationResult sealed interface"
```

### Phase 2 (Foundational) - Validators in Parallel

```bash
# Launch these in parallel:
Task: "T020 Create EmailValidator"
Task: "T021 Create PasswordValidator"
Task: "T022 Create DisplayNameValidator"
```

### Phase 2 (Foundational) - Validator Tests in Parallel

```bash
# Launch these in parallel:
Task: "T029 Create EmailValidatorTest"
Task: "T030 Create PasswordValidatorTest"
Task: "T031 Create DisplayNameValidatorTest"
```

### Phase 3 (US1) - Theme Files in Parallel

```bash
# Launch these in parallel:
Task: "T035 Create Color.kt"
Task: "T036 Create Type.kt"
```

### Phase 5 (US3) - Placeholder Screens in Parallel

```bash
# Launch these in parallel:
Task: "T059 Create WeekScreen"
Task: "T060 Create ProgressScreen"
Task: "T061 Create GoalsScreen"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T012)
2. Complete Phase 2: Foundational (T013-T032) - **CRITICAL BLOCKER**
3. Complete Phase 3: User Story 1 (T033-T046)
4. **STOP and VALIDATE**: Test registration flow on device
5. Deploy/demo if ready - MVP achieved!

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Demo (MVP: Registration works!)
3. Add User Story 2 → Test independently → Demo (Sign-in/out works!)
4. Add User Story 3 → Test independently → Demo (Navigation works!)
5. Add User Story 4 → Test independently → Demo (Theming works!)
6. Polish → Final validation → Release ready

### Parallel Team Strategy

With multiple developers after Foundational phase:

- **Developer A**: User Story 1 (Registration)
- **Developer B**: User Story 3 (Navigation shells - can mock auth)
- **Developer C**: User Story 4 (Theme - can work on Theme.kt independently)

Then:
- **Developer A**: User Story 2 (Sign-in) after US1 complete
- **All**: Polish phase

---

## Summary

| Phase | Tasks | Parallel Opportunities |
|-------|-------|------------------------|
| Setup | T001-T012 (12 tasks) | T003, T004, T005 |
| Foundational | T013-T032 (20 tasks) | T014-T016, T018-T019, T020-T022, T024-T025, T029-T031 |
| US1 (P1) | T033-T046 (14 tasks) | T035-T036 |
| US2 (P2) | T047-T057 (11 tasks) | T047 |
| US3 (P3) | T058-T068 (11 tasks) | T058-T061 |
| US4 (P3) | T069-T075 (7 tasks) | - |
| Polish | T076-T083 (8 tasks) | T076-T077 |

**Total**: 83 tasks

---

## Notes

- [P] tasks = different files, no dependencies within that group
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable after Foundational
- Unit tests are included for validators and AuthViewModel as explicitly requested
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Build validation command: `:composeApp:compileDebugKotlinAndroid`
- Test command: `:composeApp:testDebugUnitTest`
