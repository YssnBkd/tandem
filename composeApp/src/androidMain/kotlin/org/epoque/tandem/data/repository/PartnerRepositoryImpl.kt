package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.Partner
import org.epoque.tandem.domain.model.PartnershipStatus
import org.epoque.tandem.domain.repository.PartnerException
import org.epoque.tandem.domain.repository.PartnerRepository

/**
 * Implementation of PartnerRepository using Supabase and SQLDelight.
 * Uses Supabase RPC functions for remote operations and SQLDelight for local cache.
 */
class PartnerRepositoryImpl(
    private val database: TandemDatabase,
    private val supabase: SupabaseClient
) : PartnerRepository {

    private val queries = database.partnershipQueries

    @Serializable
    private data class PartnerResponse(
        val partner_id: String,
        val partner_name: String?,
        val partner_email: String,
        val partnership_id: String,
        val connected_at: String
    )

    override suspend fun getPartner(userId: String): Partner? = withContext(Dispatchers.IO) {
        try {
            // Try remote first
            val response = supabase.postgrest.rpc(
                function = "get_partner",
                parameters = mapOf("p_user_id" to userId)
            ).decodeSingleOrNull<PartnerResponse>()

            if (response != null) {
                val partner = Partner(
                    id = response.partner_id,
                    name = response.partner_name ?: response.partner_email.substringBefore('@'),
                    email = response.partner_email,
                    partnershipId = response.partnership_id,
                    connectedAt = Instant.parse(response.connected_at)
                )

                // Cache the partnership locally
                cachePartnership(userId, response)

                return@withContext partner
            }
        } catch (e: Exception) {
            // Fall back to local cache
        }

        // Check local cache
        val localPartnership = queries.getActivePartnership(userId, userId).executeAsOneOrNull()
        localPartnership?.let { partnership ->
            // We don't have partner details in local cache, only partnership
            // Return null to indicate we need network
            null
        }
    }

    override fun observePartner(userId: String): Flow<Partner?> {
        return queries.getActivePartnership(userId, userId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { localPartnership ->
                if (localPartnership != null) {
                    // We have a cached partnership, try to get full partner details
                    getPartner(userId)
                } else {
                    null
                }
            }
    }

    override suspend fun dissolvePartnership(userId: String): Unit = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc(
                function = "dissolve_partnership",
                parameters = mapOf("p_user_id" to userId)
            )

            // Clear local cache
            val partnership = queries.getActivePartnership(userId, userId).executeAsOneOrNull()
            partnership?.let {
                queries.updatePartnershipStatus(PartnershipStatus.DISSOLVED, it.id)
            }
        } catch (e: Exception) {
            if (e.message?.contains("No active partnership found") == true) {
                throw PartnerException.NoPartnership
            }
            throw e
        }
    }

    override suspend fun hasPartner(userId: String): Boolean = withContext(Dispatchers.IO) {
        getPartner(userId) != null
    }

    private fun cachePartnership(userId: String, response: PartnerResponse) {
        val user1Id = if (userId < response.partner_id) userId else response.partner_id
        val user2Id = if (userId < response.partner_id) response.partner_id else userId

        queries.upsertPartnership(
            id = response.partnership_id,
            user1_id = user1Id,
            user2_id = user2Id,
            created_at = Instant.parse(response.connected_at),
            status = PartnershipStatus.ACTIVE
        )
    }
}
