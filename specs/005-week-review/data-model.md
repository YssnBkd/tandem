# Data Model: Week Review

**Feature**: 005-week-review | **Date**: 2026-01-03

This document defines data structures, state classes, and persistence for the Week Review feature.

## Table of Contents

1. [Existing Entities (No Changes)](#1-existing-entities-no-changes)
2. [UI State Classes](#2-ui-state-classes)
3. [Event Classes](#3-event-classes)
4. [Side Effect Classes](#4-side-effect-classes)
5. [DataStore Progress Schema](#5-datastore-progress-schema)
6. [New Use Cases](#6-new-use-cases)

---

## 1. Existing Entities (No Changes)

The Week Review feature uses existing entities from Feature 002. **No schema changes required.**

### Week Entity

```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Week.kt
data class Week(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: String,
    val overallRating: Int?,           // ✅ Used for week rating (1-5)
    val reviewNote: String?,           // ✅ Used for optional rating note
    val reviewedAt: Instant?,          // ✅ Used to mark review complete
    val planningCompletedAt: Instant?
) {
    val isReviewed: Boolean get() = reviewedAt != null  // ✅ Used for banner visibility
    val isPlanningComplete: Boolean get() = planningCompletedAt != null
}
```

### Task Entity

```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Task.kt
data class Task(
    val id: String,
    val title: String,
    val notes: String?,
    val ownerId: String,
    val ownerType: OwnerType,
    val weekId: String,
    val status: TaskStatus,            // ✅ Updated during review
    val createdBy: String,
    val repeatTarget: Int?,
    val repeatCompleted: Int,
    val linkedGoalId: String?,
    val reviewNote: String?,           // ✅ Used for per-task review note
    val rolledFromWeekId: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### TaskStatus Enum

```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/TaskStatus.kt
enum class TaskStatus {
    PENDING,                  // Not started
    PENDING_ACCEPTANCE,       // Partner request awaiting acceptance
    COMPLETED,                // ✅ "Done" in review UI
    TRIED,                    // ✅ "Tried" in review UI
    SKIPPED,                  // ✅ "Skipped" in review UI
    DECLINED                  // Partner rejected request
}
```

### Existing Repository Methods Used

| Repository | Method | Purpose in Review |
|------------|--------|-------------------|
| TaskRepository | `observeTasksForWeek(weekId, userId)` | Load tasks for review |
| TaskRepository | `updateTaskStatus(taskId, status)` | Mark Done/Tried/Skipped |
| TaskRepository | `updateTaskReviewNote(taskId, note)` | Save per-task note |
| WeekRepository | `getOrCreateCurrentWeek(userId)` | Ensure week exists |
| WeekRepository | `updateWeekReview(weekId, rating, note)` | Save overall rating |
| WeekRepository | `observeWeeksForUser(userId)` | Calculate streak |

---

## 2. UI State Classes

### ReviewUiState

```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewUiState.kt

data class ReviewUiState(
    // Review mode and step
    val reviewMode: ReviewMode = ReviewMode.SOLO,
    val currentStep: ReviewStep = ReviewStep.MODE_SELECT,

    // Week data
    val currentWeek: Week? = null,
    val isReviewWindowOpen: Boolean = false,

    // Rating step
    val overallRating: Int? = null,
    val overallNote: String = "",

    // Task review step
    val tasksToReview: List<Task> = emptyList(),
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, TaskStatus> = emptyMap(),
    val taskNotes: Map<String, String> = emptyMap(),

    // Summary
    val currentStreak: Int = 0,
    val completionPercentage: Int = 0,

    // Loading and error states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,

    // Resume capability
    val hasIncompleteProgress: Boolean = false
) {
    // Computed properties
    val currentTask: Task?
        get() = tasksToReview.getOrNull(currentTaskIndex)

    val totalTasks: Int
        get() = tasksToReview.size

    val reviewedTaskCount: Int
        get() = taskOutcomes.size

    val canProceedFromRating: Boolean
        get() = overallRating != null

    val isLastTask: Boolean
        get() = currentTaskIndex >= tasksToReview.size - 1

    val doneCount: Int
        get() = taskOutcomes.count { it.value == TaskStatus.COMPLETED }
}

enum class ReviewMode {
    SOLO,
    TOGETHER
}

enum class ReviewStep {
    MODE_SELECT,
    RATING,
    TASK_REVIEW,
    SUMMARY
}
```

### TogetherReviewState (for US5 - P5)

```kotlin
// Extension for Together mode (can be added to ReviewUiState or separate)
data class TogetherReviewState(
    val currentTurn: PartnerTurn = PartnerTurn.PARTNER_A,
    val partnerAUserId: String = "",
    val partnerBUserId: String = "",
    val partnerARating: Int? = null,
    val partnerBRating: Int? = null,
    val partnerARatingNote: String = "",
    val partnerBRatingNote: String = "",
    val reactions: Map<String, List<Reaction>> = emptyMap() // taskId -> reactions
)

enum class PartnerTurn {
    PARTNER_A,
    PARTNER_B
}

data class Reaction(
    val emoji: String,      // "👏", "❤️", "💪"
    val reactorUserId: String,
    val timestamp: Instant
)
```

---

## 3. Event Classes

```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewEvent.kt

sealed interface ReviewEvent {
    // Mode selection
    data class SelectMode(val mode: ReviewMode) : ReviewEvent

    // Rating step
    data class SelectRating(val rating: Int) : ReviewEvent
    data class UpdateRatingNote(val note: String) : ReviewEvent
    object ContinueToTasks : ReviewEvent
    object QuickFinish : ReviewEvent

    // Task review step
    data class SelectTaskOutcome(
        val taskId: String,
        val status: TaskStatus
    ) : ReviewEvent
    data class UpdateTaskNote(
        val taskId: String,
        val note: String
    ) : ReviewEvent
    object NextTask : ReviewEvent
    object PreviousTask : ReviewEvent

    // Summary
    object CompleteReview : ReviewEvent
    object StartNextWeek : ReviewEvent
    object Done : ReviewEvent

    // Progress
    object ResumeProgress : ReviewEvent
    object DiscardProgress : ReviewEvent

    // Together mode (P5)
    object PassToPartner : ReviewEvent
    data class AddReaction(
        val taskId: String,
        val emoji: String
    ) : ReviewEvent

    // Error handling
    object DismissError : ReviewEvent
    object Retry : ReviewEvent
}
```

---

## 4. Side Effect Classes

```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/review/ReviewSideEffect.kt

sealed interface ReviewSideEffect {
    // Navigation
    object NavigateToRating : ReviewSideEffect
    data class NavigateToTask(val index: Int) : ReviewSideEffect
    object NavigateToSummary : ReviewSideEffect
    object NavigateToPlanning : ReviewSideEffect
    object NavigateBack : ReviewSideEffect
    object CloseReview : ReviewSideEffect

    // Feedback
    data class ShowError(val message: String) : ReviewSideEffect
    object ShowReviewComplete : ReviewSideEffect

    // Together mode
    object ShowPassToPartnerDialog : ReviewSideEffect
}
```

---

## 5. DataStore Progress Schema

```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ReviewProgress.kt

import kotlinx.serialization.Serializable

@Serializable
data class ReviewProgress(
    val weekId: String,
    val reviewMode: String = "SOLO",  // Serialized as string for DataStore
    val currentStep: String = "MODE_SELECT",
    val overallRating: Int? = null,
    val overallNote: String = "",
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, String> = emptyMap(),  // taskId -> status name
    val taskNotes: Map<String, String> = emptyMap(),
    val lastUpdatedAt: Long = 0
) {
    fun toUiState(): Pair<ReviewMode, ReviewStep> {
        return Pair(
            ReviewMode.valueOf(reviewMode),
            ReviewStep.valueOf(currentStep)
        )
    }

    fun getTaskOutcomesAsStatus(): Map<String, TaskStatus> {
        return taskOutcomes.mapValues { TaskStatus.valueOf(it.value) }
    }
}
```

### DataStore Definition

```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/data/preferences/ReviewProgressDataStore.kt

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ReviewProgressDataStore(
    private val dataStore: DataStore<Preferences>
) {
    private val progressKey = stringPreferencesKey("review_progress")

    val progress: Flow<ReviewProgress?> = dataStore.data.map { prefs ->
        prefs[progressKey]?.let { json ->
            try {
                Json.decodeFromString<ReviewProgress>(json)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun saveProgress(progress: ReviewProgress) {
        dataStore.edit { prefs ->
            prefs[progressKey] = Json.encodeToString(progress)
        }
    }

    suspend fun clearProgress() {
        dataStore.edit { prefs ->
            prefs.remove(progressKey)
        }
    }
}
```

---

## 6. New Use Cases

### CalculateStreakUseCase

```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/CalculateStreakUseCase.kt

class CalculateStreakUseCase(
    private val weekRepository: WeekRepository
) {
    /**
     * Calculate the current streak of consecutive reviewed weeks.
     * Streak counts backwards from the most recent reviewed week.
     *
     * @param userId The user ID
     * @return The number of consecutive reviewed weeks
     */
    suspend operator fun invoke(userId: String): Int {
        val weeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .sortedByDescending { it.startDate }

        var streak = 0
        for (week in weeks) {
            if (week.isReviewed) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
```

### IsReviewWindowOpenUseCase

```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/IsReviewWindowOpenUseCase.kt

import kotlinx.datetime.*

class IsReviewWindowOpenUseCase(
    private val clock: Clock = Clock.System
) {
    /**
     * Check if the review window is currently open.
     * Review window: Friday 6PM - Sunday 11:59PM (device timezone)
     *
     * @return true if review window is open
     */
    operator fun invoke(): Boolean {
        val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayOfWeek = now.dayOfWeek
        val hour = now.hour

        return when (dayOfWeek) {
            DayOfWeek.FRIDAY -> hour >= 18      // Friday 6PM onwards
            DayOfWeek.SATURDAY -> true          // All of Saturday
            DayOfWeek.SUNDAY -> true            // All of Sunday (until 11:59PM)
            else -> false
        }
    }
}
```

### GetReviewStatsUseCase

```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/usecase/review/GetReviewStatsUseCase.kt

data class ReviewStats(
    val totalTasks: Int,
    val doneCount: Int,
    val triedCount: Int,
    val skippedCount: Int,
    val completionPercentage: Int  // Based on Done only
)

class GetReviewStatsUseCase {
    /**
     * Calculate review statistics from task outcomes.
     *
     * @param taskOutcomes Map of taskId to TaskStatus
     * @return ReviewStats with counts and percentage
     */
    operator fun invoke(taskOutcomes: Map<String, TaskStatus>): ReviewStats {
        val doneCount = taskOutcomes.count { it.value == TaskStatus.COMPLETED }
        val triedCount = taskOutcomes.count { it.value == TaskStatus.TRIED }
        val skippedCount = taskOutcomes.count { it.value == TaskStatus.SKIPPED }
        val totalTasks = taskOutcomes.size

        val completionPercentage = if (totalTasks > 0) {
            (doneCount * 100) / totalTasks
        } else {
            0
        }

        return ReviewStats(
            totalTasks = totalTasks,
            doneCount = doneCount,
            triedCount = triedCount,
            skippedCount = skippedCount,
            completionPercentage = completionPercentage
        )
    }
}
```

---

## Summary

| Category | Items | Location |
|----------|-------|----------|
| **Existing (unchanged)** | Week, Task, TaskStatus | shared/domain/model |
| **UI State** | ReviewUiState, ReviewMode, ReviewStep | commonMain/presentation/review |
| **Events** | ReviewEvent (sealed interface) | commonMain/presentation/review |
| **Side Effects** | ReviewSideEffect (sealed interface) | commonMain/presentation/review |
| **DataStore** | ReviewProgress, ReviewProgressDataStore | androidMain/data/preferences |
| **Use Cases** | CalculateStreak, IsReviewWindowOpen, GetReviewStats | commonMain/domain/usecase/review |

**Key Point**: No database schema changes required. All persistence uses existing SQLDelight entities and DataStore for progress.
