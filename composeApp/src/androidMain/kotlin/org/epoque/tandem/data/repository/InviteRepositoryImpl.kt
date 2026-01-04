package org.epoque.tandem.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.Invite
import org.epoque.tandem.domain.model.InviteInfo
import org.epoque.tandem.domain.model.InviteStatus
import org.epoque.tandem.domain.model.Partnership
import org.epoque.tandem.domain.model.PartnershipStatus
import org.epoque.tandem.domain.model.TaskPreview
import org.epoque.tandem.domain.repository.InviteException
import org.epoque.tandem.domain.repository.InviteRepository

/**
 * Implementation of InviteRepository using Supabase and SQLDelight.
 */
class InviteRepositoryImpl(
    private val database: TandemDatabase,
    private val supabase: SupabaseClient
) : InviteRepository {

    private val inviteQueries = database.inviteQueries
    private val partnershipQueries = database.partnershipQueries

    @Serializable
    private data class InviteResponse(
        val code: String,
        val creator_id: String,
        val created_at: String,
        val expires_at: String,
        val accepted_by: String?,
        val accepted_at: String?,
        val status: String
    )

    @Serializable
    private data class PartnershipResponse(
        val id: String,
        val user1_id: String,
        val user2_id: String,
        val created_at: String,
        val status: String
    )

    override suspend fun createInvite(userId: String): Invite = withContext(Dispatchers.IO) {
        try {
            val response = supabase.postgrest.rpc(
                function = "create_invite",
                parameters = mapOf("p_creator_id" to userId)
            ).decodeSingle<InviteResponse>()

            val invite = response.toDomain()

            // Cache locally
            cacheInvite(invite)

            invite
        } catch (e: Exception) {
            if (e.message?.contains("already has an active partnership") == true) {
                throw InviteException.AlreadyHasPartner
            }
            throw e
        }
    }

    override suspend fun getActiveInvite(userId: String): Invite? = withContext(Dispatchers.IO) {
        // Check local cache first
        val localInvite = inviteQueries.getPendingInviteByCreator(userId).executeAsOneOrNull()
        if (localInvite != null) {
            return@withContext Invite(
                code = localInvite.code,
                creatorId = localInvite.creator_id,
                createdAt = localInvite.created_at,
                expiresAt = localInvite.expires_at,
                acceptedBy = localInvite.accepted_by,
                acceptedAt = localInvite.accepted_at,
                status = localInvite.status
            )
        }

        // No local cache, don't hit network for this passive check
        null
    }

    override suspend fun validateInvite(code: String): InviteInfo = withContext(Dispatchers.IO) {
        try {
            // Fetch invite details from Supabase
            val invite = supabase.postgrest["invites"]
                .select {
                    filter { eq("code", code) }
                }
                .decodeSingleOrNull<InviteResponse>()
                ?: throw InviteException.InvalidCode

            if (invite.status != "PENDING") {
                throw InviteException.InvalidCode
            }

            val expiresAt = Instant.parse(invite.expires_at)
            if (expiresAt < kotlinx.datetime.Clock.System.now()) {
                throw InviteException.Expired
            }

            // Get creator's name from profiles
            val creatorName = getCreatorName(invite.creator_id)

            // Get creator's task preview (simplified - just show we can do this)
            val taskPreview = emptyList<TaskPreview>()

            InviteInfo(
                code = invite.code,
                creatorName = creatorName,
                creatorTaskPreview = taskPreview,
                expiresAt = expiresAt
            )
        } catch (e: InviteException) {
            throw e
        } catch (e: Exception) {
            throw InviteException.InvalidCode
        }
    }

    override suspend fun acceptInvite(code: String, acceptorId: String): Partnership = withContext(Dispatchers.IO) {
        try {
            val response = supabase.postgrest.rpc(
                function = "accept_invite",
                parameters = mapOf(
                    "p_code" to code,
                    "p_acceptor_id" to acceptorId
                )
            ).decodeSingle<PartnershipResponse>()

            val partnership = Partnership(
                id = response.id,
                user1Id = response.user1_id,
                user2Id = response.user2_id,
                createdAt = Instant.parse(response.created_at),
                status = PartnershipStatus.valueOf(response.status)
            )

            // Cache partnership locally
            partnershipQueries.upsertPartnership(
                id = partnership.id,
                user1_id = partnership.user1Id,
                user2_id = partnership.user2Id,
                created_at = partnership.createdAt,
                status = partnership.status
            )

            // Clear local invite cache
            inviteQueries.deleteInvite(code)

            partnership
        } catch (e: Exception) {
            when {
                e.message?.contains("Invalid invite code") == true -> throw InviteException.InvalidCode
                e.message?.contains("expired") == true -> throw InviteException.Expired
                e.message?.contains("your own invite") == true -> throw InviteException.SelfInvite
                e.message?.contains("already have") == true -> throw InviteException.AlreadyHasPartner
                else -> throw e
            }
        }
    }

    override suspend fun cancelInvite(userId: String): Unit = withContext(Dispatchers.IO) {
        // Get pending invite
        val invite = inviteQueries.getPendingInviteByCreator(userId).executeAsOneOrNull()

        if (invite != null) {
            // Update remote
            try {
                supabase.postgrest["invites"]
                    .update({
                        set("status", "CANCELLED")
                    }) {
                        filter {
                            eq("code", invite.code)
                            eq("creator_id", userId)
                            eq("status", "PENDING")
                        }
                    }
            } catch (e: Exception) {
                // Ignore remote errors, still clear local
            }

            // Clear local cache
            inviteQueries.deleteInvite(invite.code)
        }
    }

    private fun InviteResponse.toDomain(): Invite = Invite(
        code = code,
        creatorId = creator_id,
        createdAt = Instant.parse(created_at),
        expiresAt = Instant.parse(expires_at),
        acceptedBy = accepted_by,
        acceptedAt = accepted_at?.let { Instant.parse(it) },
        status = InviteStatus.valueOf(status)
    )

    private fun cacheInvite(invite: Invite) {
        inviteQueries.upsertInvite(
            code = invite.code,
            creator_id = invite.creatorId,
            created_at = invite.createdAt,
            expires_at = invite.expiresAt,
            accepted_by = invite.acceptedBy,
            accepted_at = invite.acceptedAt,
            status = invite.status
        )
    }

    private suspend fun getCreatorName(creatorId: String): String {
        return try {
            @Serializable
            data class ProfileResponse(val display_name: String?, val id: String)

            val profile = supabase.postgrest["profiles"]
                .select {
                    filter { eq("id", creatorId) }
                }
                .decodeSingleOrNull<ProfileResponse>()

            profile?.display_name ?: "Your Partner"
        } catch (e: Exception) {
            "Your Partner"
        }
    }
}
