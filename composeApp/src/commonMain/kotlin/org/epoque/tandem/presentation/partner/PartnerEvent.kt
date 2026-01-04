package org.epoque.tandem.presentation.partner

/**
 * User events in the Partner screen.
 * Processed by PartnerViewModel to update state.
 *
 * Following Android UDF pattern: events go UP to ViewModel,
 * state flows DOWN to UI.
 */
sealed interface PartnerEvent {
    // Invite creation events
    data object GenerateInvite : PartnerEvent
    data object ShareInvite : PartnerEvent
    data object CancelInvite : PartnerEvent

    // Accept invite events (from deep link)
    data class LoadInvite(val code: String) : PartnerEvent
    data object AcceptInvite : PartnerEvent
    data object DeclineInvite : PartnerEvent

    // Task request events
    data object ShowRequestTaskSheet : PartnerEvent
    data object DismissRequestTaskSheet : PartnerEvent
    data class UpdateRequestTitle(val title: String) : PartnerEvent
    data class UpdateRequestNote(val note: String) : PartnerEvent
    data object SubmitTaskRequest : PartnerEvent

    // Pending request handling
    data class AcceptRequest(val taskId: String) : PartnerEvent
    data class DeclineRequest(val taskId: String) : PartnerEvent

    // Disconnect events
    data object ShowDisconnectDialog : PartnerEvent
    data object DismissDisconnectDialog : PartnerEvent
    data object ConfirmDisconnect : PartnerEvent

    // Error handling
    data object DismissError : PartnerEvent
}
