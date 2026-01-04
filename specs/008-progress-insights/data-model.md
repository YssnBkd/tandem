# Data Model: Progress & Insights

**Feature**: 008-progress-insights
**Date**: 2026-01-04

## Overview

Feature 008 is primarily a **read-only aggregation** feature. It queries existing entities (Week, Task, Partnership) and computes derived data (streaks, completion percentages, trends). The only new persistent data is milestone celebration tracking in DataStore.

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     EXISTING ENTITIES (Read-Only)                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────────┐   │
│  │    Week     │         │    Task     │         │   Partnership   │   │
│  ├─────────────┤         ├─────────────┤         ├─────────────────┤   │
│  │ id (PK)     │◄────────│ week_id     │         │ id (PK)         │   │
│  │ user_id     │         │ id (PK)     │         │ user1_id        │   │
│  │ start_date  │         │ owner_id    │         │ user2_id        │   │
│  │ end_date    │         │ status      │         │ status          │   │
│  │ reviewed_at │         │ title       │         │ created_at      │   │
│  │ rating      │         │ ...         │         └─────────────────┘   │
│  │ review_note │         └─────────────┘                               │
│  └─────────────┘                                                       │
│         │                                                               │
│         │ Queries                                                       │
│         ▼                                                               │
├─────────────────────────────────────────────────────────────────────────┤
│                      COMPUTED AGGREGATIONS                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────────┐   │
│  │  StreakResult   │   │ CompletionStats │   │   TrendDataPoint    │   │
│  ├─────────────────┤   ├─────────────────┤   ├─────────────────────┤   │
│  │ count: Int      │   │ completed: Int  │   │ weekId: String      │   │
│  │ isPartner: Bool │   │ total: Int      │   │ userPct: Int        │   │
│  │ milestone: Int? │   │ percentage: Int │   │ partnerPct: Int?    │   │
│  └─────────────────┘   └─────────────────┘   └─────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                     NEW PERSISTENT DATA (DataStore)                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────┐                               │
│  │      ProgressPreferences            │                               │
│  ├─────────────────────────────────────┤                               │
│  │ last_celebrated_milestone: Int (0)  │  // 0, 5, 10, 20, or 50      │
│  └─────────────────────────────────────┘                               │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Existing Entities (Referenced)

### Week Entity

**Source**: Feature 005 (Week Review)
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Week.kt`

```kotlin
data class Week(
    val id: String,                    // ISO 8601: "2026-W01"
    val startDate: LocalDate,          // Monday of week
    val endDate: LocalDate,            // Sunday of week
    val userId: String,
    val overallRating: Int?,           // 1-5 (null if not reviewed)
    val reviewNote: String?,           // Review reflection text
    val reviewedAt: Instant?,          // When review was completed
    val planningCompletedAt: Instant?
) {
    val isReviewed: Boolean get() = reviewedAt != null
}
```

**Used For**:
- Streak calculation (`isReviewed` property)
- Mood emoji display (`overallRating`)
- Review notes in detail view (`reviewNote`)
- Date range display (`startDate`, `endDate`)

### Task Entity

**Source**: Feature 002 (Task Data Layer)
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Task.kt`

```kotlin
data class Task(
    val id: String,
    val title: String,
    val notes: String?,
    val ownerId: String,
    val ownerType: OwnerType,          // SELF, PARTNER, SHARED
    val weekId: String,
    val status: TaskStatus,            // PENDING, COMPLETED, TRIED, SKIPPED, etc.
    val createdBy: String,
    val requestNote: String?,
    val repeatTarget: Int?,
    val repeatCompleted: Int,
    val linkedGoalId: String?,
    val reviewNote: String?,
    val rolledFromWeekId: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class TaskStatus {
    PENDING,
    PENDING_ACCEPTANCE,
    COMPLETED,           // Counts as "done" for completion %
    TRIED,               // Counts as "incomplete"
    SKIPPED,             // Counts as "incomplete"
    DECLINED
}
```

**Used For**:
- Completion percentage calculation (count by `status`)
- Task outcome display in detail view (`status`, `title`)

### Partnership Entity

**Source**: Feature 006 (Partner System)
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Partnership.kt`

```kotlin
data class Partnership(
    val id: String,
    val user1Id: String,
    val user2Id: String,
    val createdAt: Instant,
    val status: PartnershipStatus
)

enum class PartnershipStatus {
    ACTIVE,
    DISSOLVED
}
```

**Used For**:
- Determining if user has partner for comparison
- Getting partner's user ID for querying their weeks/tasks

### Partner Entity

**Source**: Feature 006 (Partner System)
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Partner.kt`

```kotlin
data class Partner(
    val id: String,                    // Partner's user ID
    val name: String,
    val email: String,
    val partnershipId: String,
    val connectedAt: Instant
)
```

**Used For**:
- Partner name display in UI
- Partner ID for fetching their week/task data

