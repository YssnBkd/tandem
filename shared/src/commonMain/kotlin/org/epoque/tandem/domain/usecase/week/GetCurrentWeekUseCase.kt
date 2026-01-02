package org.epoque.tandem.domain.usecase.week

import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Use case for getting the current week.
 * Auto-creates the week if it doesn't exist.
 */
class GetCurrentWeekUseCase(
    private val weekRepository: WeekRepository
) {
    /**
     * Gets the current week, creating it if necessary.
     *
     * @param userId The user's ID
     * @return The current week (guaranteed non-null)
     */
    suspend operator fun invoke(userId: String): Week {
        return weekRepository.getOrCreateCurrentWeek(userId)
    }
}
