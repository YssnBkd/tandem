# Tandem: Spec-Driven Development Strategy & Prompts

## Overview

This document provides a complete strategy for building Tandem using GitHub's Spec Kit with Claude Code. The app will be built on Kotlin Multiplatform (KMP) targeting Android first, with iOS preparation for later.

---

## Part 1: Development Strategy

### 1.1 Project Architecture Decision

**Platform:** Kotlin Multiplatform (KMP)
- **Shared Code:** Business logic, data models, repositories
- **Platform-Specific:** UI (Jetpack Compose for Android, SwiftUI for iOS later)

**Architecture Pattern:** Clean Architecture + MVI
- **Domain Layer:** Use cases, entities, repository interfaces (shared)
- **Data Layer:** Repository implementations, data sources, DTOs (shared)
- **Presentation Layer:** ViewModels, UI state, Compose UI (platform-specific)

**Key Libraries:**
- Compose Multiplatform (UI)
- Koin (Dependency Injection)
- Ktor (Networking)
- SQLDelight (Local Database)
- Kotlin Coroutines + Flow (Async)
- DataStore (Preferences)

### 1.2 Feature Decomposition Strategy

Rather than building the entire app in one spec, we'll decompose into 8 sequential features. Each feature goes through the complete Spec Kit cycle: specify → clarify → plan → tasks → implement.

| Feature # | Name | Description | Dependencies |
|-----------|------|-------------|--------------|
| 001 | Core Infrastructure | Auth, navigation shell, DI setup, theme | None |
| 002 | Task Data Layer | Task entity, database, repository | 001 |
| 003 | Week View | Main screen with task list, segments | 001, 002 |
| 004 | Week Planning | Sunday Setup flow | 001, 002, 003 |
| 005 | Week Review | Solo and Together review flows | 001, 002, 003 |
| 006 | Partner System | Invite, connect, sync | 001, 002 |
| 007 | Goals System | Goals CRUD, tracking, task linking | 001, 002, 003 |
| 008 | Progress & Insights | Stats, trends, past weeks | 001, 002, 003, 005 |

### 1.3 Implementation Phases

**Phase 1: Foundation (Features 001-003)**
- Can demo a working task list without partner features
- Core loop: Add task → View task → Complete task

**Phase 2: Rituals (Features 004-005)**
- Week Planning and Review flows
- Core differentiator functionality

**Phase 3: Social (Feature 006)**
- Partner connection
- Real-time sync

**Phase 4: Long-term (Features 007-008)**
- Goals system
- Progress tracking

---

## Part 2: Project Setup

### 2.1 Prerequisites

```bash
# Install Spec Kit CLI
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git

# Verify installation
specify --version
specify check
```

### 2.2 Initialize Project

You'll start from an existing KMP template. After cloning/creating the template:

```bash
# Navigate to project root
cd Tandem

# Initialize Spec Kit for Claude Code
specify init . --ai claude --force
```

This creates the `.specify/` directory structure and `.github/` prompt files.

---

## Part 3: The Prompts

### 3.1 Constitution Prompt

Use this with `/speckit.constitution`:

```
/speckit.constitution

Create the constitution for Tandem, a weekly productivity app for couples.

## Core Values

1. **Relationship-First Design**: Every feature must strengthen, not strain, the couple's relationship. Never enable surveillance, nagging, or control dynamics.

2. **Weekly Rhythm**: The week is the atomic unit of time. All features center on the weekly planning → execution → review cycle.

3. **Autonomous Partnership**: Each partner owns their tasks and goals. Visibility is passive. Requests require acceptance.

4. **Celebration Over Judgment**: Frame outcomes positively. "Tried" not "Failed". "Skipped" not "Abandoned".

5. **Intentional Simplicity**: Resist feature creep. No due dates within weeks. No priority levels. No subtasks. No categories.

## Technical Principles

### Platform
- Kotlin Multiplatform with Compose Multiplatform
- Android-first (Material Design 3), iOS preparation
- Minimum Android SDK: 24 (Android 7.0)

### Architecture
- Clean Architecture with MVI pattern
- Unidirectional data flow
- Domain layer is 100% shared code
- UI layer is platform-specific (Jetpack Compose)

### Code Quality
- Kotlin-first idioms (sealed classes, data classes, extension functions)
- Null safety enforced (no \`!!\` operators)
- All public functions documented with KDoc
- Unit tests for all use cases and ViewModels
- UI tests for critical flows
- Test if build is successful (:composeApp:compileDebugKotlinAndroid) to validate Android implementations

### State Management
- Single source of truth per feature (ViewModel)
- UI State represented as immutable data classes
- Side effects via sealed class events
- Compose state hoisting

### Data Layer
- SQLDelight for local persistence
- Repository pattern with interfaces in domain
- Offline-first with sync queue
- DataStore for preferences

### Dependency Injection
- Koin for DI
- Module per feature
- Constructor injection only

### Design System
- Material Design 3 components
- Dynamic color (Material You) support
- Light and Dark mode required
- Custom theme tokens for brand colors
- 8dp grid system

### Performance
- Lazy loading for lists
- Image caching
- Debounced inputs
- Background sync with WorkManager

### Accessibility
- Content descriptions for all interactive elements
- Minimum touch target 48dp
- Support for TalkBack
- Dynamic text sizing

## Decision Framework

When making technical decisions:
1. Does it strengthen the weekly rhythm?
2. Does it respect partner autonomy?
3. Is it the simplest solution that works?
4. Can it work offline?
5. Does it follow Material Design 3 patterns?

## Non-Negotiables

- NO tracking of partner's incomplete tasks
- NO notifications for partner's task completions (default off)
- NO assigning tasks without acceptance workflow
- NO shame language in UI copy
- NO complex task hierarchies
```

---

### 3.2 Feature 001: Core Infrastructure

#### Specify Prompt

```
/speckit.specify

Feature: Core Infrastructure

## Overview
Set up the foundational architecture for Tandem including authentication, navigation shell, dependency injection, and theming.

## User Stories

### US1: App Launch
As a user, when I open the app for the first time, I see a welcome screen with sign-in options so I can create an account.

### US2: Authentication
As a user, I can sign in with email/password or Google Sign-In so I can access my account securely.

### US3: Navigation Shell
As an authenticated user, I see a bottom navigation bar with three tabs (Week, Progress, Goals) so I can navigate the app.

### US4: Theme Support
As a user, I see the app respects my system light/dark mode preference so the app feels native.

## Functional Requirements

### Authentication
- R1.1: Support email/password registration with validation
- R1.2: Support email/password sign-in
- R1.3: Support Google Sign-In (Android)
- R1.4: Persist authentication state across app restarts
- R1.5: Support sign-out functionality
- R1.6: Display user's display name after authentication

### Navigation
- R2.1: Bottom navigation with 3 destinations: Week, Progress, Goals
- R2.2: Preserve navigation state across configuration changes
- R2.3: Week tab is the default/start destination
- R2.4: Show current tab indicator

### Theming
- R3.1: Support Material Design 3 dynamic colors
- R3.2: Support light and dark mode based on system setting
- R3.3: Custom color scheme with brand accent color
- R3.4: Consistent typography scale

### Error Handling
- R4.1: Display user-friendly error messages
- R4.2: Retry capability for network errors
- R4.3: Graceful degradation when offline

## Out of Scope
- Password reset flow (v1.1)
- Biometric authentication (v1.1)
- Account deletion (v1.1)

## Success Criteria
- User can register, sign in, and sign out
- Navigation between all three tabs works
- Theme switches with system setting
- App remembers auth state after restart
```

#### Plan Prompt

```
/speckit.plan

Use Kotlin Multiplatform with the following technical approach:

## Tech Stack
- Kotlin 2.0+ with Compose Multiplatform 1.6+
- Jetpack Compose for Android UI
- Supabase Authentication (Android SDK)
- Jetpack Navigation Compose with type-safe routes
- Koin for dependency injection
- Kotlin Coroutines + Flow for async operations
- DataStore for auth state persistence

## Architecture

### Module Structure
```
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/User.kt
│   ├── repository/AuthRepository.kt
│   └── usecase/auth/
└── data/
    └── repository/AuthRepositoryImpl.kt

composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/
│   ├── theme/
│   ├── navigation/
│   ├── auth/
│   └── main/
└── di/
```

### Authentication Flow
1. App checks DataStore for persisted auth token
2. If token exists → validate with Supabase → navigate to MainScreen
3. If no token → show WelcomeScreen
4. After sign-in → persist token → navigate to MainScreen

### Navigation Structure
- NavHost with nested graphs
- AuthGraph: Welcome, SignIn, Register
- MainGraph: BottomNavigation with Week, Progress, Goals
- Conditional start destination based on auth state

### State Management
- AuthViewModel manages auth UI state
- MainViewModel manages navigation state
- UI State as sealed interface with Loading, Success, Error

## File Structure Plan
- 15-20 files for this feature
- Focus on establishing patterns for subsequent features
```

#### Tasks Prompt

