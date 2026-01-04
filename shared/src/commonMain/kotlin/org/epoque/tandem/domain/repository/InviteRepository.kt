package org.epoque.tandem.domain.repository

import org.epoque.tandem.domain.model.Invite
import org.epoque.tandem.domain.model.InviteInfo
import org.epoque.tandem.domain.model.Partnership

/**
 * Repository for managing partner invites.
 */
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