## New Domain Models (Computed, Not Persisted)

### StreakResult

**Purpose**: Result of streak calculation with metadata
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/StreakResult.kt`

```kotlin
data class StreakResult(
    val count: Int,                    // Current streak count
    val isPartnerStreak: Boolean,      // true if partner-based streak
    val pendingMilestone: Int?         // Unseen milestone (5, 10, 20, 50) or null
)
```

**Validation Rules**:
- `count` >= 0
- `pendingMilestone` must be in [5, 10, 20, 50] or null

### CompletionStats

**Purpose**: Task completion statistics for a single week/user
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/CompletionStats.kt`

```kotlin
data class CompletionStats(
    val completedCount: Int,           // Tasks with status == COMPLETED
    val totalCount: Int                // All tasks for the week
) {
    val percentage: Int get() = if (totalCount > 0) {
        (completedCount * 100) / totalCount
    } else 0

    val displayText: String get() = "$completedCount/$totalCount"
}
```

**Validation Rules**:
- `completedCount` >= 0
- `totalCount` >= 0
- `completedCount` <= `totalCount`

### TrendDataPoint

**Purpose**: Single data point for trend chart
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/TrendDataPoint.kt`

```kotlin
data class TrendDataPoint(
    val weekId: String,                // ISO 8601 week ID
    val weekLabel: String,             // Display label: "W01", "W02"
    val userPercentage: Int,           // 0-100
    val partnerPercentage: Int?        // null if no partner that week
)
```

**Validation Rules**:
- `userPercentage` in 0..100
- `partnerPercentage` in 0..100 or null

### TrendChartData

**Purpose**: Complete trend chart data for UI
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/TrendChartData.kt`

```kotlin
data class TrendChartData(
    val dataPoints: List<TrendDataPoint>,  // Ordered oldest to newest
    val hasPartner: Boolean,               // Whether to show partner line
    val insufficientData: Boolean          // True if < 4 weeks
) {
    val weekCount: Int get() = dataPoints.size
}
```

**Validation Rules**:
- `dataPoints.size` <= 8
- `dataPoints` ordered by week ascending

### WeekSummary

**Purpose**: Summary data for past weeks list item
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/WeekSummary.kt`

```kotlin
data class WeekSummary(
    val weekId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userCompletion: CompletionStats,
    val partnerCompletion: CompletionStats?,
    val userRating: Int?,              // 1-5
    val partnerRating: Int?,
    val isReviewed: Boolean            // User completed review
)
```

### PastWeekDetail

**Purpose**: Full detail for past week detail view
**Location**: `shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/PastWeekDetail.kt`

```kotlin
data class PastWeekDetail(
    val weekId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userReview: ReviewDetail,
    val partnerReview: ReviewDetail?,
    val tasks: List<TaskOutcome>
)

data class ReviewDetail(
    val rating: Int?,
    val note: String?,
    val completion: CompletionStats,
    val reviewedAt: Instant?
)

data class TaskOutcome(
    val taskId: String,
    val title: String,
    val userStatus: TaskStatus,
    val partnerStatus: TaskStatus?     // null if not shared task
)
```

## New Persisted Data

### ProgressPreferences (DataStore)

**Purpose**: Track which milestones have been celebrated
**Location**: `composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ProgressPreferences.kt`

```kotlin
object ProgressPreferencesKeys {
    val LAST_CELEBRATED_MILESTONE = intPreferencesKey("last_celebrated_milestone")
}

class ProgressPreferences(
    private val dataStore: DataStore<Preferences>
) {
    val lastCelebratedMilestone: Flow<Int> = dataStore.data
        .map { it[LAST_CELEBRATED_MILESTONE] ?: 0 }

    suspend fun setLastCelebratedMilestone(milestone: Int) {
        dataStore.edit { it[LAST_CELEBRATED_MILESTONE] = milestone }
    }
}
```

**Valid Values**: 0, 5, 10, 20, 50
**Default**: 0 (no milestones celebrated)

## UI State Models

### ProgressUiState

**Purpose**: Complete UI state for Progress screen
**Location**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/ProgressUiState.kt`

```kotlin
data class ProgressUiState(
    // Streak Section
    val currentStreak: Int = 0,
    val isPartnerStreak: Boolean = false,
    val streakMessage: String = "",
    val showMilestoneCelebration: Boolean = false,
    val milestoneValue: Int? = null,

    // Completion Bars (This Month)
    val userMonthlyCompletion: Int = 0,        // 0-100%
    val partnerMonthlyCompletion: Int? = null,
    val userMonthlyText: String = "0/0",
    val partnerMonthlyText: String? = null,

    // Trend Chart
    val trendData: TrendChartData? = null,
    val showTrendChart: Boolean = false,

    // Past Weeks List
    val pastWeeks: List<WeekSummaryUiModel> = emptyList(),
    val hasMoreWeeks: Boolean = false,
    val isLoadingMoreWeeks: Boolean = false,

    // Navigation
    val selectedWeekId: String? = null,

    // General State
    val hasPartner: Boolean = false,
    val partnerName: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    // Computed properties
    val showEmptyState: Boolean get() = !isLoading && pastWeeks.isEmpty() && currentStreak == 0
    val streakDisplayText: String get() = if (isPartnerStreak) {
        "$currentStreak-week streak together"
    } else {
        "$currentStreak-week streak"
    }
}
```

