# Research: Progress & Insights

**Feature**: 008-progress-insights
**Date**: 2026-01-04
**Status**: Complete

## Research Tasks

### 1. Chart Library Evaluation

**Decision**: Custom Compose Canvas implementation
**Rationale**:
- No chart libraries currently in dependencies
- App philosophy prioritizes minimal dependencies
- Trend chart is simple (8 data points, two lines)
- Compose Canvas provides sufficient drawing primitives
- Keeps bundle size small and reduces maintenance burden

**Alternatives Considered**:
- **Vico**: Popular Android charting library, but adds ~500KB to APK and requires learning new API
- **Charts-Compose**: Less mature, limited community support
- **MPAndroidChart**: Not Compose-native, would require interop

**Implementation Approach**:
- Use `Canvas` composable with `drawLine()` for trend lines
- Use `drawRect()` for horizontal completion bars
- Apply Material Design 3 colors from theme
- Animate values with `Animatable` for smooth transitions

### 2. Streak Calculation for Partners

**Decision**: Extend existing `CalculateStreakUseCase` with partner-aware logic
**Rationale**:
- Existing use case only checks single user's weeks
- FR-001 requires both partners to complete review for streak to count
- Reusing existing Week model and repository queries

**Implementation**:
```kotlin
class CalculatePartnerStreakUseCase(
    private val weekRepository: WeekRepository,
    private val partnerRepository: PartnerRepository
) {
    suspend operator fun invoke(userId: String): StreakResult {
        val partner = partnerRepository.getPartner(userId)

        // Solo user: original streak calculation
        if (partner == null) {
            return StreakResult(
                count = calculateSoloStreak(userId),
                isPartnerStreak = false
            )
        }

        // Partner streak: both must complete
        val userWeeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .associateBy { it.id }

        val partnerWeeks = weekRepository.observeWeeksForUser(partner.id)
            .first()
            .associateBy { it.id }

        val allWeekIds = (userWeeks.keys + partnerWeeks.keys)
            .distinct()
            .sortedDescending()

        var streak = 0
        for (weekId in allWeekIds) {
            val userReviewed = userWeeks[weekId]?.isReviewed == true
            val partnerReviewed = partnerWeeks[weekId]?.isReviewed == true

            if (userReviewed && partnerReviewed) {
                streak++
            } else {
                break
            }
        }

        return StreakResult(count = streak, isPartnerStreak = true)
    }
}
```

**Alternatives Considered**:
- Storing streak as a denormalized field (rejected: adds sync complexity, not source of truth)
- Calculating on backend (rejected: offline-first requirement)

### 3. Completion Percentage Calculation

**Decision**: Calculate per-week using existing Task status counts
**Rationale**:
- Task model already has `status` field with `COMPLETED`, `TRIED`, `SKIPPED`
- Per spec FR-005: completion = (completed / total) × 100
- Only `COMPLETED` status counts as complete (Tried/Skipped are incomplete but celebrated)

**Implementation**:
```kotlin
data class CompletionStats(
    val completedCount: Int,
    val totalCount: Int
) {
    val percentage: Int get() = if (totalCount > 0) {
        (completedCount * 100) / totalCount
    } else 0
}

class GetCompletionStatsUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(weekId: String, userId: String): CompletionStats {
        val tasks = taskRepository.observeTasksForWeek(weekId, userId).first()
        val completed = tasks.count { it.status == TaskStatus.COMPLETED }
        return CompletionStats(
            completedCount = completed,
            totalCount = tasks.size
        )
    }
}
```

### 4. Milestone Persistence

**Decision**: Use DataStore for milestone celebration tracking
**Rationale**:
- FR-003 requires milestones shown "once on first view after reaching"
- Need to track which milestones user has seen
- DataStore already used for preferences (segment selection, etc.)
- Simple key-value storage sufficient

**Schema**:
```kotlin
object ProgressPreferences {
    val LAST_CELEBRATED_MILESTONE = intPreferencesKey("last_celebrated_milestone")
    // Stores highest milestone celebrated: 0, 5, 10, 20, or 50
}
```

**Milestone Logic**:
```kotlin
val milestones = listOf(5, 10, 20, 50)

fun getUnseenMilestone(currentStreak: Int, lastCelebrated: Int): Int? {
    return milestones
        .filter { it > lastCelebrated && currentStreak >= it }
        .maxOrNull()
}
```

### 5. Past Weeks Pagination

**Decision**: Offset-based pagination with 10 items per page
**Rationale**:
- FR-007 specifies 10 weeks at a time
- Offset pagination simple and sufficient for linear list
- Total weeks bounded by user history (months, not years typically)

**Implementation**:
```kotlin
class GetPastWeeksUseCase(
    private val weekRepository: WeekRepository
) {
    suspend operator fun invoke(
        userId: String,
        currentWeekId: String,
        offset: Int = 0,
        limit: Int = 10
    ): PastWeeksResult {
        val allPastWeeks = weekRepository.observePastWeeks(currentWeekId, userId)
            .first()
            .sortedByDescending { it.startDate }

        val paged = allPastWeeks.drop(offset).take(limit)

        return PastWeeksResult(
            weeks = paged,
            hasMore = offset + limit < allPastWeeks.size,
            totalCount = allPastWeeks.size
        )
    }
}
```

