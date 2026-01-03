# Data Model: Week View (Presentation Layer)

**Feature Branch**: `003-week-view`
**Date**: 2026-01-02

## Overview

This document defines the presentation-layer data models for the Week View feature. These models are UI-specific and built on top of the domain models from Feature 002 (Task Data Layer).

---

## Presentation Models

### Segment

Represents the view filter for task ownership.

```kotlin
package org.epoque.tandem.presentation.week.model

import org.epoque.tandem.domain.model.OwnerType

/**
 * Represents the segment/tab selection in the Week View.
 */
enum class Segment(val displayName: String) {
    YOU("You"),
    PARTNER("Partner"),
    SHARED("Shared");

    /**
     * Convert to domain OwnerType for repository queries.
     */
    fun toOwnerType(): OwnerType = when (this) {
        YOU -> OwnerType.SELF
        PARTNER -> OwnerType.PARTNER
        SHARED -> OwnerType.SHARED
    }

    companion object {
        fun fromOwnerType(ownerType: OwnerType): Segment = when (ownerType) {
            OwnerType.SELF -> YOU
            OwnerType.PARTNER -> PARTNER
            OwnerType.SHARED -> SHARED
        }
    }
}
```

---

### TaskUiModel

UI-ready representation of a task for display in the Week View.

```kotlin
package org.epoque.tandem.presentation.week.model

import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskStatus

/**
 * UI model for displaying a task in the Week View.
 * Optimized for Compose rendering with pre-computed display values.
 */
data class TaskUiModel(
    val id: String,
    val title: String,
    val notes: String?,
    val isCompleted: Boolean,
    val ownerType: OwnerType,
    val segment: Segment,

    // Repeat task support
    val isRepeating: Boolean,
    val repeatTarget: Int?,
    val repeatCompleted: Int,
    val repeatProgressText: String?,     // "2/3"
    val repeatProgressDots: String?,     // "●●○"

    // Metadata for detail view
    val statusDisplayText: String,       // "Completed", "In Progress"
    val createdByCurrentUser: Boolean,
    val rolledOver: Boolean,
    val completedByName: String?         // For shared tasks
) {
    companion object {
        /**
         * Create TaskUiModel from domain Task.
         *
         * @param task Domain task entity
         * @param currentUserId Current user's ID for ownership checks
         * @param partnerName Partner's display name (for shared task completion)
         */
        fun fromTask(
            task: Task,
            currentUserId: String,
            partnerName: String? = null
        ): TaskUiModel {
            val isCompleted = task.status == TaskStatus.COMPLETED ||
                (task.isRepeating && task.repeatCompleted >= (task.repeatTarget ?: 0))

            return TaskUiModel(
                id = task.id,
                title = task.title,
                notes = task.notes,
                isCompleted = isCompleted,
                ownerType = task.ownerType,
                segment = Segment.fromOwnerType(task.ownerType),
                isRepeating = task.isRepeating,
                repeatTarget = task.repeatTarget,
                repeatCompleted = task.repeatCompleted,
                repeatProgressText = task.repeatTarget?.let { "${task.repeatCompleted}/$it" },
                repeatProgressDots = task.repeatTarget?.let { target ->
                    buildString {
                        repeat(task.repeatCompleted.coerceAtMost(target)) { append("●") }
                        repeat((target - task.repeatCompleted).coerceAtLeast(0)) { append("○") }
                    }
                },
                statusDisplayText = when (task.status) {
                    TaskStatus.PENDING -> "In Progress"
                    TaskStatus.PENDING_ACCEPTANCE -> "Pending Acceptance"
                    TaskStatus.COMPLETED -> "Completed"
                    TaskStatus.TRIED -> "Tried"
                    TaskStatus.SKIPPED -> "Skipped"
                    TaskStatus.DECLINED -> "Declined"
                },
                createdByCurrentUser = task.createdBy == currentUserId,
                rolledOver = task.rolledFromWeekId != null,
                completedByName = if (task.ownerType == OwnerType.SHARED && isCompleted) {
                    if (task.createdBy == currentUserId) "you" else partnerName
                } else null
            )
        }
    }
}
```

