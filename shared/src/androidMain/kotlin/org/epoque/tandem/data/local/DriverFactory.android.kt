package org.epoque.tandem.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Creates a SqlDriver for Android using AndroidSqliteDriver.
 */
actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = TandemDatabase.Schema,
            context = context,
            name = "tandem.db"
        )
    }
}
