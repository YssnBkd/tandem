package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.epoque.tandem.presentation.progress.PastWeekDetailEvent
import org.epoque.tandem.presentation.progress.PastWeekDetailSideEffect
import org.epoque.tandem.presentation.progress.PastWeekDetailUiState
import org.epoque.tandem.presentation.progress.PastWeekDetailViewModel
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTextStyles
import org.koin.compose.viewmodel.koinViewModel

/**
 * Past Week Detail screen composable.
 *
 * Displays detailed information for a specific past week including
 * review summaries and task outcomes.
 * Uses design tokens for consistent styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastWeekDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PastWeekDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is PastWeekDetailSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.dateRange.isNotEmpty()) {
                            "Week of ${uiState.dateRange}"
                        } else {
                            "Week Details"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(PastWeekDetailEvent.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        PastWeekDetailContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun PastWeekDetailContent(
    uiState: PastWeekDetailUiState,
    onEvent: (PastWeekDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            val errorMessage = uiState.error
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TandemSpacing.md)
                ) {
                    Text(
                        text = errorMessage,
                        style = TandemTextStyles.Body.primary,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(
                        onClick = { onEvent(PastWeekDetailEvent.Retry) }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = TandemSpacing.Screen.topPadding),
                verticalArrangement = Arrangement.spacedBy(TandemSpacing.lg)
            ) {
                // Review summaries (side-by-side)
                uiState.userReview?.let { userReview ->
                    ReviewSummaryCards(
                        userReview = userReview,
                        partnerReview = uiState.partnerReview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TandemSpacing.Screen.horizontalPadding)
                    )
                }

                // Task outcomes list
                TaskOutcomesList(
                    tasks = uiState.tasks,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
