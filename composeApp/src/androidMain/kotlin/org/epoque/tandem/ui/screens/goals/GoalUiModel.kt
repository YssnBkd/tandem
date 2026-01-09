package org.epoque.tandem.ui.screens.goals

/**
 * UI model for displaying a goal in the Goals screen.
 */
data class GoalUiModel(
    val id: String,
    val icon: String,
    val name: String,
    val ownershipLabel: String,
    val ownershipType: OwnershipType,
    val progressFraction: Float,
    val progressText: String,
    val goalTypeLabel: String
)

/**
 * Ownership type for goal display styling.
 */
enum class OwnershipType {
    YOURS,
    PARTNER,
    TOGETHER
}

/**
 * Segment tabs for filtering goals by status.
 */
enum class GoalStatusSegment(val displayName: String) {
    ACTIVE("Active"),
    COMPLETED("Completed")
}
