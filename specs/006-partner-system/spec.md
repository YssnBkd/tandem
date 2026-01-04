# Feature Specification: Partner System

**Feature Branch**: `006-partner-system`
**Created**: 2026-01-04
**Status**: Draft
**Input**: User description: "Feature: Partner System - Implement partner invitation, connection, and real-time synchronization between coupled accounts."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Invite Partner (Priority: P1)

As a user, I can generate an invite link to send to my partner so that we can connect our accounts and share task management together.

**Why this priority**: This is the foundational action that enables the entire partner system. Without the ability to invite a partner, no other partner features can be used.

**Independent Test**: Can be fully tested by a single user generating an invite link and verifying the system share sheet opens with the correct link format. Delivers value by enabling the first step of partner connection.

**Acceptance Scenarios**:

1. **Given** I am an authenticated user without a partner, **When** I tap "Invite Partner", **Then** the system generates a unique invite link and opens the share sheet
2. **Given** I have already generated an active invite, **When** I tap "Invite Partner" again, **Then** the system shows my existing invite (not a new one)
3. **Given** I am viewing the invite screen, **When** I choose "I'll do this later", **Then** I return to the previous screen without generating an invite
4. **Given** my invite is 7 days old, **When** a partner tries to use it, **Then** the invite is rejected as expired

---

### User Story 2 - Accept Invitation and Connect (Priority: P1)

As an invited user, I can open the invite link and connect with my partner after signing in or creating an account.

**Why this priority**: Equally critical as US1 - completing the connection enables all partner features. This is the counterpart to invitation.

**Independent Test**: Can be tested with a valid invite link by navigating to it, authenticating, and verifying both accounts become connected. Delivers value by establishing the partner relationship.

**Acceptance Scenarios**:

1. **Given** I receive a valid invite link, **When** I open it, **Then** I see the inviter's name and a preview of their current tasks
2. **Given** I am on the partner landing page and not signed in, **When** I sign up or sign in, **Then** I am automatically connected with the inviter
3. **Given** I am already signed in and open a valid invite link, **When** the page loads, **Then** I see an option to connect with this partner
4. **Given** I open an expired invite link, **When** the page loads, **Then** I see an error message explaining the link has expired
5. **Given** I open an invalid invite link, **When** the page loads, **Then** I see an error message explaining the link is invalid
6. **Given** I try to accept my own invite link, **When** I attempt to connect, **Then** the system prevents self-invitation with a clear error

---

### User Story 3 - View Connection Confirmation (Priority: P2)

As a newly connected user, I see a confirmation screen celebrating the partnership and showing next steps.

**Why this priority**: Provides positive feedback after connection and guides users to their next action, improving engagement and onboarding completion.

**Independent Test**: Can be tested after successful connection by verifying the confirmation screen displays partner name, week preview, and CTA. Delivers value by confirming success and guiding next steps.

**Acceptance Scenarios**:

1. **Given** I just connected with a partner, **When** the connection completes, **Then** I see a success screen with "You're connected with [Partner Name]!"
2. **Given** I am on the confirmation screen, **When** I view it, **Then** I see a preview of my partner's week
3. **Given** I am on the confirmation screen, **When** I tap "Plan Your Week", **Then** I am navigated to the week planning flow

---

### User Story 4 - Request Task from Partner (Priority: P2)

As a connected user, I can request my partner to do a task, which appears in their task list for acceptance.

**Why this priority**: Enables core collaborative functionality. Once connected, partners need a way to delegate and request tasks from each other.

**Independent Test**: Can be tested by creating a task request and verifying it appears in partner's pending requests. Delivers value by enabling collaborative task management.

**Acceptance Scenarios**:

1. **Given** I am connected with a partner, **When** I tap "Request a Task", **Then** I see a modal with title input, optional note, and helper text showing partner's name
2. **Given** I am in the request modal, **When** I enter a task title and tap "Send Request", **Then** the task is created and assigned to my partner as pending acceptance
3. **Given** I am a partner receiving a request, **When** I view my task list, **Then** I see the requested task marked as pending my acceptance
4. **Given** I submit a request without a title, **When** I tap "Send Request", **Then** the system shows a validation error requiring a title
5. **Given** I have a pending task request, **When** I tap "Accept", **Then** the task becomes a normal task in my list
6. **Given** I have a pending task request, **When** I tap "Decline", **Then** the request is removed from my list and the requester is notified

---

### User Story 5 - View Partner Status (Priority: P3)

As a connected user, I can view my partner's connection status and manage the partnership from settings.

**Why this priority**: Provides visibility into partnership status and allows users to manage or end the relationship when needed.

**Independent Test**: Can be tested by navigating to settings and verifying partner name, status, and disconnect option are displayed. Delivers value by giving users control over their partnership.

**Acceptance Scenarios**:

1. **Given** I am connected with a partner, **When** I go to Settings > Partner, **Then** I see my partner's name and connection status
2. **Given** I am viewing partner settings, **When** I tap "Disconnect", **Then** I see a confirmation dialog
3. **Given** I confirm disconnection, **When** the action completes, **Then** both accounts are disconnected and I see the "Invite Partner" option again
4. **Given** my partner disconnects, **When** I next open the app, **Then** I see that I am no longer connected

---

### User Story 6 - Real-time Task Synchronization (Priority: P3)

As a connected user, I see my partner's task completions and updates appear in real-time without refreshing.

**Why this priority**: Enhances the collaborative experience by keeping both partners in sync. Important for user experience but not blocking core functionality.

