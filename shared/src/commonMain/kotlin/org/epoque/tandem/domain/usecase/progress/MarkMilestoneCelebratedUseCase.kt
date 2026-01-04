package org.epoque.tandem.domain.usecase.progress

import org.epoque.tandem.domain.repository.ProgressPreferencesRepository

/**
 * Marks a milestone as celebrated, persisting to DataStore.
 *
 * This ensures each milestone is only celebrated once.
 */
class MarkMilestoneCelebratedUseCase(
    private val progressPreferencesRepository: ProgressPreferencesRepository
) {
    /**
     * Mark a milestone as celebrated.
     *
     * @param milestone The milestone value (5, 10, 20, or 50)
     * @throws IllegalArgumentException if milestone is not a valid value
     */
    suspend operator fun invoke(milestone: Int) {
        progressPreferencesRepository.setLastCelebratedMilestone(milestone)
    }
}
