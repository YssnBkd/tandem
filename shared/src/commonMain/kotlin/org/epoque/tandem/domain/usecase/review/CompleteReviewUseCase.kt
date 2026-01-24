package org.epoque.tandem.domain.usecase.review

import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Use case for completing weekly review and publishing feed events.
 *
 * Handles:
 * - Creating feed item for the user
 * - Creating feed item for partner if connected
 */
class CompleteReviewUseCase(
    private val weekRepository: WeekRepository,
    private val feedRepository: FeedRepository,
    private val partnerRepository: PartnerRepository
) {
    /**
     * Complete review for a week and publish feed events.
     *
     * @param weekId The week that was reviewed
     * @param userId The user who completed review
     * @param overallRating The rating given (1-5)
     * @param reviewNote Optional note about the week
     * @return The week reviewed feed item
     */
    suspend operator fun invoke(
        weekId: String,
        userId: String,
        overallRating: Int?,
        reviewNote: String?
    ): FeedItem.WeekReviewed {
        // Save the review data (sets reviewedAt)
        weekRepository.updateWeekReview(
            weekId = weekId,
            overallRating = overallRating,
            reviewNote = reviewNote
        )

        // Create feed item for the user
        val feedItem = feedRepository.createWeekReviewedItem(
            weekId = weekId,
            userId = userId
        )

        // If user has a partner, create feed item for partner's feed too
        val partner = partnerRepository.getPartner(userId)
        if (partner != null) {
            feedRepository.createWeekReviewedItem(
                weekId = weekId,
                userId = userId  // The user who reviewed (shows "Partner reviewed their week")
            )
        }

        return feedItem
    }
}
