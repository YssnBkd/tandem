package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a user's task/commitment for a specific week.
 */
data class Task(
    val id: String,
    val title: String,
    val notes: String?,
    val ownerId: String,
    val ownerType: OwnerType,
    val weekId: String,
    val status: TaskStatus,
    val createdBy: String,
    val requestNote: String?,
    val repeatTarget: Int?,
    val repeatCompleted: Int,
    val linkedGoalId: String?,
    val reviewNote: String?,
    val rolledFromWeekId: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isRepeating: Boolean get() = repeatTarget != null
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED
    val repeatProgress: String? get() = repeatTarget?.let { "$repeatCompleted/$it" }
    val isPendingAcceptance: Boolean get() = status == TaskStatus.PENDING_ACCEPTANCE
    val isPartnerRequest: Boolean get() = ownerId != createdBy
}
