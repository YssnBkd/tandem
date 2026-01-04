package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

/**
 * Information about an invite for validation and display on landing page.
 */
data class InviteInfo(
    val code: String,
    val creatorName: String,
    val creatorTaskPreview: List<TaskPreview>,
    val expiresAt: Instant
)

/**
 * Minimal task information for invite preview.
 */
data class TaskPreview(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)
