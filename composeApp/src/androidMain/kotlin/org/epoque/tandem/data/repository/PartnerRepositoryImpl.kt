package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeOldRecord
import io.github.jan.supabase.realtime.decodeRecord
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.epoque.tandem.BuildConfig
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.seed.MockDataSeeder
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
            // DEBUG only: Return synthetic partner for mock data testing
            if (BuildConfig.DEBUG) {
                val partnerId = if (partnership.user1_id == userId) partnership.user2_id else partnership.user1_id
                // Check if this is our mock partner
                if (partnerId == MockDataSeeder.FAKE_PARTNER_ID) {
                    return@withContext Partner(
                        id = partnerId,
                        name = "Alex Partner",
                        email = "alex.partner@mock.tandem",
                        partnershipId = partnership.id,
                        connectedAt = partnership.created_at
                    )
                }
            }
            // Production: Return null to indicate we need network
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
     * Uses @SerialName for proper JSON deserialization with decodeRecord<T>().
     */
    @Serializable
    private data class RemoteTask(
        val id: String,
        val title: String,
        val notes: String? = null,
        @SerialName("owner_id") val ownerId: String,
        @SerialName("owner_type") val ownerType: String,
        @SerialName("week_id") val weekId: String,
        val status: String,
        @SerialName("created_by") val createdBy: String,
        @SerialName("request_note") val requestNote: String? = null,
        @SerialName("repeat_target") val repeatTarget: Long? = null,
        @SerialName("repeat_completed") val repeatCompleted: Long = 0,
        @SerialName("linked_goal_id") val linkedGoalId: String? = null,
        @SerialName("review_note") val reviewNote: String? = null,
        @SerialName("rolled_from_week_id") val rolledFromWeekId: String? = null,
        @SerialName("created_at") val createdAt: String,
        @SerialName("updated_at") val updatedAt: String
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

                // Collect changes and update local cache using proper decodeRecord API
                changeFlow.onEach { action ->
                    try {
                        when (action) {
                            is PostgresAction.Insert -> {
                                val task = action.decodeRecord<RemoteTask>()
                                if (task.ownerId == partnerId) {
                                    upsertLocalTask(task)
                                }
                            }
                            is PostgresAction.Update -> {
                                val task = action.decodeRecord<RemoteTask>()
                                if (task.ownerId == partnerId) {
                                    upsertLocalTask(task)
                                }
                            }
                            is PostgresAction.Delete -> {
                                // Note: Requires REPLICA IDENTITY FULL on tasks table
                                // to receive old record data on DELETE events
                                val oldTask = action.decodeOldRecord<RemoteTask>()
                                if (oldTask.ownerId == partnerId) {
                                    deleteLocalTask(oldTask.id)
                                }
                            }
                            else -> { /* Ignore Select actions */ }
                        }
                    } catch (e: Exception) {
                        // Log deserialization errors but continue processing
                        e.printStackTrace()
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
            owner_id = remote.ownerId,
            owner_type = parseOwnerType(remote.ownerType),
            week_id = remote.weekId,
            status = parseTaskStatus(remote.status),
            created_by = remote.createdBy,
            request_note = remote.requestNote,
            repeat_target = remote.repeatTarget,
            repeat_completed = remote.repeatCompleted,
            linked_goal_id = remote.linkedGoalId,
            review_note = remote.reviewNote,
            rolled_from_week_id = remote.rolledFromWeekId,
            created_at = Instant.parse(remote.createdAt),
            updated_at = Instant.parse(remote.updatedAt)
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
