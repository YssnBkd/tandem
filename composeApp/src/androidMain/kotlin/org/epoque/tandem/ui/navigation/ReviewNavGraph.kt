package org.epoque.tandem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.presentation.review.ReviewEvent
import org.epoque.tandem.presentation.review.ReviewMode
import org.epoque.tandem.presentation.review.ReviewUiState
import org.epoque.tandem.ui.review.OverallRatingStepScreen
import org.epoque.tandem.ui.review.ReviewModeSelectionScreen
import org.epoque.tandem.ui.review.ReviewSummaryScreen
import org.epoque.tandem.ui.review.TaskReviewStepScreen

/**
 * Navigation graph for weekly review wizard.
 *
 * Flow:
 * 1. ModeSelection - Choose Solo or Together
 * 2. Rating - Rate overall week with emoji scale
 * 3. TaskReview - Review each task (Done/Tried/Skipped)
 * 4. Summary - View completion stats and streak
 */
fun NavGraphBuilder.reviewNavGraph(
    navController: NavController,
    state: ReviewUiState,
    onEvent: (ReviewEvent) -> Unit
) {
    composable<Routes.Review.ModeSelection> {
        ReviewModeSelectionScreen(
            currentStreak = state.currentStreak,
            onSoloSelected = {
                onEvent(ReviewEvent.SelectMode(ReviewMode.SOLO))
                navController.navigate(Routes.Review.Rating)
            },
            onTogetherSelected = {
                onEvent(ReviewEvent.SelectMode(ReviewMode.TOGETHER))
                navController.navigate(Routes.Review.Rating)
            }
        )
    }

    composable<Routes.Review.Rating> {
        OverallRatingStepScreen(
            selectedRating = state.overallRating,
            note = state.overallNote,
            canContinue = state.canProceedFromRating,
            onRatingSelected = { onEvent(ReviewEvent.SelectRating(it)) },
            onNoteChanged = { onEvent(ReviewEvent.UpdateRatingNote(it)) },
            onContinue = {
                onEvent(ReviewEvent.ContinueToTasks)
                // Navigation handled by side effect from ViewModel
            },
            onQuickFinish = {
                onEvent(ReviewEvent.QuickFinish)
                // Navigation handled by side effect from ViewModel
            }
        )
    }

    composable<Routes.Review.TaskReview> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Review.TaskReview>()
        val taskIndex = route.taskIndex
        val task = state.tasksToReview.getOrNull(taskIndex)
        val currentOutcome = task?.let { state.taskOutcomes[it.id] }
        val note = task?.let { state.taskNotes[it.id] } ?: ""

        TaskReviewStepScreen(
            task = task,
            taskIndex = taskIndex,
            totalTasks = state.totalTasks,
            currentOutcome = currentOutcome,
            note = note,
            isLastTask = taskIndex >= state.tasksToReview.size - 1,
            onOutcomeSelected = { status ->
                task?.let { onEvent(ReviewEvent.SelectTaskOutcome(it.id, status)) }
            },
            onNoteChanged = { newNote ->
                task?.let { onEvent(ReviewEvent.UpdateTaskNote(it.id, newNote)) }
            },
            onNext = { onEvent(ReviewEvent.NextTask) },
            onPrevious = { onEvent(ReviewEvent.PreviousTask) },
            onQuickFinish = { onEvent(ReviewEvent.QuickFinish) }
        )
    }

    composable<Routes.Review.Summary> {
        ReviewSummaryScreen(
            completionPercentage = state.completionPercentage,
            doneCount = state.doneCount,
            triedCount = state.triedCount,
            skippedCount = state.skippedCount,
            totalTasks = state.totalTasks,
            currentStreak = state.currentStreak,
            onStartNextWeek = { onEvent(ReviewEvent.StartNextWeek) },
            onDone = { onEvent(ReviewEvent.Done) }
        )
    }
}
