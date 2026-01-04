package org.epoque.tandem.presentation.progress

/**
 * Events for Past Week Detail screen.
 */
sealed interface PastWeekDetailEvent {
    /**
     * User tapped back button.
     */
    data object Back : PastWeekDetailEvent

    /**
     * User tapped retry after error.
     */
    data object Retry : PastWeekDetailEvent
}
