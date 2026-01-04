# API Contracts: Partner System

**Feature**: 006-partner-system
**Date**: 2026-01-04

## Supabase Tables

### partnerships

```sql
CREATE TABLE partnerships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user1_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user2_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DISSOLVED')),

    CONSTRAINT chk_user_order CHECK (user1_id < user2_id),
    CONSTRAINT unique_partnership UNIQUE (user1_id, user2_id)
);

-- Indexes for lookup by either user
CREATE INDEX idx_partnerships_user1 ON partnerships(user1_id) WHERE status = 'ACTIVE';
CREATE INDEX idx_partnerships_user2 ON partnerships(user2_id) WHERE status = 'ACTIVE';

-- Row Level Security
ALTER TABLE partnerships ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own partnerships"
ON partnerships FOR SELECT
USING (auth.uid() = user1_id OR auth.uid() = user2_id);

CREATE POLICY "Users can dissolve own partnerships"
ON partnerships FOR UPDATE
USING (auth.uid() = user1_id OR auth.uid() = user2_id)
WITH CHECK (status = 'DISSOLVED');
```

---

### invites

```sql
CREATE TABLE invites (
    code TEXT PRIMARY KEY,
    creator_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '7 days',
    accepted_by UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    accepted_at TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED')),

    CONSTRAINT chk_code_format CHECK (code ~ '^[A-Za-z0-9_-]{6,32}$'),
    CONSTRAINT chk_no_self_invite CHECK (accepted_by IS NULL OR accepted_by != creator_id)
);

-- Ensure only one pending invite per user
CREATE UNIQUE INDEX idx_invites_one_pending
ON invites(creator_id)
WHERE status = 'PENDING';

CREATE INDEX idx_invites_creator ON invites(creator_id);
CREATE INDEX idx_invites_expires ON invites(expires_at) WHERE status = 'PENDING';

-- Row Level Security
ALTER TABLE invites ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own invites"
ON invites FOR SELECT
USING (auth.uid() = creator_id OR auth.uid() = accepted_by);

CREATE POLICY "Users can create invites"
ON invites FOR INSERT
WITH CHECK (auth.uid() = creator_id);

CREATE POLICY "Authenticated users can view pending invites by code"
ON invites FOR SELECT
USING (status = 'PENDING' AND expires_at > NOW());

CREATE POLICY "Users can accept invites"
ON invites FOR UPDATE
USING (status = 'PENDING' AND expires_at > NOW() AND auth.uid() != creator_id)
WITH CHECK (accepted_by = auth.uid() AND status = 'ACCEPTED');

CREATE POLICY "Creators can cancel invites"
ON invites FOR UPDATE
USING (auth.uid() = creator_id AND status = 'PENDING')
WITH CHECK (status = 'CANCELLED');
```

---

### profiles (extension)

```sql
-- Add columns to existing profiles table
ALTER TABLE profiles
ADD COLUMN IF NOT EXISTS fcm_token TEXT,
ADD COLUMN IF NOT EXISTS fcm_token_updated_at TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS notify_task_completed BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS notify_task_edited BOOLEAN NOT NULL DEFAULT FALSE;

-- Update RLS to allow users to update their own FCM token
CREATE POLICY "Users can update own profile"
ON profiles FOR UPDATE
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);
```

---

### notifications

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    action_type TEXT NOT NULL CHECK (action_type IN (
        'INVITE_ACCEPTED',
        'TASK_REQUESTED',
        'TASK_REQUEST_ACCEPTED',
        'TASK_REQUEST_DECLINED',
        'TASK_COMPLETED',
        'TASK_EDITED',
        'PARTNER_DISCONNECTED'
    )),
    action_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);

-- Row Level Security
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own notifications"
ON notifications FOR SELECT
USING (auth.uid() = user_id);

CREATE POLICY "Users can mark own notifications as read"
ON notifications FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (read_at IS NOT NULL);
```

---

### Task extension

```sql
-- Add columns to existing Task table
ALTER TABLE "Task"
ADD COLUMN IF NOT EXISTS created_by UUID REFERENCES auth.users(id),
ADD COLUMN IF NOT EXISTS request_note TEXT;

-- Update status check constraint to include PENDING_ACCEPTANCE
-- (This depends on existing Task schema - adjust as needed)
ALTER TABLE "Task"
DROP CONSTRAINT IF EXISTS chk_task_status;

ALTER TABLE "Task"
ADD CONSTRAINT chk_task_status CHECK (
    status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'TRIED', 'SKIPPED', 'PENDING_ACCEPTANCE')
);
```

---

## Supabase Functions

### generate_invite_code

```sql
CREATE OR REPLACE FUNCTION generate_invite_code()
RETURNS TEXT AS $$
DECLARE
    new_code TEXT;
BEGIN
    -- Generate URL-safe 8-character code
    new_code := encode(gen_random_bytes(6), 'base64');
    new_code := replace(replace(new_code, '+', '-'), '/', '_');
    new_code := substring(new_code, 1, 8);
    RETURN new_code;
