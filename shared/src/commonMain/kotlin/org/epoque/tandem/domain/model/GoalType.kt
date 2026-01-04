package org.epoque.tandem.domain.model

sealed class GoalType {
    data class WeeklyHabit(val targetPerWeek: Int) : GoalType()

    data object RecurringTask : GoalType()

    data class TargetAmount(val targetTotal: Int) : GoalType()

    companion object {
        fun fromString(value: String): GoalType {
            return when {
                value.startsWith("WEEKLY_HABIT:") -> {
                    val target = value.substringAfter(":").toIntOrNull() ?: 1
                    WeeklyHabit(target)
                }
                value == "RECURRING_TASK" -> RecurringTask
                value.startsWith("TARGET_AMOUNT:") -> {
                    val target = value.substringAfter(":").toIntOrNull() ?: 1
                    TargetAmount(target)
                }
                // Legacy format support
                value == "WEEKLY_HABIT" -> WeeklyHabit(1)
                value == "TARGET_AMOUNT" -> TargetAmount(1)
                else -> RecurringTask
            }
        }
    }
}

fun GoalType.toDbString(): String = when (this) {
    is GoalType.WeeklyHabit -> "WEEKLY_HABIT:$targetPerWeek"
    is GoalType.RecurringTask -> "RECURRING_TASK"
    is GoalType.TargetAmount -> "TARGET_AMOUNT:$targetTotal"
}
