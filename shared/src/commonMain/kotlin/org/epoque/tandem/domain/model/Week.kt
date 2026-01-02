package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Represents a calendar week for a user.
 */
data class Week(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: String,
    val overallRating: Int?,
    val reviewNote: String?,
    val reviewedAt: Instant?,
    val planningCompletedAt: Instant?
) {
    val isReviewed: Boolean get() = reviewedAt != null
    val isPlanningComplete: Boolean get() = planningCompletedAt != null
}
