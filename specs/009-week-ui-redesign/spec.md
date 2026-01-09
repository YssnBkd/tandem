# Feature Specification: Week UI Redesign

**Feature Branch**: `009-week-ui-redesign`
**Created**: 2026-01-08
**Status**: Draft
**Input**: User description: "Implement Week View, Add Task, and Task Detail screens from HTML mockups as faithful Android Compose UI. The mockups are located at mockups/screens/01-week-view-v2.html (Week View), mockups/screens/02-add-task.html (Add Task Modal), and mockups/screens/09-task-detail.html (Task Detail Modal). The implementation must faithfully match the visual design while connecting to the existing codebase data layer."

## Overview

This feature implements a visual redesign of the Week View and related screens (Add Task modal, Task Detail modal) based on Todoist-inspired HTML mockups. The goal is to faithfully translate the mockup designs into Android Compose UI while integrating with the existing Task Data Layer (Feature 002) and core infrastructure.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Week with Task List (Priority: P1)

As a user, I want to see my weekly tasks displayed in a clean, organized interface with clear sections for overdue, today, tomorrow, and later tasks, so I can quickly understand what needs my attention.

**Why this priority**: The Week View is the primary screen users interact with. Without this core display, no other features can function. The Todoist-style task list with priority indicators and time grouping is the foundation of the user experience.

**Independent Test**: Can be fully tested by opening the Week tab and verifying tasks appear correctly grouped by time sections (Overdue, Today, Tomorrow, Later this week) with proper priority color indicators and metadata display. Delivers immediate value by giving users a clear view of their weekly commitments.

**Acceptance Scenarios**:

1. **Given** the user is authenticated and has tasks for the current week, **When** they navigate to the Week tab, **Then** they see a header showing "This Week" with date range (e.g., "Jan 5 - 11 · 7 tasks") and a season context chip.

2. **Given** the user has tasks with different due dates, **When** viewing the Week screen, **Then** they see tasks grouped into sections: "Overdue" (red header), "Today", "Tomorrow", and "Later this week".

3. **Given** the user has tasks with different priorities, **When** viewing the task list, **Then** they see color-coded priority indicators: P1 (red), P2 (orange), P3 (blue), P4 (gray border).

4. **Given** the user has completed tasks, **When** viewing the Week screen, **Then** they see a collapsible "Completed" section at the bottom showing completed tasks with strike-through text.

5. **Given** the user views a task row, **When** they see the task, **Then** they see the task title, optional time/schedule, optional recurring indicator, and project/goal tag.

---

### User Story 2 - Complete Tasks with Visual Feedback (Priority: P1)

As a user, I want to mark tasks as complete by tapping the priority checkbox, with satisfying visual feedback that includes animation and automatic movement to the completed section.

**Why this priority**: Task completion is the core interaction that drives user engagement and productivity tracking. The visual feedback (checkbox animation, task moving to completed section) is essential for user satisfaction.

**Independent Test**: Can be tested by tapping a task's checkbox and verifying the completion animation (checkbox pop), checkmark appearance, and task repositioning to the completed section with fade effect.

**Acceptance Scenarios**:

1. **Given** the user sees an incomplete task, **When** they tap the priority checkbox, **Then** the checkbox animates (scale pop effect), fills with the priority color, and displays a checkmark.

2. **Given** the user completes a task, **When** the animation finishes, **Then** the task moves to the "Completed" section with a visual transition.

3. **Given** the user views a completed task, **When** they see the task row, **Then** the task title has strike-through styling and muted colors.

4. **Given** the user has completed tasks, **When** they tap the "Completed" section header, **Then** the section expands/collapses to show/hide completed tasks.

---

### User Story 3 - Navigate Week Days (Priority: P1)

As a user, I want to see a week selector strip showing all days of the week, with the ability to navigate between weeks and select specific days, so I can quickly access tasks for any day.

**Why this priority**: The week navigation strip is prominently featured in the mockup and provides essential navigation context. It shows task indicators for days with tasks and highlights today and the selected day.

**Independent Test**: Can be tested by interacting with the week selector strip, verifying day selection highlights, today indicator, and week navigation arrows functioning correctly.

**Acceptance Scenarios**:

1. **Given** the user views the Week screen, **When** they see the week selector strip, **Then** they see all 7 days (Sun-Sat) with day names and date numbers.

2. **Given** the user views the week selector, **When** today is within the displayed week, **Then** today is highlighted with a distinct background color (primary light) and the date is bold.

3. **Given** a day has tasks, **When** the user views that day in the strip, **Then** a small dot indicator appears below the date number.

4. **Given** the user taps the left/right navigation arrows, **When** they navigate to a different week, **Then** the week display updates with the new date range and day numbers.