END;
$$ LANGUAGE plpgsql;
```

---

### create_invite

```sql
CREATE OR REPLACE FUNCTION create_invite(p_creator_id UUID)
RETURNS invites AS $$
DECLARE
    v_existing_partnership partnerships;
    v_existing_invite invites;
    v_new_invite invites;
    v_code TEXT;
BEGIN
    -- Check if user already has a partner
    SELECT * INTO v_existing_partnership
    FROM partnerships
    WHERE (user1_id = p_creator_id OR user2_id = p_creator_id)
    AND status = 'ACTIVE';

    IF FOUND THEN
        RAISE EXCEPTION 'User already has an active partnership';
    END IF;

    -- Check for existing pending invite
    SELECT * INTO v_existing_invite
    FROM invites
    WHERE creator_id = p_creator_id AND status = 'PENDING';

    IF FOUND THEN
        -- Return existing invite
        RETURN v_existing_invite;
    END IF;

    -- Generate new invite
    v_code := generate_invite_code();

    INSERT INTO invites (code, creator_id)
    VALUES (v_code, p_creator_id)
    RETURNING * INTO v_new_invite;

    RETURN v_new_invite;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

### accept_invite

```sql
CREATE OR REPLACE FUNCTION accept_invite(p_code TEXT, p_acceptor_id UUID)
RETURNS partnerships AS $$
DECLARE
    v_invite invites;
    v_existing_partnership partnerships;
    v_new_partnership partnerships;
    v_user1 UUID;
    v_user2 UUID;
BEGIN
    -- Lock and fetch invite
    SELECT * INTO v_invite
    FROM invites
    WHERE code = p_code
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Invalid invite code';
    END IF;

    IF v_invite.status != 'PENDING' THEN
        RAISE EXCEPTION 'Invite is no longer valid (status: %)', v_invite.status;
    END IF;

    IF v_invite.expires_at < NOW() THEN
        -- Mark as expired
        UPDATE invites SET status = 'EXPIRED' WHERE code = p_code;
        RAISE EXCEPTION 'Invite has expired';
    END IF;

    IF v_invite.creator_id = p_acceptor_id THEN
        RAISE EXCEPTION 'Cannot accept your own invite';
    END IF;

    -- Check if acceptor already has a partner
    SELECT * INTO v_existing_partnership
    FROM partnerships
    WHERE (user1_id = p_acceptor_id OR user2_id = p_acceptor_id)
    AND status = 'ACTIVE';

    IF FOUND THEN
        RAISE EXCEPTION 'You already have an active partnership';
    END IF;

    -- Check if creator still doesn't have a partner
    SELECT * INTO v_existing_partnership
    FROM partnerships
    WHERE (user1_id = v_invite.creator_id OR user2_id = v_invite.creator_id)
    AND status = 'ACTIVE';

    IF FOUND THEN
        UPDATE invites SET status = 'CANCELLED' WHERE code = p_code;
        RAISE EXCEPTION 'Inviter already has a partner';
    END IF;

    -- Order user IDs for constraint
    IF v_invite.creator_id < p_acceptor_id THEN
        v_user1 := v_invite.creator_id;
        v_user2 := p_acceptor_id;
    ELSE
        v_user1 := p_acceptor_id;
        v_user2 := v_invite.creator_id;
    END IF;

    -- Create partnership
    INSERT INTO partnerships (user1_id, user2_id)
    VALUES (v_user1, v_user2)
    RETURNING * INTO v_new_partnership;

    -- Mark invite as accepted
    UPDATE invites
    SET status = 'ACCEPTED', accepted_by = p_acceptor_id, accepted_at = NOW()
    WHERE code = p_code;

    -- Create notification for inviter
    INSERT INTO notifications (user_id, title, body, action_type, action_data)
    VALUES (
        v_invite.creator_id,
        'Partner Connected!',
        'Your partner has accepted your invite!',
        'INVITE_ACCEPTED',
        jsonb_build_object('partnership_id', v_new_partnership.id)
    );

    RETURN v_new_partnership;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

### dissolve_partnership

```sql
CREATE OR REPLACE FUNCTION dissolve_partnership(p_user_id UUID)
RETURNS VOID AS $$
DECLARE
    v_partnership partnerships;
    v_partner_id UUID;
