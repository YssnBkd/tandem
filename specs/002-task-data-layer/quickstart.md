# Quickstart: Task Data Layer

**Feature Branch**: `002-task-data-layer`
**Date**: 2026-01-01

## Overview

This guide provides step-by-step implementation instructions for the Task Data Layer feature.

---

## Prerequisites

Before starting implementation:

1. Ensure you're on the `002-task-data-layer` branch
2. Read the [spec.md](./spec.md) for requirements
3. Review [research.md](./research.md) for technical decisions
4. Review [data-model.md](./data-model.md) for schema details

---

## Implementation Order

Follow this sequence for correct dependency ordering:

```
1. Gradle Setup (dependencies)
   ↓
2. Domain Models (enums, data classes)
   ↓
3. SQLDelight Schema (.sq files)
   ↓
4. Type Adapters
   ↓
5. Driver Factory (platform-specific)
   ↓
6. Repository Interfaces
   ↓
7. Repository Implementations
   ↓
8. Use Cases
   ↓
9. Koin Module
   ↓
10. Unit Tests
```

---

## Step 1: Gradle Setup

### 1.1 Update `gradle/libs.versions.toml`

Add SQLDelight dependencies:

```toml
[versions]
sqlDelight = "2.0.2"

[libraries]
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqlDelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqlDelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqlDelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqlDelight" }
sqldelight-sqlite-driver = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqlDelight" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqlDelight" }
```

### 1.2 Update `shared/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("TandemDatabase") {
            packageName.set("org.epoque.tandem.data.local")
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
```

---

## Step 2: Domain Models

Create in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/`:

### 2.1 OwnerType.kt

```kotlin
package org.epoque.tandem.domain.model

enum class OwnerType {
    SELF,
    PARTNER,
    SHARED
}
```

### 2.2 TaskStatus.kt

```kotlin
package org.epoque.tandem.domain.model

enum class TaskStatus {
    PENDING,
    PENDING_ACCEPTANCE,
    COMPLETED,
    TRIED,
    SKIPPED,
    DECLINED
}
```

### 2.3 Task.kt

