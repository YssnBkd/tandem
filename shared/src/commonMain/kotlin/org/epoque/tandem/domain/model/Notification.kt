package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

/**
 * Tracks push notifications sent to users.
 */
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val actionType: NotificationActionType,
    val actionData: Map<String, String>?,
    val createdAt: Instant,
    val sentAt: Instant?,
    val readAt: Instant?
) {
    val isRead: Boolean get() = readAt != null
    val isSent: Boolean get() = sentAt != null
}

/**
 * Types of actions that can trigger notifications.
 */
enum class NotificationActionType {
    /** Partner accepted invite */
    INVITE_ACCEPTED,
    /** Partner sent a task request */
    TASK_REQUESTED,
    /** Partner accepted your task request */
    TASK_REQUEST_ACCEPTED,
    /** Partner declined your task request */
    TASK_REQUEST_DECLINED,
    /** Partner completed a task (opt-in) */
    TASK_COMPLETED,
    /** Partner edited a task (opt-in) */
    TASK_EDITED,
    /** Partner ended the partnership */
    PARTNER_DISCONNECTED
}
