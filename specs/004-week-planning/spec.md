# Feature Specification: Week Planning

**Feature Branch**: `004-week-planning`
**Created**: 2026-01-03
**Status**: Draft
**Input**: User description: "Sunday Setup weekly planning flow where users set their tasks for the upcoming week"

## Clarifications

### Session 2026-01-03

- Q: What happens when user loses network connectivity or app is force-closed mid-planning? → A: Save progress locally, sync when back online (offline-first)
- Q: What happens to the original task from last week when rolled over? → A: Original task remains unchanged (stays incomplete in previous week)
- Q: How long does the planning banner persist if user doesn't complete planning? → A: Banner persists until planning complete or next Sunday 6pm (weekly reset)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Complete Weekly Planning Flow (Priority: P1)

As a user, I want to complete my weekly planning so that I have a clear set of tasks for the upcoming week.

**Why this priority**: This is the core value proposition - without a complete planning flow, the feature has no utility. Users need to be able to plan their week from start to finish.

**Independent Test**: Can be fully tested by starting the planning flow and completing all steps, verifying that tasks are created and the planning completion is recorded.

**Acceptance Scenarios**:

1. **Given** a user on the Week Tab on Sunday after 6pm with incomplete planning, **When** they tap "Start" on the planning banner, **Then** they enter the planning wizard at Step 1
2. **Given** a user in the planning wizard, **When** they complete all applicable steps and reach confirmation, **Then** they see a summary of tasks planned
3. **Given** a user who completes planning, **When** they return to the Week Tab, **Then** the planning banner is no longer visible

---

### User Story 2 - Roll Over Incomplete Tasks (Priority: P2)

As a user, I want to review incomplete tasks from last week so that I can decide whether to carry them forward or leave them behind.

**Why this priority**: Rolling over tasks prevents lost work and helps users maintain continuity between weeks. This is a key differentiator from simple task creation.

**Independent Test**: Can be fully tested by having incomplete tasks from a previous week, then starting planning and verifying each task can be added or skipped.

**Acceptance Scenarios**:

1. **Given** a user with incomplete tasks from the previous week, **When** they enter Step 1 of planning, **Then** they see each incomplete task as a full-screen card
2. **Given** a user viewing a rollover task card, **When** they tap "Add to This Week", **Then** a new task is created in the current week with a reference to the original
3. **Given** a user viewing a rollover task card, **When** they tap "Skip", **Then** the task is not carried forward and they proceed to the next card
4. **Given** a user with no incomplete tasks from last week, **When** they enter planning, **Then** Step 1 is skipped automatically

---

### User Story 3 - Add New Tasks During Planning (Priority: P2)

As a user, I want to add new tasks during the planning flow so that I can capture everything I need to do this week in one session.

**Why this priority**: Task creation is essential for users who don't have rollover tasks or need to add fresh tasks. Equal priority to rollover since either can stand alone.

**Independent Test**: Can be fully tested by entering Step 2 and adding multiple new tasks, verifying they appear in the running list and are saved.

**Acceptance Scenarios**:

1. **Given** a user in Step 2 of planning, **When** they enter a task title and tap the Add button, **Then** the task appears in the running list below
2. **Given** a user in Step 2 with an empty title, **When** they attempt to add, **Then** an inline error message is displayed
3. **Given** a user in Step 2 with tasks added, **When** they tap "Done Adding Tasks", **Then** they proceed to the next step

---

### User Story 4 - Review Partner Task Requests (Priority: P3)

As a user, I want to review tasks my partner has requested of me so that I can accept them into my week or discuss them.

**Why this priority**: Partner collaboration is a social feature that adds value but is not essential for solo use. The discuss functionality is deferred to v1.1.

**Independent Test**: Can be fully tested by having pending partner requests, viewing them in Step 3, and accepting them to verify status change.

**Acceptance Scenarios**:

1. **Given** a user with pending partner requests, **When** they enter Step 3, **Then** they see each request as a full-screen card with task title and optional note
2. **Given** a user viewing a partner request, **When** they tap "Accept", **Then** the task status changes to pending and is added to their week
3. **Given** a user viewing a partner request, **When** they tap "Discuss", **Then** a "Coming soon" toast is displayed (v1.0 placeholder)
4. **Given** a user with no partner requests, **When** they enter planning, **Then** Step 3 is skipped automatically

---

### User Story 5 - View Planning Summary (Priority: P3)

As a user, I want to see a summary of my planned week after completing the planning flow so that I can confirm what I've committed to.

**Why this priority**: Confirmation provides closure and confidence but is supplementary to the actual planning work.

**Independent Test**: Can be fully tested by completing planning and verifying the confirmation screen shows correct task count and list.

**Acceptance Scenarios**:

