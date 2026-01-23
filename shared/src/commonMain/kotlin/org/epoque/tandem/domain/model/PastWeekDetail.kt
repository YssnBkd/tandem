package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Full detail for past week detail view.
 *
 * @property weekId ISO 8601 week ID (e.g., "2026-W01")
 * @property startDate Monday of the week
 * @property endDate Sunday of the week
 * @property userReview User's review details for this week
 * @property partnerReview Partner's review details, or null if no partner
 * @property tasks List of task outcomes for both user and partner
 */
data class PastWeekDetail(
    val weekId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userReview: ReviewDetail,
    val partnerReview: ReviewDetail?,
    val tasks: List<TaskOutcome>
)

/**
 * Review detail for a single user.
 *
 * @property rating Overall week rating (1-5)
 * @property note Review reflection text
 * @property completion Task completion stats
 * @property reviewedAt When the review was completed
 */
data class ReviewDetail(
    val rating: Int?,
    val note: String?,
    val completion: CompletionStats,
    val reviewedAt: Instant?
) {
    init {
        require(rating == null || rating in 1..5) {
            "rating must be null or in range 1-5"
        }
    }

    val isReviewed: Boolean
        get() = reviewedAt != null
}

/**
 * Task outcome showing both user and partner status.
 *
 * @property taskId Task identifier
 * @property title Task title
 * @property priority Task priority for display
 * @property userStatus User's task status
 * @property partnerStatus Partner's status for shared tasks, or null if not shared
 */
data class TaskOutcome(
    val taskId: String,
    val title: String,
    val priority: TaskPriority,
    val userStatus: TaskStatus,
    val partnerStatus: TaskStatus?
)
