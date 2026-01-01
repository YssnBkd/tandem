# Data Model: Task Data Layer

**Feature Branch**: `002-task-data-layer`
**Date**: 2026-01-01

## Overview

This document defines the data model for tasks and weeks in Tandem, including entity definitions, SQLDelight schema, validation rules, and relationships.

---

## Entities

### Task

Represents a user's task/commitment for a specific week.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | String | No | Unique identifier (UUID) |
| title | String | No | Task title (required, non-empty) |
| notes | String | Yes | Optional notes/details |
| ownerId | String | No | User ID who the task is for |
| ownerType | OwnerType | No | SELF, PARTNER, or SHARED |
| weekId | String | No | ISO 8601 week ID (e.g., "2026-W01") |
| status | TaskStatus | No | Current task status |
| createdBy | String | No | User ID who created the task |
| repeatTarget | Int | Yes | Target repetitions (e.g., 3 for "Gym 3x") |
| repeatCompleted | Int | No | Current repeat count (default 0) |
| linkedGoalId | String | Yes | Optional reference to a goal |
| reviewNote | String | Yes | Note from week review |
| rolledFromWeekId | String | Yes | If rolled over from previous week |
| createdAt | Instant | No | Creation timestamp (UTC) |
| updatedAt | Instant | No | Last update timestamp (UTC) |

### Week

Represents a calendar week for a user.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | String | No | ISO 8601 week ID (e.g., "2026-W01") |
| startDate | LocalDate | No | First day of week (Monday) |
| endDate | LocalDate | No | Last day of week (Sunday) |
| userId | String | No | User ID this week belongs to |
| overallRating | Int | Yes | Week rating 1-5 (from review) |
| reviewNote | String | Yes | Review notes |
| reviewedAt | Instant | Yes | When review was completed |
| planningCompletedAt | Instant | Yes | When planning was completed |

### OwnerType (Enum)

| Value | Description |
|-------|-------------|
| SELF | User's own task |
| PARTNER | Task assigned to/for partner |
| SHARED | Joint responsibility |

### TaskStatus (Enum)

| Value | Description |
|-------|-------------|
| PENDING | Not started |
| PENDING_ACCEPTANCE | Awaiting partner acceptance |
| COMPLETED | Done |
| TRIED | Attempted but incomplete |
| SKIPPED | Intentionally not done |
| DECLINED | Partner rejected |

---

## SQLDelight Schema

### Task.sq

```sql
import org.epoque.tandem.domain.model.TaskStatus;
import org.epoque.tandem.domain.model.OwnerType;
import kotlinx.datetime.Instant;

CREATE TABLE Task (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    notes TEXT,
    owner_id TEXT NOT NULL,
    owner_type TEXT AS OwnerType NOT NULL,
    week_id TEXT NOT NULL,
    status TEXT AS TaskStatus NOT NULL DEFAULT 'PENDING',
    created_by TEXT NOT NULL,
    repeat_target INTEGER,
    repeat_completed INTEGER NOT NULL DEFAULT 0,
    linked_goal_id TEXT,
    review_note TEXT,
    rolled_from_week_id TEXT,
    created_at INTEGER AS Instant NOT NULL,
    updated_at INTEGER AS Instant NOT NULL
);

CREATE INDEX task_week_id ON Task(week_id);
CREATE INDEX task_owner_type ON Task(owner_type);
CREATE INDEX task_owner_id ON Task(owner_id);

-- Queries

selectAll:
SELECT * FROM Task WHERE owner_id = ? OR created_by = ?;

selectById:
SELECT * FROM Task WHERE id = ?;

selectByWeek:
SELECT * FROM Task WHERE week_id = ? AND (owner_id = ? OR created_by = ?);

selectByOwnerType:
SELECT * FROM Task WHERE owner_type = ? AND (owner_id = ? OR created_by = ?);

selectByWeekAndOwnerType:
SELECT * FROM Task
WHERE week_id = ? AND owner_type = ? AND (owner_id = ? OR created_by = ?);

insert:
INSERT INTO Task (
    id, title, notes, owner_id, owner_type, week_id, status, created_by,
    repeat_target, repeat_completed, linked_goal_id, review_note,
    rolled_from_week_id, created_at, updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

updateTask:
UPDATE Task SET
    title = ?,
    notes = ?,
    status = ?,
    updated_at = ?
WHERE id = ?;

updateStatus:
UPDATE Task SET status = ?, updated_at = ? WHERE id = ?;

updateReviewNote:
UPDATE Task SET review_note = ?, updated_at = ? WHERE id = ?;

incrementRepeatCompleted:
UPDATE Task SET repeat_completed = repeat_completed + 1, updated_at = ? WHERE id = ?;

delete:
DELETE FROM Task WHERE id = ?;

deleteByWeek:
DELETE FROM Task WHERE week_id = ?;
```

### Week.sq

