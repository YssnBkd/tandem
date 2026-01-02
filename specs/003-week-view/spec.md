# Feature Specification: Week View

**Feature Branch**: `003-week-view`
**Created**: 2026-01-02
**Status**: Draft
**Input**: User description: "Implement the main Week tab screen where users view and manage their tasks for the current week, with segment navigation, quick task addition, task completion with feedback, and real-time progress tracking."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View My Weekly Tasks (Priority: P1)

As a user, I want to see all my tasks for the current week organized by completion status, so I can understand what I need to accomplish and track my progress.

**Why this priority**: This is the core functionality of the Week View - without displaying tasks, no other features make sense. Users need visibility into their weekly commitments as the foundation for all other interactions.

**Independent Test**: Can be fully tested by opening the Week tab and verifying tasks appear correctly organized with progress indicator visible. Delivers immediate value by giving users visibility into their week.

**Acceptance Scenarios**:

1. **Given** the user is authenticated and has tasks for the current week, **When** they navigate to the Week tab, **Then** they see their incomplete tasks displayed first, followed by completed tasks (visually faded).

2. **Given** the user is on the Week tab, **When** viewing the screen, **Then** they see the week date range header (e.g., "Week of Dec 30 - Jan 5") and their progress indicator showing "X/Y" completed tasks.

3. **Given** the user has no tasks for the current week, **When** they navigate to the Week tab, **Then** they see an empty state message encouraging them to add tasks.

4. **Given** the user has repeating tasks (e.g., "Gym 3x"), **When** viewing the task list, **Then** they see repeat progress indicators (e.g., "1/3") showing current completion count.

---

### User Story 2 - Complete a Task (Priority: P1)

As a user, I want to mark tasks as complete with satisfying feedback, so I feel a sense of accomplishment and can track my weekly progress.

**Why this priority**: Task completion is the primary action users take - it's the core interaction that drives the weekly productivity loop. Without this, the app cannot fulfill its purpose.

**Independent Test**: Can be tested by tapping a task's checkbox and verifying the completion animation, haptic feedback, and task repositioning to the completed section. Delivers satisfaction and progress tracking.

**Acceptance Scenarios**:

1. **Given** the user is viewing their task list with incomplete tasks, **When** they tap the checkbox on a task, **Then** the task is marked complete with an animated checkmark appearance.

2. **Given** the user completes a task, **When** the completion animation finishes, **Then** the task moves to the bottom of the list with a visual fade effect.

3. **Given** the user completes a task, **When** the task is marked complete, **Then** the progress indicator updates immediately (e.g., "4/8" becomes "5/8").

4. **Given** the user has a repeating task (e.g., "Gym 3x"), **When** they tap the checkbox, **Then** the repeat count increments (e.g., "1/3" becomes "2/3") until fully complete.

---

### User Story 3 - Quick Add Task (Priority: P2)

As a user, I want to quickly add new tasks from the main screen without navigating away, so I can capture tasks as they come to mind without interrupting my flow.

**Why this priority**: Adding tasks is essential but secondary to viewing and completing them. Users need to populate their week, but the viewing experience must work first.

**Independent Test**: Can be tested by entering a task title in the quick-add field and submitting. Delivers immediate value by allowing users to capture tasks quickly.

**Acceptance Scenarios**:

1. **Given** the user is on the Week tab, **When** they view the screen, **Then** they see an always-visible text field for quick task entry at the top of the task list.

2. **Given** the user types a task title in the quick-add field, **When** they press enter/submit, **Then** the task is added to their list for the current week and the field clears.

3. **Given** the user tries to submit an empty task title, **When** they press enter/submit, **Then** they see an error indication and the task is not added.

4. **Given** the user adds a task via quick-add, **When** the task is created, **Then** it appears in the incomplete tasks section and the progress indicator total updates.

---

### User Story 4 - Switch Between Segments (Priority: P2)

As a user, I want to switch between "You", "Partner", and "Shared" segments to view different sets of tasks, so I can see my own tasks, my partner's tasks (read-only), and shared tasks.

**Why this priority**: Segment navigation enables the couple-focused nature of the app but depends on the core task viewing functionality working first.