5. **Given** the user taps a specific day, **When** they select it, **Then** that day becomes highlighted (primary color background, white text) and the view title updates to show that day's name.

---

### User Story 4 - Filter Tasks by Segment (Priority: P2)

As a user, I want to switch between "You", "Partner", and "Together" segments to view different sets of tasks, so I can see my own tasks, my partner's tasks, and shared tasks separately.

**Why this priority**: The segment control is a key Tandem-specific feature that enables couple-focused task management. It depends on the core task display working first.

**Independent Test**: Can be tested by tapping each segment and verifying the task list updates to show the appropriate filtered tasks.

**Acceptance Scenarios**:

1. **Given** the user is on the Week tab, **When** they view the screen, **Then** they see a three-segment control with "You" (with checkmark icon), "Partner", and "Together" options.

2. **Given** the user taps a different segment, **When** the segment changes, **Then** the task list updates to show tasks matching that ownership filter.

3. **Given** the user selects a segment, **When** they return to the Week tab later, **Then** their last selected segment is preserved.

---

### User Story 5 - Add New Task via FAB (Priority: P2)

As a user, I want to tap a floating action button to open a bottom sheet for adding new tasks with full details (title, description, owner, date, priority, goal, labels), so I can create tasks with all necessary information.

**Why this priority**: Task creation is essential but secondary to viewing and completing tasks. The Add Task bottom sheet provides a full-featured task creation experience.

**Independent Test**: Can be tested by tapping the FAB, verifying the bottom sheet appears with all input fields, and successfully creating a task.

**Acceptance Scenarios**:

1. **Given** the user is on the Week screen, **When** they tap the floating action button (red "+" button), **Then** a bottom sheet slides up with task creation fields.

2. **Given** the Add Task sheet is open, **When** the user views it, **Then** they see: task name input with coral left border, description input, and scrollable action pills (Me, Date, Priority, Goal, Label, Deadline).

3. **Given** the user fills in the task name, **When** they tap "Add Task" button, **Then** the task is created and the sheet dismisses.

4. **Given** the user taps the "Me" pill, **When** the owner popover appears, **Then** they can select between "Me", "Partner", and "Together" with emoji icons.

5. **Given** the user taps the "Priority" pill, **When** the priority popover appears, **Then** they can select between P1 (red), P2 (orange), P3 (blue), and "No priority".

6. **Given** the user taps the "Goal" pill, **When** the goal selector sheet appears, **Then** they see a list of available goals with emoji, name, and progress percentage.

7. **Given** the user taps outside the bottom sheet or the overlay, **When** they dismiss, **Then** the sheet closes and returns to the Week view.

---

### User Story 6 - View Task Details (Priority: P2)

As a user, I want to tap a task to see its full details in a bottom sheet, including description, owner, date, priority, labels, linked goal with progress, sub-tasks, and action buttons.

**Why this priority**: Task detail view enables users to see and edit comprehensive task information. It depends on the task list working first.

**Independent Test**: Can be tested by tapping a task row and verifying the detail sheet appears with all expected sections.

**Acceptance Scenarios**:

1. **Given** the user is viewing the task list, **When** they tap on a task row (not the checkbox), **Then** a bottom sheet slides up showing task details.

2. **Given** the Task Detail sheet is open, **When** the user views it, **Then** they see: project/week indicator, task title with priority checkbox, description, owner row, date row, priority row, and label chips.

3. **Given** the task is linked to a goal, **When** the user views the detail sheet, **Then** they see a goal progress row showing emoji, goal name, weekly progress (e.g., "2 of 3"), progress dots, and a chevron to navigate to goal detail.

4. **Given** the task has sub-tasks, **When** the user views the detail sheet, **Then** they see an expandable "Sub-tasks" section with count (e.g., "1/2"), individual sub-tasks with checkboxes, and an "Add sub-task" option.

5. **Given** the user views the bottom of the detail sheet, **When** they see the action area, **Then** they see a comment input field, a prominent green "Complete" button, and a "Skip" button.

6. **Given** the user expands the detail sheet by dragging up, **When** the sheet is expanded, **Then** it shows additional options as scrollable chips (Deadline, Reminders, Location, Repeat).

---

### User Story 7 - Access Plan and Review Banners (Priority: P3)

As a user, I want to see contextual banners for "Plan your week" and "Review your week" that guide me to planning and review flows at appropriate times.

**Why this priority**: These banners connect to separate features (Planning, Review) and provide contextual prompts. They are important but not core to the Week View functionality.

**Independent Test**: Can be tested by verifying banners appear with correct styling and tap navigation.

**Acceptance Scenarios**:

