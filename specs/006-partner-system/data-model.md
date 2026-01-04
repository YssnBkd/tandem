# Data Model: Partner System

**Feature**: 006-partner-system
**Date**: 2026-01-04

## Entity Overview

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    User     │───────│ Partnership │───────│    User     │
│  (user1_id) │  1:1  │             │  1:1  │  (user2_id) │
└─────────────┘       └─────────────┘       └─────────────┘
      │
      │ creates
      ▼
┌─────────────┐
│   Invite    │
│  (pending)  │
└─────────────┘
      │
      │ accepted by
      ▼
┌─────────────┐       ┌─────────────┐
│    Task     │───────│    User     │
│(created_by) │  N:1  │   (owner)   │
└─────────────┘       └─────────────┘
```

## Entities

### Partnership

Represents an active connection between two users (partners).

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | String (UUID) | No | Primary key |
| `user1_id` | String (UUID) | No | First user's ID (FK to auth.users) |
| `user2_id` | String (UUID) | No | Second user's ID (FK to auth.users) |
| `created_at` | Instant | No | When partnership was established |
| `status` | PartnershipStatus | No | Current partnership state |

**PartnershipStatus Enum**:
- `ACTIVE` - Partnership is active, both users connected
- `DISSOLVED` - Partnership ended (either user disconnected)

**Constraints**:
- `user1_id` < `user2_id` (enforced ordering to prevent duplicates)
- Unique constraint on `(user1_id, user2_id)`
- Each user can have at most one active partnership

**Indexes**:
- `idx_partnerships_user1` on `user1_id`
- `idx_partnerships_user2` on `user2_id`
- `idx_partnerships_status` on `status`

---

### Invite

Represents a pending invitation to connect as partners.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `code` | String | No | Primary key, unique invite code (6-32 chars) |
| `creator_id` | String (UUID) | No | User who created invite (FK to auth.users) |
| `created_at` | Instant | No | When invite was created |
| `expires_at` | Instant | No | When invite expires (created_at + 7 days) |
| `accepted_by` | String (UUID) | Yes | User who accepted (FK to auth.users) |
| `accepted_at` | Instant | Yes | When invite was accepted |
| `status` | InviteStatus | No | Current invite state |

**InviteStatus Enum**:
- `PENDING` - Invite is active, waiting for acceptance
- `ACCEPTED` - Invite was accepted, partnership created
- `EXPIRED` - Invite passed expiration date
- `CANCELLED` - Invite was cancelled by creator

**Constraints**:
- `code` must match pattern `^[A-Za-z0-9_-]{6,32}$`
- Only one `PENDING` invite per `creator_id` at a time
- `accepted_by` cannot equal `creator_id` (no self-invitation)
- `accepted_by` must not have an active partnership

**Indexes**:
- `idx_invites_creator` on `creator_id`
- `idx_invites_status` on `status`
- `idx_invites_expires` on `expires_at`

---

### Task (Extended)

Extends existing Task entity with request tracking.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| ... | ... | ... | (existing Task fields) |
| `created_by` | String (UUID) | Yes | User who created/requested task |
| `request_note` | String | Yes | Optional note from requester |

**New Status Value**:
- `PENDING_ACCEPTANCE` - Task was requested by partner, awaiting accept/decline

**Business Rules**:
- When `owner_id` != `created_by`, task was requested by partner
- `created_by` defaults to `owner_id` for self-created tasks
- When accepting request: status changes from `PENDING_ACCEPTANCE` to `NOT_STARTED`
- When declining request: task is deleted (or marked as `DECLINED` for audit)

---

### Notification

Tracks push notifications sent to users.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | String (UUID) | No | Primary key |
| `user_id` | String (UUID) | No | Recipient user (FK to auth.users) |
| `title` | String | No | Notification title |
| `body` | String | No | Notification body text |
| `action_type` | NotificationActionType | No | Type of action that triggered notification |
| `action_data` | JSON | Yes | Additional data for deep linking |
| `created_at` | Instant | No | When notification was created |
| `sent_at` | Instant | Yes | When notification was sent via FCM |
| `read_at` | Instant | Yes | When user read the notification |

**NotificationActionType Enum**:
- `INVITE_ACCEPTED` - Partner accepted invite
- `TASK_REQUESTED` - Partner sent a task request
- `TASK_REQUEST_ACCEPTED` - Partner accepted your task request
- `TASK_REQUEST_DECLINED` - Partner declined your task request
- `TASK_COMPLETED` - Partner completed a task (opt-in)
- `TASK_EDITED` - Partner edited a task (opt-in)
- `PARTNER_DISCONNECTED` - Partner ended the partnership

**Indexes**:
- `idx_notifications_user` on `user_id`
- `idx_notifications_created` on `created_at DESC`

---

### UserProfile (Extended)

Extends existing user profile with partner-related fields.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| ... | ... | ... | (existing profile fields) |
| `fcm_token` | String | Yes | Firebase Cloud Messaging token |
| `fcm_token_updated_at` | Instant | Yes | When FCM token was last updated |
| `notifications_enabled` | Boolean | No | Global notification toggle (default: true) |
| `notify_task_completed` | Boolean | No | Notify on partner task completion (default: **false**) |
| `notify_task_edited` | Boolean | No | Notify on partner task edit (default: **false**) |

**Note**: `notify_task_completed` and `notify_task_edited` default to `false` per constitution requirement.

---

## State Transitions

### Partnership Lifecycle

```
[No Partnership] ──(invite accepted)──> [ACTIVE]
                                            │
                                            │ (either user disconnects)
                                            ▼
                                       [DISSOLVED]
