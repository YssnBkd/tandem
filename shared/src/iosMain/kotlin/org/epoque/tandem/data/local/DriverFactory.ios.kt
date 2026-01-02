package org.epoque.tandem.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * Creates a SqlDriver for iOS using NativeSqliteDriver.
 */
actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = TandemDatabase.Schema,
            name = "tandem.db"
        )
    }
}