### 6. Week Summary UI Model

**Decision**: Create `WeekSummaryUiModel` with pre-computed display values
**Rationale**:
- List items need formatted strings, not raw data
- Compute once in ViewModel, not in each Compose recomposition
- Includes both user and partner completion stats

**Schema**:
```kotlin
data class WeekSummaryUiModel(
    val weekId: String,
    val dateRange: String,           // "Jan 6 - Jan 12"
    val userCompletion: String,      // "6/8"
    val partnerCompletion: String?,  // "5/6" or null if no partner
    val userMoodEmoji: String?,      // Emoji from rating 1-5
    val partnerMoodEmoji: String?,
    val isReviewed: Boolean
)
```

**Mood Emoji Mapping**:
```kotlin
fun ratingToEmoji(rating: Int?): String? = when (rating) {
    1 -> "😞"
    2 -> "😐"
    3 -> "😊"
    4 -> "😄"
    5 -> "🎉"
    else -> null
}
```

### 7. Past Week Detail View

**Decision**: Navigate to detail screen with weekId parameter
**Rationale**:
- FR-009/FR-010 require navigation to full detail view
- Reuse existing navigation patterns from Week/Review screens
- Load full task list and review notes on demand

**Data Loading**:
```kotlin
data class PastWeekDetailUiState(
    val weekId: String,
    val dateRange: String,
    val userReview: ReviewSummary,
    val partnerReview: ReviewSummary?,
    val tasks: List<TaskOutcomeUiModel>,
    val isLoading: Boolean = false
)

data class ReviewSummary(
    val rating: Int?,
    val moodEmoji: String?,
    val note: String?,
    val completionText: String  // "6 of 8 tasks"
)

data class TaskOutcomeUiModel(
    val id: String,
    val title: String,
    val userStatus: TaskStatus,
    val partnerStatus: TaskStatus?
)
```

### 8. Trend Chart Data Structure

**Decision**: 8-week time series with user and partner percentages
**Rationale**:
- FR-004 specifies past 8 weeks for trend visualization
- Two lines: user completion %, partner completion %
- X-axis: week labels ("W01", "W02", etc.)
- Y-axis: 0-100%

**Schema**:
```kotlin
data class TrendDataPoint(
    val weekId: String,
    val weekLabel: String,           // "W01" for display
    val userPercentage: Int,         // 0-100
    val partnerPercentage: Int?      // null if no partner that week
)

data class TrendChartData(
    val dataPoints: List<TrendDataPoint>,  // Max 8 points
    val hasPartner: Boolean
)
```

### 9. Solo User Handling

**Decision**: Graceful degradation with single-user views
**Rationale**:
- Edge case: user has no partner connected
- FR-012 requires handling solo users
- Show individual streak and trends only
- Prompt to invite partner without blocking functionality

**UI Adaptations**:
- Streak card: "Your {n}-week streak" instead of "Your {n}-week streak together"
- Trend chart: Single line (user only)
- Completion bars: Only user's bar shown
- Past weeks: Only user's completion and mood

### 10. Offline Behavior

**Decision**: Full read-only functionality offline
**Rationale**:
- SC-008 requires cached data display when offline
- All data from local SQLDelight (Week, Task, Partnership tables)
- No network calls needed for Progress tab
- "Load more" pagination works offline (local data)

**Edge Cases**:
- Milestone celebration saved locally via DataStore
- Sync status indicator if partnership data stale (optional enhancement)

## Technology Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Chart Library | Custom Canvas | Minimal dependencies, simple requirements |
| Streak Storage | Computed on demand | Source of truth is Week.reviewedAt |
| Milestone Persistence | DataStore | Simple key-value, already in dependencies |
| Pagination | Offset-based (10/page) | Simple, bounded data set |
| Partner Comparison | Parallel week queries | No new sync infrastructure needed |

## Dependencies on Existing Features

| Feature | Dependency | Interface |
|---------|------------|-----------|
| 002 Task Data | Task status counts | `TaskRepository.observeTasksForWeek()` |
| 005 Week Review | Week.reviewedAt, overallRating, reviewNote | `WeekRepository.observeWeeksForUser()` |
| 006 Partner System | Partner info | `PartnerRepository.getPartner()` |
| 003 Week View | Week navigation | `WeekRepository.getCurrentWeekId()` |

## Open Questions - Resolved

1. **Q: Should streak reset if only one partner reviews?**
   A: Yes, per FR-001 "both partners completed their weekly review"

2. **Q: What if partners change mid-history?**
   A: Show data with partner active that week (Partnership table has history)

3. **Q: How to handle partial week data in trends?**
   A: Include weeks with at least one task; show 0% if no tasks

4. **Q: Trend chart when <8 weeks of data?**
   A: Show available data with "Need more weeks for full trends" message (per US2 AC3)
