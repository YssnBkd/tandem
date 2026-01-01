# Contract: WeekRepository

**Feature Branch**: `002-task-data-layer`
**Date**: 2026-01-01

## Overview

The WeekRepository interface defines the contract for week data access. It manages week entities, including automatic creation of the current week and review data storage.

---

## Interface Definition

```kotlin
package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.epoque.tandem.domain.model.Week

interface WeekRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (Reactive)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Observe a specific week by ID.
     * Emits null if week doesn't exist, then creates it if it's the current week.
     *
     * @param weekId ISO 8601 week ID (e.g., "2026-W01")
     * @return Flow of the week (null if not found)
     */
    fun observeWeek(weekId: String): Flow<Week?>

    /**
     * Observe all weeks for a user, ordered by ID descending (most recent first).
     *
     * @param userId The user's ID
     * @return Flow of all weeks for the user
     */
    fun observeWeeksForUser(userId: String): Flow<List<Week>>

    /**
     * Observe past weeks (before the specified week).
     *
     * @param currentWeekId The reference week ID
     * @param userId The user's ID
     * @return Flow of past weeks, ordered descending
     */
    fun observePastWeeks(currentWeekId: String, userId: String): Flow<List<Week>>

    // ═══════════════════════════════════════════════════════════════════════════
    // READ OPERATIONS (One-shot)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get a week by ID.
     *
     * @param weekId ISO 8601 week ID
     * @return The week, or null if not found
     */
    suspend fun getWeekById(weekId: String): Week?

    /**
     * Get the current week, creating it if it doesn't exist.
     *
     * @param userId The user's ID
     * @return The current week (guaranteed non-null)
     */
    suspend fun getOrCreateCurrentWeek(userId: String): Week

    /**
     * Calculate the current week ID based on system time.
     *
     * @return ISO 8601 week ID for the current date
     */
    fun getCurrentWeekId(): String

    // ═══════════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create or update a week.
     * Use for initial week creation or full week updates.
     *
     * @param week The week to save
     * @return The saved week
     * @throws IllegalArgumentException if week ID format is invalid
     * @throws IllegalArgumentException if startDate is not a Monday
     */
    suspend fun saveWeek(week: Week): Week

    /**
     * Update week review data.
     *
     * @param weekId The week ID
     * @param overallRating Rating 1-5 (nullable to clear)
     * @param reviewNote Review notes (nullable)
     * @return The updated week, or null if week not found
     * @throws IllegalArgumentException if rating not in 1-5 range
     */
    suspend fun updateWeekReview(
        weekId: String,
        overallRating: Int?,
        reviewNote: String?
    ): Week?

    /**
     * Mark planning as completed for a week.
     *
     * @param weekId The week ID
     * @return The updated week, or null if not found
     */
    suspend fun markPlanningCompleted(weekId: String): Week?

    /**
     * Delete a week (and cascade delete its tasks via TaskRepository).
     *
     * @param weekId The week ID to delete
     * @return true if deleted, false if not found
     */
    suspend fun deleteWeek(weekId: String): Boolean
}
```

---

## Method Requirements Mapping

| Method | Spec Requirement | Notes |
|--------|------------------|-------|
| observeWeek | FR-012 | Reactive single week |
| observeWeeksForUser | - | For week list UI |
| observePastWeeks | FR-014 | For review history |
| getWeekById | FR-012 | One-shot retrieval |
| getOrCreateCurrentWeek | FR-011 | Auto-creates if missing |
| getCurrentWeekId | FR-016 | ISO 8601 calculation |
| saveWeek | - | Create/update full week |
| updateWeekReview | FR-013 | Rating + notes + timestamp |
| markPlanningCompleted | - | Planning flow support |
| deleteWeek | - | Week cleanup |

---

## Week ID Calculation

The `getCurrentWeekId()` method calculates the ISO 8601 week:

```kotlin
import kotlinx.datetime.*

fun getCurrentWeekId(): String {
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    // ISO 8601: Week 1 contains the first Thursday of the year
    // Weeks start on Monday
    val year = today.year
    val dayOfYear = today.dayOfYear

    // Calculate ISO week number
    val jan1 = LocalDate(year, 1, 1)
    val jan1DayOfWeek = jan1.dayOfWeek.ordinal // Monday = 0
    val daysToFirstMonday = if (jan1DayOfWeek == 0) 0 else (7 - jan1DayOfWeek)

    val weekNumber = if (dayOfYear <= daysToFirstMonday) {
        // Last week of previous year
        getLastWeekOfYear(year - 1)
    } else {
        ((dayOfYear - daysToFirstMonday - 1) / 7) + 1
    }

    return "$year-W${weekNumber.toString().padStart(2, '0')}"
}
```

---

## Week Creation Logic

### getOrCreateCurrentWeek

```
weekId = getCurrentWeekId()
existing = getWeekById(weekId)

IF existing != null THEN
  RETURN existing

// Calculate week boundaries
today = Clock.System.now().toLocalDate()
dayOfWeek = today.dayOfWeek.ordinal  // Monday = 0
startDate = today.minus(dayOfWeek, DateTimeUnit.DAY)
endDate = startDate.plus(6, DateTimeUnit.DAY)

week = Week(
    id = weekId,
    startDate = startDate,
    endDate = endDate,
    userId = userId,
    overallRating = null,
    reviewNote = null,
    reviewedAt = null,
    planningCompletedAt = null
)

RETURN saveWeek(week)
```

---

## Validation Behavior

### saveWeek

```
IF NOT weekId.matches(ISO_8601_WEEK_PATTERN) THEN
  throw IllegalArgumentException("Invalid week ID format: $weekId")

IF startDate.dayOfWeek != DayOfWeek.MONDAY THEN
  throw IllegalArgumentException("Week start date must be a Monday")

IF endDate != startDate.plus(6, DateTimeUnit.DAY) THEN
  throw IllegalArgumentException("Week end date must be 6 days after start")

INSERT OR REPLACE week
RETURN week
```

### updateWeekReview

```
IF overallRating != null AND (overallRating < 1 OR overallRating > 5) THEN
  throw IllegalArgumentException("Rating must be between 1 and 5")

IF NOT exists(weekId) THEN
  RETURN null

SET reviewedAt = Clock.System.now()
UPDATE week
RETURN updated week
```

---

## Flow Behavior

All `observe*` methods return Flows with:

1. **Immediate emission**: Current database state
2. **Reactive updates**: New emissions on database changes
3. **Null handling**: `observeWeek` can emit `null` for non-existent weeks
4. **Ordering**: Lists ordered by week ID descending (most recent first)

---

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Invalid week ID format | `IllegalArgumentException` |
| Invalid startDate (not Monday) | `IllegalArgumentException` |
| Invalid rating (not 1-5) | `IllegalArgumentException` |
| Week not found (update/delete) | Return `null` or `false` |
| Database error | Propagate as exception |

---

## Usage Examples

### Getting Current Week

```kotlin
// Auto-creates if doesn't exist
val currentWeek = weekRepository.getOrCreateCurrentWeek(userId)
```

### Observing Past Weeks

```kotlin
weekRepository.observePastWeeks(currentWeekId, userId)
    .collect { pastWeeks ->
        showReviewHistory(pastWeeks)
    }
```

### Completing Week Review

```kotlin
weekRepository.updateWeekReview(
    weekId = "2026-W01",
    overallRating = 4,
    reviewNote = "Great week! Completed most tasks."
)
```

### Marking Planning Complete

```kotlin
weekRepository.markPlanningCompleted("2026-W01")
```