---

### WeekInfo

Display information about the current week.

```kotlin
package org.epoque.tandem.presentation.week.model

import kotlinx.datetime.LocalDate
import org.epoque.tandem.domain.model.Week

/**
 * Week display information for the header.
 */
data class WeekInfo(
    val weekId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dateRangeText: String,          // "Week of Dec 30 - Jan 5"
    val isCurrentWeek: Boolean
) {
    companion object {
        /**
         * Create WeekInfo from domain Week.
         */
        fun fromWeek(week: Week, currentWeekId: String): WeekInfo {
            return WeekInfo(
                weekId = week.id,
                startDate = week.startDate,
                endDate = week.endDate,
                dateRangeText = formatDateRange(week.startDate, week.endDate),
                isCurrentWeek = week.id == currentWeekId
            )
        }

        private fun formatDateRange(start: LocalDate, end: LocalDate): String {
            val startMonth = start.month.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
            val endMonth = end.month.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }

            return if (start.month == end.month) {
                "Week of $startMonth ${start.dayOfMonth} - ${end.dayOfMonth}"
            } else {
                "Week of $startMonth ${start.dayOfMonth} - $endMonth ${end.dayOfMonth}"
            }
        }
    }
}
```

---

### WeekUiState

Complete UI state for the Week View screen.

```kotlin
package org.epoque.tandem.presentation.week

import org.epoque.tandem.presentation.week.model.Segment
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.presentation.week.model.WeekInfo

/**
 * Complete UI state for the Week View screen.
 * Single source of truth for all UI elements.
 */
data class WeekUiState(
    // Week header
    val weekInfo: WeekInfo? = null,

    // Segment selection
    val selectedSegment: Segment = Segment.YOU,

    // Task list
    val tasks: List<TaskUiModel> = emptyList(),
    val incompleteTasks: List<TaskUiModel> = emptyList(),
    val completedTasks: List<TaskUiModel> = emptyList(),

    // Progress
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressText: String = "0/0",

    // Quick add
    val quickAddText: String = "",
    val quickAddError: String? = null,

    // Loading/error states
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    // Sheet states
    val selectedTaskId: String? = null,
    val showDetailSheet: Boolean = false,
    val showAddTaskSheet: Boolean = false,

    // Partner state
    val hasPartner: Boolean = false,
    val partnerName: String? = null
) {
    /**
     * Whether the current segment allows task completion.
     * Partner segment is read-only.
     */
    val isReadOnly: Boolean get() = selectedSegment == Segment.PARTNER

    /**
     * Whether to show empty state.
     */
    val showEmptyState: Boolean get() = !isLoading && tasks.isEmpty()

    /**
     * Get the selected task for the detail sheet.
     */
    val selectedTask: TaskUiModel? get() = selectedTaskId?.let { id ->
        tasks.find { it.id == id }
    }

    /**
     * Empty state message based on segment.
     */
    val emptyStateMessage: String get() = when (selectedSegment) {
        Segment.YOU -> "No tasks for this week yet.\nAdd one using the field above!"
        Segment.PARTNER -> if (hasPartner) {
            "Your partner hasn't added any tasks yet."
        } else {
            "Connect with your partner to see their tasks."
        }
        Segment.SHARED -> "No shared tasks yet.\nAdd one to work on together!"
    }

    /**
     * Empty state action text.
     */
    val emptyStateActionText: String? get() = when (selectedSegment) {
        Segment.YOU -> null  // Quick add is visible
        Segment.PARTNER -> if (!hasPartner) "Invite Partner" else null
        Segment.SHARED -> "Add Shared Task"
    }
}
```

---

### WeekEvent

User-triggered events for the ViewModel.

