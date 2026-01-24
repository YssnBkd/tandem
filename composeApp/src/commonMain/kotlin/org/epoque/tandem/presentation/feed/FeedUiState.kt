package org.epoque.tandem.presentation.feed

import org.epoque.tandem.presentation.feed.model.FeedDayGroup
import org.epoque.tandem.presentation.feed.model.FeedFilter

/**
 * Complete UI state for the Feed screen.
 * Single source of truth for all UI elements.
 */
data class FeedUiState(
    // Feed content
    val feedGroups: List<FeedDayGroup> = emptyList(),

    // Filter
    val activeFilter: FeedFilter = FeedFilter.ALL,

    // Unread state
    val unreadCount: Int = 0,
    val lastReadIndex: Int = -1,  // Index after which to show "caught up" separator (-1 = no separator)

    // Partner info (for message input placeholder)
    val hasPartner: Boolean = false,
    val partnerName: String? = null,

    // Message input
    val messageText: String = "",
    val isSendingMessage: Boolean = false,

    // Loading states
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /**
     * Whether the feed has any items to display.
     */
    val hasContent: Boolean get() = feedGroups.isNotEmpty()

    /**
     * Whether the feed is empty (after loading).
     */
    val showEmptyState: Boolean get() = !isLoading && feedGroups.isEmpty()

    /**
     * Message input placeholder text.
     */
    val messageInputPlaceholder: String get() = if (partnerName != null) {
        "Message $partnerName..."
    } else {
        "Send a message..."
    }

    /**
     * Whether send button should be enabled.
     */
    val canSendMessage: Boolean get() = messageText.isNotBlank() && hasPartner && !isSendingMessage

    /**
     * Total items count across all groups.
     */
    val totalItemCount: Int get() = feedGroups.sumOf { it.items.size }
}
