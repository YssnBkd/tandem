package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter

/**
 * Generic adapter for Kotlin enums to/from SQLite TEXT.
 * Stores enum values as their name strings.
 */
inline fun <reified T : Enum<T>> EnumColumnAdapter(): ColumnAdapter<T, String> {
    return object : ColumnAdapter<T, String> {
        override fun decode(databaseValue: String): T {
            return enumValues<T>().first { it.name == databaseValue }
        }

        override fun encode(value: T): String = value.name
    }
}