**Independent Test**: Can be tested by tapping each segment and verifying the task list updates to show the appropriate tasks. Delivers visibility into different task ownership contexts.

**Acceptance Scenarios**:

1. **Given** the user is on the Week tab, **When** they view the screen, **Then** they see a segmented control with "You", "Partner", and "Shared" options.

2. **Given** the user is on the "You" segment, **When** they tap the "Partner" segment, **Then** the task list updates to show their partner's tasks in read-only mode (no checkboxes).

3. **Given** the user is on the "Partner" segment, **When** viewing their partner's tasks, **Then** they see a "Request a Task" button at the bottom.

4. **Given** the user is on the "Shared" segment, **When** viewing a completed shared task, **Then** they see "completed by [partner name]" indicating who completed it.

5. **Given** the user switches to a different segment and later returns, **When** they reopen the Week tab, **Then** the app remembers and restores their last selected segment.

---

### User Story 5 - View and Edit Task Details (Priority: P3)

As a user, I want to tap a task to see its full details and make edits, so I can add notes, update the title, or manage the task more thoroughly.

**Why this priority**: Detailed task editing is an enhancement to the core viewing/completion flow. Most quick interactions don't require the detail view.

**Independent Test**: Can be tested by tapping a task and verifying the detail modal appears with editable fields. Delivers ability to manage task details beyond the list view.

**Acceptance Scenarios**:

1. **Given** the user is viewing their task list, **When** they tap on a task, **Then** a modal/sheet appears showing the task's full details.

2. **Given** the user is viewing task details, **When** they see the detail view, **Then** they can view/edit the title, notes, see status, owner info, and creation/rollover information.

3. **Given** the user is viewing task details for their own incomplete task, **When** they tap "Mark Complete", **Then** the task is marked complete and the modal closes.

4. **Given** the user is viewing task details, **When** they tap the delete option and confirm, **Then** the task is removed from their week.

---

### User Story 6 - Add Task with Details (Priority: P3)

As a user, I want to add a task with additional details like notes, so I can provide context for tasks that need more information.

**Why this priority**: Adding tasks with notes is less common than quick-add and depends on the core add functionality working first.

**Independent Test**: Can be tested by triggering the add task sheet and creating a task with title and notes. Delivers richer task creation for tasks requiring context.

**Acceptance Scenarios**:

1. **Given** the user wants to add a task with details, **When** they trigger the add task sheet, **Then** a bottom sheet appears with title input (required) and notes input (optional).

2. **Given** the user is on the "Shared" segment, **When** they open the add task sheet, **Then** they see an owner selector to choose between "You" and "Shared".

3. **Given** the user fills out the add task form, **When** they tap "Save", **Then** the task is created with all provided details and the sheet closes.

---

### Edge Cases

- What happens when the user tries to complete a partner's task from the Partner segment?
  - The checkbox is not displayed; the task list is read-only

- What happens when the user has no partner connected?
  - The "Partner" segment shows an empty state with messaging about inviting a partner

- What happens when the user pulls to refresh but is offline?
  - The refresh indicator shows briefly, and if sync fails, a subtle error message appears; local data remains displayed

- What happens when a task title is extremely long?
  - The title is truncated in the list view with ellipsis; full title visible in detail view

- What happens when both partners try to complete a shared task simultaneously?
  - The first completion wins; the second partner sees the task already completed with the first partner's name

## Requirements *(mandatory)*

### Functional Requirements

#### Task List Display
- **FR-001**: System MUST display tasks grouped by status with incomplete tasks first and completed tasks last
- **FR-002**: System MUST show task title and visual completion status indicator for each task
- **FR-003**: System MUST show repeat progress indicators (e.g., "1/3") for repeating tasks
- **FR-004**: System MUST support pull-to-refresh gesture to trigger data synchronization
- **FR-005**: System MUST display an empty state message when no tasks exist for the current week

#### Task Completion
- **FR-006**: System MUST allow users to toggle task completion by tapping the checkbox
- **FR-007**: System MUST provide haptic feedback (light impact) on task completion
- **FR-008**: System MUST animate the checkmark appearance when a task is completed
- **FR-009**: System MUST visually move completed tasks to the bottom of the list with a fade effect
- **FR-010**: System MUST increment repeat count for repeating tasks instead of marking fully complete until target reached

