package org.epoque.tandem.presentation.progress

import org.epoque.tandem.domain.model.TaskStatus

/**
 * Status labels and emojis for task outcomes.
 *
 * Uses "Celebration Over Judgment" language - "Tried" and "Skipped"
 * instead of "Failed" or "Incomplete".
 */
object TaskStatusDisplay {

    /**
     * Get display label for a task status.
     *
     * @param status The task status
     * @return Human-readable label
     */
    fun labelFor(status: TaskStatus): String = when (status) {
        TaskStatus.COMPLETED -> "Done"
        TaskStatus.TRIED -> "Tried"
        TaskStatus.SKIPPED -> "Skipped"
        TaskStatus.PENDING -> "Pending"
        TaskStatus.PENDING_ACCEPTANCE -> "Awaiting"
        TaskStatus.DECLINED -> "Declined"
    }

    /**
     * Get emoji for a task status.
     *
     * @param status The task status
     * @return Emoji string
     */
    fun emojiFor(status: TaskStatus): String = when (status) {
        TaskStatus.COMPLETED -> "\u2705" // ✅
        TaskStatus.TRIED -> "\uD83D\uDCAA" // 💪
        TaskStatus.SKIPPED -> "\u23ED\uFE0F" // ⏭️
        TaskStatus.PENDING -> "\u23F3" // ⏳
        TaskStatus.PENDING_ACCEPTANCE -> "\uD83D\uDCEC" // 📬
        TaskStatus.DECLINED -> "\u274C" // ❌
    }
}
