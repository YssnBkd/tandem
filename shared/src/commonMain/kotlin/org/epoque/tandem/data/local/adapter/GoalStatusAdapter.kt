package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import org.epoque.tandem.domain.model.GoalStatus

/**
 * SQLDelight adapter for GoalStatus enum.
 */
object GoalStatusAdapter : ColumnAdapter<GoalStatus, String> {
    override fun decode(databaseValue: String): GoalStatus {
        return GoalStatus.fromString(databaseValue)
    }

    override fun encode(value: GoalStatus): String {
        return value.name
    }
}
