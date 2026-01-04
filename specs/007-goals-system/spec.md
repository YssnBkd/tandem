# Feature Specification: Goals System

**Feature Branch**: `007-goals-system`
**Created**: 2026-01-04
**Status**: Draft
**Input**: User description: "Implement long-term goals that span multiple weeks, with progress tracking and task linking."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Personal and Shared Goals (Priority: P1)

As a user, I can view my personal goals and shared goals with my partner in a dedicated Goals tab, organized by ownership type.

**Why this priority**: This is the foundational user journey - users must be able to see their goals before they can interact with them. Without this, no other goal functionality is accessible.

**Independent Test**: Can be fully tested by navigating to Goals tab and verifying goals are displayed with correct ownership segmentation. Delivers immediate visibility into user's goal landscape.

**Acceptance Scenarios**:

1. **Given** I have personal goals created, **When** I navigate to Goals tab and select "Yours" segment, **Then** I see only my personal goals displayed as cards with progress indicators
2. **Given** I have shared goals with my partner, **When** I select "Shared" segment, **Then** I see all shared goals visible to both partners
3. **Given** I have no goals, **When** I navigate to Goals tab, **Then** I see an empty state with guidance to create my first goal

---

### User Story 2 - Create a New Goal (Priority: P1)

As a user, I can create a new goal specifying its name, icon, type (Weekly Habit, Recurring Task, or Target Amount), duration, and whether it's shared with my partner.

**Why this priority**: Users must be able to create goals to populate the system. This is a critical write operation that enables all subsequent goal tracking.

**Independent Test**: Can be fully tested by opening Add Goal sheet, filling required fields, and saving. Delivers ability to capture user intentions as structured goals.

**Acceptance Scenarios**:

1. **Given** I am on the Goals tab, **When** I tap the Add Goal button, **Then** I see the Add Goal sheet with all input fields
2. **Given** I am creating a Weekly Habit goal, **When** I set name, icon, target per week, and duration, **Then** the goal is saved and appears in my goals list
3. **Given** I want to create a shared goal, **When** I enable the shared toggle and save, **Then** the goal is visible to both me and my partner
4. **Given** I am creating a Target Amount goal, **When** I set a total target number, **Then** the goal tracks cumulative progress toward that number

---

### User Story 3 - Track Goal Progress Visually (Priority: P1)

As a user, I can see visual progress toward each goal through progress bars and completion fractions, with current week indicators.

**Why this priority**: Visual progress is the core value proposition - it motivates users by showing advancement toward their goals. This directly supports habit formation.

**Independent Test**: Can be fully tested by viewing goal cards and verifying progress bar reflects actual completion percentage. Delivers immediate feedback loop for goal pursuit.

**Acceptance Scenarios**:

1. **Given** I have a Weekly Habit goal with 3/5 completions this week, **When** I view the goal card, **Then** I see a progress bar at 60% with "3/5" displayed
2. **Given** I have a Target Amount goal at 75/100, **When** I view the goal card, **Then** I see a progress bar at 75% with "75/100" displayed
3. **Given** I have a Recurring Task goal completed this week, **When** I view the goal card, **Then** I see a "This week" indicator showing completion status

---

### User Story 4 - Link Tasks to Goals (Priority: P2)

As a user, I can link my tasks to specific goals so that completing tasks automatically updates goal progress.

**Why this priority**: Task-goal linking automates progress tracking, reducing manual effort. It builds on existing task functionality from Feature 002.

**Independent Test**: Can be fully tested by editing a task, selecting a goal link, completing the task, and verifying goal progress updates. Delivers automatic progress tracking.

**Acceptance Scenarios**:

1. **Given** I am editing a task, **When** I tap the goal picker, **Then** I see a list of my active goals to choose from
2. **Given** I have a task linked to a Weekly Habit goal, **When** I complete the task, **Then** the goal's weekly count increments by 1
3. **Given** I have a task linked to a Target Amount goal, **When** I complete the task, **Then** the goal's cumulative progress increments
4. **Given** I view a task linked to a goal, **When** I see the task card, **Then** I see a goal badge indicating the connection

