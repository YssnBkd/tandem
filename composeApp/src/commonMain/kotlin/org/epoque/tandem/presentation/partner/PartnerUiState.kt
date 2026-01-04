package org.epoque.tandem.presentation.partner

import org.epoque.tandem.domain.model.Invite
import org.epoque.tandem.domain.model.InviteInfo
import org.epoque.tandem.domain.model.Partner
import org.epoque.tandem.domain.model.Task

/**
 * Complete UI state for the Partner screen.
 * Single source of truth for all UI elements.
 *
 * Following Android best practices:
 * - Immutable data class
 * - Single responsibility (only UI state)
 * - Naming convention: [Functionality]UiState
 */
data class PartnerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Partner state
    val partner: Partner? = null,
    val hasPartner: Boolean = false,

    // Invite state (for creator)
    val activeInvite: Invite? = null,
    val hasActiveInvite: Boolean = false,

    // Invite acceptance (from deep link)
    val inviteInfo: InviteInfo? = null,
    val isAcceptingInvite: Boolean = false,

    // Task request
    val showRequestTaskSheet: Boolean = false,
    val requestTaskTitle: String = "",
    val requestTaskNote: String = "",
    val isSubmittingRequest: Boolean = false,

    // Pending task requests from partner
    val pendingRequests: List<Task> = emptyList(),

    // Disconnect
    val showDisconnectDialog: Boolean = false,
    val isDisconnecting: Boolean = false
) {
    /**
     * Get the invite link for sharing.
     */
    val inviteLink: String? get() = activeInvite?.link

    /**
     * Whether the submit request button should be enabled.
     */
    val canSubmitRequest: Boolean get() = requestTaskTitle.isNotBlank() && !isSubmittingRequest

    /**
     * Whether to show the empty state (no partner and no active invite).
     */
    val showEmptyState: Boolean get() = !isLoading && !hasPartner && !hasActiveInvite

    /**
     * Number of pending requests from partner.
     */
    val pendingRequestCount: Int get() = pendingRequests.size
}
