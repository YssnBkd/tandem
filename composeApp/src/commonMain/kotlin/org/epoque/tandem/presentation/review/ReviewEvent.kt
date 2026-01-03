package org.epoque.tandem.presentation.review

import org.epoque.tandem.domain.model.TaskStatus

/**
 * Events representing user actions in the Review feature.
 *
 * Follows the MVI pattern - each user interaction becomes an event
 * that the ViewModel processes to update state.
 */
sealed interface ReviewEvent {
    // Mode selection
    /** User selected a review mode (Solo or Together) */
    data class SelectMode(val mode: ReviewMode) : ReviewEvent

    // Rating step
    /** User selected an overall rating (1-5) */
    data class SelectRating(val rating: Int) : ReviewEvent

    /** User updated the optional rating note */
    data class UpdateRatingNote(val note: String) : ReviewEvent

    /** User wants to continue from rating to task review */
    data object ContinueToTasks : ReviewEvent

    /** User wants to quick finish - mark remaining tasks as Skipped */
    data object QuickFinish : ReviewEvent

    // Task review step
    /** User selected an outcome for a task */
    data class SelectTaskOutcome(
        val taskId: String,
        val status: TaskStatus
    ) : ReviewEvent

    /** User updated the optional note for a task */
    data class UpdateTaskNote(
        val taskId: String,
        val note: String
    ) : ReviewEvent

    /** User wants to go to the next task */
    data object NextTask : ReviewEvent

    /** User wants to go to the previous task */
    data object PreviousTask : ReviewEvent

    // Summary
    /** User finished reviewing - complete the review */
    data object CompleteReview : ReviewEvent

    /** User wants to start planning next week */
    data object StartNextWeek : ReviewEvent

    /** User is done with review - close */
    data object Done : ReviewEvent

    // Progress management
    /** User wants to resume saved progress */
    data object ResumeProgress : ReviewEvent

    /** User wants to discard saved progress and start fresh */
    data object DiscardProgress : ReviewEvent

    // Together mode (P5 - deferred)
    /** User is passing the device to their partner */
    data object PassToPartner : ReviewEvent

    /** Observer adding a reaction to partner's task */
    data class AddReaction(
        val taskId: String,
        val emoji: String
    ) : ReviewEvent

    // Error handling
    /** User dismissed an error message */
    data object DismissError : ReviewEvent

    /** User wants to retry the last failed operation */
    data object Retry : ReviewEvent
}