---

### User Story 5 - View Goal Details and History (Priority: P2)

As a user, I can tap on a goal to see its full details, week-by-week progress history, and all linked tasks.

**Why this priority**: Detail view provides deeper insight and management capabilities. It enhances the viewing experience but is not essential for basic goal tracking.

**Independent Test**: Can be fully tested by tapping a goal card and verifying detail screen shows complete information. Delivers comprehensive goal management.

**Acceptance Scenarios**:

1. **Given** I tap on a goal card, **When** the detail screen opens, **Then** I see full goal info including name, icon, type, duration, and targets
2. **Given** a goal has been active for multiple weeks, **When** I view the detail screen, **Then** I see week-by-week progress history
3. **Given** a goal has linked tasks, **When** I view the detail screen, **Then** I see a list of all tasks linked to this goal
4. **Given** I want to modify a goal, **When** I tap Edit on the detail screen, **Then** I can update the goal's properties

---

### User Story 6 - Edit and Delete Goals (Priority: P3)

As a user, I can edit goal properties or delete goals that are no longer relevant.

**Why this priority**: Management operations are important but less frequent than viewing and progress tracking. Users can work around this initially.

**Independent Test**: Can be fully tested by opening goal detail, editing fields or deleting, and verifying changes persist. Delivers full lifecycle management.

**Acceptance Scenarios**:

1. **Given** I am on a goal detail screen, **When** I tap Edit and modify the goal name, **Then** the updated name is saved and reflected everywhere
2. **Given** I want to remove a goal, **When** I tap Delete and confirm, **Then** the goal is removed and linked tasks lose their goal reference
3. **Given** I edit a shared goal, **When** I save changes, **Then** my partner sees the updated goal

---

### User Story 7 - Goal-Based Task Suggestions in Planning (Priority: P3)

As a user, during week planning I see task suggestions based on my active goals to help me make progress.

**Why this priority**: This enhances the planning workflow from Feature 004 but is an optimization rather than core functionality.

**Independent Test**: Can be fully tested by entering week planning mode and verifying goal-based suggestions appear. Delivers guided planning experience.

**Acceptance Scenarios**:

1. **Given** I have active Weekly Habit goals, **When** I enter week planning, **Then** I see a "Based on your goals" section with relevant task suggestions
2. **Given** I have an incomplete Recurring Task goal, **When** I plan my week, **Then** I see a reminder to schedule the recurring task

---

### Edge Cases

- What happens when a goal's duration expires?
  - System marks goal as "Completed" or "Expired" based on whether target was met
  - Goal moves to an archive/history view but remains accessible
- How does system handle deleting a goal with linked tasks?
  - Tasks remain intact but lose their goal reference (linkedGoalId becomes null)
  - Progress history for the goal is preserved in case of restoration
- What happens when a user unlinks from their partner while having shared goals?
  - Shared goals become personal goals owned by the original creator
  - Partner loses access to shared goals they didn't create
- How does progress calculate for a Weekly Habit at week boundary?
  - Progress resets each week; historical weekly completions are stored for history view
  - Current week progress is always relative to current week's target
- What if a task linked to a goal is deleted?
  - Goal progress from that task's previous completions is preserved
  - Task deletion does not retroactively adjust goal progress
- What happens when both partners edit a shared goal simultaneously?
  - Last-write-wins: most recent save overwrites previous changes
  - No merge conflict resolution or locking mechanism required
- What happens when a user tries to create a goal but has 10 active goals?
  - System prevents creation and displays message indicating the limit
  - User must complete or delete an existing goal before creating a new one

## Requirements *(mandatory)*

### Functional Requirements

**Goal Management**
- **FR-001**: System MUST allow users to create goals with name, icon (emoji), type, and duration
- **FR-002**: System MUST support three goal types: Weekly Habit (target per week), Recurring Task (complete each week), and Target Amount (cumulative total)
- **FR-003**: System MUST allow goals to have a duration of 4, 8, 12 weeks, or ongoing (no end date)
- **FR-004**: System MUST allow goals to be marked as shared with a partner
- **FR-005**: System MUST allow users to edit goal properties after creation
- **FR-006**: System MUST allow users to delete goals with confirmation
- **FR-006a**: System MUST limit users to a maximum of 10 active goals (completed/expired goals do not count toward this limit)

