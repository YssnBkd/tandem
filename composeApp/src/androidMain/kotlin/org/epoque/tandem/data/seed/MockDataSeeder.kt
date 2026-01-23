package org.epoque.tandem.data.seed

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.GoalStatus
import org.epoque.tandem.domain.model.GoalType
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.PartnershipStatus
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.model.toDbString
import org.epoque.tandem.domain.util.WeekCalculator
import kotlin.random.Random

/**
 * Mock data seeder for testing purposes.
 *
 * Creates realistic test data including:
 * - 22 weeks of history for both user and partner
 * - Tasks with varied statuses and owner types
 * - Goals of all types with progress snapshots
 * - Partnership record
 *
 * DEBUG builds only. This file should be deleted before production.
 */
class MockDataSeeder(
    private val database: TandemDatabase,
    private val seedPreferences: SeedPreferences
) {
    companion object {
        const val FAKE_PARTNER_ID = "mock-partner-00000000-0000-0000-0000-000000000001"
        const val FAKE_PARTNER_NAME = "Alex Partner"
        private const val WEEKS_TO_GENERATE = 22
        private const val TASKS_PER_WEEK_MIN = 5
        private const val TASKS_PER_WEEK_MAX = 8
        private const val GOALS_PER_USER = 6
    }

    private val random = Random(System.currentTimeMillis())
    private val now = Clock.System.now()
    private val tz = TimeZone.currentSystemDefault()

    /**
     * Seeds mock data if not already seeded for this user.
     * Safe to call multiple times - will only seed once.
     */
    suspend fun seedIfNeeded(userId: String) {
        if (seedPreferences.isSeededSync()) {
            return
        }
        seedData(userId)
        seedPreferences.markSeeded(userId)
    }

    /**
     * Seeds all mock data in a single transaction.
     */
    private fun seedData(userId: String) {
        database.transaction {
            val weekIds = generateWeekIds()
            val currentWeekId = weekIds.first()

            // Seed partnership first
            seedPartnership(userId)

            // Seed weeks for both users
            val userReviewedWeeks = seedWeeks(userId, weekIds)
            val partnerReviewedWeeks = seedWeeks(FAKE_PARTNER_ID, weekIds)

            // Seed goals for both users
            val userGoals = seedGoals(userId, currentWeekId)
            val partnerGoals = seedGoals(FAKE_PARTNER_ID, currentWeekId)

            // Seed tasks for both users (pass goal IDs for linking)
            seedTasks(userId, weekIds, userGoals.map { it.first }, userReviewedWeeks)
            seedTasks(FAKE_PARTNER_ID, weekIds, partnerGoals.map { it.first }, partnerReviewedWeeks)

            // Seed goal progress snapshots
            userGoals.forEach { (goalId, goalType, startWeekIdx) ->
                seedGoalProgress(goalId, goalType, weekIds, startWeekIdx)
            }
            partnerGoals.forEach { (goalId, goalType, startWeekIdx) ->
                seedGoalProgress(goalId, goalType, weekIds, startWeekIdx)
            }
        }
    }

    /**
     * Generate week IDs from current week back to WEEKS_TO_GENERATE weeks ago.
     * Returns list ordered from most recent (current) to oldest.
     */
    private fun generateWeekIds(): List<String> {
        val currentWeekId = WeekCalculator.getWeekId()
        val weekIds = mutableListOf(currentWeekId)

        var weekDate = WeekCalculator.parseWeekId(currentWeekId)
        repeat(WEEKS_TO_GENERATE - 1) {
            weekDate = weekDate.minus(1, DateTimeUnit.WEEK)
            weekIds.add(WeekCalculator.getWeekId(weekDate))
        }

        return weekIds
    }

    /**
     * Seeds partnership between user and fake partner.
     */
    private fun seedPartnership(userId: String) {
        val partnershipId = "partnership-${userId.take(8)}"

        // Ensure user1_id < user2_id for constraint
        val (user1, user2) = if (userId < FAKE_PARTNER_ID) {
            userId to FAKE_PARTNER_ID
        } else {
            FAKE_PARTNER_ID to userId
        }

        val createdAt = now.minus(150, DateTimeUnit.DAY, tz)

        database.partnershipQueries.upsertPartnership(
            id = partnershipId,
            user1_id = user1,
            user2_id = user2,
            created_at = createdAt,
            status = PartnershipStatus.ACTIVE
        )
    }

    /**
     * Seeds weeks for a user.
     * Returns set of week indices that were marked as reviewed.
     *
     * Creates 18 consecutive reviewed weeks to establish streak,
     * with a gap at week 8 to test streak breaking.
     */
    private fun seedWeeks(userId: String, weekIds: List<String>): Set<Int> {
        val reviewedWeeks = mutableSetOf<Int>()

        weekIds.forEachIndexed { index, weekId ->
            val weekDate = WeekCalculator.parseWeekId(weekId)
            val endDate = weekDate.plus(6, DateTimeUnit.DAY)

            // Current week (index 0) is not reviewed
            // Week 8 is also not reviewed (to break streak)
            // All other past weeks are reviewed
            val isReviewed = index > 0 && index != 8

            val rating = if (isReviewed) weightedRandomRating() else null
            val reviewNote = if (isReviewed && random.nextFloat() < 0.3f) {
                randomReviewNote()
            } else null
            val reviewedAt = if (isReviewed) {
                endDate.toInstantAtEndOfWeek()
            } else null
            val planningCompletedAt = if (index > 0) {
                weekDate.toInstantAtStartOfWeek()
            } else null

            if (isReviewed) reviewedWeeks.add(index)

            database.weekQueries.upsertWeek(
                id = weekId,
                start_date = weekDate,
                end_date = endDate,
                user_id = userId,
                overall_rating = rating?.toLong(),
                review_note = reviewNote,
                reviewed_at = reviewedAt,
                planning_completed_at = planningCompletedAt
            )
        }

        return reviewedWeeks
    }

    /**
     * Seeds tasks for a user across all weeks.
     * Returns nothing, but creates 5-8 tasks per week with varied:
     * - Status distribution: 60% COMPLETED, 15% TRIED, 10% SKIPPED, 15% PENDING
     * - Owner types: 70% SELF, 20% PARTNER, 10% SHARED
     * - Some repeating tasks with targets
     * - Some linked to goals
     */
    private fun seedTasks(
        userId: String,
        weekIds: List<String>,
        goalIds: List<String>,
        reviewedWeeks: Set<Int>
    ) {
        weekIds.forEachIndexed { weekIndex, weekId ->
            val taskCount = random.nextInt(TASKS_PER_WEEK_MIN, TASKS_PER_WEEK_MAX + 1)
            val isCurrentWeek = weekIndex == 0

            repeat(taskCount) { taskIndex ->
                val taskId = "task-$userId-$weekId-$taskIndex"
                val title = randomTaskTitle(taskIndex)
                val notes = if (random.nextFloat() < 0.2f) randomTaskNote() else null

                // Status based on whether week is reviewed
                val status = if (isCurrentWeek) {
                    // Current week: mostly pending, some in progress
                    if (random.nextFloat() < 0.3f) TaskStatus.COMPLETED else TaskStatus.PENDING
                } else if (weekIndex in reviewedWeeks) {
                    // Reviewed weeks: realistic distribution
                    randomTaskStatus()
                } else {
                    // Unreviewed past weeks: mostly pending
                    TaskStatus.PENDING
                }

                val ownerType = randomOwnerType()
                val createdBy = if (ownerType == OwnerType.PARTNER) FAKE_PARTNER_ID else userId

                // Some tasks are repeating
                val isRepeating = random.nextFloat() < 0.2f
                val repeatTarget = if (isRepeating) random.nextInt(2, 6).toLong() else null
                val repeatCompleted = if (isRepeating) {
                    random.nextInt(0, (repeatTarget?.toInt() ?: 1) + 1).toLong()
                } else 0L

                // Some tasks linked to goals
                val linkedGoalId = if (random.nextFloat() < 0.15f && goalIds.isNotEmpty()) {
                    goalIds.random()
                } else null

                val reviewNote = if (status == TaskStatus.TRIED && random.nextFloat() < 0.4f) {
                    randomTaskReviewNote()
                } else null

                val weekDate = WeekCalculator.parseWeekId(weekId)
                val createdAt = weekDate.toInstantAtStartOfWeek()
                    .plus(random.nextInt(0, 3), DateTimeUnit.DAY, tz)
                val updatedAt = if (status != TaskStatus.PENDING) {
                    createdAt.plus(random.nextInt(1, 5), DateTimeUnit.DAY, tz)
                } else createdAt

                database.taskQueries.upsertTask(
                    id = taskId,
                    title = title,
                    notes = notes,
                    owner_id = userId,
                    owner_type = ownerType,
                    week_id = weekId,
                    status = status,
                    created_by = createdBy,
                    request_note = if (ownerType == OwnerType.PARTNER) "Can you help with this?" else null,
                    repeat_target = repeatTarget,
                    repeat_completed = repeatCompleted,
                    linked_goal_id = linkedGoalId,
                    review_note = reviewNote,
                    rolled_from_week_id = null,
                    priority = TaskPriority.entries.random(),
                    scheduled_date = null,
                    scheduled_time = null,
                    deadline = null,
                    parent_task_id = null,
                    labels = null,
                    created_at = createdAt,
                    updated_at = updatedAt
                )
            }
        }
    }

    /**
     * Seeds goals for a user.
     * Returns list of (goalId, goalType, startWeekIndex) for progress seeding.
     *
     * Creates 6 goals covering all types:
     * - 2 WeeklyHabit (different targets)
     * - 2 TargetAmount (different totals)
     * - 2 RecurringTask
     */
    private fun seedGoals(
        userId: String,
        currentWeekId: String
    ): List<Triple<String, GoalType, Int>> {
        data class GoalDef(
            val name: String,
            val icon: String,
            val type: GoalType,
            val durationWeeks: Int?,
            val startWeekOffset: Int, // weeks ago from current
            val status: GoalStatus
        )

        val goalDefs = listOf(
            GoalDef("Exercise 3x weekly", "🏃", GoalType.WeeklyHabit(3), 12, 10, GoalStatus.ACTIVE),
            GoalDef("Meditate daily", "🧘", GoalType.WeeklyHabit(7), null, 8, GoalStatus.ACTIVE),
            GoalDef("Read 12 books", "📚", GoalType.TargetAmount(12), 52, 20, GoalStatus.ACTIVE),
            GoalDef("Weekly date night", "💑", GoalType.RecurringTask, null, 15, GoalStatus.ACTIVE),
            GoalDef("Save $5000", "💰", GoalType.TargetAmount(5000), 26, 18, GoalStatus.ACTIVE),
            GoalDef("Learn Spanish", "🇪🇸", GoalType.WeeklyHabit(5), 8, 6, GoalStatus.COMPLETED)
        )

        return goalDefs.mapIndexed { index, def ->
            val goalId = "goal-$userId-$index"

            // Calculate start week
            val startDate = WeekCalculator.parseWeekId(currentWeekId)
                .minus(def.startWeekOffset, DateTimeUnit.WEEK)
            val startWeekId = WeekCalculator.getWeekId(startDate)

            // Calculate current progress based on type and age
            val weeksActive = def.startWeekOffset
            val currentProgress = when (def.type) {
                is GoalType.WeeklyHabit -> {
                    // For weekly habits, current progress is just this week's count
                    random.nextInt(0, def.type.targetPerWeek + 1)
                }
                is GoalType.RecurringTask -> {
                    // For recurring tasks, it's either done (1) or not (0) this week
                    if (random.nextFloat() < 0.7f) 1 else 0
                }
                is GoalType.TargetAmount -> {
                    // For target amounts, accumulate based on time
                    val weeklyRate = def.type.targetTotal / 52.0
                    (weeklyRate * weeksActive * random.nextDouble(0.7, 1.1)).toInt()
                        .coerceAtMost(def.type.targetTotal)
                }
            }

            val createdAt = startDate.toInstantAtStartOfWeek()
            val updatedAt = now.minus(random.nextInt(0, 7), DateTimeUnit.DAY, tz)

            database.goalQueries.upsertGoal(
                id = goalId,
                name = def.name,
                icon = def.icon,
                type = def.type,
                target_per_week = when (def.type) {
                    is GoalType.WeeklyHabit -> def.type.targetPerWeek.toLong()
                    else -> null
                },
                target_total = when (def.type) {
                    is GoalType.TargetAmount -> def.type.targetTotal.toLong()
                    else -> null
                },
                duration_weeks = def.durationWeeks?.toLong(),
                start_week_id = startWeekId,
                owner_id = userId,
                current_progress = currentProgress.toLong(),
                current_week_id = currentWeekId,
                status = def.status,
                created_at = createdAt,
                updated_at = updatedAt
            )

            Triple(goalId, def.type, def.startWeekOffset)
        }
    }

    /**
     * Seeds goal progress snapshots for historical weeks.
     */
    private fun seedGoalProgress(
        goalId: String,
        goalType: GoalType,
        weekIds: List<String>,
        startWeekOffset: Int
    ) {
        // Only create progress for weeks after the goal was created
        val relevantWeeks = weekIds.take(startWeekOffset.coerceAtMost(weekIds.size))

        relevantWeeks.forEachIndexed { index, weekId ->
            // Skip current week (index 0) - no snapshot yet
            if (index == 0) return@forEachIndexed

            val progressId = "progress-$goalId-$weekId"
            val weekDate = WeekCalculator.parseWeekId(weekId)

            val (progressValue, targetValue) = when (goalType) {
                is GoalType.WeeklyHabit -> {
                    val target = goalType.targetPerWeek
                    // Sometimes hit target, sometimes not
                    val progress = if (random.nextFloat() < 0.7f) {
                        random.nextInt(target - 1, target + 1).coerceIn(0, target)
                    } else {
                        random.nextInt(0, target)
                    }
                    progress to target
                }
                is GoalType.RecurringTask -> {
                    val done = if (random.nextFloat() < 0.8f) 1 else 0
                    done to 1
                }
                is GoalType.TargetAmount -> {
                    val total = goalType.targetTotal
                    // Progress accumulates over time
                    val weeksFromStart = startWeekOffset - index
                    val expectedProgress = (total.toDouble() / 52 * weeksFromStart).toInt()
                    val variance = (expectedProgress * 0.2).toInt()
                    val progress = (expectedProgress + random.nextInt(-variance, variance + 1))
                        .coerceIn(0, total)
                    progress to total
                }
            }

            database.goalProgressQueries.insertProgress(
                id = progressId,
                goal_id = goalId,
                week_id = weekId,
                progress_value = progressValue.toLong(),
                target_value = targetValue.toLong(),
                created_at = weekDate.plus(6, DateTimeUnit.DAY).toInstantAtEndOfWeek()
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RANDOM DATA GENERATORS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun weightedRandomRating(): Int {
        // Favor 3-4, occasionally 2 or 5
        val roll = random.nextFloat()
        return when {
            roll < 0.05f -> 1
            roll < 0.15f -> 2
            roll < 0.45f -> 3
            roll < 0.85f -> 4
            else -> 5
        }
    }

    private fun randomReviewNote(): String {
        val notes = listOf(
            "Good week overall!",
            "Could have been more productive.",
            "Feeling accomplished.",
            "Struggled a bit with focus.",
            "Made good progress on goals.",
            "Need to plan better next week.",
            "Partner was super helpful!",
            "Busy but satisfying week."
        )
        return notes.random()
    }

    private fun randomTaskTitle(index: Int): String {
        val titles = listOf(
            "Morning workout",
            "Grocery shopping",
            "Call mom",
            "Review budget",
            "Clean apartment",
            "Meal prep for week",
            "Pay bills",
            "Schedule dentist appointment",
            "Walk the dog",
            "Water plants",
            "Organize closet",
            "Read for 30 minutes",
            "Meditate",
            "Journal entry",
            "Plan weekend trip",
            "Fix leaky faucet",
            "Research new laptop",
            "Update resume",
            "Practice guitar",
            "Cook dinner together"
        )
        return titles[index % titles.size]
    }

    private fun randomTaskNote(): String {
        val notes = listOf(
            "Remember to bring the list!",
            "Check the budget first.",
            "Do this in the morning.",
            "Partner might want to join.",
            "High priority this week."
        )
        return notes.random()
    }

    private fun randomTaskReviewNote(): String {
        val notes = listOf(
            "Started but got interrupted.",
            "Partially done, will finish next week.",
            "Made progress but not complete.",
            "About 50% done."
        )
        return notes.random()
    }

    private fun randomTaskStatus(): TaskStatus {
        val roll = random.nextFloat()
        return when {
            roll < 0.60f -> TaskStatus.COMPLETED
            roll < 0.75f -> TaskStatus.TRIED
            roll < 0.85f -> TaskStatus.SKIPPED
            else -> TaskStatus.PENDING
        }
    }

    private fun randomOwnerType(): OwnerType {
        val roll = random.nextFloat()
        return when {
            roll < 0.70f -> OwnerType.SELF
            roll < 0.90f -> OwnerType.PARTNER
            else -> OwnerType.SHARED
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun LocalDate.toInstantAtStartOfWeek(): Instant {
        return this.toLocalDateTime(9, 0) // Monday 9:00 AM
    }

    private fun LocalDate.toInstantAtEndOfWeek(): Instant {
        return this.toLocalDateTime(20, 0) // Sunday 8:00 PM
    }

    private fun LocalDate.toLocalDateTime(hour: Int, minute: Int): Instant {
        val localDateTime = kotlinx.datetime.LocalDateTime(
            year = this.year,
            monthNumber = this.monthNumber,
            dayOfMonth = this.dayOfMonth,
            hour = hour,
            minute = minute
        )
        return localDateTime.toInstant(tz)
    }
}
