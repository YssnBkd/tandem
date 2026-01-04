package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Partner
import org.epoque.tandem.domain.model.PartnershipStatus
import org.epoque.tandem.domain.model.TaskStatus
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
    private val taskQueries = database.taskQueries

    // Realtime sync state
    private var realtimeChannel: RealtimeChannel? = null
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    // ═══════════════════════════════════════════════════════════════════════════
    // REALTIME SYNC
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Remote task record structure from Supabase.
     */
    @Serializable
    private data class RemoteTask(
        val id: String,
        val title: String,
        val notes: String? = null,
        val owner_id: String,
        val owner_type: String,
        val week_id: String,
        val status: String,
        val created_by: String,
        val request_note: String? = null,
        val repeat_target: Long? = null,
        val repeat_completed: Long = 0,
        val linked_goal_id: String? = null,
        val review_note: String? = null,
        val rolled_from_week_id: String? = null,
        val created_at: String,
        val updated_at: String
    )

    override suspend fun startPartnerTaskSync(userId: String, partnerId: String) {
        // Stop any existing sync first
        stopPartnerTaskSync()

        withContext(Dispatchers.IO) {
            try {
                // Create a channel for partner task sync
                val channel = supabase.channel("partner-tasks-$partnerId")

                // Subscribe to all changes on tasks table
                // Note: Filtering is done client-side as the filter API may vary by version
                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "tasks"
                }

                // Collect changes and update local cache
                changeFlow.onEach { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            val record = action.record
                            if (record["owner_id"]?.toString()?.trim('"') == partnerId) {
                                parseRemoteTask(record)?.let { upsertLocalTask(it) }
                            }
                        }
                        is PostgresAction.Update -> {
                            val record = action.record
                            if (record["owner_id"]?.toString()?.trim('"') == partnerId) {
                                parseRemoteTask(record)?.let { upsertLocalTask(it) }
                            }
                        }
                        is PostgresAction.Delete -> {
                            val oldRecord = action.oldRecord
                            val taskId = oldRecord["id"]?.toString()?.trim('"')
                            if (taskId != null && oldRecord["owner_id"]?.toString()?.trim('"') == partnerId) {
                                deleteLocalTask(taskId)
                            }
                        }
                        else -> { /* Ignore Select actions */ }
                    }
                }.launchIn(syncScope)

                // Subscribe to the channel
                channel.subscribe()
                realtimeChannel = channel
            } catch (e: Exception) {
                // Log error but don't crash - realtime sync is a nice-to-have
                e.printStackTrace()
            }
        }
    }

    /**
     * Parse a remote task record from JSON map.
     */
    private fun parseRemoteTask(record: Map<String, Any?>): RemoteTask? {
        return try {
            RemoteTask(
                id = record["id"]?.toString()?.trim('"') ?: return null,
                title = record["title"]?.toString()?.trim('"') ?: return null,
                notes = record["notes"]?.toString()?.trim('"'),
                owner_id = record["owner_id"]?.toString()?.trim('"') ?: return null,
                owner_type = record["owner_type"]?.toString()?.trim('"') ?: "SELF",
                week_id = record["week_id"]?.toString()?.trim('"') ?: return null,
                status = record["status"]?.toString()?.trim('"') ?: "PENDING",
                created_by = record["created_by"]?.toString()?.trim('"') ?: return null,
                request_note = record["request_note"]?.toString()?.trim('"'),
                repeat_target = record["repeat_target"]?.toString()?.toLongOrNull(),
                repeat_completed = record["repeat_completed"]?.toString()?.toLongOrNull() ?: 0,
                linked_goal_id = record["linked_goal_id"]?.toString()?.trim('"'),
                review_note = record["review_note"]?.toString()?.trim('"'),
                rolled_from_week_id = record["rolled_from_week_id"]?.toString()?.trim('"'),
                created_at = record["created_at"]?.toString()?.trim('"') ?: return null,
                updated_at = record["updated_at"]?.toString()?.trim('"') ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun stopPartnerTaskSync() {
        withContext(Dispatchers.IO) {
            try {
                realtimeChannel?.unsubscribe()
                realtimeChannel = null
            } catch (e: Exception) {
                // Ignore errors when stopping
            }
        }
    }

    private fun upsertLocalTask(remote: RemoteTask) {
        taskQueries.upsertTask(
            id = remote.id,
            title = remote.title,
            notes = remote.notes,
            owner_id = remote.owner_id,
            owner_type = parseOwnerType(remote.owner_type),
            week_id = remote.week_id,
            status = parseTaskStatus(remote.status),
            created_by = remote.created_by,
            request_note = remote.request_note,
            repeat_target = remote.repeat_target,
            repeat_completed = remote.repeat_completed,
            linked_goal_id = remote.linked_goal_id,
            review_note = remote.review_note,
            rolled_from_week_id = remote.rolled_from_week_id,
            created_at = Instant.parse(remote.created_at),
            updated_at = Instant.parse(remote.updated_at)
        )
    }

    private fun deleteLocalTask(taskId: String) {
        taskQueries.deleteTaskById(taskId)
    }

    private fun parseOwnerType(value: String): OwnerType = when (value.uppercase()) {
        "SELF" -> OwnerType.SELF
        "PARTNER" -> OwnerType.PARTNER
        "SHARED" -> OwnerType.SHARED
        else -> OwnerType.SELF
    }

    private fun parseTaskStatus(value: String): TaskStatus = when (value.uppercase()) {
        "PENDING" -> TaskStatus.PENDING
        "COMPLETED" -> TaskStatus.COMPLETED
        "TRIED" -> TaskStatus.TRIED
        "SKIPPED" -> TaskStatus.SKIPPED
        "DECLINED" -> TaskStatus.DECLINED
        "PENDING_ACCEPTANCE" -> TaskStatus.PENDING_ACCEPTANCE
        else -> TaskStatus.PENDING
    }
}
