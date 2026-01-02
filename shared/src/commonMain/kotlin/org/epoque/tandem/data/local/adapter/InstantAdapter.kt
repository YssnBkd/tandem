package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

/**
 * Adapts kotlinx.datetime.Instant to/from SQLite INTEGER (epoch milliseconds).
 */
val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}
