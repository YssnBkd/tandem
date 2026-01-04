package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

data class Goal(
    val id: String,
    val name: String,
    val icon: String,
    val type: GoalType,
    val durationWeeks: Int?,
    val startWeekId: String,
    val ownerId: String,
    val currentProgress: Int,
    val currentWeekId: String,
    val status: GoalStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /** Target based on goal type */
    val target: Int get() = when (type) {
        is GoalType.WeeklyHabit -> type.targetPerWeek
        is GoalType.RecurringTask -> 1
        is GoalType.TargetAmount -> type.targetTotal
    }

    /** Progress fraction (0.0 to 1.0+) */
    val progressFraction: Float get() = if (target > 0) {
        currentProgress.toFloat() / target
    } else 0f

    /** Progress display text (e.g., "3/5" or "75/100") */
    val progressText: String get() = "$currentProgress/$target"

    /** Whether goal is still active */
    val isActive: Boolean get() = status == GoalStatus.ACTIVE

    /** Whether goal has met its target */
    val hasMetTarget: Boolean get() = currentProgress >= target

    /** Whether this is a weekly reset goal */
    val resetsWeekly: Boolean get() = type is GoalType.WeeklyHabit
}