1. **Given** a user who completes all planning steps, **When** they reach Step 4, **Then** they see a checkmark success state with "X tasks planned" summary
2. **Given** a user on the confirmation screen, **When** they tap "Done", **Then** they return to the Week View with planning complete

---

### Edge Cases

- What happens when the user exits planning mid-flow? The progress is saved and they can resume later.
- What happens when there are zero tasks in all categories? User can still complete planning (marking the week as planned with 0 tasks).
- What happens if the user's session expires during planning? User is redirected to authenticate and can resume from where they left off.
- What happens if a rollover task is modified by another source during planning? The system shows the current state of the task at the time of display.
- What happens when the user loses network connectivity or app is force-closed? Progress is saved locally (offline-first) and syncs when back online.

## Requirements *(mandatory)*

### Functional Requirements

#### Flow Control
- **FR-001**: System MUST display a planning banner on the Week Tab starting Sunday at 6pm local time; banner persists until planning is complete or the next Sunday at 6pm (weekly reset)
- **FR-002**: System MUST implement a sequential 4-step wizard with back navigation
- **FR-003**: System MUST display a progress indicator showing current step (e.g., "Step 2 of 4")
- **FR-004**: System MUST save planning progress when user exits, allowing them to resume later
- **FR-004a**: System MUST save planning progress locally (offline-first) and sync when back online; app force-close or network loss must not result in data loss
- **FR-005**: System MUST automatically skip steps that have no items (no rollover tasks, no partner requests)

#### Rollover Tasks (Step 1)
- **FR-006**: System MUST query and display incomplete tasks from the previous week
- **FR-007**: When user taps "Add to This Week", System MUST create a new task in the current week with a reference to the original week (rolledFromWeekId)
- **FR-008**: When user taps "Skip", System MUST NOT carry the task forward to the current week
- **FR-009**: System MUST display rollover tasks as full-screen cards with progress dots showing remaining items

#### New Tasks (Step 2)
- **FR-010**: System MUST provide a text input with a VISIBLE "Add" button (not keyboard-only submission)
- **FR-011**: New tasks MUST default to SELF owner type
- **FR-012**: System MUST validate non-empty task titles and display inline error for empty submissions
- **FR-013**: System MUST display a running list of tasks added during this session
- **FR-014**: System MUST provide a "Done Adding Tasks" button to proceed to the next step

#### Partner Requests (Step 3)
- **FR-015**: System MUST display tasks with status PENDING_ACCEPTANCE as full-screen cards
- **FR-016**: When user taps "Accept", System MUST change task status to PENDING
- **FR-017**: When user taps "Discuss", System MUST display a "Coming soon" toast (v1.0 placeholder)
- **FR-018**: Partner request cards MUST show task title and optional note from partner

#### Completion (Step 4)
- **FR-019**: System MUST display a checkmark success state with "X tasks planned" summary
- **FR-020**: System MUST mark the week's planning as complete by setting planningCompletedAt timestamp
- **FR-021**: System MUST dismiss the planning banner after completion
- **FR-022**: System MUST provide "See Partner's Week" and "Done" buttons on the confirmation screen

#### UI Accessibility
- **FR-023**: All action buttons (Add to This Week, Skip, Add, Accept, Discuss) MUST have touch targets of at least 48dp
- **FR-024**: All action buttons MUST have appropriate content descriptions for accessibility

### Key Entities

- **Week**: Represents a calendar week for a user; contains planningCompletedAt timestamp to track planning status
- **Task**: Represents a to-do item; has status (PENDING, PENDING_ACCEPTANCE, COMPLETED), owner type (SELF, PARTNER), optional rolledFromWeekId to track rollover origin, and optional note for partner requests. When rolled over, the original task remains unchanged in the previous week; a new task is created in the current week with rolledFromWeekId as the audit trail.
- **Planning Progress**: Tracks the user's current position in the planning wizard for resume functionality

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete the full planning flow in under 5 minutes (excluding time spent deciding on individual tasks)
- **SC-002**: 95% of planning sessions that are started reach the confirmation screen
- **SC-003**: Users can resume an interrupted planning session within 2 taps from the Week Tab
- **SC-004**: Zero tasks are lost when rolling over - each rollover action either creates a new task or explicitly skips
- **SC-005**: Planning banner appears within 1 second of the 6pm Sunday trigger time
- **SC-006**: All button touch targets meet accessibility standards (48dp minimum)

## Assumptions

- Users have a concept of "partner" already established from prior features
- The previous week's incomplete tasks are available via the existing task data layer
- The Week entity already supports planningCompletedAt field or can be extended
- User authentication and session management are handled by existing infrastructure
- Time zone for "Sunday 6pm" is determined by the user's device local time
- Partner requests use the existing task status PENDING_ACCEPTANCE
