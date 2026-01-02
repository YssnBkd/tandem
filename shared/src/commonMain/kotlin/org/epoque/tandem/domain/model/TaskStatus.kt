package org.epoque.tandem.domain.model

/**
 * Tracks the lifecycle of a task.
 * The data layer allows any status transition; business rules for valid transitions
 * are enforced at a higher layer.
 */
enum class TaskStatus {
    /** Not started */
    PENDING,

    /** Awaiting partner acceptance */
    PENDING_ACCEPTANCE,

    /** Done */
    COMPLETED,

    /** Attempted but incomplete */
    TRIED,

    /** Intentionally not done */
    SKIPPED,

    /** Partner rejected */
    DECLINED
}
