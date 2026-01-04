package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.Notification

/**
 * Repository for managing push notifications.
 */
interface NotificationRepository {
    /**
     * Get all notifications for user.
     */
    suspend fun getNotifications(userId: String): List<Notification>

    /**
     * Mark notification as read.
     */
    suspend fun markAsRead(notificationId: String)

    /**
     * Observe unread notification count.
     */
    fun observeUnreadCount(userId: String): Flow<Int>

    /**
     * Update FCM token for user.
     */
    suspend fun updateFcmToken(userId: String, token: String)
}
