package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a pending invitation to connect as partners.
 */
data class Invite(
    val code: String,
    val creatorId: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val acceptedBy: String?,
    val acceptedAt: Instant?,
    val status: InviteStatus
) {
    val link: String get() = "https://tandem.app/invite/$code"
    val isExpired: Boolean get() = status == InviteStatus.EXPIRED
    val isPending: Boolean get() = status == InviteStatus.PENDING
}

/**
 * Invite lifecycle states.
 */
enum class InviteStatus {
    /** Invite is active, waiting for acceptance */
    PENDING,
    /** Invite was accepted, partnership created */
    ACCEPTED,
    /** Invite passed expiration date */
    EXPIRED,
    /** Invite was cancelled by creator */
    CANCELLED
}