### WeekSummaryUiModel

**Purpose**: UI-ready model for past weeks list item
**Location**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/WeekSummaryUiModel.kt`

```kotlin
data class WeekSummaryUiModel(
    val weekId: String,
    val dateRange: String,             // "Jan 6 - Jan 12"
    val userCompletionText: String,    // "6/8"
    val partnerCompletionText: String?, // "5/6" or null
    val userMoodEmoji: String?,        // Emoji from rating
    val partnerMoodEmoji: String?,
    val isReviewed: Boolean
)
```

### PastWeekDetailUiState

**Purpose**: UI state for past week detail screen
**Location**: `composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/progress/PastWeekDetailUiState.kt`

```kotlin
data class PastWeekDetailUiState(
    val weekId: String = "",
    val dateRange: String = "",
    val userReviewSummary: ReviewSummaryUiModel? = null,
    val partnerReviewSummary: ReviewSummaryUiModel? = null,
    val tasks: List<TaskOutcomeUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ReviewSummaryUiModel(
    val name: String,                  // "You" or partner name
    val moodEmoji: String?,
    val completionText: String,        // "6 of 8 tasks"
    val note: String?
)

data class TaskOutcomeUiModel(
    val id: String,
    val title: String,
    val userStatusLabel: String,       // "Done", "Tried", "Skipped"
    val partnerStatusLabel: String?,
    val userStatusColor: Color,
    val partnerStatusColor: Color?
)
```

## State Transitions

### Milestone Celebration Flow

```
┌─────────────────┐
│  Load Progress  │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Calculate Streak                    │
│  currentStreak = 10                  │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Check Milestone                     │
│  lastCelebrated = 5                  │
│  pendingMilestone = 10               │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Show Celebration                    │
│  showMilestoneCelebration = true     │
│  milestoneValue = 10                 │
└────────┬────────────────────────────┘
         │
         ▼ (after 3 seconds)
┌─────────────────────────────────────┐
│  Dismiss & Persist                   │
│  showMilestoneCelebration = false    │
│  lastCelebrated → 10 (DataStore)     │
└─────────────────────────────────────┘
```

### Past Weeks Pagination Flow

```
┌─────────────────┐
│  Initial Load   │
│  offset = 0     │
│  limit = 10     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  pastWeeks = [W01..W10]              │
│  hasMoreWeeks = true                 │
└────────┬────────────────────────────┘
         │
         ▼ (user scrolls to bottom)
┌─────────────────────────────────────┐
│  Load More                           │
│  offset = 10                         │
│  isLoadingMoreWeeks = true           │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  pastWeeks = [W01..W20]              │
│  hasMoreWeeks = false                │
│  isLoadingMoreWeeks = false          │
└─────────────────────────────────────┘
```

## Data Access Patterns

### Streak Calculation Query

```kotlin
// User's reviewed weeks
SELECT * FROM Week
WHERE user_id = :userId
  AND reviewed_at IS NOT NULL
ORDER BY start_date DESC;

// Partner's reviewed weeks (if partner exists)
SELECT * FROM Week
WHERE user_id = :partnerId
  AND reviewed_at IS NOT NULL
ORDER BY start_date DESC;

// Count consecutive matching weeks
```

### Completion Stats Query

```kotlin
// Count tasks by status for a week
SELECT status, COUNT(*) as count
FROM Task
WHERE week_id = :weekId
  AND owner_id = :userId
GROUP BY status;
```

### Trend Data Query (Last 8 Weeks)

```kotlin
// Get past 8 weeks with completion data
SELECT w.id, w.start_date, w.end_date,
       (SELECT COUNT(*) FROM Task t
        WHERE t.week_id = w.id AND t.owner_id = w.user_id
        AND t.status = 'COMPLETED') as completed,
       (SELECT COUNT(*) FROM Task t
        WHERE t.week_id = w.id AND t.owner_id = w.user_id) as total
FROM Week w
WHERE w.user_id = :userId
  AND w.id < :currentWeekId
ORDER BY w.start_date DESC
LIMIT 8;
```

## Migration Notes

No database migrations required. Feature 008 only reads from existing tables:
- `Week` (Feature 005)
- `Task` (Feature 002)
- `Partnership` (Feature 006)

DataStore preferences are additive (new key: `last_celebrated_milestone`).
