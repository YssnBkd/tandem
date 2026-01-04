package org.epoque.tandem.domain.model

/**
 * Single data point for trend chart visualization.
 *
 * @property weekId ISO 8601 week ID (e.g., "2026-W01")
 * @property weekLabel Display label for chart axis (e.g., "W01", "W02")
 * @property userPercentage User's task completion percentage (0-100)
 * @property partnerPercentage Partner's completion percentage, or null if no partner that week
 */
data class TrendDataPoint(
    val weekId: String,
    val weekLabel: String,
    val userPercentage: Int,
    val partnerPercentage: Int?
) {
    init {
        require(userPercentage in 0..100) { "userPercentage must be in range 0-100" }
        require(partnerPercentage == null || partnerPercentage in 0..100) {
            "partnerPercentage must be null or in range 0-100"
        }
    }
}
