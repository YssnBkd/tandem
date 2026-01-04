package org.epoque.tandem.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalStatus
import org.epoque.tandem.domain.model.GoalType
import org.epoque.tandem.domain.repository.GoalRepository

/**
 * Android-specific implementation of GoalRepository that extends the base
 * implementation with Supabase sync capabilities for partner goal visibility.
 */
class GoalRepositoryAndroidImpl(
    private val database: TandemDatabase,
    private val supabase: SupabaseClient,
    private val baseRepository: GoalRepository
) : GoalRepository by baseRepository {

    private val partnerGoalQueries = database.partnerGoalQueries

    // Realtime sync state
    private var realtimeChannel: RealtimeChannel? = null
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Remote goal record structure from Supabase.
     */
    @Serializable
    private data class RemoteGoal(
        val id: String,
        @SerialName("owner_id") val ownerId: String,
        val name: String,
        val icon: String,
        val type: String,
        @SerialName("target_per_week") val targetPerWeek: Int? = null,
        @SerialName("target_total") val targetTotal: Int? = null,
        @SerialName("duration_weeks") val durationWeeks: Int? = null,
        @SerialName("start_week_id") val startWeekId: String,
        @SerialName("current_progress") val currentProgress: Int,
        @SerialName("current_week_id") val currentWeekId: String,
        val status: String,
        @SerialName("created_at") val createdAt: String,
        @SerialName("updated_at") val updatedAt: String
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SYNC OPERATIONS (Supabase)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun syncPartnerGoals(partnerId: String) = withContext(Dispatchers.IO) {
        try {
            // Fetch partner's goals from Supabase
            val remoteGoals = supabase.postgrest
                .from("goals")
                .select {
                    filter { eq("owner_id", partnerId) }
                }
                .decodeList<RemoteGoal>()

            // Update local cache
            val now = Clock.System.now()
            remoteGoals.forEach { remote ->
                upsertPartnerGoal(remote, now)
            }

            // Clean up goals that no longer exist remotely
            val remoteIds = remoteGoals.map { it.id }.toSet()
            val localGoals = partnerGoalQueries.getPartnerGoals(partnerId).executeAsList()
            localGoals.forEach { local ->
                if (local.id !in remoteIds) {
                    partnerGoalQueries.deletePartnerGoalById(local.id)
                }
            }
        } catch (e: Exception) {
            // Sync failure is non-fatal - we'll use cached data
            e.printStackTrace()
        }
    }

    /**
     * Start realtime subscription for partner goal changes.
     * Should be called when partner is detected.
     */
    suspend fun startPartnerGoalSync(partnerId: String) {
        // Stop any existing sync first
        stopPartnerGoalSync()

        withContext(Dispatchers.IO) {
            try {
                val channel = supabase.channel("partner-goals-$partnerId")

                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "goals"
                }

                changeFlow.onEach { action ->
                    try {
                        val now = Clock.System.now()
                        when (action) {
                            is PostgresAction.Insert -> {
                                val goal = action.decodeRecord<RemoteGoal>()
                                if (goal.ownerId == partnerId) {
                                    upsertPartnerGoal(goal, now)
                                }
                            }
                            is PostgresAction.Update -> {
                                val goal = action.decodeRecord<RemoteGoal>()
                                if (goal.ownerId == partnerId) {
                                    upsertPartnerGoal(goal, now)
                                }
                            }
                            is PostgresAction.Delete -> {
                                val oldGoal = action.decodeOldRecord<RemoteGoal>()
                                if (oldGoal.ownerId == partnerId) {
                                    partnerGoalQueries.deletePartnerGoalById(oldGoal.id)
                                }
                            }
                            else -> { /* Ignore Select actions */ }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.launchIn(syncScope)

                channel.subscribe()
                realtimeChannel = channel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Stop realtime subscription for partner goals.
     * Should be called when partner disconnects.
     */
    suspend fun stopPartnerGoalSync() {
        withContext(Dispatchers.IO) {
            try {
                realtimeChannel?.unsubscribe()
                realtimeChannel = null
            } catch (e: Exception) {
                // Ignore errors when stopping
            }
        }
    }

    private fun upsertPartnerGoal(remote: RemoteGoal, syncTime: Instant) {
        val goalType = parseGoalType(remote.type, remote.targetPerWeek, remote.targetTotal)

        partnerGoalQueries.upsertPartnerGoal(
            id = remote.id,
            name = remote.name,
            icon = remote.icon,
            type = goalType,
            target_per_week = remote.targetPerWeek?.toLong(),
            target_total = remote.targetTotal?.toLong(),
            duration_weeks = remote.durationWeeks?.toLong(),
            start_week_id = remote.startWeekId,
            owner_id = remote.ownerId,
            current_progress = remote.currentProgress.toLong(),
            current_week_id = remote.currentWeekId,
            status = parseGoalStatus(remote.status),
            created_at = Instant.parse(remote.createdAt),
            updated_at = Instant.parse(remote.updatedAt),
            synced_at = syncTime
        )
    }

    private fun parseGoalType(type: String, targetPerWeek: Int?, targetTotal: Int?): GoalType {
        return when (type.uppercase()) {
            "WEEKLY_HABIT" -> GoalType.WeeklyHabit(targetPerWeek ?: 1)
            "RECURRING_TASK" -> GoalType.RecurringTask
            "TARGET_AMOUNT" -> GoalType.TargetAmount(targetTotal ?: 1)
            else -> GoalType.RecurringTask
        }
    }

    private fun parseGoalStatus(status: String): GoalStatus {
        return when (status.uppercase()) {
            "ACTIVE" -> GoalStatus.ACTIVE
            "COMPLETED" -> GoalStatus.COMPLETED
            "EXPIRED" -> GoalStatus.EXPIRED
            else -> GoalStatus.ACTIVE
        }
    }
}
