# Feature Specification: Task Data Layer

**Feature Branch**: `002-task-data-layer`
**Created**: 2026-01-01
**Status**: Draft
**Input**: User description: "Implement the core task data model, local database, and repository for managing tasks in Tandem."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Task Persistence (Priority: P1)

As a user, I want my tasks to be saved locally so they persist across app restarts, ensuring I never lose my weekly planning progress.

**Why this priority**: Without persistence, the entire feature is unusable. This is the foundational capability that all other functionality depends on.

**Independent Test**: Can be fully tested by creating tasks, closing the app, reopening, and verifying tasks are still present. Delivers the core value of reliable task storage.

**Acceptance Scenarios**:

1. **Given** a user has created tasks, **When** the app is closed and reopened, **Then** all previously created tasks are displayed with their original data intact
2. **Given** a user modifies a task's status or content, **When** the app is restarted, **Then** the modifications are preserved
3. **Given** the app crashes unexpectedly during use, **When** the user reopens the app, **Then** all tasks saved before the crash are recovered

---

### User Story 2 - Task CRUD Operations (Priority: P1)

As a user, I can create, read, update, and delete tasks to manage my weekly planning effectively.

**Why this priority**: CRUD operations are equally critical with persistence - users must be able to manage their tasks. This forms the core interaction model.

**Independent Test**: Can be fully tested by performing each operation (create, read, update, delete) and verifying the expected outcome. Delivers the ability to manage task data.

**Acceptance Scenarios**:

1. **Given** a user wants to add a new task, **When** they provide a title (and optionally notes), **Then** the task is created with a unique identifier, timestamps, and default pending status
2. **Given** a user views their task list, **When** they request all tasks, **Then** all tasks belonging to them are displayed
3. **Given** a user wants to modify a task, **When** they update the title, notes, or status, **Then** the changes are saved and the updated timestamp is refreshed
4. **Given** a user wants to remove a task, **When** they delete it, **Then** the task is permanently removed from their list
5. **Given** a user marks a task as completed, **When** the status is updated, **Then** the task status changes to COMPLETED and timestamp is recorded
6. **Given** a task has a repeat target (e.g., "Gym 3x"), **When** the user completes one occurrence, **Then** the repeat count is incremented

---

### User Story 3 - Week-Based Task Filtering (Priority: P2)

As a user, I can view tasks filtered by week so I can focus on my current week's commitments and review past weeks.

**Why this priority**: Important for organizing tasks temporally, but the core CRUD and persistence must work first.

**Independent Test**: Can be tested by creating tasks for different weeks and filtering by week ID. Delivers focused weekly task views.

**Acceptance Scenarios**:

1. **Given** tasks exist for multiple weeks, **When** a user filters by a specific week ID, **Then** only tasks for that week are displayed
2. **Given** the user opens the app, **When** the current week doesn't exist in storage, **Then** the current week is automatically created
3. **Given** a user wants to view past weeks, **When** they request the week history, **Then** a list of past weeks with their metadata is displayed

---

### User Story 4 - Task Owner Filtering (Priority: P2)

