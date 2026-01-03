package org.epoque.tandem.presentation.review

/**
 * One-time side effects for the Review feature.
 *
 * Side effects are actions that should only happen once (unlike state
 * which is continuously observed). They're consumed by the UI layer
 * via a Channel/Flow.
 */
sealed interface ReviewSideEffect {
    // Navigation
    /** Navigate to the rating step screen */
    data object NavigateToRating : ReviewSideEffect

    /** Navigate to a specific task review screen */
    data class NavigateToTask(val index: Int) : ReviewSideEffect

    /** Navigate to the summary screen */
    data object NavigateToSummary : ReviewSideEffect

    /** Navigate to the planning flow for next week */
    data object NavigateToPlanning : ReviewSideEffect

    /** Navigate back (pop the back stack) */
    data object NavigateBack : ReviewSideEffect

    /** Close the review flow entirely */
    data object CloseReview : ReviewSideEffect

    // Feedback
    /** Show an error message to the user */
    data class ShowError(val message: String) : ReviewSideEffect

    /** Show review complete celebration/feedback */
    data object ShowReviewComplete : ReviewSideEffect

    // Together mode (P5 - deferred)
    /** Show dialog for passing device to partner */
    data object ShowPassToPartnerDialog : ReviewSideEffect
}
