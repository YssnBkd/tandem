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

/**
 * Tracking type for goal creation.
 * Each type has different settings (stepper label varies).
 */
enum class TrackingType(
    val icon: String,
    val title: String,
    val description: String,
    val stepperLabel: String?  // null for Project (no stepper)
) {
    HABIT("⚡", "Habit", "Repeatable routine", "Times per week"),
    TARGET("💰", "Target Amount", "Reach a specific number", "Target Value"),
    PROJECT("🚀", "Project", "Milestones & sub-tasks", null)
}

/**
 * Duration options for goal timeframe.
 */
enum class GoalDuration(val label: String) {
    FOUR_WEEKS("4 weeks"),
    TWELVE_WEEKS("12 weeks"),
    SIX_MONTHS("6 months"),
    ONGOING("Ongoing")
}

/**
 * Ownership options for goal creation.
 */
enum class GoalOwnership(val label: String) {
    JUST_ME("Just me"),
    TOGETHER("Together"),
    PARTNER("Partner")
}