```kotlin
package org.epoque.tandem.presentation.week

import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.presentation.week.model.Segment

/**
 * User events in the Week View.
 * Processed by WeekViewModel to update state.
 */
sealed class WeekEvent {
    // Segment navigation
    data class SegmentSelected(val segment: Segment) : WeekEvent()

    // Task interactions
    data class TaskTapped(val taskId: String) : WeekEvent()
    data class TaskCheckboxTapped(val taskId: String) : WeekEvent()

    // Quick add
    data class QuickAddTextChanged(val text: String) : WeekEvent()
    data object QuickAddSubmitted : WeekEvent()

    // Task detail sheet
    data object DetailSheetDismissed : WeekEvent()
    data class TaskTitleChanged(val title: String) : WeekEvent()
    data class TaskNotesChanged(val notes: String) : WeekEvent()
    data object TaskSaveRequested : WeekEvent()
    data object TaskDeleteRequested : WeekEvent()
    data object TaskDeleteConfirmed : WeekEvent()
    data object TaskMarkCompleteRequested : WeekEvent()

    // Add task sheet
    data object AddTaskSheetRequested : WeekEvent()
    data object AddTaskSheetDismissed : WeekEvent()
    data class AddTaskSubmitted(
        val title: String,
        val notes: String?,
        val ownerType: OwnerType
    ) : WeekEvent()

    // Refresh
    data object RefreshRequested : WeekEvent()

    // Partner actions
    data object RequestTaskFromPartnerTapped : WeekEvent()
    data object InvitePartnerTapped : WeekEvent()
}
```

---

### WeekSideEffect

One-time effects that don't belong in state.

```kotlin
package org.epoque.tandem.presentation.week

/**
 * One-time side effects from the Week View.
 * Consumed once by the UI layer.
 */
sealed class WeekSideEffect {
    /**
     * Trigger haptic feedback for task completion.
     */
    data object TriggerHapticFeedback : WeekSideEffect()

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : WeekSideEffect()

    /**
     * Navigate to partner invitation flow.
     */
    data object NavigateToPartnerInvite : WeekSideEffect()

    /**
     * Navigate to request task flow.
     */
    data object NavigateToRequestTask : WeekSideEffect()

    /**
     * Clear keyboard focus.
     */
    data object ClearFocus : WeekSideEffect()
}
```

---

## State Transitions

### Task Completion Flow

```
1. User taps checkbox
2. Event: TaskCheckboxTapped(taskId)
3. ViewModel:
   - If regular task: updateTaskStatus(COMPLETED)
   - If repeating task: incrementRepeatCount()
4. Effect: TriggerHapticFeedback
5. State: task moves from incompleteTasks to completedTasks
6. UI: animateItem() handles visual transition
```

### Segment Change Flow

```
1. User taps segment button
2. Event: SegmentSelected(segment)
3. ViewModel:
   - Save to DataStore
   - Re-filter tasks by new segment
4. State: selectedSegment + tasks updated
5. UI: task list updates with animation
```

### Quick Add Flow

```
1. User types in field → QuickAddTextChanged(text)
2. User submits → QuickAddSubmitted
3. ViewModel:
   - Validate non-empty
   - If empty: set quickAddError
   - If valid: createTask(title)
4. State: task added to list, quickAddText cleared
5. Effect: ClearFocus
```

---

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| Quick add title | Non-empty, trimmed | "Task title cannot be empty" |
| Task edit title | Non-empty, trimmed | "Task title cannot be empty" |
| Notes | Any (optional) | N/A |

---

## Relationship to Domain Models

This presentation layer builds on Feature 002 domain models:

| Presentation | Domain | Relationship |
|--------------|--------|--------------|
| TaskUiModel | Task | Enriched with display values |
| WeekInfo | Week | Subset with formatting |
| Segment | OwnerType | UI label variant |
| WeekUiState | - | Aggregates all UI concerns |

Domain models are **not modified**. Presentation models wrap and extend them for UI needs.
