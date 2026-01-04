package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

data class GoalProgress(
    val id: String,
    val goalId: String,
    val weekId: String,
    val progressValue: Int,
    val targetValue: Int,
    val createdAt: Instant
) {
    val progressFraction: Float get() = if (targetValue > 0) {
        progressValue.toFloat() / targetValue
    } else 0f

    val progressText: String get() = "$progressValue/$targetValue"
}
