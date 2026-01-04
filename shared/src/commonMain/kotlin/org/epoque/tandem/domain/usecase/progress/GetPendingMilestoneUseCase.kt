package org.epoque.tandem.domain.usecase.progress

import org.epoque.tandem.domain.model.StreakResult

/**
 * Determines if there's an unseen milestone to celebrate.
 *
 * Milestones are awarded at: 5, 10, 20, 50 consecutive weeks.
 * Each milestone should only be celebrated once.
 */
class GetPendingMilestoneUseCase {

    /**
     * Check for unseen milestones based on current streak.
     *
     * @param currentStreak Current streak count
     * @param lastCelebratedMilestone Last milestone the user has seen (0 if none)
     * @return Milestone value to celebrate, or null if none pending
     */
    operator fun invoke(currentStreak: Int, lastCelebratedMilestone: Int): Int? {
        // Find all milestones the user has reached but not celebrated
        val reachedMilestones = StreakResult.MILESTONES.filter { milestone ->
            currentStreak >= milestone && milestone > lastCelebratedMilestone
        }

        // Return the highest uncelebrated milestone (if any)
        return reachedMilestones.maxOrNull()
    }
}
