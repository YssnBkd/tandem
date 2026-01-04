package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

/**
 * Represents an active connection between two users (partners).
 */
data class Partnership(
    val id: String,
    val user1Id: String,
    val user2Id: String,
    val createdAt: Instant,
    val status: PartnershipStatus
)

/**
 * Partnership lifecycle states.
 */
enum class PartnershipStatus {
    /** Partnership is active, both users connected */
    ACTIVE,
    /** Partnership ended (either user disconnected) */
    DISSOLVED
}
