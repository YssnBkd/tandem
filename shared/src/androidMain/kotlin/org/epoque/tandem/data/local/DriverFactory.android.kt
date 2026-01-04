package org.epoque.tandem.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Creates a SqlDriver for Android using AndroidSqliteDriver.
 * Handles schema migrations when database structure changes.
 */
actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = TandemDatabase.Schema,
            context = context,
            name = "tandem.db",
            callback = object : AndroidSqliteDriver.Callback(TandemDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Always ensure schema is up to date when database opens
                    // This handles cases where version didn't change but schema did
                    ensureSchemaUpToDate(db)
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    // Handle migrations for schema changes
                    migrateIfNeeded(db, oldVersion, newVersion)
                }
            }
        )
    }

    /**
     * Ensures schema is up to date by checking for missing columns/tables.
     * Called on every database open to handle edge cases.
     */
    private fun ensureSchemaUpToDate(db: SupportSQLiteDatabase) {
        // Check if Task table has all required columns
        if (!columnExists(db, "Task", "request_note")) {
            safeAddColumn(db, "Task", "request_note", "TEXT")
        }
        if (!columnExists(db, "Task", "repeat_target")) {
            safeAddColumn(db, "Task", "repeat_target", "INTEGER")
        }
        if (!columnExists(db, "Task", "repeat_completed")) {
            safeAddColumn(db, "Task", "repeat_completed", "INTEGER NOT NULL DEFAULT 0")
        }
        if (!columnExists(db, "Task", "linked_goal_id")) {
            safeAddColumn(db, "Task", "linked_goal_id", "TEXT")
        }
        if (!columnExists(db, "Task", "review_note")) {
            safeAddColumn(db, "Task", "review_note", "TEXT")
        }
        if (!columnExists(db, "Task", "rolled_from_week_id")) {
            safeAddColumn(db, "Task", "rolled_from_week_id", "TEXT")
        }

        // Ensure all feature tables exist
        createPartnerTablesIfNeeded(db)
        createGoalTablesIfNeeded(db)
    }

    /**
     * Checks if a column exists in a table.
     */
    private fun columnExists(db: SupportSQLiteDatabase, table: String, column: String): Boolean {
        return try {
            var found = false
            db.query("PRAGMA table_info($table)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == column) {
                        found = true
                        break
                    }
                }
            }
            found
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Performs database migrations between versions.
     * SQLDelight auto-increments version when schema changes.
     */
    private fun migrateIfNeeded(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration from version 1 to 2+: Add new Task columns
        if (oldVersion < 2) {
            // Add columns that were added in Features 004-007
            safeAddColumn(db, "Task", "request_note", "TEXT")
            safeAddColumn(db, "Task", "repeat_target", "INTEGER")
            safeAddColumn(db, "Task", "repeat_completed", "INTEGER NOT NULL DEFAULT 0")
            safeAddColumn(db, "Task", "linked_goal_id", "TEXT")
            safeAddColumn(db, "Task", "review_note", "TEXT")
            safeAddColumn(db, "Task", "rolled_from_week_id", "TEXT")
        }

        // Create new tables if they don't exist
        // SQLDelight Schema.create() won't run on upgrade, so we need to create them
        if (oldVersion < newVersion) {
            createPartnerTablesIfNeeded(db)
            createGoalTablesIfNeeded(db)
        }
    }

    /**
     * Safely adds a column if it doesn't exist.
     */
    private fun safeAddColumn(
        db: SupportSQLiteDatabase,
        table: String,
        column: String,
        type: String
    ) {
        try {
            db.execSQL("ALTER TABLE $table ADD COLUMN $column $type")
        } catch (e: Exception) {
            // Column might already exist, which is fine
        }
    }

    /**
     * Creates Partner-related tables if they don't exist (Feature 006).
     */
    private fun createPartnerTablesIfNeeded(db: SupportSQLiteDatabase) {
        // Partnership table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Partnership (
                id TEXT NOT NULL PRIMARY KEY,
                user1_id TEXT NOT NULL,
                user2_id TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE'
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_partnership_users ON Partnership(user1_id, user2_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_partnership_user1 ON Partnership(user1_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_partnership_user2 ON Partnership(user2_id)")

        // Invite table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Invite (
                code TEXT NOT NULL PRIMARY KEY,
                creator_id TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                accepted_by TEXT,
                accepted_at INTEGER,
                status TEXT NOT NULL DEFAULT 'PENDING'
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_invite_creator ON Invite(creator_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_invite_status ON Invite(status)")
    }

    /**
     * Creates Goal-related tables if they don't exist (Feature 007).
     */
    private fun createGoalTablesIfNeeded(db: SupportSQLiteDatabase) {
        // Goal table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Goal (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                icon TEXT NOT NULL,
                type TEXT NOT NULL,
                target_per_week INTEGER,
                target_total INTEGER,
                duration_weeks INTEGER,
                start_week_id TEXT NOT NULL,
                owner_id TEXT NOT NULL,
                current_progress INTEGER NOT NULL DEFAULT 0,
                current_week_id TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS goal_owner_id ON Goal(owner_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS goal_status ON Goal(status)")

        // GoalProgress table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS GoalProgress (
                id TEXT NOT NULL PRIMARY KEY,
                goal_id TEXT NOT NULL,
                week_id TEXT NOT NULL,
                progress_value INTEGER NOT NULL,
                target_value INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (goal_id) REFERENCES Goal(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS progress_goal_id ON GoalProgress(goal_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS progress_week_id ON GoalProgress(week_id)")

        // PartnerGoal table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS PartnerGoal (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                icon TEXT NOT NULL,
                type TEXT NOT NULL,
                target_per_week INTEGER,
                target_total INTEGER,
                duration_weeks INTEGER,
                start_week_id TEXT NOT NULL,
                owner_id TEXT NOT NULL,
                current_progress INTEGER NOT NULL DEFAULT 0,
                current_week_id TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                synced_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS partner_goal_owner_id ON PartnerGoal(owner_id)")
    }
}
