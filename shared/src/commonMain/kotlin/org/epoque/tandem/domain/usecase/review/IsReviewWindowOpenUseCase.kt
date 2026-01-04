package org.epoque.tandem.domain.usecase.review

import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Checks if the review window is currently open.
 * Review window: Friday 6PM - Sunday 11:59PM (device timezone).
 *
 * This respects the user's local context without requiring timezone settings.
 * The fixed times establish a consistent weekly rhythm for all users.
 */
class IsReviewWindowOpenUseCase(
    private val clock: Clock = Clock.System
) {
    /**
     * Check if the review window is currently open.
     *
     * @return true if review window is open (Friday 6PM - Sunday 11:59PM)
     */
    operator fun invoke(): Boolean {
        val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayOfWeek = now.dayOfWeek
        val hour = now.hour

        return when (dayOfWeek) {
            DayOfWeek.FRIDAY -> hour >= 18      // Friday 6PM onwards
            DayOfWeek.SATURDAY -> true          // All of Saturday
            DayOfWeek.SUNDAY -> true            // All of Sunday (until 11:59PM)
            else -> false
        }
    }
}
