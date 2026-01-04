package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import org.epoque.tandem.domain.model.GoalType
import org.epoque.tandem.domain.model.toDbString

/**
 * SQLDelight adapter for GoalType sealed class.
 * Stores goal type with its associated value (e.g., "WEEKLY_HABIT:3", "TARGET_AMOUNT:50").
 */
object GoalTypeAdapter : ColumnAdapter<GoalType, String> {
    override fun decode(databaseValue: String): GoalType {
        return GoalType.fromString(databaseValue)
    }

    override fun encode(value: GoalType): String {
        return value.toDbString()
    }
}