BEGIN
    -- Find and lock partnership
    SELECT * INTO v_partnership
    FROM partnerships
    WHERE (user1_id = p_user_id OR user2_id = p_user_id)
    AND status = 'ACTIVE'
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'No active partnership found';
    END IF;

    -- Determine partner ID
    IF v_partnership.user1_id = p_user_id THEN
        v_partner_id := v_partnership.user2_id;
    ELSE
        v_partner_id := v_partnership.user1_id;
    END IF;

    -- Mark partnership as dissolved
    UPDATE partnerships
    SET status = 'DISSOLVED'
    WHERE id = v_partnership.id;

    -- Notify partner
    INSERT INTO notifications (user_id, title, body, action_type, action_data)
    VALUES (
        v_partner_id,
        'Partnership Ended',
        'Your partner has disconnected.',
        'PARTNER_DISCONNECTED',
        jsonb_build_object('partnership_id', v_partnership.id)
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

### get_partner

```sql
CREATE OR REPLACE FUNCTION get_partner(p_user_id UUID)
RETURNS TABLE (
    partner_id UUID,
    partner_name TEXT,
    partner_email TEXT,
    partnership_id UUID,
    connected_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        CASE
            WHEN p.user1_id = p_user_id THEN p.user2_id
            ELSE p.user1_id
        END as partner_id,
        pr.display_name as partner_name,
        u.email as partner_email,
        p.id as partnership_id,
        p.created_at as connected_at
    FROM partnerships p
    JOIN auth.users u ON u.id = CASE
        WHEN p.user1_id = p_user_id THEN p.user2_id
        ELSE p.user1_id
    END
    LEFT JOIN profiles pr ON pr.id = CASE
        WHEN p.user1_id = p_user_id THEN p.user2_id
        ELSE p.user1_id
    END
    WHERE (p.user1_id = p_user_id OR p.user2_id = p_user_id)
    AND p.status = 'ACTIVE';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

## Repository Interface Contracts

### PartnerRepository

```kotlin
interface PartnerRepository {
    /**
     * Get current partner for user.
     * @return Partner info or null if no active partnership
     */
    suspend fun getPartner(userId: String): Partner?

    /**
     * Observe current partner (reactive).
     * Emits null when no partner, Partner when connected.
     */
    fun observePartner(userId: String): Flow<Partner?>

    /**
     * Dissolve current partnership.
     * @throws PartnerException.NoPartnership if not connected
     */
    suspend fun dissolvePartnership(userId: String)

    /**
     * Check if user has active partnership.
     */
    suspend fun hasPartner(userId: String): Boolean
}

data class Partner(
    val id: String,
    val name: String,
    val email: String,
    val partnershipId: String,
    val connectedAt: Instant
)
```

---

### InviteRepository

```kotlin
interface InviteRepository {
    /**
     * Create or get existing invite.
     * @return Invite with shareable link
     * @throws InviteException.AlreadyHasPartner if user has partner
     */
    suspend fun createInvite(userId: String): Invite

    /**
     * Get user's active invite if exists.
     */
    suspend fun getActiveInvite(userId: String): Invite?

    /**
     * Validate invite code without accepting.
     * @return Invite info if valid
     * @throws InviteException.InvalidCode if not found
     * @throws InviteException.Expired if expired
     */
    suspend fun validateInvite(code: String): InviteInfo

    /**
     * Accept invite and create partnership.
     * @return Created partnership
     * @throws InviteException.InvalidCode if not found
     * @throws InviteException.Expired if expired
     * @throws InviteException.SelfInvite if own invite
     * @throws InviteException.AlreadyHasPartner if either user has partner
     */
    suspend fun acceptInvite(code: String, acceptorId: String): Partnership

    /**
     * Cancel user's pending invite.
     */
    suspend fun cancelInvite(userId: String)
}

data class Invite(
    val code: String,
    val link: String,  // "https://tandem.app/invite/{code}"
    val createdAt: Instant,
    val expiresAt: Instant
)

data class InviteInfo(
    val code: String,
    val creatorName: String,
    val creatorTaskPreview: List<TaskPreview>,
    val expiresAt: Instant
)
```

---

## Realtime Subscriptions

### Partner Tasks Channel

```kotlin
// Subscribe to partner's task changes
val channel = supabase.channel("partner-tasks-$partnerId")

val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
    table = "Task"
    filter = "created_by=eq.$partnerId"  // Tasks created by partner
}

// Also subscribe to tasks owned by partner (for completions)
val ownerChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
    table = "Task"
    filter = "owner_id=eq.$partnerId"
}
```

### Partnership Status Channel

```kotlin
// Subscribe to partnership changes (for disconnect detection)
val partnershipChannel = supabase.channel("partnership-$partnershipId")

val statusChanges = partnershipChannel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
    table = "partnerships"
    filter = "id=eq.$partnershipId"
}
```

---

## Edge Function: send-notification

**Trigger**: Database webhook on `notifications` INSERT

**Input**:
```json
{
  "type": "INSERT",
  "table": "notifications",
  "record": {
    "id": "uuid",
    "user_id": "uuid",
    "title": "string",
    "body": "string",
    "action_type": "string",
    "action_data": {}
  }
}
```

**Logic**:
1. Fetch user's `fcm_token` and `notifications_enabled` from profiles
2. Check notification type against user preferences
3. If allowed, send FCM message via HTTP v1 API
4. Update `notifications.sent_at` on success

**Output**: FCM message ID or error
