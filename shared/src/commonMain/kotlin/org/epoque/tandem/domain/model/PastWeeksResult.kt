package org.epoque.tandem.domain.model

/**
 * Paginated result for past weeks list.
 *
 * @property weeks List of week summaries for the current page
 * @property hasMore True if more weeks are available for loading
 * @property totalCount Total number of past weeks available
 */
data class PastWeeksResult(
    val weeks: List<WeekSummary>,
    val hasMore: Boolean,
    val totalCount: Int
) {
    init {
        require(totalCount >= 0) { "totalCount cannot be negative" }
    }

    companion object {
        val EMPTY = PastWeeksResult(weeks = emptyList(), hasMore = false, totalCount = 0)
    }
}