See [data-model.md](./data-model.md#taskkt) for full implementation.

### 2.4 Week.kt

See [data-model.md](./data-model.md#weekkt) for full implementation.

---

## Step 3: SQLDelight Schema

Create `.sq` files in `shared/src/commonMain/sqldelight/org/epoque/tandem/data/local/`:

### 3.1 Task.sq

See [data-model.md](./data-model.md#tasksq) for full schema and queries.

### 3.2 Week.sq

See [data-model.md](./data-model.md#weeksq) for full schema and queries.

### 3.3 Generate Database Code

Run Gradle task to generate SQLDelight code:

```bash
./gradlew :shared:generateCommonMainTandemDatabaseInterface
```

---

## Step 4: Type Adapters

Create in `shared/src/commonMain/kotlin/org/epoque/tandem/data/local/adapter/`:

### 4.1 InstantAdapter.kt

```kotlin
package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}
```

### 4.2 LocalDateAdapter.kt

```kotlin
package org.epoque.tandem.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDate

val localDateAdapter = object : ColumnAdapter<LocalDate, String> {
    override fun decode(databaseValue: String): LocalDate =
        LocalDate.parse(databaseValue)

    override fun encode(value: LocalDate): String =
        value.toString()
}
```

---

## Step 5: Driver Factory

### 5.1 Common Interface

`shared/src/commonMain/kotlin/org/epoque/tandem/data/local/DatabaseDriverFactory.kt`:

```kotlin
package org.epoque.tandem.data.local

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```

### 5.2 Android Implementation

`shared/src/androidMain/kotlin/org/epoque/tandem/data/local/DatabaseDriverFactory.kt`:

```kotlin
package org.epoque.tandem.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        AndroidSqliteDriver(TandemDatabase.Schema, context, "tandem.db")
}
```

### 5.3 iOS Implementation

`shared/src/iosMain/kotlin/org/epoque/tandem/data/local/DatabaseDriverFactory.kt`:

```kotlin
package org.epoque.tandem.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IOSDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        NativeSqliteDriver(TandemDatabase.Schema, "tandem.db")
}
```

---

## Step 6: Repository Interfaces

Create in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/`:

### 6.1 TaskRepository.kt

See [contracts/task-repository.md](./contracts/task-repository.md) for full interface.

### 6.2 WeekRepository.kt

See [contracts/week-repository.md](./contracts/week-repository.md) for full interface.

---

## Step 7: Repository Implementations

Create in `shared/src/commonMain/kotlin/org/epoque/tandem/data/repository/`:

### 7.1 TaskRepositoryImpl.kt

Key implementation patterns:

```kotlin
package org.epoque.tandem.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.domain.model.*
import org.epoque.tandem.domain.repository.TaskRepository
import kotlin.uuid.Uuid

class TaskRepositoryImpl(
    private val database: TandemDatabase
) : TaskRepository {

    private val queries get() = database.taskQueries

    override fun observeTasksForWeek(weekId: String, userId: String): Flow<List<Task>> =
        queries.selectByWeek(weekId, userId, userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { dbTasks -> dbTasks.map { it.toDomainModel() } }

    override suspend fun createTask(task: Task): Task = withContext(Dispatchers.IO) {
        require(task.title.isNotBlank()) { "Task title cannot be empty" }
        require(isValidWeekId(task.weekId)) { "Invalid week ID format: ${task.weekId}" }

        val now = Clock.System.now()
        val newTask = task.copy(
            id = Uuid.random().toString(),
            createdAt = now,
            updatedAt = now,
            status = TaskStatus.PENDING,
            repeatCompleted = 0
        )

        queries.insert(/* ... parameters from newTask ... */)
        newTask
    }

    // ... other implementations
}

private fun isValidWeekId(weekId: String): Boolean =
    weekId.matches(Regex("""^\d{4}-W(0[1-9]|[1-4]\d|5[0-3])$"""))
```

### 7.2 WeekRepositoryImpl.kt

Key implementation patterns:

```kotlin
package org.epoque.tandem.data.repository

import kotlinx.datetime.*
import org.epoque.tandem.domain.model.Week
import org.epoque.tandem.domain.repository.WeekRepository

class WeekRepositoryImpl(
    private val database: TandemDatabase
) : WeekRepository {

    override fun getCurrentWeekId(): String {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return calculateIsoWeekId(today)
    }

    override suspend fun getOrCreateCurrentWeek(userId: String): Week {
        val weekId = getCurrentWeekId()
        return getWeekById(weekId) ?: createWeekForDate(weekId, userId)
    }

    private fun calculateIsoWeekId(date: LocalDate): String {
        // ISO 8601 week calculation
        // See kotlinx.datetime documentation for proper implementation
    }

    // ... other implementations
}
```

---

## Step 8: Use Cases

Create in `shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/`:

### Example: CreateTaskUseCase.kt

```kotlin
package org.epoque.tandem.domain.usecase.task

import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.TaskRepository
import kotlinx.datetime.Instant

class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        title: String,
        notes: String?,
        ownerId: String,
        ownerType: OwnerType,
        weekId: String,
        createdBy: String,
        repeatTarget: Int? = null
    ): Task {
        val task = Task(
            id = "", // Will be generated
            title = title.trim(),
            notes = notes?.trim(),
            ownerId = ownerId,
            ownerType = ownerType,
            weekId = weekId,
            status = if (ownerType == OwnerType.PARTNER) TaskStatus.PENDING_ACCEPTANCE else TaskStatus.PENDING,
            createdBy = createdBy,
            repeatTarget = repeatTarget,
            repeatCompleted = 0,
            linkedGoalId = null,
            reviewNote = null,
            rolledFromWeekId = null,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST
        )
        return taskRepository.createTask(task)
    }
}
```

---

## Step 9: Koin Module

Create in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/`:

### TaskModule.kt

```kotlin
package org.epoque.tandem.di

import org.epoque.tandem.data.local.AndroidDatabaseDriverFactory
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.local.createDatabase
import org.epoque.tandem.data.repository.TaskRepositoryImpl
import org.epoque.tandem.data.repository.WeekRepositoryImpl
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository
import org.epoque.tandem.domain.usecase.task.*
import org.epoque.tandem.domain.usecase.week.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val taskModule = module {
    // Database
    single { AndroidDatabaseDriverFactory(androidContext()) }
    single { createDatabase(get<AndroidDatabaseDriverFactory>().createDriver()) }

    // Repositories
    single<TaskRepository> { TaskRepositoryImpl(get()) }
    single<WeekRepository> { WeekRepositoryImpl(get()) }

    // Use Cases - Task
    factory { CreateTaskUseCase(get()) }
    factory { GetTasksForWeekUseCase(get()) }
    factory { UpdateTaskStatusUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }

    // Use Cases - Week
    factory { GetCurrentWeekUseCase(get()) }
    factory { SaveWeekReviewUseCase(get()) }
}
```

---

## Step 10: Unit Tests

Create in `shared/src/commonTest/kotlin/org/epoque/tandem/`:

### Example: TaskRepositoryTest.kt

```kotlin
package org.epoque.tandem.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.epoque.tandem.data.local.TandemDatabase
import org.epoque.tandem.data.local.createDatabase
import org.epoque.tandem.domain.model.*
import kotlin.test.*

class TaskRepositoryTest {
    private lateinit var database: TandemDatabase
    private lateinit var repository: TaskRepositoryImpl

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TandemDatabase.Schema.create(driver)
        database = createDatabase(driver)
        repository = TaskRepositoryImpl(database)
    }

    @Test
    fun `createTask generates ID and timestamps`() = runTest {
        val task = createTestTask(title = "Test Task")

        val created = repository.createTask(task)

        assertNotEquals("", created.id)
        assertNotEquals(Instant.DISTANT_PAST, created.createdAt)
        assertEquals("Test Task", created.title)
    }

    @Test
    fun `createTask throws on empty title`() = runTest {
        val task = createTestTask(title = "   ")

        assertFailsWith<IllegalArgumentException> {
            repository.createTask(task)
        }
    }

    // ... more tests
}
```

---

## Validation Checklist

Before completing implementation:

- [ ] `./gradlew :shared:compileDebugKotlinAndroid` succeeds
- [ ] All repository interface methods implemented
- [ ] Unit tests pass: `./gradlew :shared:testDebugUnitTest`
- [ ] Koin module provides all dependencies
- [ ] No compiler warnings related to SQLDelight

---

## Common Issues

### SQLDelight Generation Fails

```bash
# Clean and regenerate
./gradlew :shared:clean :shared:generateCommonMainTandemDatabaseInterface
```

### Missing SQLite Dependency on iOS

Add to iOS framework config in `shared/build.gradle.kts`:

```kotlin
iosTarget.binaries.framework {
    baseName = "Shared"
    isStatic = true
    linkerOpts("-lsqlite3")
}
```

### Flow Not Emitting Updates

Ensure you're using `asFlow()` from `coroutines-extensions`, not creating a custom flow.

---

## Next Steps

After completing implementation:

1. Run full test suite: `./gradlew :composeApp:testDebugUnitTest`
2. Build Android: `./gradlew :composeApp:assembleDebug`
3. Run `/speckit.tasks` to generate task breakdown for UI layer