```sql
import kotlinx.datetime.Instant;
import kotlinx.datetime.LocalDate;

CREATE TABLE Week (
    id TEXT NOT NULL PRIMARY KEY,
    start_date TEXT AS LocalDate NOT NULL,
    end_date TEXT AS LocalDate NOT NULL,
    user_id TEXT NOT NULL,
    overall_rating INTEGER,
    review_note TEXT,
    reviewed_at INTEGER AS Instant,
    planning_completed_at INTEGER AS Instant
);

CREATE INDEX week_user_id ON Week(user_id);

-- Queries

selectById:
SELECT * FROM Week WHERE id = ?;

selectByUserId:
SELECT * FROM Week WHERE user_id = ? ORDER BY id DESC;

selectPastWeeks:
SELECT * FROM Week WHERE user_id = ? AND id < ? ORDER BY id DESC;

insert:
INSERT INTO Week (
    id, start_date, end_date, user_id, overall_rating, review_note,
    reviewed_at, planning_completed_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);

insertOrUpdate:
INSERT OR REPLACE INTO Week (
    id, start_date, end_date, user_id, overall_rating, review_note,
    reviewed_at, planning_completed_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);

updateReview:
UPDATE Week SET
    overall_rating = ?,
    review_note = ?,
    reviewed_at = ?
WHERE id = ?;

updatePlanningCompleted:
UPDATE Week SET planning_completed_at = ? WHERE id = ?;

delete:
DELETE FROM Week WHERE id = ?;
```

---

## Domain Models (Kotlin)

### Task.kt

```kotlin
package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

data class Task(
    val id: String,
    val title: String,
    val notes: String?,
    val ownerId: String,
    val ownerType: OwnerType,
    val weekId: String,
    val status: TaskStatus,
    val createdBy: String,
    val repeatTarget: Int?,
    val repeatCompleted: Int,
    val linkedGoalId: String?,
    val reviewNote: String?,
    val rolledFromWeekId: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isRepeating: Boolean get() = repeatTarget != null
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED
    val repeatProgress: String? get() = repeatTarget?.let { "$repeatCompleted/$it" }
}
```

### Week.kt

```kotlin
package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Week(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: String,
    val overallRating: Int?,
    val reviewNote: String?,
    val reviewedAt: Instant?,
    val planningCompletedAt: Instant?
) {
    val isReviewed: Boolean get() = reviewedAt != null
    val isPlanningComplete: Boolean get() = planningCompletedAt != null
}
```

### Enums

```kotlin
package org.epoque.tandem.domain.model

enum class OwnerType {
    SELF,
    PARTNER,
    SHARED
}

enum class TaskStatus {
    PENDING,
    PENDING_ACCEPTANCE,
    COMPLETED,
    TRIED,
    SKIPPED,
    DECLINED
}
```

---

## Validation Rules

### Task Validation

| Rule | Field | Constraint |
|------|-------|------------|
| V-T1 | title | Non-empty, trimmed whitespace |
| V-T2 | weekId | Matches ISO 8601 week format: `^\d{4}-W(0[1-9]|[1-4]\d|5[0-3])$` |
| V-T3 | repeatTarget | If present, must be > 0 |
| V-T4 | repeatCompleted | Must be >= 0 |
| V-T5 | overallRating | If present, must be 1-5 |

### Week Validation

| Rule | Field | Constraint |
|------|-------|------------|
| V-W1 | id | Matches ISO 8601 week format |
| V-W2 | startDate | Must be a Monday |
| V-W3 | endDate | Must be startDate + 6 days (Sunday) |
| V-W4 | overallRating | If present, must be 1-5 |

---

## Relationships

```
┌─────────────┐       ┌─────────────┐
│    Task     │       │    Week     │
├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │
│ week_id (FK)│──────▶│             │
│ ...         │       │ user_id     │
└─────────────┘       └─────────────┘
       │
       │ rolled_from_week_id (optional)
       ▼
┌─────────────┐
│    Week     │
│  (previous) │
└─────────────┘
```

**Notes**:
- Task.weekId → Week.id (logical FK, not enforced at DB level for flexibility)
- Task.rolledFromWeekId → Week.id (optional, for rollover tracking)
- Task.linkedGoalId → Goal.id (future feature, stored as TEXT)

---

## Type Adapters

### InstantAdapter.kt

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

### LocalDateAdapter.kt

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

### Database Creation

```kotlin
package org.epoque.tandem.data.local

import app.cash.sqldelight.EnumColumnAdapter
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.TaskStatus

fun createDatabase(driver: SqlDriver): TandemDatabase {
    return TandemDatabase(
        driver = driver,
        TaskAdapter = Task.Adapter(
            owner_typeAdapter = EnumColumnAdapter<OwnerType>(),
            statusAdapter = EnumColumnAdapter<TaskStatus>(),
            created_atAdapter = instantAdapter,
            updated_atAdapter = instantAdapter
        ),
        WeekAdapter = Week.Adapter(
            start_dateAdapter = localDateAdapter,
            end_dateAdapter = localDateAdapter,
            reviewed_atAdapter = instantAdapter,
            planning_completed_atAdapter = instantAdapter
        )
    )
}
```

---

## Migration Strategy

### Initial Schema (Version 1)

No migrations needed for initial release. Schema versioning will be added when schema changes are required.

### Future Considerations

- Add sync metadata columns (remote_id, sync_status, last_synced_at) when implementing cloud sync
- Add goal entity and enforce linked_goal_id FK when Goals feature is implemented