#### Quick Add
- **FR-011**: System MUST display an always-visible text field at the top of the task list for quick task entry
- **FR-012**: System MUST create a new task when user submits text in the quick-add field
- **FR-013**: System MUST clear the quick-add field after successful task creation
- **FR-014**: System MUST show validation error if user attempts to submit an empty task title

#### Segment Navigation
- **FR-015**: System MUST display a segmented control with "You", "Partner", and "Shared" options
- **FR-016**: System MUST persist the user's last selected segment and restore it on next visit
- **FR-017**: System MUST display partner's tasks in read-only mode (no checkboxes) on the Partner segment
- **FR-018**: System MUST display a "Request a Task" button on the Partner segment
- **FR-019**: System MUST allow either partner to complete tasks on the Shared segment
- **FR-020**: System MUST display "completed by [name]" for completed shared tasks

#### Task Detail View
- **FR-021**: System MUST display a modal/sheet when user taps on a task
- **FR-022**: System MUST allow editing of task title and notes in the detail view
- **FR-023**: System MUST display task status, owner info, and creation/rollover information
- **FR-024**: System MUST provide a "Mark Complete" action in the detail view
- **FR-025**: System MUST provide a delete option with confirmation in the detail view

#### Add Task Sheet
- **FR-026**: System MUST display a bottom sheet for adding tasks with details
- **FR-027**: System MUST require a non-empty title to save a new task
- **FR-028**: System MUST allow optional notes input when adding a task
- **FR-029**: System MUST show an owner selector (You/Shared) on the Shared segment's add task sheet

#### Week Header & Progress
- **FR-030**: System MUST display the current week's date range in the header (e.g., "Week of Dec 30 - Jan 5")
- **FR-031**: System MUST display progress indicator showing completed/total tasks (e.g., "5/8")
- **FR-032**: System MUST update progress indicator in real-time when tasks are completed or added

### Key Entities

- **Task**: Represents a user's task with title, notes, status, owner type (self/partner/shared), week association, and optional repeat target. Referenced from Feature 002 Task Data Layer.
- **Week**: Represents a calendar week with start/end dates and associated user context. Referenced from Feature 002 Task Data Layer.
- **Segment**: Represents the view filter (You, Partner, Shared) determining which tasks are displayed and interaction mode.
- **Progress**: Calculated value representing completed tasks vs. total tasks for display purposes.

## Assumptions

- **Task Data Layer Available**: Feature 002 (Task Data Layer) is implemented and provides TaskRepository and WeekRepository with required CRUD operations
- **Authentication Available**: Feature 001 (Core Infrastructure) provides authenticated user context and navigation shell
- **Partner Connection Deferred**: Partner data synchronization and real-time sync are handled in Feature 006; this feature assumes local partner task data may be stale or empty
- **Haptic Feedback**: Device supports light impact haptic feedback; graceful degradation if unavailable
- **Week Definition**: Weeks follow ISO 8601 (Monday-Sunday) as established in Task Data Layer
- **Single User Testing**: This feature can be tested without a connected partner; Partner segment will show empty state

## Out of Scope

- Partner invitation and connection flow (Feature 006)
- Real-time synchronization with partner (Feature 006)
- Week review banners and flows (Feature 005)
- Goal linking and display (Feature 007)
- Task rollover from previous weeks (Feature 004)
- Offline sync queue management (handled by data layer)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can view their current week's tasks within 2 seconds of opening the Week tab
- **SC-002**: Users can add a new task via quick-add in under 5 seconds (type and submit)
- **SC-003**: Task completion provides immediate visual and haptic feedback (under 100ms perceived response)
- **SC-004**: Progress indicator updates in real-time when tasks are completed or added (no manual refresh required)
- **SC-005**: Segment switching displays the new task set within 500ms
- **SC-006**: 95% of users can successfully add and complete a task on their first attempt without guidance
- **SC-007**: The Week tab correctly preserves and restores the last selected segment across app sessions
- **SC-008**: Empty states provide clear guidance for users with no tasks or no partner connection
