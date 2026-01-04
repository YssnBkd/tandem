package org.epoque.tandem.domain.usecase.progress

import kotlinx.coroutines.flow.first
import org.epoque.tandem.domain.model.StreakResult
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.ProgressPreferencesRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * Calculate current streak for user (and partner if connected).
 *
 * Streak is consecutive weeks where:
 * - Solo user: User completed weekly review (Week.reviewedAt != null)
 * - With partner: Both user AND partner completed weekly review for the same week
 *
 * This use case extends the basic streak calculation with partner awareness
 * and milestone detection.
 */
class CalculatePartnerStreakUseCase(
    private val weekRepository: WeekRepository,
    private val partnerRepository: PartnerRepository,
    private val progressPreferencesRepository: ProgressPreferencesRepository,
    private val getPendingMilestoneUseCase: GetPendingMilestoneUseCase
) {
    /**
     * Calculate the current streak for the user.
     *
     * @param userId The current user's ID
     * @return StreakResult with count, partner flag, and pending milestone
     */
    suspend operator fun invoke(userId: String): StreakResult {
        val partner = partnerRepository.getPartner(userId)
        val lastCelebrated = progressPreferencesRepository.lastCelebratedMilestone.first()

        val streak = if (partner == null) {
            calculateSoloStreak(userId)
        } else {
            calculatePartnerStreak(userId, partner.id)
        }

        val pendingMilestone = getPendingMilestoneUseCase(streak, lastCelebrated)

        return StreakResult(
            count = streak,
            isPartnerStreak = partner != null,
            pendingMilestone = pendingMilestone
        )
    }

    /**
     * Calculate solo streak - consecutive weeks where user reviewed.
     */
    private suspend fun calculateSoloStreak(userId: String): Int {
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

    /**
     * Calculate partner streak - consecutive weeks where BOTH reviewed.
     *
     * For a week to count:
     * 1. User must have reviewed (Week.reviewedAt != null)
     * 2. Partner must have reviewed the same week (Week.reviewedAt != null)
     */
    private suspend fun calculatePartnerStreak(userId: String, partnerId: String): Int {
        val userWeeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .associateBy { it.id }

        val partnerWeeks = weekRepository.observeWeeksForUser(partnerId)
            .first()
            .associateBy { it.id }

        // Get all unique week IDs from both users, sorted descending
        val allWeekIds = (userWeeks.keys + partnerWeeks.keys)
            .distinct()
            .sortedDescending()

        var streak = 0
        for (weekId in allWeekIds) {
            val userReviewed = userWeeks[weekId]?.isReviewed == true
            val partnerReviewed = partnerWeeks[weekId]?.isReviewed == true

            if (userReviewed && partnerReviewed) {
                streak++
            } else {
                break
            }
        }

        return streak
    }
}