1. **Given** the user is on the Week screen, **When** a new week starts (planning window open), **Then** they see a purple gradient "Plan your week" banner with sunrise emoji.

2. **Given** the user is on the Week screen, **When** it's the end of the week (review window open), **Then** they see an orange gradient "Review your week" banner with sunset emoji, showing "Friday · Keep your streak going!" and a streak count badge.

3. **Given** the user taps the "Plan your week" banner, **When** they interact, **Then** they navigate to the planning flow (Feature 004).

4. **Given** the user taps the "Review your week" banner, **When** they interact, **Then** they navigate to the review flow (Feature 005).

---

### Edge Cases

- What happens when a task title is very long?
  - The title truncates with ellipsis in the list view; full title is visible in the detail view

- What happens when the user has no tasks for the week?
  - An empty state is displayed encouraging task creation

- What happens when the user taps the checkbox but is offline?
  - The task is marked complete locally and synced when connectivity returns

- What happens when the user drags the bottom sheet down quickly?
  - The sheet dismisses with a smooth animation

- What happens when the user has no partner connected?
  - The "Partner" segment shows an empty state with messaging about inviting a partner

- What happens when goal data is not yet implemented?
  - Goal-related UI shows placeholder with TODO marker; tapping shows "Coming soon" message

- What happens when Seasons feature is not yet implemented?
  - Season context chip shows placeholder with TODO marker; tapping shows "Coming soon" message

## Requirements *(mandatory)*

### Functional Requirements

#### Week View Header
- **FR-001**: System MUST display "This Week" as the main title when viewing the current week
- **FR-002**: System MUST display a subtitle showing the date range and task count (e.g., "Jan 5 - 11 · 7 tasks")
- **FR-003**: System MUST display a season context chip showing quarter and week number (placeholder for Feature TBD)

#### Week Selector Strip
- **FR-004**: System MUST display a horizontal week day selector showing 7 days (Sun-Sat)
- **FR-005**: System MUST highlight today with a distinct background color (primary light)
- **FR-006**: System MUST highlight the selected day with primary color background and white text
- **FR-007**: System MUST show a dot indicator below days that have tasks
- **FR-008**: System MUST provide left/right navigation arrows to switch weeks
- **FR-009**: System MUST update the header title and task list when a different day is selected

#### Segment Control
- **FR-010**: System MUST display a three-segment control with "You", "Partner", and "Together" options
- **FR-011**: System MUST show a checkmark icon next to the label in the "You" segment
- **FR-012**: System MUST persist and restore the user's last selected segment
- **FR-013**: System MUST filter the task list based on the selected segment

#### Task List Display
- **FR-014**: System MUST group tasks into sections: "Overdue" (red text), "Today", "Tomorrow", "Later this week"
- **FR-015**: System MUST display each task with a priority-colored checkbox (P1: red, P2: orange, P3: blue, P4: gray)
- **FR-016**: System MUST display task title, optional schedule time, optional recurring indicator, and project/goal tag
- **FR-017**: System MUST display a collapsible "Completed" section showing completed tasks with count
- **FR-018**: System MUST display completed tasks with strike-through text and muted colors
- **FR-019**: System MUST show a three-dot action button on hover/focus for each task row

#### Task Completion
- **FR-020**: System MUST animate the checkbox with a scale pop effect when completing a task
- **FR-021**: System MUST display a checkmark inside the filled checkbox when complete
- **FR-022**: System MUST move completed tasks to the "Completed" section
- **FR-023**: System MUST provide haptic feedback on task completion (graceful degradation if unsupported)

#### Planning & Review Banners
- **FR-024**: System MUST display a "Plan your week" banner with purple gradient during planning window
- **FR-025**: System MUST display a "Review your week" banner with orange gradient during review window
- **FR-026**: System MUST show streak count badge on the review banner
- **FR-027**: System MUST navigate to appropriate flow when banner is tapped

#### Floating Action Button
- **FR-028**: System MUST display a red FAB with "+" icon in the bottom-right corner
- **FR-029**: System MUST open the Add Task bottom sheet when FAB is tapped
- **FR-030**: System MUST position FAB above the bottom navigation bar

#### Add Task Bottom Sheet
- **FR-031**: System MUST display a bottom sheet with drag handle for adding tasks
- **FR-032**: System MUST display a task name input field with coral left border indicator
- **FR-033**: System MUST display an optional description input field
- **FR-034**: System MUST display scrollable action pills: Me (owner), Date, Priority, Goal, Label, Deadline
- **FR-035**: System MUST show owner selection popover with Me/Partner/Together options when owner pill is tapped
- **FR-036**: System MUST show date selection popover with Today/Tomorrow/Next week/Pick a date options
- **FR-037**: System MUST show priority selection popover with P1/P2/P3/No priority options with color indicators
- **FR-038**: System MUST show goal selection sheet with list of available goals (placeholder if not implemented)
- **FR-039**: System MUST show label selection sheet with available labels and "Create new label" option
- **FR-040**: System MUST display an "AI assist" button (placeholder, coming soon)
- **FR-041**: System MUST display an "Add Task" button that creates the task and dismisses the sheet
- **FR-042**: System MUST validate that task title is not empty before creation

