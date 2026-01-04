package org.epoque.tandem.domain.model

/**
 * Result of streak calculation with metadata.
 *
 * @property count Current streak count (consecutive weeks reviewed)
 * @property isPartnerStreak True if this is a partner-based streak (both reviewed)
 * @property pendingMilestone Unseen milestone to celebrate (5, 10, 20, or 50), or null if none
 */
data class StreakResult(
    val count: Int,
    val isPartnerStreak: Boolean,
    val pendingMilestone: Int?
) {
    init {
        require(count >= 0) { "Streak count cannot be negative" }
        require(pendingMilestone == null || pendingMilestone in MILESTONES) {
            "pendingMilestone must be null or one of $MILESTONES"
        }
    }

    companion object {
        val MILESTONES = listOf(5, 10, 20, 50)
        val EMPTY = StreakResult(count = 0, isPartnerStreak = false, pendingMilestone = null)
    }
}
