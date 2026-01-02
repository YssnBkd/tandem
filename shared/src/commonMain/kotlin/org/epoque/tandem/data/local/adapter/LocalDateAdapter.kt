package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDate

/**
 * Adapts kotlinx.datetime.LocalDate to/from SQLite TEXT (ISO-8601 format).
 */
val localDateAdapter = object : ColumnAdapter<LocalDate, String> {
    override fun decode(databaseValue: String): LocalDate =
        LocalDate.parse(databaseValue)

    override fun encode(value: LocalDate): String =
        value.toString()
}
