# Research: Partner System

**Feature**: 006-partner-system
**Date**: 2026-01-04
**Status**: Complete

## Research Areas

### 1. Supabase Realtime for Partner Sync

**Decision**: Use Supabase Realtime Postgres Changes with filtered subscriptions

**Rationale**:
- Native Kotlin SDK support (`io.github.jan-tennert.supabase`)
- Postgres Changes provide INSERT/UPDATE/DELETE events automatically
- Filter capability allows subscribing only to partner's changes (`filter = "created_by=eq.$partnerId"`)
- Built-in reconnection handling and Flow integration

**Alternatives Considered**:
- WebSocket from scratch: More control but unnecessary complexity
- Polling: Simpler but doesn't meet 2-second sync requirement
- Firebase Realtime Database: Would require additional dependency

**Implementation Pattern**:
```kotlin
val channel = supabase.channel("partner-tasks-$partnerId")
val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
    table = "tasks"
    filter = "created_by=eq.$partnerId"
}
changes
    .onEach { action ->
        when (action) {
            is PostgresAction.Insert -> handleTaskCreated(action.record)
            is PostgresAction.Update -> handleTaskUpdated(action.record)
            is PostgresAction.Delete -> handleTaskDeleted(action.oldRecord)
            else -> {}
        }
    }
    .launchIn(viewModelScope)

channel.subscribe()
```

**Lifecycle Management**:
- Store channel reference for cleanup in `onCleared()`
- Use separate Job for Flow collection to enable cancellation
- Implement retry with exponential backoff for reconnection

---

### 2. Android App Links for Invite Deep Links

**Decision**: Use Universal Links (iOS) and App Links (Android) with HTTPS scheme

**Rationale**:
- Best UX: Opens app directly when installed, falls back to web when not
- Secure: Requires domain verification via Digital Asset Links
- Standard: Well-supported by Android and Jetpack Navigation

**Alternatives Considered**:
- Custom URL scheme (tandem://): Requires app installed, no fallback
- Web-only: Poor UX for users with app installed

**Implementation Requirements**:

1. **AndroidManifest.xml**:
```xml
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="https"
        android:host="tandem.app"
        android:pathPrefix="/invite/" />
</intent-filter>
```

2. **Digital Asset Links** (hosted at `https://tandem.app/.well-known/assetlinks.json`):
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "org.epoque.tandem",
    "sha256_cert_fingerprints": ["<release-key-fingerprint>"]
  }
}]
```

3. **Navigation Integration**:
```kotlin
@Serializable
data class InviteAccept(val inviteCode: String) : Routes.DeepLink

// In MainActivity: pass intent to NavHost
// Extract invite code from URI path segments
```

**Invite Code Validation**:
- Pattern: `^[A-Za-z0-9_-]{6,32}$`
- Validate domain and scheme before processing
- Handle expired/invalid codes gracefully

---

### 3. Push Notifications with FCM

**Decision**: Firebase Cloud Messaging (FCM) with Supabase Edge Functions for server-side sending

**Rationale**:
- FCM is the standard for Android push notifications
- Supabase Edge Functions can call FCM HTTP v1 API
- Database triggers can insert notification records automatically

**Constitution Compliance Note**:
- FR-035/FR-036 (task completion/edit notifications) must be **OFF by default**
- Users opt-in via notification settings
- This aligns with constitution's "NO notifications for partner's task completions (default off)"

**Implementation Pattern**:

1. **Notification Channels** (Android 8+):
   - `partner_actions` (HIGH priority): Task requests, invite accepted
   - `task_updates` (DEFAULT priority): Task completions (opt-in)
   - `week_reminders` (LOW priority): Planning/review reminders

2. **FCM Token Management**:
   - Store token in `profiles.fcm_token` column
   - Refresh on `onNewToken()` callback
   - Sync token to Supabase after authentication

3. **Server-Side Trigger** (Supabase):
   - Insert notification record on partner action
   - Database webhook calls Edge Function
   - Edge Function sends FCM message

4. **Notification Preferences**:
   - Store in DataStore (local) + Supabase (remote)
   - `notifications_enabled` column in profiles table
   - Edge Function checks this before sending

---

### 4. Offline-First Architecture

**Decision**: SQLDelight local cache with sync queue

**Rationale**:
- Consistent with existing Features 002-005 architecture
- Tasks and partnerships cached locally
- Changes queued when offline, synced on reconnect

**Implementation**:
- `Partnership` and `Invite` tables in SQLDelight schema
- Repository layer handles sync logic
- Supabase Realtime reconnects automatically

---

### 5. Task Model Extension

**Decision**: Add `createdBy` field and `PENDING_ACCEPTANCE` status to Task

**Rationale**:
- `createdBy` tracks who requested a task (can differ from `ownerId`)
- `PENDING_ACCEPTANCE` status indicates partner must accept/decline
- Minimal schema change, preserves existing task behavior

**Migration Required**:
```sql
ALTER TABLE Task ADD COLUMN created_by TEXT;
-- Status enum already supports extension via SQLDelight
```

---

## Key Decisions Summary

| Area | Decision | Key Benefit |
|------|----------|-------------|
| Realtime Sync | Supabase Postgres Changes | Native Kotlin Flow, <2s latency |
| Deep Links | Android App Links | Best UX with web fallback |
| Push Notifications | FCM + Supabase Edge Functions | Standard, scalable |
| Notification Default | OFF for task completions | Constitution compliance |
| Offline Support | SQLDelight + sync queue | Consistent with existing features |
| Task Model | Add `createdBy` + `PENDING_ACCEPTANCE` | Minimal, non-breaking change |

## Dependencies to Add

```kotlin
// Already in project
io.github.jan-tennert.supabase:realtime-kt

// New dependency
com.google.firebase:firebase-messaging:24.1.0
```

## Database Schema Changes

1. **New Tables**: `partnerships`, `invites`, `notifications`
2. **Modified Tables**: `tasks` (add `created_by` column)
3. **New Indexes**: For partnership lookup by user_id

## Next Steps

1. Define detailed data model in `data-model.md`
2. Create Supabase table contracts in `contracts/`
3. Generate quickstart guide in `quickstart.md`
