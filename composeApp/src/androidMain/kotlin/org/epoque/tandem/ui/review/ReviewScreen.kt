package org.epoque.tandem.ui.review

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.epoque.tandem.presentation.review.ReviewSideEffect
import org.epoque.tandem.presentation.review.ReviewViewModel
import org.epoque.tandem.ui.navigation.Routes
import org.epoque.tandem.ui.navigation.reviewNavGraph
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main container screen for the Review wizard.
 *
 * Hosts the nested navigation graph for review steps and handles
 * side effects for navigation and feedback.
 *
 * @param onNavigateBack Callback when user exits review
 * @param onNavigateToPlanning Callback when user wants to start planning next week
 * @param viewModel ViewModel instance (injected via Koin)
 */
@Composable
fun ReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    viewModel: ReviewViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                ReviewSideEffect.CloseReview -> onNavigateBack()

                ReviewSideEffect.NavigateToPlanning -> onNavigateToPlanning()

                ReviewSideEffect.NavigateToRating ->
                    navController.navigate(Routes.Review.Rating)

                is ReviewSideEffect.NavigateToTask ->
                    navController.navigate(Routes.Review.TaskReview(effect.index))

                ReviewSideEffect.NavigateToSummary ->
                    navController.navigate(Routes.Review.Summary)

                ReviewSideEffect.NavigateBack ->
                    navController.popBackStack()

                is ReviewSideEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)

                ReviewSideEffect.ShowReviewComplete -> {
                    // Could show a celebration animation here
                }

                ReviewSideEffect.ShowPassToPartnerDialog -> {
                    // TODO: Implement pass-to-partner dialog for Together mode
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {
                    // Content - nested navigation
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Review.ModeSelection
                    ) {
                        reviewNavGraph(
                            navController = navController,
                            state = state,
                            onEvent = viewModel::onEvent
                        )
                    }

                    // Handle incomplete progress prompt
                    if (state.hasIncompleteProgress) {
                        ResumeProgressDialog(
                            onResume = { viewModel.onEvent(org.epoque.tandem.presentation.review.ReviewEvent.ResumeProgress) },
                            onDiscard = { viewModel.onEvent(org.epoque.tandem.presentation.review.ReviewEvent.DiscardProgress) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog asking user if they want to resume incomplete progress.
 */
@Composable
private fun ResumeProgressDialog(
    onResume: () -> Unit,
    onDiscard: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDiscard,
        title = { Text("Resume Review?") },
        text = { Text("You have an incomplete review from before. Would you like to continue where you left off?") },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onResume) {
                Text("Resume")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDiscard) {
                Text("Start Fresh")
            }
        }
    )
}
