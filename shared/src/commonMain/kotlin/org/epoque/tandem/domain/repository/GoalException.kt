package org.epoque.tandem.domain.repository

/**
 * Goal-related exceptions.
 */
sealed class GoalException(message: String) : Exception(message) {
    data object LimitExceeded : GoalException("Maximum of 10 active goals reached")
    data class InvalidGoal(override val message: String) : GoalException(message)
    data object NotFound : GoalException("Goal not found")
    data object NotOwner : GoalException("Cannot modify goals you don't own")
}
