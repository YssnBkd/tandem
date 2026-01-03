package org.epoque.tandem.presentation.planning

/**
 * Represents the current step in the weekly planning wizard.
 * Each step corresponds to a specific screen in the planning flow.
 */
enum class PlanningStep {
    /**
     * Step 1: Review and roll over incomplete tasks from the previous week.
     * Users can add or skip each task individually.
     */
    ROLLOVER,

    /**
     * Step 2: Add new tasks for the current week.
     * Users can create multiple tasks with title and optional notes.
     */
    ADD_TASKS,

    /**
     * Step 3: Review partner task requests.
     * Users can accept or discuss each request.
     */
    PARTNER_REQUESTS,

    /**
     * Step 4: Summary confirmation screen.
     * Shows total tasks planned with breakdown by category.
     */
    CONFIRMATION
}
