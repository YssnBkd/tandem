# Research: Task Data Layer

**Feature Branch**: `002-task-data-layer`
**Date**: 2026-01-01

## Overview

This document consolidates research findings for implementing the Task Data Layer using SQLDelight in a Kotlin Multiplatform project.

## Decision 1: SQLDelight Version and Configuration

**Decision**: Use SQLDelight 2.0.2 (stable) with Kotlin Multiplatform

**Rationale**:
- SQLDelight 2.0+ is the current stable version with full KMP support
- Generates type-safe Kotlin APIs from SQL at compile time
- Validates schema and queries at compile time, preventing runtime SQL errors
- Official Kotlin Multiplatform documentation recommends SQLDelight for local persistence

**Configuration** (for `shared/build.gradle.kts`):
```kotlin
plugins {
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("TandemDatabase") {
            packageName.set("org.epoque.tandem.data.local")
        }
    }
}
```

**Dependencies** (for `gradle/libs.versions.toml`):
```toml
[versions]
sqlDelight = "2.0.2"

[libraries]
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqlDelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqlDelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqlDelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqlDelight" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqlDelight" }
```

**Alternatives Considered**:
- Room: Android-only, doesn't support KMP
- Realm: More complex, overkill for simple task storage

**Sources**: [SQLDelight Multiplatform SQLite](https://sqldelight.github.io/sqldelight/2.0.2/multiplatform_sqlite/), [Kotlin Multiplatform Tutorial](https://kotlinlang.org/docs/multiplatform/multiplatform-ktor-sqldelight.html)

---

## Decision 2: .sq File Organization

**Decision**: Use separate .sq files per entity (Task.sq, Week.sq)

**Rationale**:
- Follows single responsibility principle
- Easier to navigate and maintain
- SQLDelight compiles all .sq files in the directory into a single database
- Queries are grouped logically by entity

**File Structure**:
```text
shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/
├── Task.sq          # Task table and queries
└── Week.sq          # Week table and queries
```

**Alternatives Considered**:
- Single TandemDatabase.sq file: Becomes unwieldy as schema grows
- Queries in separate files from schema: Adds complexity without benefit

---

## Decision 3: Type Adapters for Custom Kotlin Types

**Decision**: Create custom ColumnAdapters for kotlinx.datetime.Instant and enums

**Rationale**:
- SQLDelight requires explicit adapters for non-primitive types
- Enums can use built-in EnumColumnAdapter
- Instant requires custom adapter (store as INTEGER epoch milliseconds for efficiency)

**Instant Adapter Implementation**:
```kotlin
import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}
```

**Enum Adapter** (using built-in):
```kotlin
import app.cash.sqldelight.EnumColumnAdapter

// For TaskStatus and OwnerType
val taskStatusAdapter = EnumColumnAdapter<TaskStatus>()
val ownerTypeAdapter = EnumColumnAdapter<OwnerType>()
```

**SQL Declaration**:
```sql
import org.epoque.tandem.domain.model.TaskStatus;
import org.epoque.tandem.domain.model.OwnerType;
import kotlinx.datetime.Instant;

CREATE TABLE Task (
    id TEXT NOT NULL PRIMARY KEY,
    status TEXT AS TaskStatus NOT NULL,
    owner_type TEXT AS OwnerType NOT NULL,
    created_at INTEGER AS Instant NOT NULL
);
```

**Alternatives Considered**:
- Store Instant as TEXT (ISO 8601): Less efficient for queries, more storage
- Store enums as INTEGER: Less readable in database, error-prone on schema changes

**Sources**: [SQLDelight Types](https://sqldelight.github.io/sqldelight/2.0.2/multiplatform_sqlite/types/)

---

## Decision 4: Reactive Queries with Flow

**Decision**: Use SQLDelight coroutines-extensions for reactive data access

**Rationale**:
- Provides `asFlow()` extension on queries
- Automatically emits new results when database changes
- Integrates seamlessly with Kotlin Coroutines
- Follows existing codebase patterns (kotlinx-coroutines already used)

**Usage Pattern**:
```kotlin
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class TaskRepositoryImpl(private val database: TandemDatabase) : TaskRepository {

    override fun getTasksForWeek(weekId: String): Flow<List<Task>> =
        database.taskQueries
            .selectByWeek(weekId)
            .asFlow()
            .mapToList(Dispatchers.IO)
}
```

**Required Dependency**:
```kotlin
implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
```

**Alternatives Considered**:
- Manual observer pattern: More boilerplate, error-prone
- LiveData: Android-only, not suitable for KMP

**Sources**: [SQLDelight Coroutines](https://sqldelight.github.io/sqldelight/2.0.2/multiplatform_sqlite/coroutines/)

---

## Decision 5: Platform-Specific Driver Pattern

**Decision**: Use expect/actual pattern with DatabaseDriverFactory interface

**Rationale**:
- Official recommended pattern for SQLDelight in KMP
- Abstracts platform differences behind common interface
- Enables dependency injection with Koin
- Supports in-memory driver for testing

**Interface** (commonMain):
```kotlin
interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```

**Android Implementation** (androidMain):
```kotlin
class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        AndroidSqliteDriver(TandemDatabase.Schema, context, "tandem.db")
}
```

**iOS Implementation** (iosMain):
```kotlin
class IOSDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        NativeSqliteDriver(TandemDatabase.Schema, "tandem.db")
}
```

**Test Implementation**:
```kotlin
class TestDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            .also { TandemDatabase.Schema.create(it) }
}
```

**Alternatives Considered**:
- expect/actual functions: Less flexible for DI
- Direct driver instantiation: Harder to test, couples to platform

**Sources**: [Kotlin Multiplatform Tutorial](https://kotlinlang.org/docs/multiplatform/multiplatform-ktor-sqldelight.html)

---

## Decision 6: UUID Generation for Task IDs

**Decision**: Use kotlin.uuid.Uuid (Kotlin 2.0+) for generating task IDs

**Rationale**:
- Kotlin 2.0+ includes built-in UUID support in standard library
- No additional dependency needed
- Consistent across platforms (KMP compatible)
- Stored as TEXT in SQLite for simplicity and human-readability

**Usage**:
```kotlin
import kotlin.uuid.Uuid

val taskId = Uuid.random().toString()
```

**Alternatives Considered**:
- Platform-specific UUID: Requires expect/actual, more complex
- kotlinx-uuid library: Extra dependency when stdlib has it
- Auto-increment INTEGER: Not suitable for distributed systems, sync complexity

---

## Decision 7: Week ID Format

**Decision**: Use ISO 8601 week format: "YYYY-Www" (e.g., "2026-W01")

**Rationale**:
- Matches spec requirement (FR-016)
- Human-readable and sortable
- kotlinx.datetime provides utilities for week calculations
- Standard format recognized globally

**Validation Regex**:
```kotlin
val weekIdPattern = Regex("""^\d{4}-W(0[1-9]|[1-4]\d|5[0-3])$""")
```

**Week Calculation** (using kotlinx.datetime):
```kotlin
import kotlinx.datetime.*

fun getCurrentWeekId(): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val year = today.year
    val weekNumber = today.dayOfYear / 7 + 1 // Simplified; use proper ISO week calc
    return "$year-W${weekNumber.toString().padStart(2, '0')}"
}
```

**Alternatives Considered**:
- Epoch-based week number: Not human-readable
- Date range as ID: More complex, harder to query

---

## Decision 8: Testing Strategy

**Decision**: Use in-memory SQLite driver with JVM-only test module

**Rationale**:
- Fast execution (no disk I/O)
- Isolated tests (fresh database per test)
- JdbcSqliteDriver available for JVM tests
- Validates actual SQL execution, not mocked

**Test Setup**:
```kotlin
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

class TaskRepositoryTest {
    private lateinit var database: TandemDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TandemDatabase.Schema.create(driver)
        database = TandemDatabase(driver, taskAdapter, weekAdapter)
    }

    @AfterTest
    fun teardown() {
        // Driver auto-cleans in-memory database
    }
}
```

**Required Test Dependency**:
```kotlin
commonTest.dependencies {
    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
}
```

**Alternatives Considered**:
- Mock repositories: Doesn't validate SQL correctness
- On-disk test database: Slower, requires cleanup

---

## Summary

| Decision | Choice | Key Benefit |
|----------|--------|-------------|
| SQLDelight Version | 2.0.2 | Stable, full KMP support |
| File Organization | Separate .sq per entity | Maintainability |
| Type Adapters | Custom for Instant, EnumColumnAdapter | Type safety |
| Reactive Queries | coroutines-extensions Flow | Automatic UI updates |
| Driver Pattern | Interface + expect/actual | Testability, DI support |
| ID Generation | kotlin.uuid.Uuid | No extra dependencies |
| Week ID Format | ISO 8601 (YYYY-Www) | Human-readable, sortable |
| Testing | In-memory JdbcSqliteDriver | Fast, isolated tests |
