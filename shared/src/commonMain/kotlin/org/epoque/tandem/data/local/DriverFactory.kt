package org.epoque.tandem.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific factory for creating SqlDriver instances.
 * Android requires a Context, iOS does not.
 */
expect class DriverFactory {
    fun createDriver(): SqlDriver
}