**Goal Display**
- **FR-007**: System MUST display goals in a dedicated Goals tab
- **FR-008**: System MUST segment goals into "Yours" (personal) and "Shared" views
- **FR-009**: System MUST display goal cards with icon, title, progress bar, and progress fraction
- **FR-010**: System MUST indicate current week's progress status on goal cards
- **FR-011**: System MUST display an empty state when no goals exist

**Progress Tracking**
- **FR-012**: System MUST calculate Weekly Habit progress as completions / target per week
- **FR-013**: System MUST calculate Recurring Task progress as completed/not completed for current week
- **FR-014**: System MUST calculate Target Amount progress as cumulative completions / total target
- **FR-015**: System MUST reset weekly progress for Weekly Habit goals at the start of each week
- **FR-016**: System MUST preserve week-by-week progress history for goals

**Task-Goal Linking**
- **FR-017**: System MUST allow optional goal reference on tasks
- **FR-018**: System MUST display a goal badge on tasks linked to goals
- **FR-019**: System MUST update goal progress when linked tasks are completed
- **FR-020**: System MUST allow users to select from active goals when linking a task

**Goal Detail View**
- **FR-021**: System MUST display full goal information on a detail screen
- **FR-022**: System MUST display week-by-week progress history on detail screen
- **FR-023**: System MUST display list of linked tasks on detail screen
- **FR-024**: System MUST provide edit and delete options on detail screen

**Partner Integration**
- **FR-025**: System MUST sync shared goals with partner via existing partnership infrastructure
- **FR-026**: System MUST allow both partners to view and track progress on shared goals
- **FR-027**: System MUST check partner existence before enabling shared goal creation

**Week Planning Integration**
- **FR-028**: System MUST provide goal-based task suggestions during week planning
- **FR-029**: System MUST display a "Based on your goals" section in planning view

### Key Entities

- **Goal**: Represents a user's long-term objective. Contains identity (id, name, icon), configuration (type, targetPerWeek, targetTotal, durationWeeks), state (currentProgress, startWeekId), ownership (ownerId, isShared), and metadata (createdAt). Related to Tasks via optional linking and to Users via ownership.

- **GoalType**: Enumeration defining how progress is calculated - Weekly Habit (frequency-based weekly reset), Recurring Task (binary weekly completion), Target Amount (cumulative accumulation).

- **GoalProgress**: Weekly snapshot of goal progress for history tracking. Contains goalId, weekId, progressValue, and targetValue at that point in time.

- **Task (extended)**: Existing Task entity gains optional linkedGoalId field to associate tasks with goals.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a new goal in under 60 seconds
- **SC-002**: Goal progress updates are reflected within 2 seconds of task completion
- **SC-003**: Users can navigate between personal and shared goal views in under 1 second
- **SC-004**: 100% of linked task completions correctly increment associated goal progress
- **SC-005**: Week-by-week progress history is accurately preserved for all active goals
- **SC-006**: Shared goals are visible to both partners within 5 seconds of creation
- **SC-007**: Goal-based suggestions appear in week planning view for users with active goals

## Clarifications

### Session 2026-01-04

- Q: When both partners edit a shared goal, what happens with conflicting edits? → A: Last-write-wins; most recent save overwrites previous changes
- Q: Should the system limit how many active goals a user can have? → A: Limit 10 active goals per user (completed goals don't count toward limit)

## Assumptions

- Users have completed onboarding and are authenticated (Feature 001)
- Task management infrastructure exists and supports adding optional fields (Feature 002)
- Week structure and planning flow exist (Features 003-004)
- Partner system exists for shared goal functionality (Feature 006)
- Default icon set uses standard emoji that render consistently across platforms
- Week boundaries align with existing week calculation logic in the app
- Users understand the three goal types through clear UI labeling without additional onboarding
