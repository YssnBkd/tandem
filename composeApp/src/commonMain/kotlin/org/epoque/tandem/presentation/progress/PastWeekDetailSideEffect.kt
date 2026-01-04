package org.epoque.tandem.presentation.progress

/**
 * Side effects for Past Week Detail screen.
 */
sealed interface PastWeekDetailSideEffect {
    /**
     * Navigate back to Progress screen.
     */
    data object NavigateBack : PastWeekDetailSideEffect
}
