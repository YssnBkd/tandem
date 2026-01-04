package org.epoque.tandem.presentation.progress

/**
 * Emoji mapping for mood ratings.
 *
 * Maps week review ratings (1-5) to appropriate mood emojis.
 * Uses positive framing per the "Celebration Over Judgment" principle.
 */
object MoodEmojis {

    /**
     * Convert a rating to a mood emoji.
     *
     * @param rating Week rating (1-5), or null if not reviewed
     * @return Emoji string, or null if rating is null
     */
    fun fromRating(rating: Int?): String? = when (rating) {
        1 -> "\uD83D\uDE1E" // 😞
        2 -> "\uD83D\uDE10" // 😐
        3 -> "\uD83D\uDE0A" // 😊
        4 -> "\uD83D\uDE04" // 😄
        5 -> "\uD83C\uDF89" // 🎉
        else -> null
    }

    /**
     * Get a display label for a rating.
     *
     * @param rating Week rating (1-5), or null if not reviewed
     * @return Human-readable label, or null if rating is null
     */
    fun labelFor(rating: Int?): String? = when (rating) {
        1 -> "Tough week"
        2 -> "Okay week"
        3 -> "Good week"
        4 -> "Great week"
        5 -> "Amazing week"
        else -> null
    }
}