**Independent Test**: Can be tested with two connected devices by completing a task on one and verifying it appears on the other within 2 seconds. Delivers value by creating a seamless shared experience.

**Acceptance Scenarios**:

1. **Given** I am viewing my partner's tasks, **When** my partner completes a task, **Then** I see the completion reflected within 2 seconds
2. **Given** I am offline, **When** my partner makes changes, **Then** I see the changes when I reconnect
3. **Given** I make changes while offline, **When** I reconnect, **Then** my changes are synced to my partner

---

### Edge Cases

- What happens when a user tries to invite a new partner while already connected?
  - System prevents new invites until current partnership is dissolved
- How does the system handle concurrent invite acceptance attempts?
  - Only the first acceptance succeeds; subsequent attempts see "already accepted" error
- What happens if both users disconnect simultaneously?
  - Both disconnections are processed; final state is both unconnected
- How does the system handle network failures during sync?
  - Changes are queued locally and synced when connection is restored
- What happens when an invited user's account is deleted before accepting?
  - Invite becomes invalid; inviter can create a new invite

## Requirements *(mandatory)*

### Functional Requirements

#### Invitation

- **FR-001**: System MUST generate a unique, URL-safe invite code when user requests an invite
- **FR-002**: System MUST format invite links as `tandem.app/invite/[code]`
- **FR-003**: System MUST open the native share sheet with the invite link when sharing
- **FR-004**: System MUST expire invite codes after 7 days from creation
- **FR-005**: System MUST allow only one active invite per user at a time
- **FR-006**: System MUST invalidate previous invite when a new one is generated

#### Connection

- **FR-007**: System MUST validate invite codes when a user opens an invite link
- **FR-008**: System MUST display appropriate error for invalid or expired codes
- **FR-009**: System MUST prevent users from accepting their own invite
- **FR-010**: System MUST automatically link accounts after both users are authenticated
- **FR-011**: System MUST show inviter's name and task preview on the landing page
- **FR-012**: System MUST mark invite as accepted after successful connection
- **FR-013**: System MUST support Universal Links (iOS) and App Links (Android) for invite URLs
- **FR-014**: System MUST open invite directly in app when app is installed
- **FR-015**: System MUST fall back to web landing page when app is not installed

#### Partnership Management

- **FR-016**: System MUST display partner's name and connection status in settings
- **FR-017**: System MUST require confirmation before disconnecting from partner
- **FR-018**: System MUST dissolve partnership for both users when either disconnects
- **FR-019**: System MUST prevent new partner invites while already connected

#### Task Requests

- **FR-020**: System MUST allow connected users to request tasks from their partner
- **FR-021**: System MUST create requested tasks with a "pending acceptance" status
- **FR-022**: System MUST assign the requested task to the partner as owner
- **FR-023**: System MUST record the requester as the task creator
- **FR-024**: System MUST support optional notes on task requests
- **FR-025**: System MUST allow partner to explicitly Accept or Decline pending task requests
- **FR-026**: System MUST change accepted requests to normal task status
- **FR-027**: System MUST remove declined requests from both users' views

#### Real-time Synchronization

- **FR-028**: System MUST sync task changes between partners in real-time
- **FR-029**: System MUST reflect task completions within 2 seconds when online
- **FR-030**: System MUST queue changes made offline for sync when reconnected
- **FR-031**: System MUST resolve sync conflicts using last-write-wins strategy

#### Push Notifications

- **FR-032**: System MUST send push notification when partner accepts invite
- **FR-033**: System MUST send push notification when partner sends a task request
- **FR-034**: System MUST send push notification when partner accepts or declines a task request
- **FR-035**: System MUST send push notification when partner completes a task
- **FR-036**: System MUST send push notification when partner edits a task
- **FR-037**: System MUST send push notification when partner disconnects

### Key Entities

- **Partnership**: Represents the connection between two users; tracks both user IDs, creation date, and active/dissolved status
- **Invite**: Represents a pending invitation; contains unique code, creator, expiration, and status (pending/accepted/expired)
- **Task** (extended): Tasks now include a `createdBy` field to track who requested the task, and support `PENDING_ACCEPTANCE` status

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can generate and share an invite link in under 30 seconds
- **SC-002**: Partners can connect within 2 minutes of receiving an invite link
- **SC-003**: Task requests appear for the receiving partner within 2 seconds
- **SC-004**: Real-time task updates sync between partners within 2 seconds when both are online
- **SC-005**: Offline changes sync within 10 seconds of reconnection
- **SC-006**: 95% of users complete the partner connection flow on first attempt
- **SC-007**: System supports 100,000 active partnerships without degradation

## Clarifications

### Session 2026-01-04

- Q: What happens after a partner sees a task request with PENDING_ACCEPTANCE status? → A: Partner must explicitly Accept or Decline; declined requests are removed
- Q: How does the app handle invite links (tandem.app/invite/[code])? → A: Universal Links (iOS) / App Links (Android) - opens app if installed, web fallback otherwise
- Q: How are users notified of partner actions when not actively viewing the app? → A: Push notifications for all partner actions (completions, edits, requests, etc.)

## Assumptions

- Users have authenticated accounts before attempting to invite or connect with partners
- The app can access the system share sheet on both iOS and Android
- Network connectivity is available for real-time sync (offline mode queues changes)
- Partners trust each other to see task details (no privacy filtering between partners)
- One-to-one partnerships only (no group support in this feature)
