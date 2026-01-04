package org.epoque.tandem.domain.usecase.review

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Calculates the current streak of consecutive reviewed weeks.
 * Streak counts backwards from the most recent reviewed week.
 *
 * A streak breaks if any week is missed - no grace period.
 */
class CalculateStreakUseCase(
    private val weekRepository: WeekRepository
) {
    /**
     * Calculate the current streak of consecutive reviewed weeks.
     *
     * @param userId The user ID
     * @return The number of consecutive reviewed weeks (0 if no streak)
     */
    suspend operator fun invoke(userId: String): Int {
        val weeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .sortedByDescending { it.startDate }

        var streak = 0
        for (week in weeks) {
            if (week.isReviewed) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
