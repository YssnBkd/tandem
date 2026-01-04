# Feature Specification: Progress & Insights

**Feature Branch**: `008-progress-insights`
**Created**: 2026-01-04
**Status**: Draft
**Input**: User description: "Progress tab showing trends, insights, past weeks, and streak information with partner comparison"

## Clarifications

### Session 2026-01-04

- Q: When is a week considered "missed" for streak calculation? → A: A week is missed if not completed before the next week starts (Monday 12:00 AM user's timezone).
- Q: How should milestone celebrations appear and be dismissed? → A: Show once on first view after reaching milestone, auto-dismiss after a few seconds.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Streak with Partner (Priority: P1)

As a user, I want to see my current review streak with my partner so that I feel motivated to maintain our weekly review habit together.

**Why this priority**: Streaks are the primary motivational mechanic that drive user retention and engagement. This is the core psychological hook of the Progress tab and delivers immediate value by showing users their commitment progress.

**Independent Test**: Can be fully tested by completing weekly reviews with a partner over multiple consecutive weeks and verifying the streak counter increments correctly. Delivers motivational value even without trends or past weeks.

**Acceptance Scenarios**:

1. **Given** a user has completed reviews with their partner for 3 consecutive weeks, **When** they view the Progress tab, **Then** they see a streak count of 3 with appropriate messaging.
2. **Given** a user missed their review last week, **When** they view the Progress tab, **Then** they see their streak has reset to 0.
3. **Given** a user reaches a 5-week streak milestone, **When** they view the Progress tab, **Then** they see a celebration indicator acknowledging the milestone.

---

### User Story 2 - View Completion Trends (Priority: P2)

As a user, I want to see my task completion rate over time compared to my partner so that I can track our progress patterns and stay motivated together.

**Why this priority**: Trends provide actionable insights and foster healthy accountability between partners. This builds on streak functionality by adding historical context and comparison.

**Independent Test**: Can be tested by viewing completion percentages over past weeks for both the user and their partner. Delivers value by showing patterns even without streak or past week detail functionality.

**Acceptance Scenarios**:

1. **Given** a user has 4+ weeks of completed reviews, **When** they view the Progress tab, **Then** they see a line chart showing weekly completion percentages for themselves and their partner.
2. **Given** a user has completed 6 of 8 tasks this month and their partner completed 5 of 6, **When** they view the completion comparison, **Then** they see horizontal bars with accurate percentage labels.
3. **Given** a user has less than 4 weeks of data, **When** they view trends, **Then** they see available data with messaging about needing more weeks for full trends.

---

### User Story 3 - Browse Past Weeks (Priority: P3)

As a user, I want to browse a list of past weeks so that I can see my historical review and task completion summaries at a glance.

**Why this priority**: Historical context enhances the value of trends by letting users explore specific weeks. This is valuable but depends on having review data to browse.

**Independent Test**: Can be tested by scrolling through past weeks list and verifying summary stats (completion counts, mood emojis) display correctly. Delivers archival value independently.

**Acceptance Scenarios**:

1. **Given** a user has 15 completed weeks, **When** they view the Past Weeks section, **Then** they see the first 10 weeks with summary stats.
2. **Given** a user is viewing past weeks, **When** they scroll to the bottom, **Then** more weeks load (or a "Load More" button appears).
3. **Given** a past week where user completed 6/8 tasks and partner completed 5/6, **When** viewing that week in the list, **Then** they see "You: 6/8, Partner: 5/6" with mood emojis from both reviews.

---

### User Story 4 - View Past Week Detail (Priority: P4)

As a user, I want to tap a past week to see the full review details so that I can recall specific tasks, outcomes, and notes from that week.

**Why this priority**: Detail view provides the deepest level of insight but requires the past weeks list (US3) to access. This is an enhancement to browsing that delivers complete transparency.

**Independent Test**: Can be tested by tapping any past week and verifying the detail view shows side-by-side summaries, review notes, and task list with outcomes.

**Acceptance Scenarios**:

1. **Given** a user taps a past week in the list, **When** the detail view loads, **Then** they see side-by-side partner summaries with both review notes.
2. **Given** a past week detail view is open, **When** viewing the task list, **Then** each task shows its completion outcome for both partners.
3. **Given** a user is viewing a past week detail, **When** they navigate back, **Then** they return to the past weeks list at the same scroll position.

---

### Edge Cases

- What happens when a user has no partner connected? Display streak and trends for just the user with messaging to invite a partner.
- What happens when a user has 0 completed weeks? Show an empty state with encouragement to complete their first week.
- What happens if one partner reviewed but the other didn't? Streak should not increment; show partial week data with indicator.
- What happens with weeks where partner was different (changed partners)? Show data with the partner who was active that week.
- What happens during offline usage? Display cached data with sync indicator; load more functionality disabled until online.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST calculate streak as consecutive weeks where both partners completed their weekly review.
- **FR-002**: System MUST reset streak to 0 when either partner misses a weekly review (a week is missed if not completed before Monday 12:00 AM user's timezone).
- **FR-003**: System MUST display milestone celebrations at streak counts of 5, 10, 20, and 50 weeks (shown once on first view after reaching milestone, auto-dismiss after a few seconds).
- **FR-004**: System MUST query and display completion data for the past 8 weeks for trend visualization.
- **FR-005**: System MUST calculate completion percentage per week as (completed tasks / total tasks) * 100.
- **FR-006**: System MUST display completion comparison as horizontal bars with percentage labels for current month.
- **FR-007**: System MUST paginate past weeks list, loading 10 weeks at a time.
- **FR-008**: System MUST display summary stats for each past week: date range, completion counts, and mood emojis.
- **FR-009**: System MUST provide navigation from past week list item to detail view.
- **FR-010**: System MUST display past week detail with side-by-side partner summaries, review notes, and task outcomes.
- **FR-011**: System MUST persist streak data locally and sync when online.
- **FR-012**: System MUST handle solo users (no partner) by showing individual streak and trends only.

### Key Entities

- **Streak**: Represents consecutive weeks of completed reviews; attributes include count, last milestone reached, and reset date.
- **Week Summary**: Historical week data including date range, completion stats for both partners, and mood indicators.
- **Completion Trend**: Time-series data point with week identifier, user completion percentage, and partner completion percentage.
- **Past Week Detail**: Full week review data including all tasks with outcomes, review notes from both partners, and ratings.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can view their current streak within 2 seconds of opening the Progress tab.
- **SC-002**: Streak calculation is accurate to within 1 week of actual consecutive review history.
- **SC-003**: Trend chart displays 8 weeks of data clearly readable on mobile screens.
- **SC-004**: Past weeks list loads initial 10 items within 1 second.
- **SC-005**: Users can load additional past weeks within 2 seconds per batch.
- **SC-006**: 90% of users can successfully navigate to and understand their streak count on first visit.
- **SC-007**: Past week detail view displays complete information within 1 second of tap.
- **SC-008**: Progress tab functions with cached data when offline (read-only mode).

## Assumptions

- Weekly reviews from Feature 005 provide the necessary review completion data.
- Partner system from Feature 006 provides partner information and sync capabilities.
- Task data layer from Feature 002 provides historical task and week data.
- Milestone celebration UI follows existing design patterns from the app.
- Mood emojis are stored as part of the review data from Feature 005.
- "Month" for completion comparison refers to the current calendar month.