As a user, I can view tasks filtered by owner type (mine, partner's, shared) to focus on specific responsibility areas.

**Why this priority**: Supports the collaborative nature of Tandem but requires the base filtering infrastructure first.

**Independent Test**: Can be tested by creating tasks with different owner types and filtering by each type.

**Acceptance Scenarios**:

1. **Given** tasks exist with various owner types, **When** a user filters by SELF, **Then** only their own tasks are displayed
2. **Given** tasks exist with various owner types, **When** a user filters by PARTNER, **Then** only partner-assigned tasks are displayed
3. **Given** tasks exist with various owner types, **When** a user filters by SHARED, **Then** only shared tasks are displayed

---

### User Story 5 - Week Review Management (Priority: P3)

As a user, I can update week review data to reflect on my weekly progress and record overall ratings.

**Why this priority**: Review functionality enhances the experience but is not required for basic task management.

**Independent Test**: Can be tested by completing a week review and verifying the rating and notes are saved.

**Acceptance Scenarios**:

1. **Given** a week exists, **When** the user completes a review with a rating (1-5) and notes, **Then** the review data is saved with a timestamp
2. **Given** a week has not been reviewed, **When** the user views week details, **Then** the review fields show as empty/null

---

### Edge Cases

- What happens when a task is created with an empty title? System rejects the creation and indicates title is required.
- What happens when filtering by a week that has no tasks? An empty list is returned (not an error).
- What happens when deleting a task that is linked to a goal? The task is deleted; the goal reference becomes orphaned (acceptable for MVP).
- What happens when incrementing repeat count beyond the target? The count continues to increment (no upper limit enforced).
- What happens when the week ID format is invalid? System rejects the operation with a validation error.
- What happens when attempting to update a non-existent task? System indicates the task was not found.
- What happens when the same task is modified concurrently? Last-write-wins; the most recent update overwrites previous values.

## Requirements *(mandatory)*

### Functional Requirements

#### Task Operations
- **FR-001**: System MUST allow creating a task with a title (required), notes (optional), owner type, and week ID
- **FR-002**: System MUST automatically generate a unique ID, timestamps (createdAt, updatedAt), and set default status to PENDING for new tasks
- **FR-003**: System MUST allow reading all tasks for the current user
- **FR-004**: System MUST allow reading tasks filtered by a specific week ID
- **FR-005**: System MUST allow reading tasks filtered by owner type (SELF, PARTNER, SHARED)
- **FR-006**: System MUST allow updating task title, notes, and status
- **FR-007**: System MUST update the updatedAt timestamp whenever a task is modified
- **FR-008**: System MUST allow deleting a task by its ID
- **FR-009**: System MUST allow marking a task as completed by changing status to COMPLETED
- **FR-010**: System MUST allow incrementing the repeat count for repeating tasks

#### Week Operations
- **FR-011**: System MUST get or create the current week based on ISO 8601 week format (e.g., "2024-W52")
- **FR-012**: System MUST allow retrieving a week by its ID
- **FR-013**: System MUST allow updating week review data (overallRating 1-5, reviewNote, reviewedAt)
- **FR-014**: System MUST allow listing past weeks for a user in chronological order

#### Data Integrity
- **FR-015**: System MUST validate that tasks have a non-empty title before creation
- **FR-016**: System MUST enforce ISO 8601 week format for week IDs
- **FR-017**: System MUST store all timestamps in UTC
- **FR-018**: System MUST persist data locally so it survives app restarts

### Key Entities

- **Task**: Represents a user's task/commitment for a specific week. Contains title, optional notes, owner information (who it's for and who created it), status tracking, optional repeat functionality for recurring commitments, and optional links to goals and previous weeks (for rolled-over tasks).

- **Week**: Represents a calendar week for a user. Contains the week identifier in ISO 8601 format, date boundaries, optional review data (rating 1-5 and notes), and timestamps for when planning and review were completed.

- **OwnerType**: Categorizes who a task belongs to - SELF (user's own task), PARTNER (task for the user's partner), or SHARED (joint responsibility).

- **TaskStatus**: Tracks the lifecycle of a task - PENDING (not started), PENDING_ACCEPTANCE (awaiting partner confirmation), COMPLETED (done), TRIED (attempted but incomplete), SKIPPED (intentionally not done), DECLINED (partner rejected). The data layer allows any status transition; business rules for valid transitions are enforced at a higher layer.

## Clarifications

### Session 2026-01-01

- Q: Should the data layer enforce valid task status transitions? → A: Allow any status transition (validation at higher layer)
- Q: How should concurrent edits to the same task be handled? → A: Last-write-wins (most recent update overwrites previous)

## Assumptions

- Users are authenticated before accessing task data (user ID is available from auth context)
- A single local database per device is sufficient (no immediate need for sync)
- Week boundaries follow ISO 8601 (weeks start on Monday)
- The "partner" relationship is managed elsewhere; this feature only stores the owner type reference
- Goals are a separate feature; this feature only stores an optional goal ID reference
- Task rollover logic (creating new tasks from previous weeks) will be handled by a higher-level feature

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create, view, update, and delete tasks successfully 100% of the time when valid data is provided
- **SC-002**: All task data persists across app restarts with zero data loss
- **SC-003**: Users can filter tasks by week and see results in under 1 second
- **SC-004**: Users can filter tasks by owner type and see results in under 1 second
- **SC-005**: Week data (current week, past weeks, review data) can be retrieved and updated reliably
- **SC-006**: Invalid operations (empty title, invalid week format) are rejected with clear feedback
- **SC-007**: System supports storing at least 1000 tasks per user without performance degradation
