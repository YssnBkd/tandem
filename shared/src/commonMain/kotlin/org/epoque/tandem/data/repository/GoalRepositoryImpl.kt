package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.Goal
import org.epoque.tandem.domain.model.GoalProgress
import org.epoque.tandem.domain.model.GoalStatus
import org.epoque.tandem.domain.model.GoalType
import org.epoque.tandem.domain.repository.GoalException
import org.epoque.tandem.domain.repository.GoalRepository
import org.epoque.tandem.domain.util.WeekCalculator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * SQLDelight implementation of GoalRepository.
 * Handles goal CRUD operations, progress tracking, and partner goal caching.
 */
@OptIn(ExperimentalUuidApi::class)
class GoalRepositoryImpl(
    private val database: TandemDatabase
) : GoalRepository {

    private val goalQueries = database.goalQueries
    private val progressQueries = database.goalProgressQueries
    private val partnerGoalQueries = database.partnerGoalQueries
    private val taskQueries = database.taskQueries

    companion object {
        private const val MAX_ACTIVE_GOALS = 10
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun org.epoque.tandem.data.local.Goal.toDomain(): Goal {
        return Goal(
            id = id,
            name = name,
            icon = icon,
            type = type,
            durationWeeks = duration_weeks?.toInt(),
            startWeekId = start_week_id,
            ownerId = owner_id,
            currentProgress = current_progress.toInt(),
            currentWeekId = current_week_id,
            status = status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }

    private fun org.epoque.tandem.data.local.GoalProgress.toDomain(): GoalProgress {
        return GoalProgress(
            id = id,
            goalId = goal_id,
            weekId = week_id,
            progressValue = progress_value.toInt(),
            targetValue = target_value.toInt(),
            createdAt = created_at
        )
    }

    private fun org.epoque.tandem.data.local.PartnerGoal.toDomain(): Goal {
        return Goal(
            id = id,
            name = name,
            icon = icon,
            type = type,
            durationWeeks = duration_weeks?.toInt(),
            startWeekId = start_week_id,
            ownerId = owner_id,
            currentProgress = current_progress.toInt(),
            currentWeekId = current_week_id,
            status = status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }

    // Helper to get target value from GoalType
    private fun getTargetValue(type: GoalType): Int = when (type) {
        is GoalType.WeeklyHabit -> type.targetPerWeek
        is GoalType.RecurringTask -> 1
        is GoalType.TargetAmount -> type.targetTotal
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS - Own Goals (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    override fun observeMyGoals(userId: String): Flow<List<Goal>> {
        return goalQueries.getMyGoals(userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { goals -> goals.map { it.toDomain() } }
    }

    override fun observeMyActiveGoals(userId: String): Flow<List<Goal>> {
        return goalQueries.getMyActiveGoals(userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { goals -> goals.map { it.toDomain() } }
    }

    override fun observeGoal(goalId: String): Flow<Goal?> {
        return goalQueries.getGoalById(goalId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }
    }

    override fun observeProgressHistory(goalId: String): Flow<List<GoalProgress>> {
        return progressQueries.getProgressForGoal(goalId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { progress -> progress.map { it.toDomain() } }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS - Partner Goals (Read-Only)
    // ═══════════════════════════════════════════════════════════════════════════

    override fun observePartnerGoals(partnerId: String): Flow<List<Goal>> {
        return partnerGoalQueries.getPartnerGoals(partnerId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { goals -> goals.map { it.toDomain() } }
    }

    override suspend fun getPartnerGoalById(goalId: String): Goal? = withContext(Dispatchers.IO) {
        partnerGoalQueries.getPartnerGoalById(goalId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getPartnerGoalsLastSyncTime(partnerId: String): Instant? =
        withContext(Dispatchers.IO) {
            partnerGoalQueries.getLastSyncTime(partnerId).executeAsOneOrNull()?.MAX
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun getGoalById(goalId: String): Goal? = withContext(Dispatchers.IO) {
        goalQueries.getGoalById(goalId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getActiveGoalCount(userId: String): Int = withContext(Dispatchers.IO) {
        goalQueries.countActiveGoalsForUser(userId).executeAsOne().toInt()
    }

    override suspend fun getGoalsById(goalIds: List<String>): Map<String, Goal> =
        withContext(Dispatchers.IO) {
            if (goalIds.isEmpty()) return@withContext emptyMap()

            goalQueries.getGoalsByIds(goalIds)
                .executeAsList()
                .associate { it.id to it.toDomain() }
        }

    override suspend fun getActiveGoalsForSuggestions(userId: String): List<Goal> =
        withContext(Dispatchers.IO) {
            goalQueries.getMyActiveGoals(userId)
                .executeAsList()
                .map { it.toDomain() }
                .filter { goal ->
                    // Only suggest goals that haven't met their weekly/total target yet
                    !goal.hasMetTarget
                }
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS (Own Goals Only)
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun createGoal(goal: Goal): Goal = withContext(Dispatchers.IO) {
        // Validate goal limit
        val activeCount = goalQueries.countActiveGoalsForUser(goal.ownerId).executeAsOne()
        if (activeCount >= MAX_ACTIVE_GOALS) {
            throw GoalException.LimitExceeded
        }

        // Validate goal data
        if (goal.name.isBlank()) {
            throw GoalException.InvalidGoal("Goal name cannot be empty")
        }
        if (goal.name.length > 100) {
            throw GoalException.InvalidGoal("Goal name cannot exceed 100 characters")
        }

        val now = Clock.System.now()
        val id = Uuid.random().toString()

        val newGoal = goal.copy(
            id = id,
            createdAt = now,
            updatedAt = now
        )

        // Extract target values from GoalType for storage
        val targetPerWeek: Long? = when (newGoal.type) {
            is GoalType.WeeklyHabit -> newGoal.type.targetPerWeek.toLong()
            else -> null
        }
        val targetTotal: Long? = when (newGoal.type) {
            is GoalType.TargetAmount -> newGoal.type.targetTotal.toLong()
            else -> null
        }

        goalQueries.upsertGoal(
            id = newGoal.id,
            name = newGoal.name,
            icon = newGoal.icon,
            type = newGoal.type,
            target_per_week = targetPerWeek,
            target_total = targetTotal,
            duration_weeks = newGoal.durationWeeks?.toLong(),
            start_week_id = newGoal.startWeekId,
            owner_id = newGoal.ownerId,
            current_progress = newGoal.currentProgress.toLong(),
            current_week_id = newGoal.currentWeekId,
            status = newGoal.status,
            created_at = newGoal.createdAt,
            updated_at = newGoal.updatedAt
        )

        newGoal
    }

    override suspend fun updateGoal(goalId: String, name: String, icon: String): Goal? =
        withContext(Dispatchers.IO) {
            if (name.isBlank()) {
                throw GoalException.InvalidGoal("Goal name cannot be empty")
            }

            val now = Clock.System.now()
            goalQueries.updateGoal(
                name = name,
                icon = icon,
                updated_at = now,
                id = goalId
            )

            goalQueries.getGoalById(goalId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun incrementProgress(goalId: String, amount: Int): Goal? =
        withContext(Dispatchers.IO) {
            val existing = goalQueries.getGoalById(goalId).executeAsOneOrNull()
                ?: return@withContext null

            val now = Clock.System.now()
            val newProgress = existing.current_progress + amount
            val currentWeekId = WeekCalculator.getWeekId()

            goalQueries.updateProgress(
                current_progress = newProgress,
                current_week_id = currentWeekId,
                updated_at = now,
                id = goalId
            )

            // Check if goal should be marked as completed (for TargetAmount)
            val goal = goalQueries.getGoalById(goalId).executeAsOneOrNull()?.toDomain()
            if (goal != null && goal.type is GoalType.TargetAmount && goal.hasMetTarget) {
                goalQueries.updateStatus(
                    status = GoalStatus.COMPLETED,
                    updated_at = now,
                    id = goalId
                )
            }

            goalQueries.getGoalById(goalId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun updateStatus(goalId: String, status: GoalStatus): Goal? =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            goalQueries.updateStatus(
                status = status,
                updated_at = now,
                id = goalId
            )
            goalQueries.getGoalById(goalId).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun recordWeeklyProgress(
        goalId: String,
        progressValue: Int,
        targetValue: Int,
        weekId: String
    ) = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        val id = Uuid.random().toString()

        progressQueries.insertProgress(
            id = id,
            goal_id = goalId,
            week_id = weekId,
            progress_value = progressValue.toLong(),
            target_value = targetValue.toLong(),
            created_at = now
        )
    }

    override suspend fun resetWeeklyProgress(goalId: String, newWeekId: String) =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()
            goalQueries.resetWeeklyProgress(
                current_week_id = newWeekId,
                updated_at = now,
                id = goalId
            )
        }

    override suspend fun deleteGoal(goalId: String): Boolean = withContext(Dispatchers.IO) {
        val exists = goalQueries.getGoalById(goalId).executeAsOneOrNull() != null
        if (exists) {
            // Clear linkedGoalId from tasks first
            clearLinkedGoalFromTasks(goalId)
            // Delete goal (progress will cascade)
            goalQueries.deleteGoalById(goalId)
        }
        exists
    }

    override suspend fun clearLinkedGoalFromTasks(goalId: String) = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        taskQueries.clearLinkedGoalIdForGoal(now, goalId)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYNC OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun syncPartnerGoals(partnerId: String) {
        // TODO: Implement Supabase sync in Phase 10
        // For now, this is a no-op since we don't have the Supabase client here
        // Partner goals will be synced when Realtime subscription receives updates
    }

    override suspend fun clearPartnerGoalCache(partnerId: String) = withContext(Dispatchers.IO) {
        partnerGoalQueries.clearPartnerGoals(partnerId)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAINTENANCE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    override suspend fun processWeeklyResets(currentWeekId: String) = withContext(Dispatchers.IO) {
        val goalsNeedingReset = goalQueries.getGoalsNeedingWeeklyReset(currentWeekId)
            .executeAsList()

        goalsNeedingReset.forEach { goal ->
            // Record the previous week's progress before resetting
            recordWeeklyProgress(
                goalId = goal.id,
                progressValue = goal.current_progress.toInt(),
                targetValue = getTargetValue(goal.type),
                weekId = goal.current_week_id
            )

            // Reset progress for the new week
            resetWeeklyProgress(goal.id, currentWeekId)
        }
    }

    override suspend fun checkGoalExpirations(currentWeekId: String) = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        val goalsWithDuration = goalQueries.getExpiredGoals().executeAsList()

        goalsWithDuration.forEach { goal ->
            val durationWeeks = goal.duration_weeks?.toInt() ?: return@forEach
            val endWeekId = WeekCalculator.calculateEndWeekId(goal.start_week_id, durationWeeks)

            if (WeekCalculator.isAfter(currentWeekId, endWeekId)) {
                // Calculate hasMetTarget directly without converting to domain
                val target = getTargetValue(goal.type)
                val hasMetTarget = goal.current_progress.toInt() >= target

                val newStatus = if (hasMetTarget) {
                    GoalStatus.COMPLETED
                } else {
                    GoalStatus.EXPIRED
                }

                goalQueries.updateStatus(
                    status = newStatus,
                    updated_at = now,
                    id = goal.id
                )
            }
        }
    }
}
