package org.epoque.tandem.domain.usecase.week

import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Use case for saving a week review.
 * Validates rating and updates review data with timestamp.
 */
class SaveWeekReviewUseCase(
    private val weekRepository: WeekRepository
) {
    /**
     * Saves a review for a week.
     *
     * @param weekId The week ID to review
     * @param overallRating Rating 1-5 (nullable to clear)
     * @param reviewNote Review notes (nullable)
     * @return The updated week, or null if week not found
     * @throws IllegalArgumentException if rating not in 1-5 range
     */
    suspend operator fun invoke(
        weekId: String,
        overallRating: Int?,
        reviewNote: String?
    ): Week? {
        return weekRepository.updateWeekReview(weekId, overallRating, reviewNote)
    }
}
