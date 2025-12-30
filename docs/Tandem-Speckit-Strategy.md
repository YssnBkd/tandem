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
- Minimum Android SDK: 26 (Android 8.0)

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
- Firebase Authentication (Android SDK)
- Jetpack Navigation Compose with type-safe routes
- Koin for dependency injection
- Kotlin Coroutines + Flow for async operations
- DataStore for auth state persistence

## Architecture

### Module Structure
```
shared/
├── commonMain/
│   ├── domain/
│   │   ├── model/User.kt
│   │   ├── repository/AuthRepository.kt
│   │   └── usecase/auth/
│   └── data/
│       └── repository/AuthRepositoryImpl.kt
androidApp/
├── ui/
│   ├── theme/
│   ├── navigation/
│   ├── auth/
│   └── main/
└── di/
```

### Authentication Flow
1. App checks DataStore for persisted auth token
2. If token exists → validate with Firebase → navigate to MainScreen
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
2. Implementing authentication with Firebase
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
shared/
├── commonMain/
│   ├── domain/
│   │   ├── model/Task.kt, Week.kt
│   │   ├── repository/TaskRepository.kt, WeekRepository.kt
│   │   └── usecase/task/, week/
│   └── data/
│       ├── local/
│       │   ├── TandemDatabase.sq
│       │   └── TaskQueries.sq, WeekQueries.sq
│       └── repository/
│           ├── TaskRepositoryImpl.kt
│           └── WeekRepositoryImpl.kt
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
- [Add to This Week] [Skip] buttons
- Progress dots showing remaining

### Step 2: Add New Tasks
- Text input for new task
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
- R3.1: Quick add interface
- R3.2: Tasks default to SELF owner
- R3.3: Validate non-empty title

### Partner Requests
- R4.1: Show tasks with status PENDING_ACCEPTANCE
- R4.2: Accept changes status to PENDING
- R4.3: Discuss placeholder for v1.0 (show toast "Coming soon")

### Completion
- R5.1: Mark week planning as complete (timestamp)
- R5.2: Dismiss banner after completion

## Success Criteria
- Planning flow completes end-to-end
- Rollover tasks can be added or skipped
- New tasks are saved
- Planning completion is persisted
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

## Success Criteria
- Solo review flow completes
- Tasks are marked with outcomes
- Week rating is saved
- Streak is calculated and displayed
- Together mode alternates correctly (v1.1 - basic implementation)
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
- R4.1: Real-time updates for task changes (Firebase Realtime DB or Firestore)
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

## Success Criteria
- Can generate and share invite link
- Partner can accept and connect
- Task requests appear for partner
- Real-time sync works for task updates
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

## Success Criteria
- Can create and view goals
- Progress updates when tasks completed
- Tasks can be linked to goals
- Shared goals visible to both partners
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

## Success Criteria
- Streak displays correctly
- Trend chart shows historical data
- Can browse and view past weeks
- Milestone celebrations appear
```

---

## Part 4: Execution Workflow

### 4.1 For Each Feature

Run these commands in Claude Code in sequence:

```bash
# 1. Create specification
/speckit.specify
[Paste the specify prompt for the feature]

# 2. Clarify ambiguities (optional but recommended)
/speckit.clarify

# 3. Create technical plan
/speckit.plan
[Paste the plan prompt for the feature]

# 4. Generate tasks
/speckit.tasks

# 5. Review and validate
/speckit.analyze

# 6. Implement
/speckit.implement
```

### 4.2 Between Features

After completing each feature:
1. Run the app and test manually
2. Run unit tests: `./gradlew test`
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

## Part 5: Tips for Success

### 5.1 Keep Context Clean

- Use `/compact` between major features
- Reference the PRD when Claude needs context
- Keep constitution.md as the stable anchor

### 5.2 Iterate on Specs

Don't treat the first `/speckit.specify` output as final:
- Read the generated spec.md
- Ask Claude to refine unclear sections
- Use `/speckit.clarify` liberally

### 5.3 Test Incrementally

After each `/speckit.implement`:
- Build the app: `./gradlew assembleDebug`
- Run on device/emulator
- Test the specific feature
- Fix bugs before moving on

### 5.4 Handle Failures Gracefully

If implementation fails:
1. Check the error message
2. Ask Claude to fix the specific issue
3. Don't restart the entire feature
4. Use targeted prompts for fixes

### 5.5 Version Control Checkpoints

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
| `/speckit.specify` | Create feature specification |
| `/speckit.clarify` | Resolve ambiguities |
| `/speckit.plan` | Create technical plan |
| `/speckit.tasks` | Generate implementation tasks |
| `/speckit.analyze` | Validate consistency |
| `/speckit.implement` | Execute implementation |

### Feature Build Order
1. Core Infrastructure → 2. Task Data Layer → 3. Week View → 4. Week Planning → 5. Week Review → 6. Partner System → 7. Goals System → 8. Progress & Insights

### Key Files
- `.specify/memory/constitution.md` - Core principles
- `.specify/specs/XXX-feature/spec.md` - Feature specification
- `.specify/specs/XXX-feature/plan.md` - Technical plan
- `.specify/specs/XXX-feature/tasks.md` - Implementation tasks