#### Task Detail Bottom Sheet
- **FR-043**: System MUST display a bottom sheet when a task row is tapped (not the checkbox)
- **FR-044**: System MUST display a drag handle for sheet expansion/collapse
- **FR-045**: System MUST display project/week indicator with folder icon and chevron
- **FR-046**: System MUST display task title with priority-colored checkbox
- **FR-047**: System MUST display task description if present
- **FR-048**: System MUST display owner row with avatar and name
- **FR-049**: System MUST display date row with calendar icon and date text
- **FR-050**: System MUST display priority row with flag icon and priority text
- **FR-051**: System MUST display label chips with colored dots
- **FR-052**: System MUST display goal progress row with emoji, name, weekly progress, progress dots, and navigation chevron (placeholder if goals not implemented)
- **FR-053**: System MUST display scrollable option chips: Deadline, Reminders, Location, Repeat
- **FR-054**: System MUST display expandable sub-tasks section with count indicator
- **FR-055**: System MUST allow toggling sub-task completion within the detail sheet
- **FR-056**: System MUST display "Add sub-task" option
- **FR-057**: System MUST display a comment input field with attachment icon
- **FR-058**: System MUST display a green "Complete" button that marks the task complete and dismisses
- **FR-059**: System MUST display a "Skip" button for deferring the task

#### Bottom Navigation
- **FR-060**: System MUST display a 4-tab bottom navigation: Week, Progress, Goals, Seasons
- **FR-061**: System MUST highlight the active "Week" tab with primary color
- **FR-062**: System MUST navigate to respective screens when other tabs are tapped (placeholders for unimplemented screens)

### Key Entities

- **Task**: Represents a user's task with title, description, status, priority (1-4), owner type, scheduled date/time, associated goal, labels, sub-tasks, and week association. Referenced from Feature 002 Task Data Layer.
- **Week**: Represents a calendar week with start/end dates. Referenced from Feature 002 Task Data Layer.
- **Segment**: View filter (You, Partner, Together) determining which tasks are displayed.
- **Priority**: Task priority level (P1-P4) with associated colors (red, orange, blue, gray).
- **Goal**: A user's goal that tasks can be linked to, showing progress tracking. (Placeholder for Feature 007)
- **Season**: A quarter-based planning period. (Placeholder for Feature TBD)

## Assumptions

- **Task Data Layer Available**: Feature 002 is implemented and provides TaskRepository with CRUD operations
- **Authentication Available**: Feature 001 provides authenticated user context
- **Theme System**: The existing Tandem theme colors will be extended to include mockup-specific colors (coral, Todoist-style priority colors)
- **Bottom Navigation Shell**: The app already has a navigation structure with tabs
- **Goal System Deferred**: Goal-related UI will show placeholders marked with TODO until Feature 007 is implemented
- **Seasons Feature Deferred**: Season context chip will show placeholder until the Seasons feature is implemented
- **Partner System Partial**: Partner filtering may show empty state until Feature 006 is fully implemented
- **Planning/Review Navigation**: Banner taps navigate to existing Planning (004) and Review (005) flows
- **Sub-tasks**: Sub-tasks may use existing task parent/child relationships or show placeholder if not in data layer

## Out of Scope

- Full Goal system implementation (Feature 007)
- Seasons/Quarter feature implementation
- Real-time partner synchronization (Feature 006)
- AI assist task suggestions
- Location-based reminders
- Repeating task rule configuration
- Dark mode (follow existing theme support)
- Tablet/large screen layouts

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Week View loads and displays tasks within 1 second of navigation
- **SC-002**: Visual design matches HTML mockups with 95% accuracy (colors, spacing, typography, layout)
- **SC-003**: Task completion animation completes within 300ms
- **SC-004**: Bottom sheets open and close with smooth animations (no jank)
- **SC-005**: Segment switching updates the task list within 200ms
- **SC-006**: Users can add a new task via the FAB flow in under 10 seconds
- **SC-007**: Users can view task details by tapping a task within 500ms (sheet appears)
- **SC-008**: Week navigation (day selection, week arrows) updates the view within 300ms
- **SC-009**: All interactive elements have appropriate touch targets (minimum 48dp)
- **SC-010**: Placeholder features are clearly marked and do not cause crashes when tapped
