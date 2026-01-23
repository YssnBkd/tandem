package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.epoque.tandem.presentation.progress.ProgressEvent
import org.epoque.tandem.presentation.progress.ProgressSideEffect
import org.epoque.tandem.presentation.progress.ProgressUiState
import org.epoque.tandem.presentation.progress.ProgressViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Progress screen composable.
 *
 * Displays streak information, completion trends, and past weeks list.
 * Handles navigation to past week detail via callback.
 *
 * Note: This screen does NOT use its own Scaffold. It receives padding
 * from MainScreen's Scaffold to ensure proper NavigationBar spacing.
 */
@Composable
fun ProgressScreen(
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onNavigateToWeekDetail: (String) -> Unit,
    onClearFab: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Clear FAB when this screen is shown (Progress has no FAB)
    DisposableEffect(Unit) {
        onClearFab()
        onDispose { }
    }

    // Collect side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ProgressSideEffect.NavigateToWeekDetail ->
                    onNavigateToWeekDetail(effect.weekId)
                is ProgressSideEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)
                is ProgressSideEffect.TriggerMilestoneHaptic -> {
                    // Haptic handled in MilestoneCelebration composable
                }
            }
        }
    }

    // Content without Scaffold - uses contentPadding from parent MainScreen
    ProgressContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        contentPadding = contentPadding,
        modifier = modifier
    )
}

@Composable
private fun ProgressContent(
    uiState: ProgressUiState,
    onEvent: (ProgressEvent) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                ProgressErrorState(
                    errorMessage = uiState.error,
                    onRetry = { onEvent(ProgressEvent.Retry) }
                )
            }
        }

        uiState.showEmptyState -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                ProgressEmptyState()
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding()
                )
            ) {
                // Streak Card
                item {
                    StreakCard(
                        streakCount = uiState.currentStreak,
                        isPartnerStreak = uiState.isPartnerStreak,
                        showCelebration = uiState.showMilestoneCelebration,
                        milestoneValue = uiState.milestoneValue,
                        onDismissCelebration = { onEvent(ProgressEvent.DismissMilestone) },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                // Completion Bars (This Month)
                item {
                    CompletionBars(
                        userPercentage = uiState.userMonthlyCompletion,
                        partnerPercentage = uiState.partnerMonthlyCompletion,
                        userText = uiState.userMonthlyText,
                        partnerText = uiState.partnerMonthlyText,
                        partnerName = uiState.partnerName,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Trend Chart
                item {
                    if (uiState.showTrendChart && uiState.trendData != null) {
                        TrendChart(
                            trendData = uiState.trendData,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        TrendChartEmptyState(
                            weekCount = uiState.trendData?.weekCount ?: 0,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                // Past Weeks Section Header
                item {
                    Text(
                        text = "Past Weeks",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Past Weeks List (or empty state)
                if (uiState.pastWeeks.isEmpty()) {
                    item {
                        PastWeeksEmptyState()
                    }
                } else {
                    items(
                        items = uiState.pastWeeks,
                        key = { it.weekId }
                    ) { week ->
                        PastWeekItem(
                            week = week,
                            onClick = { onEvent(ProgressEvent.PastWeekTapped(week.weekId)) }
                        )
                    }

                    // Load more trigger
                    if (uiState.hasMoreWeeks) {
                        item {
                            LaunchedEffect(Unit) {
                                onEvent(ProgressEvent.LoadMoreWeeks)
                            }
                            if (uiState.isLoadingMoreWeeks) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
