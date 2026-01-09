package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalTime

/**
 * Adapts kotlinx.datetime.LocalTime to/from SQLite TEXT (ISO-8601 format).
 */
val localTimeAdapter = object : ColumnAdapter<LocalTime, String> {
    override fun decode(databaseValue: String): LocalTime =
        LocalTime.parse(databaseValue)

    override fun encode(value: LocalTime): String =
        value.toString()
}
