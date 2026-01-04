package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

/**
 * Represents the connected partner's information.
 */
data class Partner(
    val id: String,
    val name: String,
    val email: String,
    val partnershipId: String,
    val connectedAt: Instant
)