```
/speckit.tasks

Generate implementation tasks for Feature 001: Core Infrastructure.

Focus on:
1. Setting up the project structure and build configuration
2. Implementing authentication with Supabase
3. Creating the navigation shell
4. Establishing the theming system
5. Writing unit tests for auth use cases

Mark parallel tasks with [P] where dependencies allow.
Include file paths for each task.
Order: models → repositories → use cases → ViewModels → UI
```

#### Implement Prompt

```
/speckit.implement

Execute all tasks for Feature 001: Core Infrastructure.

After implementation:
1. Verify the app builds and runs
2. Test registration and sign-in flows
3. Verify navigation between tabs
4. Test theme switching
5. Verify auth persistence after app restart
```

---

### 3.3 Feature 002: Task Data Layer

#### Specify Prompt

```
/speckit.specify

Feature: Task Data Layer

## Overview
Implement the core task data model, local database, and repository for managing tasks in Tandem.

## User Stories

### US1: Task Persistence
As a user, my tasks are saved locally so they persist across app restarts.

### US2: Task CRUD
As a user, I can create, read, update, and delete tasks.

### US3: Week Filtering
As a user, I can view tasks filtered by week.

## Data Model

### Task Entity
```kotlin
data class Task(
    val id: String,
    val title: String,
    val notes: String?,
    val ownerId: String,          // User ID
    val ownerType: OwnerType,     // SELF, PARTNER, SHARED
    val weekId: String,           // ISO week "2024-W52"
    val status: TaskStatus,       // PENDING, COMPLETED, TRIED, SKIPPED
    val createdBy: String,        // User ID who created
    val repeatTarget: Int?,       // For "Gym 3x" style tasks
    val repeatCompleted: Int,     // Current count
    val linkedGoalId: String?,    // Optional goal reference
    val reviewNote: String?,      // Note from week review
    val rolledFromWeekId: String?, // If rolled over
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class OwnerType { SELF, PARTNER, SHARED }
enum class TaskStatus { PENDING, PENDING_ACCEPTANCE, COMPLETED, TRIED, SKIPPED, DECLINED }
```

### Week Entity
```kotlin
data class Week(
    val id: String,              // "2024-W52"
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: String,
    val overallRating: Int?,     // 1-5
    val reviewNote: String?,
    val reviewedAt: Instant?,
    val planningCompletedAt: Instant?
)
```

## Functional Requirements

### Task Operations
- R1.1: Create task with title (required) and notes (optional)
- R1.2: Read all tasks for current user
- R1.3: Read tasks filtered by week ID
- R1.4: Read tasks filtered by owner type (mine, partner's, shared)
- R1.5: Update task title, notes, status
- R1.6: Delete task
- R1.7: Mark task as completed (update status + timestamp)
- R1.8: Increment repeat count for repeating tasks

### Week Operations
- R2.1: Get or create current week
- R2.2: Get week by ID
- R2.3: Update week review data
- R2.4: List past weeks for user

### Data Integrity
- R3.1: Tasks must have valid owner reference
- R3.2: Week IDs follow ISO 8601 week format
- R3.3: Timestamps in UTC

## Success Criteria
- Can create, read, update, delete tasks
- Tasks persist after app restart
- Can filter tasks by week
- Can filter tasks by owner type
```

#### Plan Prompt

```
/speckit.plan

Use SQLDelight for local database with the following approach:

## Tech Stack
- SQLDelight 2.0+ for type-safe SQL
- Kotlin Coroutines Flow for reactive queries
- kotlinx.datetime for date/time handling

## Database Schema

### Tables
- tasks (primary task storage)
- weeks (week metadata and reviews)

### Queries
- selectTasksByWeek
- selectTasksByOwnerType
- selectTaskById
- insertTask
- updateTaskStatus
- updateTaskReview
- deleteTask
- selectWeekById
- selectPastWeeks
- insertOrUpdateWeek

## Repository Pattern
- TaskRepository interface in domain/repository
- TaskRepositoryImpl in data/repository
- WeekRepository interface in domain/repository
- WeekRepositoryImpl in data/repository

## Use Cases
- CreateTaskUseCase
- GetTasksForWeekUseCase
- UpdateTaskStatusUseCase
- DeleteTaskUseCase
- GetCurrentWeekUseCase
- SaveWeekReviewUseCase

## Module Structure
```
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/Task.kt, Week.kt
│   ├── repository/TaskRepository.kt, WeekRepository.kt
│   └── usecase/task/, week/
└── data/
    ├── local/
    │   ├── TandemDatabase.sq
    │   └── TaskQueries.sq, WeekQueries.sq
    └── repository/
        ├── TaskRepositoryImpl.kt
        └── WeekRepositoryImpl.kt
```
```

---

### 3.4 Feature 003: Week View

#### Specify Prompt

```
/speckit.specify

Feature: Week View

## Overview
Implement the main Week tab screen where users view and manage their tasks for the current week.

## User Stories

### US1: View My Tasks
As a user, I see my tasks for the current week with completion status so I can track my progress.

### US2: Segment Navigation
As a user, I can switch between "You", "Partner", and "Shared" segments to view different task sets.

### US3: Quick Add Task
As a user, I can quickly add a new task from the main screen.

### US4: Complete Task
As a user, I can tap a task to mark it complete with satisfying feedback.

### US5: View Task Details
As a user, I can tap a task to see its details and edit it.

### US6: See Progress
As a user, I see my completion progress (e.g., "5/8") for the week.

## Screens

### Week Tab - You Segment
- Week date range header ("Week of Dec 30 - Jan 5")
- Review banner (contextual, when review available)
- Segmented control: You | Partner | Shared
- Quick-add text field
- Task list with checkboxes
- Progress indicator ("5/8")
- Completed tasks at bottom (faded)

### Week Tab - Partner Segment
- Same layout but read-only (no checkboxes)
- "Request a Task" button at bottom
- Partner's tasks (cannot complete)

### Week Tab - Shared Segment
- Tasks owned by both
- Either partner can complete
- Shows "completed by [name]" when done

### Task Detail Screen (Modal)
- Task title (editable)
- Notes (editable)
- Status display
- Owner info
- Created/rolled over info
- Mark Complete button
- Delete option

### Add Task Sheet (Bottom Sheet)
- Title input (required)
- Notes input (optional)
- Owner selector (You / Shared) - only on Shared segment
- Save button

## Functional Requirements

### Task List
- R1.1: Display tasks grouped by status (incomplete first, completed last)
- R1.2: Show task title and completion status
- R1.3: Show repeat progress for repeating tasks ("1/3 ●●○")
- R1.4: Pull-to-refresh to sync
- R1.5: Empty state when no tasks

### Task Completion
- R2.1: Tap checkbox to toggle completion
- R2.2: Light haptic feedback on completion
- R2.3: Animate checkmark appearance
- R2.4: Move completed task to bottom with fade

### Quick Add
- R3.1: Always-visible text field at top
- R3.2: Add task on enter/submit
- R3.3: Clear field after adding
- R3.4: Show error if empty

### Segments
- R4.1: Remember last selected segment
- R4.2: Partner segment is read-only
- R4.3: Shared segment allows completion by either

## Success Criteria
- Can view tasks for current week
- Can add new tasks
- Can complete tasks with feedback
- Can switch between segments
- Progress updates in real-time
```

#### Plan Prompt

```
/speckit.plan

Use Jetpack Compose with Material Design 3:

## UI Components

### WeekScreen (Scaffold)
- TopAppBar with week date range
- SegmentedButtonRow for segment switching
- LazyColumn for task list
- FloatingActionButton or inline TextField for quick add

### TaskListItem (Composable)
- Checkbox (AnimatedVisibility)
- Text with strikethrough when complete
- Repeat progress indicator
- Clickable for detail navigation

### TaskDetailSheet (ModalBottomSheet)
- OutlinedTextField for title/notes
- Status chips
- Action buttons

### AddTaskSheet (ModalBottomSheet)
- TextField with keyboard focus
- Owner selector (SegmentedButton)
- Save/Cancel buttons

## State Management

### WeekViewModel
```kotlin
data class WeekUiState(
    val weekInfo: Week,
    val selectedSegment: Segment,
    val tasks: List<TaskUiModel>,
    val completedCount: Int,
    val totalCount: Int,
    val isLoading: Boolean,
    val error: String?
)

sealed class WeekEvent {
    data class TaskCompleted(val taskId: String) : WeekEvent()
    data class SegmentChanged(val segment: Segment) : WeekEvent()
    data class TaskAdded(val title: String) : WeekEvent()
    // ...
}
```

## Haptics
- Use HapticFeedback.performHapticFeedback() for task completion
- LightImpact feedback type

## Animations
- AnimatedVisibility for checkbox state
- animateItemPlacement for list reordering
```

---

### 3.5 Feature 004: Week Planning

#### Specify Prompt

```
/speckit.specify

Feature: Week Planning

## Overview
Implement the "Sunday Setup" weekly planning flow where users set their tasks for the upcoming week.

## User Stories

### US1: Planning Trigger
As a user, I see a banner prompting me to plan my week starting Sunday evening.

### US2: Rollover Review
As a user, I can review incomplete tasks from last week and choose to add them or skip them.

### US3: Add New Tasks
As a user, I can add new tasks during the planning flow.

### US4: Review Partner Requests
As a user, I can accept or discuss tasks my partner requested of me.

### US5: Planning Confirmation
As a user, I see a summary of my planned week when I finish planning.

## Screens

### Planning Banner (on Week Tab)
- Appears Sunday 6pm until planning complete
- "Plan your week" with [Start] button
- Shows week date range

### Step 1: Rollover Suggestions
- Full-screen card per incomplete task
- Task title and "from last week" label
- [Add to This Week] [Skip] buttons with visible touch targets (≥48dp)
- Progress dots showing remaining

### Step 2: Add New Tasks
- Text input for new task with visible Add button (not keyboard-only)
- [+ Add] button
- Running list of tasks added
- [Done Adding Tasks] to proceed

### Step 3: Partner Requests
- Full-screen card per request
- Task title and optional note from partner
- [Accept] [Discuss] buttons
- Discuss opens in-context thread (v1.1 - show placeholder)

### Step 4: Confirmation
- Checkmark success state
- "X tasks planned" summary
- Task list preview
- [See Partner's Week] [Done] buttons

## Functional Requirements

### Flow Control
- R1.1: Sequential wizard with back navigation
- R1.2: Progress indicator (Step X of 4)
- R1.3: Exit saves progress, can resume
- R1.4: Skip steps with no items

### Rollover
- R2.1: Query incomplete tasks from previous week
- R2.2: Add creates new task in current week with rolledFromWeekId
- R2.3: Skip does not carry task forward

### New Tasks
- R3.1: Quick add interface with VISIBLE submit button (not keyboard-only)
- R3.2: Tasks default to SELF owner
- R3.3: Validate non-empty title with inline error display

### Partner Requests
- R4.1: Show tasks with status PENDING_ACCEPTANCE
- R4.2: Accept changes status to PENDING
- R4.3: Discuss placeholder for v1.0 (show toast "Coming soon")

### Completion
- R5.1: Mark week planning as complete (timestamp via planningCompletedAt)
- R5.2: Dismiss banner after completion

## Integration Points (from Previous Features)

### From Feature 001 (Core Infrastructure):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `authRepository.authState` | Get current user | On ViewModel init | Must wait for `AuthState.Authenticated` |

### From Feature 002 (Task Data Layer):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `taskRepository.observeIncompleteTasksForWeek(previousWeekId)` | Get rollover candidates | Step 1 init | Previous week ID calculated |
| `taskRepository.createTask(task)` | Create rolled-over task | User taps "Add to This Week" | Current week must exist |
| `taskRepository.observeTasksByStatus(PENDING_ACCEPTANCE)` | Get partner requests | Step 3 init | User authenticated |
| `taskRepository.updateTaskStatus(taskId, PENDING)` | Accept partner request | User taps "Accept" | Task exists |
| `weekRepository.getOrCreateCurrentWeek(userId)` | Ensure week exists | On ViewModel init, AFTER auth | User authenticated |
| `weekRepository.updatePlanningCompleted(weekId)` | Mark planning done | Step 4 complete | Week exists |

### From Feature 003 (Week View):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| Week must exist in database | Planning requires current week entity | Before any planning operation | Call `getOrCreateCurrentWeek()` first |

## UI Affordance Requirements

| Action | Primary Method | Secondary Method | Accessibility |
|--------|----------------|------------------|---------------|
| Add rollover task | Visible "Add to This Week" button | - | Touch target ≥48dp |
| Skip rollover task | Visible "Skip" button | - | Touch target ≥48dp |
| Add new task | Visible "+" or "Add" button | Keyboard Done action | Content description |
| Accept partner request | Visible "Accept" button | - | Touch target ≥48dp |

## E2E Verification Checklist
- [ ] E1: Start planning → Step 1 shows previous week's incomplete tasks (verify DB has incomplete tasks)
- [ ] E2: Add rollover task → Task appears in current week with `rolledFromWeekId` set
- [ ] E3: Skip rollover task → Task does NOT appear in current week
- [ ] E4: Add new task in Step 2 → Task appears in Week View after completion
- [ ] E5: Complete planning → `week.planningCompletedAt` is set (verify via ADB)
- [ ] E6: Return to Week View → Planning banner no longer visible

## Success Criteria
- Planning flow completes end-to-end
- Rollover tasks can be added or skipped
- New tasks are saved
- Planning completion is persisted
```

#### Plan Prompt

```
/speckit.plan

Use Jetpack Compose with Material Design 3 and the established architecture from Features 001-003:

## Tech Stack
- Kotlin 2.1+ with Compose Multiplatform
- Jetpack Navigation Compose (wizard flow)
- Koin for dependency injection
- SQLDelight (via Feature 002 repositories)
- DataStore for planning progress persistence
- kotlinx.datetime for week calculations

## Architecture

### Module Structure
```
composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/planning/
│   ├── PlanningScreen.kt           # Main wizard container
│   ├── RolloverStep.kt             # Step 1: Review incomplete tasks
│   ├── AddTasksStep.kt             # Step 2: Add new tasks
│   ├── PartnerRequestsStep.kt      # Step 3: Accept/discuss requests
│   ├── ConfirmationStep.kt         # Step 4: Summary
│   └── PlanningBanner.kt           # Banner on Week Tab
└── presentation/planning/
    ├── PlanningViewModel.kt
    ├── PlanningUiState.kt
    └── PlanningEvent.kt
```

### Navigation
- Nested navigation graph for wizard steps
- Back navigation to previous step (not exit)
- Exit saves progress to DataStore for resume

## Initialization Sequence (CRITICAL)

The ViewModel MUST execute this exact sequence on init:

```kotlin
// PlanningViewModel.init
viewModelScope.launch {
    // 1. Wait for authentication (NEVER skip this)
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Ensure current week exists (learned from Feature 003)
    val currentWeek = weekRepository.getOrCreateCurrentWeek(userId)

    // 3. Calculate previous week ID for rollover
    val previousWeekId = getPreviousWeekId(currentWeek.id)

    // 4. Query rollover candidates
    val rolloverTasks = taskRepository
        .getIncompleteTasksForWeek(previousWeekId)
        .first()

    // 5. Query partner requests (if partner exists)
    val partnerRequests = taskRepository
        .getTasksByStatus(TaskStatus.PENDING_ACCEPTANCE, userId)
        .first()

    // 6. Initialize UI state (only now is loading complete)
    _uiState.update {
        it.copy(
            currentWeek = currentWeek,
            rolloverTasks = rolloverTasks,
            partnerRequests = partnerRequests,
            isLoading = false
        )
    }
}
```

## Implementation Patterns (CRITICAL)

### Side Effect Channel - Single Collector Only
```kotlin
// CORRECT: One LaunchedEffect handles ALL effects
LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            is ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            is NavigateToStep -> navController.navigate(effect.route)
            is NavigateBack -> navController.popBackStack()
            is ExitPlanning -> onPlanningComplete()
        }
    }
}