```

### Invite Lifecycle

```
[PENDING] ──(7 days pass)───────> [EXPIRED]
    │
    ├──(accepted by other)──────> [ACCEPTED] ──> Partnership created
    │
    └──(creator cancels/
        creates new)────────────> [CANCELLED]
```

### Task Request Lifecycle

```
[Created by Partner]
        │
        ▼
[PENDING_ACCEPTANCE] ──(accept)──> [NOT_STARTED] ──> (normal task flow)
        │
        └──(decline)──> [DELETED/DECLINED]
```

---

## Validation Rules

### Invite Code Generation
- Format: URL-safe Base64, 8 characters
- Example: `ABC12xyz`
- Generated server-side using `uuid_generate_v4()` and Base64 encoding

### Partnership Creation
- Both users must be authenticated
- Neither user can have an existing active partnership
- Invite must be in `PENDING` status
- Invite must not be expired

### Task Request
- Requester must have active partnership with target
- Title is required (non-empty)
- Note is optional (max 500 characters)

---

## SQLDelight Schema

```sql
-- Partnership table
CREATE TABLE Partnership (
    id TEXT NOT NULL PRIMARY KEY,
    user1_id TEXT NOT NULL,
    user2_id TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_user_order CHECK (user1_id < user2_id)
);

CREATE UNIQUE INDEX idx_partnership_users ON Partnership(user1_id, user2_id);
CREATE INDEX idx_partnership_user1 ON Partnership(user1_id);
CREATE INDEX idx_partnership_user2 ON Partnership(user2_id);

-- Invite table
CREATE TABLE Invite (
    code TEXT NOT NULL PRIMARY KEY,
    creator_id TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    expires_at INTEGER NOT NULL,
    accepted_by TEXT,
    accepted_at INTEGER,
    status TEXT NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_invite_creator ON Invite(creator_id);
CREATE INDEX idx_invite_status ON Invite(status);

-- Task extension (add to existing Task table)
ALTER TABLE Task ADD COLUMN created_by TEXT;
ALTER TABLE Task ADD COLUMN request_note TEXT;
```

---

## Relationships Summary

| From | To | Cardinality | Description |
|------|-----|-------------|-------------|
| Partnership | User | N:2 | Each partnership links exactly 2 users |
| Invite | User (creator) | N:1 | User creates invites |
| Invite | User (acceptor) | N:1 | User accepts invite |
| Invite | Partnership | 1:1 | Accepted invite creates partnership |
| Task | User (owner) | N:1 | Task is owned by user |
| Task | User (created_by) | N:1 | Task was requested by user |
| Notification | User | N:1 | Notification sent to user |
