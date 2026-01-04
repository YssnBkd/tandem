package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.Partner

/**
 * Repository for managing partner connections.
 */
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
