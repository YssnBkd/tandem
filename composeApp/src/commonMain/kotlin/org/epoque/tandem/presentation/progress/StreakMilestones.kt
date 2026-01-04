package org.epoque.tandem.presentation.progress

import org.epoque.tandem.domain.model.StreakResult

/**
 * Milestone values for streak celebrations.
 *
 * Milestones are celebrated at 5, 10, 20, and 50 consecutive weeks
 * to encourage consistent weekly review completion.
 */
object StreakMilestones {

    /**
     * All milestone values in ascending order.
     */
    val VALUES: List<Int> = StreakResult.MILESTONES

    /**
     * Get the next milestone the user is working towards.
     *
     * @param currentStreak Current streak count
     * @return Next milestone value, or null if all milestones reached
     */
    fun getNextMilestone(currentStreak: Int): Int? =
        VALUES.firstOrNull { it > currentStreak }

    /**
     * Get all milestones the user has reached.
     *
     * @param currentStreak Current streak count
     * @return List of reached milestone values
     */
    fun getReachedMilestones(currentStreak: Int): List<Int> =
        VALUES.filter { it <= currentStreak }

    /**
     * Get celebration message for a milestone.
     *
     * @param milestone The milestone value (5, 10, 20, or 50)
     * @param isPartnerStreak Whether this is a partner streak
     * @return Celebration message string
     */
    fun getCelebrationMessage(milestone: Int, isPartnerStreak: Boolean): String {
        val together = if (isPartnerStreak) " together" else ""
        return when (milestone) {
            5 -> "\uD83C\uDF1F 5 weeks$together! Great start!"
            10 -> "\uD83C\uDF1F 10 weeks$together! You're on fire!"
            20 -> "\uD83C\uDFC6 20 weeks$together! Incredible consistency!"
            50 -> "\uD83D\uDC51 50 weeks$together! A full year of growth!"
            else -> "\uD83C\uDF89 $milestone weeks$together! Keep going!"
        }
    }

    /**
     * Get progress message showing distance to next milestone.
     *
     * @param currentStreak Current streak count
     * @return Progress message, or null if all milestones reached
     */
    fun getProgressMessage(currentStreak: Int): String? {
        val nextMilestone = getNextMilestone(currentStreak) ?: return null
        val remaining = nextMilestone - currentStreak
        return "$remaining more ${if (remaining == 1) "week" else "weeks"} to $nextMilestone-week milestone"
    }
}
