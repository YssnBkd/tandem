# Feature Specification: Week Review

**Feature Branch**: `005-week-review`
**Created**: 2026-01-03
**Status**: Draft
**Input**: User description: "Implement the week review flow where users reflect on their week, rate their experience, and review each task."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Solo Week Review (Priority: P1)

As a user, I want to review my week by rating my overall experience and marking each task's outcome so that I can reflect on my productivity and track my progress.

**Why this priority**: Core functionality - enables the fundamental review experience that all users need. Without solo review, the feature has no value. This is the minimum viable product.

**Independent Test**: Can be fully tested by a single user completing the entire review flow from start to finish, delivering the core reflection and progress tracking value.

**Acceptance Scenarios**:

1. **Given** the user has tasks for the current week, **When** they start a solo review, **Then** they see a screen prompting them to rate their overall week experience
2. **Given** the user has rated their week, **When** they proceed to task review, **Then** they see their tasks presented one at a time as full-screen cards
3. **Given** the user is reviewing a task, **When** they select Done, Tried, or Skipped, **Then** the task status is updated and they advance to the next task
4. **Given** the user has reviewed all tasks, **When** they complete the review, **Then** they see a summary with completion percentage and streak information
5. **Given** the user is mid-review, **When** they exit the review flow, **Then** their progress is saved and they can resume later

---

### User Story 2 - Review Trigger and Mode Selection (Priority: P2)

As a user, I want to see a prompt to review my week starting Friday evening so that I don't forget to reflect on my week at the right time.

**Why this priority**: Enables discoverability and timely engagement with the review feature. Without the trigger, users may forget to review.

**Independent Test**: Can be tested by checking that the review banner appears on Friday evening and the mode selection screen displays correctly.

**Acceptance Scenarios**:

1. **Given** it is Friday 6PM or later in the user's timezone, **When** the user views the week screen, **Then** they see a banner prompting them to review their week
2. **Given** the review banner is visible, **When** the user taps it, **Then** they see mode selection with "Review Solo" and "Review Together" options
3. **Given** the user has completed their review for the week, **When** they view the week screen, **Then** the review banner is no longer displayed
4. **Given** the mode selection screen is displayed, **When** the user views it, **Then** they see their current streak count

---

### User Story 3 - Quick Finish Review (Priority: P3)

As a user, I want to quickly finish my review when I don't have time to review each task individually so that I can still complete the review process.

**Why this priority**: Provides an escape hatch for busy users, ensuring review completion rates stay high even when users are time-constrained.

**Independent Test**: Can be tested by starting a review and using Quick Finish to mark all remaining tasks as Skipped.

**Acceptance Scenarios**:

1. **Given** the user is on the overall rating screen, **When** they tap "Quick Finish", **Then** all tasks are marked as Skipped and they see the completion summary
2. **Given** the user has reviewed some tasks but not all, **When** they choose Quick Finish, **Then** only the unreviewed tasks are marked as Skipped
3. **Given** the user uses Quick Finish, **When** the review completes, **Then** the completion percentage reflects the Skipped tasks appropriately

---

### User Story 4 - Review Summary and Streak (Priority: P4)

As a user, I want to see a summary of my week with completion stats and streak information so that I can feel motivated by my progress.

**Why this priority**: Enhances user motivation through gamification but is not required for core functionality.

**Independent Test**: Can be tested by completing a review and verifying the summary screen displays correct statistics.

**Acceptance Scenarios**:

1. **Given** the user completes a review, **When** the summary screen displays, **Then** they see completion percentage with a progress bar
2. **Given** the user completes a review, **When** the summary screen displays, **Then** they see their current streak count and an encouraging message
3. **Given** the user completes a review, **When** they tap "Start Next Week", **Then** they navigate to planning for the upcoming week
4. **Given** the user completes a review, **When** they tap "Done", **Then** they return to the week view

---

### User Story 5 - Together Review Mode (Priority: P5)

As a user, I want to review my week together with my partner so that we can share our reflections and support each other.

**Why this priority**: Enhances the couples experience but requires partner functionality and is more complex. Can be deferred to v1.1.

**Independent Test**: Can be tested by two users completing a together review session, alternating between their tasks.

**Acceptance Scenarios**:

1. **Given** the user selects "Review Together", **When** the review starts, **Then** both partners take turns rating their weeks
2. **Given** it is Partner A's turn, **When** they complete their rating, **Then** they tap "Pass to Partner" and Partner B's rating screen appears
3. **Given** both partners have rated their weeks, **When** task review begins, **Then** tasks alternate between partners
4. **Given** Partner A is reviewing their task, **When** Partner B observes, **Then** Partner B can add reaction emojis (thumbs up, heart, muscle) to the task
5. **Given** both partners complete the review, **When** the summary displays, **Then** they see side-by-side summary cards with both partners' stats

---

### User Story 6 - Task Review Notes (Priority: P6)

As a user, I want to add optional notes when reviewing tasks so that I can capture context about what happened with each task.

**Why this priority**: Nice-to-have feature that adds depth but is not essential for the core review flow.

**Independent Test**: Can be tested by reviewing a task and adding an optional note, then verifying the note is persisted.

**Acceptance Scenarios**:

1. **Given** the user is reviewing a task, **When** they view the task card, **Then** they see an optional quick note field
2. **Given** the user adds a note to a task, **When** they mark the task outcome, **Then** the note is saved with the task
3. **Given** the user previously added a note to a task, **When** they view the task elsewhere, **Then** the note is visible

---

### Edge Cases

- What happens when the user has no tasks for the week? → Show an empty state message with option to skip to summary
- What happens when the user exits mid-review? → Save progress automatically; on return, prompt to continue or restart
- What happens when a task was already marked complete before review? → Pre-fill the "Done" status but allow user to change it
- How does the system handle timezone differences for Friday evening trigger? → Use the user's device timezone
- What happens if the partner never completes their portion of a Together review? → Allow the initiating user to complete their portion; partner can complete later independently
- What happens to streak if user misses a week? → Streak resets to 0; display message explaining streak rules
- What happens when review window closes (Sunday 11:59PM) with incomplete review? → Progress preserved in DataStore but week remains unreviewed; streak breaks on next review; no explicit notification in v1

## Requirements *(mandatory)*

### Functional Requirements

#### Review Flow
- **FR-001**: System MUST support two review modes: solo and together
- **FR-002**: System MUST display review flow as a full-screen modal experience
- **FR-003**: System MUST save review progress automatically when user exits (stored locally on device)
- **FR-004**: System MUST provide a "Quick Finish" option that marks all remaining tasks as Skipped

#### Rating
- **FR-005**: System MUST display a 5-point emoji scale for week rating (terrible, bad, neutral, good, great)
- **FR-006**: System MUST require users to select a week rating before proceeding to task review
- **FR-007**: System MUST allow users to add an optional text note with their week rating
- **FR-008**: System MUST persist the week rating to the Week entity

#### Task Review
- **FR-009**: System MUST display all tasks for the current week during review
- **FR-010**: System MUST present tasks one at a time as full-screen cards
- **FR-011**: System MUST provide three outcome options per task: Done, Tried, and Skipped
- **FR-012**: System MUST update task status based on user selection
- **FR-013**: System MUST allow users to add an optional note per task
- **FR-014**: System MUST pre-fill tasks that were already marked complete before review

#### Review Trigger
- **FR-015**: System MUST display a review banner starting Friday 6PM in the user's timezone
- **FR-016**: System MUST dismiss the review banner once the user completes their review
- **FR-017**: System MUST display mode selection when user initiates review
- **FR-017a**: System MUST close the review window at Sunday 11:59PM; incomplete reviews after this deadline count as missed

#### Together Mode
- **FR-018**: System MUST alternate between partners' tasks during together review
- **FR-019**: System MUST only allow task owners to select task outcomes
- **FR-020**: System MUST allow observers to add reactions (thumbs up, heart, muscle emoji) to tasks
- **FR-021**: System MUST collect both partners' overall ratings
- **FR-022**: System MUST store reactions on the task entity

#### Streak
- **FR-023**: System MUST calculate streak on review completion
- **FR-024**: System MUST reset streak if either partner misses a weekly review (in couple mode)
- **FR-025**: System MUST display shared streak count for couples

#### Summary
- **FR-026**: System MUST display completion percentage with progress bar on summary screen (completion = Done tasks only; Tried and Skipped count as incomplete)
- **FR-027**: System MUST display current streak count with encouraging message
- **FR-028**: System MUST provide navigation to next week planning from summary
- **FR-029**: System MUST record reviewedAt timestamp when review is completed

### Key Entities

- **Week**: Represents a calendar week; has rating (1-5), rating note (optional), reviewedAt timestamp, and relationship to tasks
- **Task**: Represents a task for the week; has status (pending, done, tried, skipped), review note (optional), reactions (list of emojis with reactor user)
- **Streak**: Represents consecutive weeks of completed reviews; has count and relationship to user/couple
- **Review Progress**: Temporary state tracking current review session; stored locally on device only (not synced); has current task index, mode (solo/together), partial completion status; cleared after review completion

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete a solo review flow from start to finish in under 5 minutes for a week with 10 tasks
- **SC-002**: 80% of users who start a review complete it (either fully or via Quick Finish)
- **SC-003**: Review banner appears reliably on Friday evening and disappears after review completion
- **SC-004**: All task status updates during review are persisted and reflected in the week view
- **SC-005**: Streak calculation is accurate and updates immediately upon review completion
- **SC-006**: Users can exit and resume review without losing progress
- **SC-007**: Together mode correctly alternates between partners' tasks
- **SC-008**: Observer reactions are stored and visible on tasks

## Assumptions

- Users have already set up their timezone preferences (or system uses device timezone)
- The Week entity already exists in the data layer (from Feature 002)
- Partner relationships are established through the core infrastructure (from Feature 001)
- Review will be available for the current week only (no backdating to previous weeks)
- Friday 6PM is the standard trigger time; no user customization of this timing in v1
- Streak is shared between partners in couple mode; individual users have their own streak
- Review window closes Sunday 11:59PM; reviews not completed by then count as missed for streak purposes

## Clarifications

### Session 2026-01-03

- Q: When does the review window close (impacts streak calculation)? → A: Until Sunday 11:59PM (end of calendar week)
- Q: How is completion percentage calculated? → A: Done only counts as complete (Tried + Skipped = incomplete)
- Q: Where is review progress persisted? → A: Local device only; final result syncs on completion
