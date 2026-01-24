package org.epoque.tandem.presentation.feed

import org.epoque.tandem.presentation.feed.model.FeedFilter

/**
 * Events that can be triggered by user interactions on the Feed screen.
 * Following UDF pattern: events go UP from UI to ViewModel.
 */
sealed class FeedEvent {

    // ═══════════════════════════════════════════════════════════════════════════
    // FILTER EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User selected a filter chip.
     */
    data class FilterSelected(val filter: FeedFilter) : FeedEvent()

    // ═══════════════════════════════════════════════════════════════════════════
    // FEED ITEM EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User tapped on a feed item (opens detail or navigates).
     */
    data class ItemTapped(val itemId: String) : FeedEvent()

    /**
     * User scrolled past unread items - mark them as read.
     */
    data class ItemsScrolledPast(val lastSeenItemId: String) : FeedEvent()

    /**
     * User reached the end of the feed - mark all as read.
     */
    data object ReachedEndOfFeed : FeedEvent()

    // ═══════════════════════════════════════════════════════════════════════════
    // TASK ASSIGNMENT EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User tapped Accept on a task assignment.
     */
    data class AcceptTaskAssignment(val itemId: String) : FeedEvent()

    /**
     * User tapped Decline on a task assignment.
     */
    data class DeclineTaskAssignment(val itemId: String) : FeedEvent()

    // ═══════════════════════════════════════════════════════════════════════════
    // AI PROMPT EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User tapped the CTA on an AI plan prompt.
     */
    data class StartPlanningTapped(val itemId: String) : FeedEvent()

    /**
     * User tapped the CTA on an AI review prompt.
     */
    data class StartReviewTapped(val itemId: String, val weekId: String) : FeedEvent()

    /**
     * User dismissed an AI prompt.
     */
    data class DismissAiPrompt(val itemId: String) : FeedEvent()

    // ═══════════════════════════════════════════════════════════════════════════
    // MESSAGE EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User typed in the message input.
     */
    data class MessageTextChanged(val text: String) : FeedEvent()

    /**
     * User tapped the send button.
     */
    data object SendMessageTapped : FeedEvent()

    // ═══════════════════════════════════════════════════════════════════════════
    // TASK CHECKBOX EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User tapped a task checkbox in a completion card.
     * This is for allowing users to un-complete a task from the feed.
     */
    data class TaskCheckboxTapped(val taskId: String) : FeedEvent()

    // ═══════════════════════════════════════════════════════════════════════════
    // REFRESH EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User pulled to refresh.
     */
    data object RefreshRequested : FeedEvent()

    // ═══════════════════════════════════════════════════════════════════════════
    // NAVIGATION EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * User tapped the more options button.
     */
    data object MoreOptionsTapped : FeedEvent()
}