// WRONG: Multiple LaunchedEffects collecting same channel
// This causes effects to be randomly distributed and some lost!
```

### Auth State Dependency
```kotlin
// CORRECT: Wait for auth before any data operations
authRepository.authState
    .filterIsInstance<AuthState.Authenticated>()
    .first()
    .let { authState ->
        // Now safe to call repositories
    }

// WRONG: Assuming auth is ready immediately
val userId = authRepository.currentUser?.id // May be null!
```

### UI Affordance - Always Visible Actions
```kotlin
// CORRECT: Visible button for submit action
OutlinedTextField(
    // ...
    trailingIcon = {
        IconButton(onClick = onSubmit, enabled = text.isNotBlank()) {
            Icon(Icons.AutoMirrored.Filled.Send, "Submit")
        }
    }
)

// WRONG: Keyboard-only submit (poor discoverability)
keyboardActions = KeyboardActions(onDone = { onSubmit() })
// ^ This alone is insufficient - users may not discover it
```

## State Management

### PlanningUiState
```kotlin
data class PlanningUiState(
    val currentStep: PlanningStep = PlanningStep.ROLLOVER,
    val currentWeek: Week? = null,
    val rolloverTasks: List<Task> = emptyList(),
    val processedRolloverCount: Int = 0,
    val newTasksAdded: List<Task> = emptyList(),
    val partnerRequests: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class PlanningStep { ROLLOVER, ADD_TASKS, PARTNER_REQUESTS, CONFIRMATION }
```

### PlanningEvent (Sealed Class)
```kotlin
sealed class PlanningEvent {
    data class RolloverTaskAdded(val taskId: String) : PlanningEvent()
    data class RolloverTaskSkipped(val taskId: String) : PlanningEvent()
    data class NewTaskAdded(val title: String) : PlanningEvent()
    data object DoneAddingTasks : PlanningEvent()
    data class PartnerRequestAccepted(val taskId: String) : PlanningEvent()
    data class PartnerRequestDiscussed(val taskId: String) : PlanningEvent()
    data object PlanningCompleted : PlanningEvent()
    data object BackPressed : PlanningEvent()
    data object ExitRequested : PlanningEvent()
}
```

## Data Model Extensions

Add to Week entity (if not present):
```kotlin
val planningCompletedAt: Instant?  // Set when planning finishes
```

## File Structure Plan
- 8-10 UI files for steps and components
- 3 presentation files (ViewModel, State, Events)
- Reuse existing Task and Week repositories
```

#### Tasks Prompt

```
/speckit.tasks

Generate implementation tasks for Feature 004: Week Planning.

## Task Organization Requirements

1. **Group by User Story** - Each US should have its own phase
2. **Dependency Order** - Models/state before ViewModel, ViewModel before UI
3. **Parallel Markers** - Use [P] for tasks that can run concurrently
4. **Precise File Paths** - Include exact file locations
5. **Validation Checkpoints** - Add verification tasks after each phase

## Required Phases

### Phase 1: Data Layer Extensions
- Add `planningCompletedAt` to Week entity if needed
- Add `getIncompleteTasksForWeek(weekId)` query if needed
- Add `getPreviousWeekId(currentWeekId)` utility

### Phase 2: Presentation Layer
- PlanningUiState data class
- PlanningEvent sealed class
- PlanningViewModel with CORRECT initialization sequence (see plan.md)
- **CHECKPOINT**: ViewModel unit tests pass

### Phase 3: UI Components [P]
- PlanningBanner composable
- RolloverStep composable
- AddTasksStep composable (with VISIBLE submit button)
- PartnerRequestsStep composable
- ConfirmationStep composable
- **CHECKPOINT**: Preview renders correctly

### Phase 4: Navigation Integration
- Planning navigation graph
- Integration with Week Tab (banner + navigation)
- **CHECKPOINT**: Navigation flow works

### Phase 5: E2E Verification
- E2E-001: Start planning → Shows rollover tasks from previous week
- E2E-002: Add rollover task → Task appears in current week
- E2E-003: Complete planning → `planningCompletedAt` is set
- E2E-004: Return to Week View → Banner dismissed

## Critical Implementation Notes

Include these in relevant tasks:
1. ViewModel init MUST wait for AuthState.Authenticated before calling repositories
2. ViewModel init MUST call `getOrCreateCurrentWeek(userId)` before any week operations
3. Side effects Channel MUST have only ONE collector in the UI
4. All submit actions MUST have VISIBLE buttons (not keyboard-only)
```

---

### 3.6 Feature 005: Week Review

#### Specify Prompt

```
/speckit.specify

Feature: Week Review

## Overview
Implement the week review flow where users reflect on their week, rate their experience, and review each task.

## User Stories

### US1: Review Trigger
As a user, I see a banner prompting me to review my week starting Friday evening.

### US2: Choose Review Mode
As a user, I can choose to review solo or together with my partner.

### US3: Overall Rating
As a user, I can rate my week on a 5-point scale and add an optional note.

### US4: Task-by-Task Review
As a user, I review each task one at a time, marking it as Done, Tried, or Skipped.

### US5: Review Summary
As a user, I see a summary of my week with completion stats and streak info.

### US6: Together Review
As a user, my partner and I can review together, alternating between our tasks.

## Screens

### Review Mode Selection
- "Review Solo" button
- "Review Together" button
- Current streak display

### Solo Review - Step 1: Overall Rating
- "How was your week?" prompt
- 5 emoji options (😫 😕 😐 🙂 🎉)
- Optional text note field
- [Review Tasks] [Quick Finish] buttons

### Solo Review - Step 2: Task Cards
- Full-screen card per task
- Task title centered
- [Done ✓] [Tried ~] [Skipped ○] buttons
- Optional quick note field
- Progress dots

### Solo Review - Complete
- Success checkmark
- Completion percentage with progress bar
- Streak count and message
- [Start Next Week] [Done] buttons

### Together Review - Turns
- Partner name and avatar
- Same rating UI
- [Pass to Partner] button

### Together Review - Task Cards
- Partner indicator on card (border color/label)
- Owner reviews, partner can react
- Reaction buttons (👏 ❤️ 💪) for observer

### Together Review - Complete
- Side-by-side summary cards
- Both partners' stats
- Shared streak celebration

## Functional Requirements

### Review Flow
- R1.1: Two modes: solo and together
- R1.2: Full-screen modal flow
- R1.3: Exit saves progress
- R1.4: Quick Finish marks remaining as Skipped

### Rating
- R2.1: 5-point emoji scale required
- R2.2: Note is optional
- R2.3: Persist rating to Week entity

### Task Review
- R3.1: Show all tasks for the week
- R3.2: Update task status based on selection
- R3.3: Allow optional note per task
- R3.4: Pre-fill already-completed tasks

### Together Mode
- R4.1: Alternate between partners' tasks
- R4.2: Only owner can select outcome
- R4.3: Observer can add reactions (stored on task)
- R4.4: Both overall ratings collected

### Streak
- R5.1: Calculate streak on review completion
- R5.2: Streak breaks if either partner misses
- R5.3: Display shared streak count

## Integration Points (from Previous Features)

### From Feature 001 (Core Infrastructure):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `authRepository.authState` | Get current user | On ViewModel init | Must wait for `AuthState.Authenticated` |

### From Feature 002 (Task Data Layer):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `taskRepository.observeTasksForWeek(weekId, userId)` | Get all tasks for review | Review init | Week must exist |
| `taskRepository.updateTaskStatus(taskId, status)` | Mark task Done/Tried/Skipped | User selects outcome | Task exists |
| `taskRepository.updateTaskReviewNote(taskId, note)` | Save review note | User adds note | Task exists |
| `weekRepository.getOrCreateCurrentWeek(userId)` | Ensure week exists | On ViewModel init, AFTER auth | User authenticated |
| `weekRepository.updateWeekReview(weekId, rating, note)` | Save overall rating | Step 1 complete | Week exists |

### From Feature 003 (Week View):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| Week must exist in database | Review requires week entity | Before any review operation | Call `getOrCreateCurrentWeek()` first |

## UI Affordance Requirements

| Action | Primary Method | Secondary Method | Accessibility |
|--------|----------------|------------------|---------------|
| Select rating | Visible emoji buttons (≥48dp each) | - | Content description per emoji |
| Mark task Done | Visible "Done ✓" button | - | Touch target ≥48dp |
| Mark task Tried | Visible "Tried ~" button | - | Touch target ≥48dp |
| Mark task Skipped | Visible "Skipped ○" button | - | Touch target ≥48dp |

## E2E Verification Checklist
- [ ] E1: Start review → Shows all tasks for current week
- [ ] E2: Rate week → Rating persisted to Week entity (verify via ADB)
- [ ] E3: Mark task Done → Task status updated in database
- [ ] E4: Complete review → `week.reviewedAt` is set
- [ ] E5: Return to Week View → Review banner dismissed

## Success Criteria
- Solo review flow completes
- Tasks are marked with outcomes
- Week rating is saved
- Streak is calculated and displayed
- Together mode alternates correctly (v1.1 - basic implementation)
```

#### Plan Prompt

```
/speckit.plan

Use Jetpack Compose with Material Design 3 and the established architecture from Features 001-004:

## Tech Stack
- Kotlin 2.1+ with Compose Multiplatform
- Jetpack Navigation Compose (wizard flow)
- Koin for dependency injection
- SQLDelight (via Feature 002 repositories)
- DataStore for review progress persistence
- kotlinx.datetime for week/streak calculations

## Architecture

### Module Structure
```
composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/review/
│   ├── ReviewScreen.kt              # Main wizard container
│   ├── ReviewModeSelection.kt       # Solo vs Together choice
│   ├── OverallRatingStep.kt         # Step 1: Rate week
│   ├── TaskReviewStep.kt            # Step 2: Review each task
│   ├── ReviewSummaryStep.kt         # Step 3: Summary
│   ├── ReviewBanner.kt              # Banner on Week Tab
│   └── components/
│       ├── EmojiRatingSelector.kt
│       └── TaskOutcomeCard.kt
└── presentation/review/
    ├── ReviewViewModel.kt
    ├── ReviewUiState.kt
    └── ReviewEvent.kt
```

## Initialization Sequence (CRITICAL)

The ViewModel MUST execute this exact sequence on init:

```kotlin
// ReviewViewModel.init
viewModelScope.launch {
    // 1. Wait for authentication (NEVER skip this)
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Ensure current week exists (learned from Feature 003)
    val currentWeek = weekRepository.getOrCreateCurrentWeek(userId)

    // 3. Query all tasks for the week
    val tasks = taskRepository
        .observeTasksForWeek(currentWeek.id, userId)
        .first()

    // 4. Separate already-completed vs pending review
    val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
    val pendingTasks = tasks.filter { it.status != TaskStatus.COMPLETED }

    // 5. Calculate current streak
    val streak = calculateStreak(userId)

    // 6. Initialize UI state
    _uiState.update {
        it.copy(
            currentWeek = currentWeek,
            tasksToReview = pendingTasks,
            alreadyCompleted = completedTasks,
            currentStreak = streak,
            isLoading = false
        )
    }
}
```

## Implementation Patterns (CRITICAL)

(Same patterns as Feature 004 - see plan.md for Feature 004)
- Side Effect Channel - Single Collector Only
- Auth State Dependency - Wait before data ops
- UI Affordance - Always Visible Actions

## State Management

### ReviewUiState
```kotlin
data class ReviewUiState(
    val reviewMode: ReviewMode = ReviewMode.SOLO,
    val currentStep: ReviewStep = ReviewStep.RATING,
    val currentWeek: Week? = null,
    val overallRating: Int? = null,
    val overallNote: String = "",
    val tasksToReview: List<Task> = emptyList(),
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, TaskStatus> = emptyMap(),
    val taskNotes: Map<String, String> = emptyMap(),
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class ReviewMode { SOLO, TOGETHER }
enum class ReviewStep { MODE_SELECT, RATING, TASK_REVIEW, SUMMARY }
```

## File Structure Plan
- 10-12 UI files for steps and components
- 3 presentation files (ViewModel, State, Events)
- Reuse existing Task and Week repositories
```

#### Tasks Prompt

```
/speckit.tasks

Generate implementation tasks for Feature 005: Week Review.

## Task Organization Requirements

1. **Group by User Story** - Each US should have its own phase
2. **Dependency Order** - Models/state before ViewModel, ViewModel before UI
3. **Parallel Markers** - Use [P] for tasks that can run concurrently
4. **Precise File Paths** - Include exact file locations
5. **Validation Checkpoints** - Add verification tasks after each phase

## Required Phases

### Phase 1: Data Layer Extensions
- Add `reviewedAt` to Week entity if needed
- Add `overallRating` and `reviewNote` to Week entity if needed
- Add streak calculation utility

### Phase 2: Presentation Layer
- ReviewUiState data class
- ReviewEvent sealed class
- ReviewViewModel with CORRECT initialization sequence (see plan.md)
- **CHECKPOINT**: ViewModel unit tests pass

### Phase 3: UI Components [P]
- ReviewBanner composable
- EmojiRatingSelector composable
- TaskOutcomeCard composable
- OverallRatingStep composable
- TaskReviewStep composable
- ReviewSummaryStep composable
- **CHECKPOINT**: Preview renders correctly

### Phase 4: Navigation Integration
- Review navigation graph
- Integration with Week Tab (banner + navigation)
- **CHECKPOINT**: Navigation flow works

### Phase 5: E2E Verification
- E2E-001: Start review → Shows all tasks for current week
- E2E-002: Rate week → Rating saved to Week entity
- E2E-003: Mark task outcomes → Status updated correctly
- E2E-004: Complete review → `reviewedAt` is set

## Critical Implementation Notes

Include these in relevant tasks:
1. ViewModel init MUST wait for AuthState.Authenticated before calling repositories
2. ViewModel init MUST call `getOrCreateCurrentWeek(userId)` before any week operations
3. Side effects Channel MUST have only ONE collector in the UI
4. All action buttons MUST have VISIBLE touch targets ≥48dp
```

---

### 3.7 Feature 006: Partner System

#### Specify Prompt

```
/speckit.specify

Feature: Partner System

## Overview
Implement partner invitation, connection, and real-time synchronization between coupled accounts.

## User Stories

### US1: Invite Partner
As a user, I can generate an invite link to send to my partner.

### US2: Accept Invitation
As an invited user, I can open the link and connect with my partner.

### US3: View Partner Status
As a connected user, I see my partner's connection status.

### US4: Request Task
As a user, I can request my partner to do a task.

### US5: Real-time Sync
As a user, I see my partner's task completions appear in real-time.

## Screens

### Invite Partner (from Onboarding or Settings)
- Explanation text
- [Invite Partner] button triggers share sheet
- "I'll do this later" option

### Partner Landing (from invite link)
- Inviter's name displayed
- Preview of inviter's current tasks
- Sign up / Sign in options
- Auto-connect after auth

### Connected Confirmation
- Success checkmark
- "You're connected with [Name]!"
- Partner's week preview
- [Plan Your Week] CTA

### Request Task Modal
- Task title input
- Optional note input
- "Alex will see this as a request" helper text
- [Send Request] button

### Partner Status (in Settings)
- Partner name and status
- [Disconnect] option (with confirmation)

## Functional Requirements

### Invitation
- R1.1: Generate unique invite code/link
- R1.2: Link format: Tandem.app/invite/[code]
- R1.3: Use system share sheet
- R1.4: Code expires after 7 days
- R1.5: One active invite at a time

### Connection
- R2.1: Validate invite code on open
- R2.2: Link accounts after both authenticated
- R2.3: Show error if code invalid/expired
- R2.4: Prevent self-invitation

### Task Requests
- R3.1: Create task with PENDING_ACCEPTANCE status
- R3.2: Assign to partner as owner
- R3.3: Set createdBy to requester
- R3.4: Include optional note

### Sync
- R4.1: Real-time updates for task changes (Supabase)
- R4.2: Sync task completions within 2 seconds
- R4.3: Offline queue for sync when reconnected

## Data Model Additions

### Partnership Entity
```kotlin
data class Partnership(
    val id: String,
    val user1Id: String,
    val user2Id: String,
    val createdAt: Instant,
    val status: PartnershipStatus
)

enum class PartnershipStatus { ACTIVE, DISSOLVED }
```

### Invite Entity
```kotlin
data class Invite(
    val code: String,
    val creatorId: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val acceptedBy: String?,
    val status: InviteStatus
)

enum class InviteStatus { PENDING, ACCEPTED, EXPIRED }
```

## Integration Points (from Previous Features)

### From Feature 001 (Core Infrastructure):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `authRepository.authState` | Get current user | On ViewModel init | Must wait for `AuthState.Authenticated` |
| `authRepository.currentUser` | Get user details for invite | Generate invite | User authenticated |

### From Feature 002 (Task Data Layer):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `taskRepository.createTask(task)` | Create task request | User sends request | User + partner both exist |
| `taskRepository.observeTasksByStatus(PENDING_ACCEPTANCE)` | Show pending requests | Partner views requests | Partner connected |

### New Repositories Required:
| Repository | Purpose | Methods |
|------------|---------|---------|
| `PartnerRepository` | Manage partnership lifecycle | `getPartner()`, `observePartner()`, `createPartnership()`, `dissolvePartnership()` |
| `InviteRepository` | Manage invite codes | `createInvite()`, `validateInvite()`, `acceptInvite()` |

## UI Affordance Requirements

| Action | Primary Method | Secondary Method | Accessibility |
|--------|----------------|------------------|---------------|
| Generate invite | Visible "Invite Partner" button | - | Touch target ≥48dp |
| Accept invite | Visible "Accept" button | - | Touch target ≥48dp |
| Send task request | Visible "Send Request" button | Keyboard Done | Touch target ≥48dp |
| Disconnect | Visible "Disconnect" button with confirmation dialog | - | Touch target ≥48dp |

## E2E Verification Checklist
- [ ] E1: Generate invite → Invite code created in database
- [ ] E2: Share invite → System share sheet opens with correct link
- [ ] E3: Accept invite → Partnership created, both users see each other
- [ ] E4: Send task request → Task appears for partner with PENDING_ACCEPTANCE status
- [ ] E5: Real-time sync → Task completion reflects within 2 seconds

## Success Criteria
- Can generate and share invite link
- Partner can accept and connect
- Task requests appear for partner
- Real-time sync works for task updates
```

#### Plan Prompt

```
/speckit.plan

Use Jetpack Compose with Material Design 3, Supabase Realtime, and the established architecture:

## Tech Stack
- Kotlin 2.1+ with Compose Multiplatform
- Supabase Android SDK for realtime sync
- Koin for dependency injection
- SQLDelight for local cache
- Deep linking for invite acceptance
- kotlinx.datetime for invite expiration

## Architecture

### Module Structure
```
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/Partnership.kt, Invite.kt
│   └── repository/PartnerRepository.kt, InviteRepository.kt
└── data/
    └── repository/PartnerRepositoryImpl.kt, InviteRepositoryImpl.kt

composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/partner/
│   ├── InvitePartnerScreen.kt
│   ├── PartnerLandingScreen.kt
│   ├── RequestTaskSheet.kt
│   └── PartnerStatusCard.kt
└── presentation/partner/
    ├── PartnerViewModel.kt
    └── PartnerUiState.kt
```

## Initialization Sequence (CRITICAL)

```kotlin
// PartnerViewModel.init
viewModelScope.launch {
    // 1. Wait for authentication
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Check for existing partnership
    val partner = partnerRepository.getPartner(userId)

    // 3. Setup realtime subscription if partner exists
    if (partner != null) {
        setupRealtimeSync(partner.id)
    }

    // 4. Update state
    _uiState.update {
        it.copy(
            partner = partner,
            hasPartner = partner != null,
            isLoading = false
        )
    }
}
```

## Supabase Realtime Setup

```kotlin
// Setup realtime subscription for partner's task updates
private fun setupRealtimeSync(partnerId: String) {
    supabase.realtime
        .channel("partner-tasks")
        .on<Task>("tasks") { event ->
            when (event) {
                is Insert -> handleTaskCreated(event.record)
                is Update -> handleTaskUpdated(event.record)
                is Delete -> handleTaskDeleted(event.oldRecord)
            }
        }
        .subscribe()
}
```

## Implementation Patterns (CRITICAL)

(Same patterns as Features 004-005)
- Side Effect Channel - Single Collector Only
- Auth State Dependency - Wait before data ops
- UI Affordance - Always Visible Actions
```

#### Tasks Prompt

```
/speckit.tasks

Generate implementation tasks for Feature 006: Partner System.

## Task Organization Requirements

1. **Group by User Story** - Each US should have its own phase
2. **Dependency Order** - Models → Repositories → ViewModel → UI
3. **Parallel Markers** - Use [P] for tasks that can run concurrently
4. **Precise File Paths** - Include exact file locations
5. **Validation Checkpoints** - Add verification tasks after each phase

## Required Phases

### Phase 1: Data Layer
- Partnership and Invite domain models
- PartnerRepository interface + implementation
- InviteRepository interface + implementation
- SQLDelight tables for local cache
- **CHECKPOINT**: Repository unit tests pass

### Phase 2: Supabase Integration
- Supabase realtime channel setup
- Invite code generation (server-side function or client)
- Deep link handling for invite acceptance
- **CHECKPOINT**: Realtime sync works

### Phase 3: Presentation Layer
- PartnerUiState data class
- PartnerEvent sealed class
- PartnerViewModel with CORRECT initialization sequence
- **CHECKPOINT**: ViewModel unit tests pass

### Phase 4: UI Components [P]
- InvitePartnerScreen (with share sheet)
- PartnerLandingScreen (deep link target)
- RequestTaskSheet (with visible submit button)
- PartnerStatusCard
- **CHECKPOINT**: Preview renders correctly

### Phase 5: E2E Verification
- E2E-001: Generate invite → Code in database
- E2E-002: Accept invite → Partnership created
- E2E-003: Task request → Appears for partner
- E2E-004: Realtime sync → Updates reflect in <2s

## Critical Implementation Notes

1. ViewModel init MUST wait for AuthState.Authenticated
2. Realtime subscription MUST be setup only after auth confirmed
3. Deep link handler MUST validate invite before proceeding
4. All action buttons MUST have VISIBLE touch targets ≥48dp
```

---

### 3.8 Feature 007: Goals System

#### Specify Prompt

```
/speckit.specify

Feature: Goals System

## Overview
Implement long-term goals that span multiple weeks, with progress tracking and task linking.

## User Stories

### US1: View Goals
As a user, I see my personal and shared goals in the Goals tab.

### US2: Create Goal
As a user, I can create a new goal with a name, type, and duration.

### US3: Track Progress
As a user, I see visual progress toward each goal.

### US4: Link Tasks to Goals
As a user, tasks related to a goal show their goal connection.

### US5: Shared Goals
As a user, my partner and I can have shared goals.

## Screens

### Goals Tab
- Segmented control: Yours | Shared
- Goal cards with progress bars
- [+ Add Goal] button

### Goal Card (Composable)
- Icon and title
- Target description
- Progress bar with fraction
- "This week" indicator

### Add/Edit Goal Sheet
- Name input
- Icon selector (emoji grid)
- Type selector (Weekly Habit / Recurring Task / Target Amount)
- Duration selector (4, 8, 12 weeks or Ongoing)
- Shared toggle
- [Save] button

### Goal Detail Screen
- Full goal info
- Week-by-week progress history
- Linked tasks list
- Edit/Delete options

## Functional Requirements

### Goal Types
- R1.1: Weekly Habit - "Do X times per week"
- R1.2: Recurring Task - "Complete each week"
- R1.3: Target Amount - "Reach N over time"

### Goal Tracking
- R2.1: Calculate progress from linked task completions
- R2.2: Weekly rollup for habits
- R2.3: Cumulative for targets

### Task-Goal Linking
- R3.1: Optional goal reference on tasks
- R3.2: Show goal badge on linked tasks
- R3.3: Completing task updates goal progress

### Week Planning Integration
- R4.1: Suggest tasks based on active goals
- R4.2: Show "Based on your goals" section in planning

## Data Model

### Goal Entity
```kotlin
data class Goal(
    val id: String,
    val name: String,
    val icon: String,           // Emoji
    val type: GoalType,
    val targetPerWeek: Int?,    // For weekly habit
    val targetTotal: Int?,      // For target amount
    val currentProgress: Int,
    val durationWeeks: Int?,    // null = ongoing
    val startWeekId: String,
    val ownerId: String,        // User ID or "shared"
    val isShared: Boolean,
    val createdAt: Instant
)

enum class GoalType { WEEKLY_HABIT, RECURRING_TASK, TARGET_AMOUNT }
```

## Integration Points (from Previous Features)

### From Feature 001 (Core Infrastructure):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `authRepository.authState` | Get current user | On ViewModel init | Must wait for `AuthState.Authenticated` |

### From Feature 002 (Task Data Layer):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `taskRepository.observeTasksForWeek(weekId)` | Get tasks linked to goal | Calculate progress | Week exists |
| `taskRepository.updateTask()` with `linkedGoalId` | Link task to goal | User links task | Task + Goal exist |

### From Feature 006 (Partner System):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `partnerRepository.getPartner()` | Check for shared goal visibility | Load shared goals | User authenticated |

### New Repositories Required:
| Repository | Purpose | Methods |
|------------|---------|---------|
| `GoalRepository` | Manage goals lifecycle | `createGoal()`, `observeGoals()`, `updateProgress()`, `deleteGoal()` |

## UI Affordance Requirements

| Action | Primary Method | Secondary Method | Accessibility |
|--------|----------------|------------------|---------------|
| Create goal | Visible "+" FAB or button | - | Touch target ≥48dp |
| Save goal | Visible "Save" button | - | Touch target ≥48dp |
| Link task to goal | Goal picker in task edit | - | Touch target ≥48dp |

## E2E Verification Checklist
- [ ] E1: Create goal → Goal appears in Goals tab
- [ ] E2: Complete linked task → Goal progress updates
- [ ] E3: Create shared goal → Partner can see it
- [ ] E4: Goal duration expires → Goal marked complete

## Success Criteria
- Can create and view goals
- Progress updates when tasks completed
- Tasks can be linked to goals
- Shared goals visible to both partners
```

#### Plan Prompt

```
/speckit.plan

Use Jetpack Compose with Material Design 3 and the established architecture:

## Tech Stack
- Kotlin 2.1+ with Compose Multiplatform
- Koin for dependency injection
- SQLDelight for Goal persistence
- kotlinx.datetime for week calculations

## Architecture

### Module Structure
```
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/Goal.kt
│   └── repository/GoalRepository.kt
└── data/
    └── repository/GoalRepositoryImpl.kt

composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/goals/
│   ├── GoalsScreen.kt
│   ├── GoalCard.kt
│   ├── AddGoalSheet.kt
│   ├── GoalDetailScreen.kt
│   └── components/
│       ├── EmojiPicker.kt
│       └── ProgressBar.kt
└── presentation/goals/
    ├── GoalsViewModel.kt
    └── GoalsUiState.kt
```

## Initialization Sequence (CRITICAL)

```kotlin
// GoalsViewModel.init
viewModelScope.launch {
    // 1. Wait for authentication
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Check for partner (for shared goals visibility)
    val partner = partnerRepository.getPartner(userId)

    // 3. Observe all goals (personal + shared if partner exists)
    goalRepository.observeGoals(userId, partner?.id)
        .collect { goals ->
            _uiState.update {
                it.copy(
                    personalGoals = goals.filter { !it.isShared },
                    sharedGoals = goals.filter { it.isShared },
                    isLoading = false
                )
            }
        }
}
```

## Implementation Patterns (CRITICAL)

(Same patterns as Features 004-006)
- Side Effect Channel - Single Collector Only
- Auth State Dependency - Wait before data ops
- UI Affordance - Always Visible Actions
```

#### Tasks Prompt

```
/speckit.tasks

Generate implementation tasks for Feature 007: Goals System.

## Task Organization Requirements

1. **Group by User Story** - Each US should have its own phase
2. **Dependency Order** - Models → Repositories → ViewModel → UI
3. **Parallel Markers** - Use [P] for tasks that can run concurrently
4. **Precise File Paths** - Include exact file locations
5. **Validation Checkpoints** - Add verification tasks after each phase

## Required Phases

### Phase 1: Data Layer
- Goal domain model with GoalType enum
- GoalRepository interface + implementation
- SQLDelight Goal table and queries
- Add `linkedGoalId` to Task entity if not present
- **CHECKPOINT**: Repository unit tests pass

### Phase 2: Presentation Layer
- GoalsUiState data class
- GoalsEvent sealed class
- GoalsViewModel with CORRECT initialization sequence
- **CHECKPOINT**: ViewModel unit tests pass

### Phase 3: UI Components [P]
- GoalsScreen (with segment control)
- GoalCard composable
- AddGoalSheet (with emoji picker, type selector)
- GoalDetailScreen
- EmojiPicker composable
- ProgressBar composable
- **CHECKPOINT**: Preview renders correctly

### Phase 4: Task Integration
- Update TaskDetailSheet to show goal picker
- Update TaskUiModel to include goal badge
- **CHECKPOINT**: Task-goal linking works

### Phase 5: E2E Verification
- E2E-001: Create goal → Appears in list
- E2E-002: Complete linked task → Progress updates
- E2E-003: Shared goal → Visible to partner

## Critical Implementation Notes

1. ViewModel init MUST wait for AuthState.Authenticated
2. Check for partner before loading shared goals
3. Goal progress calculation must be reactive (update on task completion)
4. All action buttons MUST have VISIBLE touch targets ≥48dp
```

---

### 3.9 Feature 008: Progress & Insights

#### Specify Prompt

```
/speckit.specify

Feature: Progress & Insights

## Overview
Implement the Progress tab showing trends, insights, past weeks, and streak information.

## User Stories

### US1: View Streak
As a user, I see my current review streak with my partner.

### US2: View Completion Trends
As a user, I see my completion rate over time compared to my partner.

### US3: View Past Weeks
As a user, I can browse and review past weeks.

### US4: View Week Detail
As a user, I can tap a past week to see the full review.

## Screens

### Progress Tab
- Streak card (prominent)
- This Month section with completion bars
- Weekly trend line chart
- Past Weeks list

### Streak Card
- Fire emoji and streak count
- Streak description message
- Celebration for milestones (5, 10, 20 weeks)

### Completion Comparison
- Horizontal bar: You vs Partner
- Percentage labels
- This month timeframe

### Trend Chart
- Line chart (4-8 weeks)
- Y-axis: completion %
- Two lines: You and Partner

### Past Weeks List
- Week date range
- Completion stats (You: 6/8, Partner: 5/6)
- Mood emojis from reviews
- Tap to expand

### Past Week Detail
- Side-by-side partner summaries
- Both review notes
- Task list with outcomes

## Functional Requirements

### Streak
- R1.1: Calculate from consecutive weeks both reviewed
- R1.2: Reset on missed review
- R1.3: Milestone celebrations at 5, 10, 20, 50

### Trends
- R2.1: Query past 8 weeks of data
- R2.2: Calculate completion % per week per user
- R2.3: Display as line chart

### Past Weeks
- R3.1: Paginated list (load 10 at a time)
- R3.2: Show summary stats
- R3.3: Navigate to detail view

## Integration Points (from Previous Features)

### From Feature 001 (Core Infrastructure):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `authRepository.authState` | Get current user | On ViewModel init | Must wait for `AuthState.Authenticated` |

### From Feature 002 (Task Data Layer):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `weekRepository.observePastWeeks(userId, limit, offset)` | Get historical weeks | Load past weeks | User authenticated |
| `taskRepository.observeTasksForWeek(weekId)` | Get tasks for detail view | View past week | Week exists |

### From Feature 005 (Week Review):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| Streak calculation utility | Get current streak | Load progress | User has reviewed weeks |

### From Feature 006 (Partner System):
| Method/API | Purpose | When Called | Preconditions |
|------------|---------|-------------|---------------|
| `partnerRepository.getPartner()` | Get partner for comparison | Load trends | User authenticated |

## UI Affordance Requirements

| Action | Primary Method | Secondary Method | Accessibility |
|--------|----------------|------------------|---------------|
| View past week | Visible list item tap | - | Touch target ≥48dp |
| Load more weeks | Visible "Load More" or infinite scroll | - | Loading indicator |

## E2E Verification Checklist
- [ ] E1: View Progress tab → Streak displays correctly
- [ ] E2: Complete review → Streak count increases
- [ ] E3: View past weeks → Historical data loads
- [ ] E4: Tap past week → Detail view shows tasks and ratings

## Success Criteria
- Streak displays correctly
- Trend chart shows historical data
- Can browse and view past weeks
- Milestone celebrations appear
```

#### Plan Prompt

```
/speckit.plan

Use Jetpack Compose with Material Design 3 and the established architecture:

## Tech Stack
- Kotlin 2.1+ with Compose Multiplatform
- Vico or similar for charts (or custom Canvas)
- Koin for dependency injection
- SQLDelight for data access
- kotlinx.datetime for week calculations

## Architecture

### Module Structure
```
composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/progress/
│   ├── ProgressScreen.kt
│   ├── StreakCard.kt
│   ├── CompletionBars.kt
│   ├── TrendChart.kt
│   ├── PastWeeksList.kt
│   └── PastWeekDetail.kt
└── presentation/progress/
    ├── ProgressViewModel.kt
    └── ProgressUiState.kt
```

## Initialization Sequence (CRITICAL)

```kotlin
// ProgressViewModel.init
viewModelScope.launch {
    // 1. Wait for authentication
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Get partner for comparison (may be null)
    val partner = partnerRepository.getPartner(userId)

    // 3. Calculate current streak
    val streak = calculateStreak(userId, partner?.id)

    // 4. Load past 8 weeks for trend chart
    val trendData = weekRepository
        .getPastWeeks(userId, limit = 8)
        .map { calculateCompletionRate(it) }

    // 5. Load initial past weeks page
    val pastWeeks = weekRepository
        .observePastWeeks(userId, limit = 10, offset = 0)
        .first()

    // 6. Update state
    _uiState.update {
        it.copy(
            streak = streak,
            trendData = trendData,
            pastWeeks = pastWeeks,
            partnerId = partner?.id,
            isLoading = false
        )
    }
}
```

## Implementation Patterns (CRITICAL)

(Same patterns as Features 004-007)
- Side Effect Channel - Single Collector Only
- Auth State Dependency - Wait before data ops
- UI Affordance - Always Visible Actions
```

#### Tasks Prompt

```
/speckit.tasks

Generate implementation tasks for Feature 008: Progress & Insights.

## Task Organization Requirements

1. **Group by User Story** - Each US should have its own phase
2. **Dependency Order** - Models → Repositories → ViewModel → UI
3. **Parallel Markers** - Use [P] for tasks that can run concurrently
4. **Precise File Paths** - Include exact file locations
5. **Validation Checkpoints** - Add verification tasks after each phase

## Required Phases

### Phase 1: Data Layer Extensions
- Add `observePastWeeks(userId, limit, offset)` to WeekRepository if needed
- Add completion rate calculation utility
- **CHECKPOINT**: Query returns correct data

### Phase 2: Presentation Layer
- ProgressUiState data class
- ProgressEvent sealed class
- ProgressViewModel with CORRECT initialization sequence
- **CHECKPOINT**: ViewModel unit tests pass

### Phase 3: UI Components [P]
- ProgressScreen (main container)
- StreakCard composable (with milestone celebration)
- CompletionBars composable (You vs Partner)
- TrendChart composable (line chart)
- PastWeeksList composable (with pagination)
- PastWeekDetail screen
- **CHECKPOINT**: Preview renders correctly

### Phase 4: Navigation Integration
- Add Progress tab to bottom navigation (if not done)
- Navigation to past week detail
- **CHECKPOINT**: Navigation flow works

### Phase 5: E2E Verification
- E2E-001: View Progress → Streak displays
- E2E-002: View trends → Chart shows data
- E2E-003: View past weeks → List loads
- E2E-004: Tap past week → Detail shows

## Critical Implementation Notes

1. ViewModel init MUST wait for AuthState.Authenticated
2. Partner may be null - handle gracefully (no comparison shown)
3. Pagination must work correctly for past weeks
4. All interactive elements MUST have VISIBLE touch targets ≥48dp
```

---

## Part 4: Execution Workflow

### 4.1 For Each Feature

Run these commands in Claude Code in sequence:

```bash
# 1. Create specification (WHAT you're building, not HOW)
/speckit.specify
[Paste the specify prompt for the feature]

# 2. Clarify ambiguities (REQUIRED - prevents expensive implementation mistakes)
/speckit.clarify

# 3. Create technical plan (NOW specify tech stack and architecture)
/speckit.plan
[Paste the plan prompt for the feature]

# 4. Generate tasks
/speckit.tasks

# 5. Cross-artifact analysis (ensures spec ↔ plan ↔ tasks alignment)
/speckit.analyze

# 6. Pre-Implementation Audit (CRITICAL - see prompts below)
# Run the audit prompts in section 4.1.1

# 7. Implement
/speckit.implement
```

### 4.1.1 Pre-Implementation Audit Prompts

**CRITICAL**: Run these prompts BEFORE `/speckit.implement` to catch issues early. Copy and paste each prompt to Claude Code.

#### Audit 1: Task Sequence Validation
```
Audit the implementation plan (plan.md) and tasks (tasks.md) for this feature.

Read through with an eye on determining:
1. Is there a clear, logical sequence of tasks?
2. Are dependencies between tasks explicit (e.g., "depends on T001")?
3. Does each task reference the appropriate section in plan.md where implementation details can be found?
4. Are there any obvious missing steps between tasks?

For each task, verify:
- The file path exists or the parent directory exists
- The task has enough context to be implemented independently
- Any referenced utilities, types, or APIs from previous features are specified

Report any gaps or ambiguities you find.
```

#### Audit 2: Integration Point Verification
```
Cross-reference the Integration Points table in spec.md with the implementation tasks in tasks.md.

For EACH method listed in the Integration Points table:
1. Find the task(s) that will CALL this method
2. Verify the preconditions are satisfied by earlier tasks
3. Check that the "When Called" timing matches the task sequence

Flag any Integration Points that:
- Are not referenced in any task
- Have preconditions that no task satisfies
- Are called before their preconditions are met
```

#### Audit 3: Initialization Sequence Check (for Features 004+)
```
Compare the Initialization Sequence in plan.md with the ViewModel implementation tasks in tasks.md.

Verify:
1. Is there a task that implements the EXACT initialization sequence from plan.md?
2. Does the task explicitly mention:
   - Waiting for AuthState.Authenticated
   - Calling getOrCreateCurrentWeek(userId) before observing
   - The correct order of async operations
3. Are the required imports (filterIsInstance, first) mentioned?

If the initialization sequence is not explicitly captured in a task, add specific sub-steps.
```

#### Audit 4: UI Affordance Verification
```
Cross-reference the UI Affordance Requirements table in spec.md with the UI tasks in tasks.md.

For EACH action in the table:
1. Find the UI component task that implements it
2. Verify the task mentions:
   - A VISIBLE button or touch target (not keyboard-only)
   - Touch target ≥48dp requirement
   - Accessibility content description

Flag any UI actions that rely solely on keyboard input without a visible affordance.
```

#### Audit 5: E2E Verification Readiness
```
Review the E2E Verification Checklist in spec.md.

For each E2E test:
1. Identify which tasks must be complete for this test to pass
2. Verify there are no missing implementation steps
3. Check if the test requires database verification (ADB commands)

Create a mapping: E2E Test → Required Tasks → Verification Method
```

### 4.1.2 Post-Audit Fixes

If audits reveal issues:
```
Based on the audit findings, update tasks.md to:
1. Add missing tasks for uncovered Integration Points
2. Add explicit sub-steps for the Initialization Sequence
3. Add UI affordance requirements to component tasks
4. Reorder tasks if dependencies are incorrect

Do NOT proceed with /speckit.implement until all audit issues are resolved.
```

### 4.1.3 Post-Implementation Verification

### 4.1.1 Post-Implementation Verification

After each feature, run manual E2E verification:
```bash
# Build and run
./gradlew :composeApp:assembleDebug

# Test FULL user flows, not just unit tests:
# - Create item via UI → Verify appears in list
# - Verify database state via ADB if needed
# - Test across app restarts
```

### 4.2 Between Features

After completing each feature:
1. Run the app and test manually
2. Run unit tests: `./gradlew :shared:test :composeApp:testDebugUnitTest`
3. Fix any issues before proceeding
4. Commit to version control
5. Move to next feature

### 4.3 Recommended Session Structure

**Session 1: Foundation**
- Feature 001: Core Infrastructure (full cycle)
- Feature 002: Task Data Layer (full cycle)

**Session 2: Week View**
- Feature 003: Week View (full cycle)
- Manual testing and polish

**Session 3: Planning & Review**
- Feature 004: Week Planning (full cycle)
- Feature 005: Week Review (full cycle)

**Session 4: Partner Features**
- Feature 006: Partner System (full cycle)

**Session 5: Goals & Progress**
- Feature 007: Goals System (full cycle)
- Feature 008: Progress & Insights (full cycle)

---

## Part 5: Lessons Learned from Feature 003

The following issues were discovered during Feature 003 implementation. These lessons inform the improved prompts for Feature 004+.

### 5.1 Issue Categories

| Category | Gap | Evidence | Impact |
|----------|-----|----------|--------|
| **Integration** | Contract defined but not invoked | `getOrCreateCurrentWeek()` in contract but not in plan | Week never created, tasks don't appear |
| **Async State** | Dependencies not formalized | No mention of waiting for auth before data ops | Race condition, empty results |
| **Pattern Guidance** | Implementation pattern omitted | Channel single-collector constraint | Side effects swallowed |
| **UI Affordance** | Action method under-specified | "Submit" without specifying visible button | Poor discoverability |
| **Verification** | No E2E integration tests | Only ViewModel unit tests in tasks.md | Bugs found only at runtime |

### 5.2 Root Cause: Contract-Plan Disconnect

**In `specs/002-task-data-layer/contracts/week-repository.md`**:
```kotlin
/**
 * Get the current week, creating it if it doesn't exist.
 * @param userId The user's ID
 * @return The current week (guaranteed non-null)
 */
suspend fun getOrCreateCurrentWeek(userId: String): Week
```

**But in `specs/003-week-view/plan.md`**, the Data Flow only showed:
```
WeekViewModel → WeekRepository (observeWeek)  // ← observe-only!
```

The gap: `getOrCreateCurrentWeek()` was documented but never specified to be CALLED.

### 5.3 Root Cause: Task Definition Too Vague

**In `specs/003-week-view/tasks.md`**:
```
- [X] T009 Implement loadInitialData() and observeWeek() in WeekViewModel
```

**What was needed**:
```
- [ ] T009 Implement loadInitialData() in WeekViewModel:
  - Wait for AuthState.Authenticated from authRepository.authState
  - Call weekRepository.getOrCreateCurrentWeek(userId) to ensure week exists
  - Then begin observing the week via observeWeek(currentWeekId)
```

### 5.4 Required Improvements for Future Features

1. **Integration Points Section**: Each `/speckit.specify` prompt must explicitly list which methods from previous features are needed and WHEN to call them.

2. **Initialization Sequence Section**: Each `/speckit.plan` prompt must include a sequence diagram or code showing the exact async operation order.

3. **Implementation Patterns Section**: Document platform-specific patterns like Channel single-collector, auth state waiting, etc.

4. **E2E Verification Tasks**: Each `/speckit.tasks` must include end-to-end verification tasks, not just unit tests.

---

## Part 6: Tips for Success

### 6.1 Keep Context Clean

- Use `/compact` between major features
- Reference the PRD when Claude needs context
- Keep constitution.md as the stable anchor

### 6.2 Iterate on Specs

Don't treat the first `/speckit.specify` output as final:
- Read the generated spec.md
- Ask Claude to refine unclear sections
- Use `/speckit.clarify` liberally

### 6.3 Test Incrementally

After each `/speckit.implement`:
- Build the app: `./gradlew :composeApp:assembleDebug`
- Run on device/emulator
- Test the specific feature
- Fix bugs before moving on

### 6.4 Handle Failures Gracefully

If implementation fails:
1. Check the error message
2. Ask Claude to fix the specific issue
3. Don't restart the entire feature
4. Use targeted prompts for fixes

### 6.5 Version Control Checkpoints

Commit after each feature completion:
```bash
git add .
git commit -m "feat(001): Core Infrastructure complete"
```

---

## Appendix: Quick Reference

### Spec Kit Commands
| Command | Purpose |
|---------|---------|
| `/speckit.constitution` | Define core principles |
| `/speckit.specify` | Create feature specification (WHAT/WHY, not HOW) |
| `/speckit.clarify` | Resolve ambiguities (REQUIRED before plan) |
| `/speckit.plan` | Create technical plan (tech stack + architecture) |
| `/speckit.tasks` | Generate implementation tasks |
| `/speckit.analyze` | Validate consistency (spec ↔ plan ↔ tasks) |
| `/speckit.implement` | Execute implementation |

### Feature Build Order
1. Core Infrastructure → 2. Task Data Layer → 3. Week View → 4. Week Planning → 5. Week Review → 6. Partner System → 7. Goals System → 8. Progress & Insights

### Key Files
- `.specify/memory/constitution.md` - Core principles
- `specs/XXX-feature-name/spec.md` - Feature specification
- `specs/XXX-feature-name/plan.md` - Technical plan
- `specs/XXX-feature-name/tasks.md` - Implementation tasks

### Required Sections for Feature 004+ Prompts

#### In Specify Prompts:
1. **Integration Points** - Table of methods from previous features with When Called and Preconditions
2. **UI Affordance Requirements** - Table of actions with Primary Method, Secondary Method, Accessibility
3. **E2E Verification Checklist** - Specific tests to run after implementation

#### In Plan Prompts:
1. **Initialization Sequence (CRITICAL)** - Exact async operation order with code example
2. **Implementation Patterns (CRITICAL)** - Platform-specific constraints (Channel single-collector, auth waiting)

#### In Tasks Prompts:
1. **Validation Checkpoints** - After each phase
2. **E2E Verification Phase** - End-to-end tests as final phase
3. **Critical Implementation Notes** - Reminders of patterns that MUST be followed

### Critical Implementation Patterns

```kotlin
// 1. ALWAYS wait for auth before data operations
authRepository.authState
    .filterIsInstance<AuthState.Authenticated>()
    .first()
    .let { authState ->
        // Now safe to call repositories
    }

// 2. ALWAYS ensure Week exists before any week operations
weekRepository.getOrCreateCurrentWeek(userId)

// 3. Side effect Channel - SINGLE collector only
LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            // ALL effects handled in ONE collector
        }
    }
}

// 4. UI actions - ALWAYS visible buttons (not keyboard-only)
trailingIcon = {
    IconButton(onClick = onSubmit, enabled = text.isNotBlank()) {
        Icon(Icons.AutoMirrored.Filled.Send, "Submit")
    }
}
```
