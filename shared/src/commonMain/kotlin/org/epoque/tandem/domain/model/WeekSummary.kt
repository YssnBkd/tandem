package org.epoque.tandem.domain.model

import kotlinx.datetime.LocalDate

/**
 * Summary data for past weeks list item.
 *
 * @property weekId ISO 8601 week ID (e.g., "2026-W01")
 * @property startDate Monday of the week
 * @property endDate Sunday of the week
 * @property userCompletion User's task completion stats for this week
 * @property partnerCompletion Partner's completion stats, or null if no partner
 * @property userRating User's mood rating (1-5)
 * @property partnerRating Partner's mood rating (1-5)
 * @property isReviewed Whether the user completed their review for this week
 */
data class WeekSummary(
    val weekId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userCompletion: CompletionStats,
    val partnerCompletion: CompletionStats?,
    val userRating: Int?,
    val partnerRating: Int?,
    val isReviewed: Boolean
) {
    init {
        require(userRating == null || userRating in 1..5) {
            "userRating must be null or in range 1-5"
        }
        require(partnerRating == null || partnerRating in 1..5) {
            "partnerRating must be null or in range 1-5"
        }
    }
}
