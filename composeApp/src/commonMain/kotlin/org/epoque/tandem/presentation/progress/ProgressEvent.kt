package org.epoque.tandem.presentation.progress

/**
 * User events for Progress screen.
 *
 * Follows unidirectional data flow - all UI interactions are
 * dispatched through the ViewModel's onEvent() function.
 */
sealed interface ProgressEvent {

    /**
     * User tapped a past week to view details.
     */
    data class PastWeekTapped(val weekId: String) : ProgressEvent

    /**
     * User scrolled to bottom, load more weeks.
     */
    data object LoadMoreWeeks : ProgressEvent

    /**
     * User dismissed milestone celebration.
     */
    data object DismissMilestone : ProgressEvent

    /**
     * Retry after error.
     */
    data object Retry : ProgressEvent

    /**
     * Screen became visible (trigger data refresh).
     */
    data object ScreenVisible : ProgressEvent
}
