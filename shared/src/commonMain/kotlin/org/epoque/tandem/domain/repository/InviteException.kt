package org.epoque.tandem.domain.repository

/**
 * Exceptions thrown by InviteRepository operations.
 */
sealed class InviteException(message: String) : Exception(message) {
    /** Invite code not found */
    data object InvalidCode : InviteException("Invalid invite code")

    /** Invite has expired */
    data object Expired : InviteException("Invite has expired")

    /** Cannot accept your own invite */
    data object SelfInvite : InviteException("Cannot accept your own invite")

    /** User already has an active partnership */
    data object AlreadyHasPartner : InviteException("Already has an active partnership")
}
