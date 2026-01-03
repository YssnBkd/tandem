# Research: Week Review

**Feature**: 005-week-review | **Date**: 2026-01-03

This document captures design decisions and rationale for the Week Review feature. Reference this when you need to understand "why" a particular approach was chosen.

## Table of Contents

1. [Review Window Time Calculation](#1-review-window-time-calculation)
2. [Streak Calculation Algorithm](#2-streak-calculation-algorithm)
3. [Review Progress Persistence Strategy](#3-review-progress-persistence-strategy)
4. [Emoji Rating Scale Design](#4-emoji-rating-scale-design)
5. [Task Review Order Strategy](#5-task-review-order-strategy)
6. [Quick Finish Behavior](#6-quick-finish-behavior)
7. [Together Mode Architecture](#7-together-mode-architecture)
8. [Navigation Flow Design](#8-navigation-flow-design)

---

## 1. Review Window Time Calculation

### Decision

Use `kotlinx.datetime` with device timezone to determine if review window is open (Friday 6PM - Sunday 11:59PM).

### Rationale

- **Device timezone**: Respects user's local context without requiring timezone settings
- **Fixed times**: Friday 6PM is natural end-of-work-week; Sunday 11:59PM gives full weekend
- **No user configuration**: Intentional Simplicity - one review rhythm for all users

### Implementation

```kotlin
// IsReviewWindowOpenUseCase.kt
class IsReviewWindowOpenUseCase(
    private val clock: Clock = Clock.System
) {
    operator fun invoke(): Boolean {
        val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayOfWeek = now.dayOfWeek
        val hour = now.hour

        return when (dayOfWeek) {
            DayOfWeek.FRIDAY -> hour >= 18  // Friday 6PM+
            DayOfWeek.SATURDAY -> true       // All Saturday
            DayOfWeek.SUNDAY -> hour < 24    // Sunday until 11:59PM (all day)
            else -> false
        }
    }
}
```

### Alternatives Considered

| Alternative | Rejected Because |
|-------------|------------------|
| User-configurable trigger time | Adds complexity, unclear benefit |
| Server-driven window | Requires online, defeats offline-first |
| Always available | Loses weekly rhythm emphasis |

---

## 2. Streak Calculation Algorithm

### Decision

Count consecutive weeks where `reviewedAt` is not null, starting from the current week going backwards.

### Rationale

- **Simple rule**: Streak breaks if any week is missed - easy to understand
- **No grace period**: Intentional Simplicity - clear expectations
- **Both partners must complete** (couple mode): Reinforces shared commitment

### Implementation

```kotlin
// CalculateStreakUseCase.kt
class CalculateStreakUseCase(
    private val weekRepository: WeekRepository
) {
    suspend operator fun invoke(userId: String): Int {
        val weeks = weekRepository.observeWeeksForUser(userId)
            .first()
            .sortedByDescending { it.startDate }

        var streak = 0
        for (week in weeks) {
            if (week.isReviewed) {
                streak++
            } else {
                break // Streak ends at first unreviewed week
            }
        }
        return streak
    }
}
```

### Edge Cases

| Scenario | Behavior |
|----------|----------|
| First week ever | Streak = 0 (or 1 after first review) |
| Skipped week in middle | Streak resets to count after gap |
| Current week not yet reviewed | Include in streak only if reviewed |
| No weeks exist | Streak = 0 |

### Alternatives Considered

| Alternative | Rejected Because |
|-------------|------------------|
| 7-day grace period | Complexity, dilutes weekly rhythm |
| Partial credit for late review | Against "Celebration Over Judgment" - either done or not |
| Individual streaks in couple mode | Misses point of shared accountability |

---

## 3. Review Progress Persistence Strategy

### Decision

Use DataStore (not SQLDelight) for review progress, keyed by weekId.

### Rationale

- **Transient data**: Progress is temporary; final review result goes to SQLDelight
- **Simple structure**: Just current step, task index, and pending outcomes
- **No schema migration**: DataStore handles versioning gracefully
- **Matches Planning feature**: Consistent pattern from Feature 004

### Data Structure

```kotlin
// ReviewProgress.kt (DataStore)
@Serializable
data class ReviewProgress(
    val weekId: String,
    val reviewMode: ReviewMode = ReviewMode.SOLO,
    val currentStep: ReviewStep = ReviewStep.RATING,
    val overallRating: Int? = null,
    val overallNote: String = "",
    val currentTaskIndex: Int = 0,
    val taskOutcomes: Map<String, TaskStatus> = emptyMap(),  // taskId -> status
    val taskNotes: Map<String, String> = emptyMap(),         // taskId -> note
    val lastUpdatedAt: Long = 0
)

enum class ReviewMode { SOLO, TOGETHER }
enum class ReviewStep { MODE_SELECT, RATING, TASK_REVIEW, SUMMARY }
```

### Lifecycle

1. **Start review**: Create/load progress for current week
2. **Each action**: Update progress in DataStore
3. **Complete review**: Persist to SQLDelight, clear DataStore
4. **Resume**: Check DataStore for incomplete progress on init

### Alternatives Considered

| Alternative | Rejected Because |
|-------------|------------------|
| SQLDelight table | Overkill for transient state, schema migrations |
| In-memory only | Loses progress on app kill |
| SharedPreferences | DataStore is the modern replacement |

---

## 4. Emoji Rating Scale Design

### Decision

5-point emoji scale: 😫 😕 😐 🙂 🎉 (maps to 1-5 internally)

### Rationale

- **Intuitive**: No explanation needed - emotions are universal
- **Celebration Over Judgment**: Emojis feel lighter than numbers/stars
- **Quick selection**: Large touch targets (≥48dp), single tap
- **Accessibility**: Each emoji has content description

### Mapping

| Emoji | Value | Description | Content Description |
|-------|-------|-------------|---------------------|
| 😫 | 1 | Terrible | "Terrible week" |
| 😕 | 2 | Bad | "Bad week" |
| 😐 | 3 | Neutral | "Okay week" |
| 🙂 | 4 | Good | "Good week" |
| 🎉 | 5 | Great | "Great week" |

### Implementation

```kotlin
// EmojiRatingSelector.kt
@Composable
fun EmojiRatingSelector(
    selectedRating: Int?,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ratings = listOf(
        1 to "😫" to "Terrible week",
        2 to "😕" to "Bad week",
        3 to "😐" to "Okay week",
        4 to "🙂" to "Good week",
        5 to "🎉" to "Great week"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ratings.forEach { (value, emoji, description) ->
            EmojiButton(
                emoji = emoji,
                contentDescription = description,
                isSelected = selectedRating == value,
                onClick = { onRatingSelected(value) }
            )
        }
    }
}
```

### Alternatives Considered

| Alternative | Rejected Because |
|-------------|------------------|
| Star rating (1-5) | Too evaluative, feels like judgment |
| Slider (1-10) | Too precise, overthinking |
| Thumbs up/down | Too binary, loses nuance |

---

## 5. Task Review Order Strategy

### Decision

Present tasks in creation order (oldest first), with pre-completed tasks at the end.

### Rationale

- **Natural sequence**: Review in order tasks were added
- **Pre-completed last**: User already knows outcomes; quick confirmation
- **No reordering**: Intentional Simplicity - consistent order

### Implementation

```kotlin
// In ReviewViewModel
private fun prepareTasksForReview(tasks: List<Task>): List<Task> {
    val (completed, pending) = tasks.partition { it.status == TaskStatus.COMPLETED }
    return pending.sortedBy { it.createdAt } + completed.sortedBy { it.createdAt }
}
```

### Navigation

- User reviews one task at a time
- Swipe or button advances to next task
- Back navigation goes to previous task
- Progress dots show position

---

## 6. Quick Finish Behavior

### Decision

"Quick Finish" marks all remaining (unreviewed) tasks as SKIPPED and completes the review.

### Rationale

- **Escape hatch**: Users can complete review even when rushed
- **"Skipped" not "Failed"**: Celebration Over Judgment - no shame
- **Completion > Abandonment**: Better to finish with skips than abandon

### Implementation

```kotlin
// ReviewViewModel
fun onQuickFinish() {
    viewModelScope.launch {
        val remaining = state.value.tasksToReview
            .drop(state.value.currentTaskIndex)
            .filter { it.id !in state.value.taskOutcomes }

        remaining.forEach { task ->
            taskRepository.updateTaskStatus(task.id, TaskStatus.SKIPPED)
        }

        completeReview()
    }
}
```

### Available From

- Overall rating screen (skip all task reviews)
- Any task review screen (skip remaining tasks)

---

## 7. Together Mode Architecture

### Decision

Single-device, turn-based review with pass-to-partner handoff.

### Rationale

- **Single device**: Couples typically review sitting together
- **Turn-based**: Clear ownership of each action
- **No sync complexity**: Avoids real-time sync requirements
- **P5 priority**: Can be deferred; architecture supports later enhancement

### Flow

1. User A selects "Review Together"
2. User A rates their week → "Pass to Partner"
3. User B rates their week → "Pass to Partner"
4. Task review alternates by owner
5. Observer can tap reaction emojis (stored on task)
6. Summary shows both partners' stats

### State Management

```kotlin
data class TogetherReviewState(
    val currentTurn: PartnerTurn = PartnerTurn.PARTNER_A,
    val partnerARating: Int? = null,
    val partnerBRating: Int? = null,
    val partnerATasks: List<Task> = emptyList(),
    val partnerBTasks: List<Task> = emptyList()
)

enum class PartnerTurn { PARTNER_A, PARTNER_B }
```

### Deferred to v1.1

- Cross-device sync for together mode
- Real-time reaction visibility
- Partner presence indicators

---

## 8. Navigation Flow Design

### Decision

Nested NavHost within ReviewScreen, following Planning feature pattern.

### Rationale

- **Consistency**: Same pattern as Feature 004 (Planning)
- **Encapsulation**: Review flow is self-contained
- **Deep linking**: Each step can be addressed directly
- **Back handling**: Natural back stack management

### Routes

```kotlin
sealed class ReviewRoute(val route: String) {
    object ModeSelection : ReviewRoute("review/mode")
    object Rating : ReviewRoute("review/rating")
    object TaskReview : ReviewRoute("review/task/{index}") {
        fun createRoute(index: Int) = "review/task/$index"
    }
    object Summary : ReviewRoute("review/summary")
}
```

### Navigation Graph

```kotlin
// ReviewNavGraph.kt
fun NavGraphBuilder.reviewNavGraph(
    navController: NavHostController,
    viewModel: ReviewViewModel,
    onComplete: () -> Unit
) {
    navigation(
        startDestination = ReviewRoute.ModeSelection.route,
        route = "review_flow"
    ) {
        composable(ReviewRoute.ModeSelection.route) {
            ReviewModeSelectionScreen(
                onSoloSelected = {
                    viewModel.onEvent(ReviewEvent.SelectMode(ReviewMode.SOLO))
                    navController.navigate(ReviewRoute.Rating.route)
                },
                onTogetherSelected = {
                    viewModel.onEvent(ReviewEvent.SelectMode(ReviewMode.TOGETHER))
                    navController.navigate(ReviewRoute.Rating.route)
                }
            )
        }

        composable(ReviewRoute.Rating.route) {
            OverallRatingStepScreen(
                state = viewModel.uiState.collectAsState().value,
                onRatingSelected = { viewModel.onEvent(ReviewEvent.SelectRating(it)) },
                onNoteChanged = { viewModel.onEvent(ReviewEvent.UpdateNote(it)) },
                onContinue = { navController.navigate(ReviewRoute.TaskReview.createRoute(0)) },
                onQuickFinish = { viewModel.onEvent(ReviewEvent.QuickFinish) }
            )
        }

        composable(
            route = ReviewRoute.TaskReview.route,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            TaskReviewStepScreen(
                state = viewModel.uiState.collectAsState().value,
                taskIndex = index,
                onOutcomeSelected = { taskId, status ->
                    viewModel.onEvent(ReviewEvent.SelectTaskOutcome(taskId, status))
                },
                onNext = {
                    val nextIndex = index + 1
                    if (nextIndex < viewModel.uiState.value.tasksToReview.size) {
                        navController.navigate(ReviewRoute.TaskReview.createRoute(nextIndex))
                    } else {
                        navController.navigate(ReviewRoute.Summary.route)
                    }
                }
            )
        }

        composable(ReviewRoute.Summary.route) {
            ReviewSummaryScreen(
                state = viewModel.uiState.collectAsState().value,
                onStartNextWeek = { /* Navigate to planning */ },
                onDone = onComplete
            )
        }
    }
}
```

---

## Summary of Key Decisions

| Area | Decision | Key Rationale |
|------|----------|---------------|
| Review window | Friday 6PM - Sunday 11:59PM, device timezone | Weekly rhythm, offline-first |
| Streak | Consecutive reviewed weeks, no grace period | Intentional Simplicity |
| Progress storage | DataStore | Transient state, no schema |
| Rating scale | 5 emojis (😫😕😐🙂🎉) | Intuitive, non-judgmental |
| Task order | Creation order, completed last | Natural sequence |
| Quick Finish | Mark remaining as Skipped | Escape hatch, positive framing |
| Together mode | Single device, turn-based | Simple, deferred complexity |
| Navigation | Nested NavHost | Consistent with Planning feature |
