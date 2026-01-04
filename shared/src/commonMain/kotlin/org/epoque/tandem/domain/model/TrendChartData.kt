package org.epoque.tandem.domain.model

/**
 * Complete trend chart data for UI visualization.
 *
 * @property dataPoints Ordered list of data points (oldest to newest), max 8 points
 * @property hasPartner Whether to show partner line in chart
 * @property insufficientData True if less than 4 weeks of data
 */
data class TrendChartData(
    val dataPoints: List<TrendDataPoint>,
    val hasPartner: Boolean,
    val insufficientData: Boolean
) {
    init {
        require(dataPoints.size <= 8) { "Trend chart supports maximum 8 data points" }
    }

    /**
     * Number of weeks with data.
     */
    val weekCount: Int
        get() = dataPoints.size

    companion object {
        val EMPTY = TrendChartData(
            dataPoints = emptyList(),
            hasPartner = false,
            insufficientData = true
        )
    }
}
