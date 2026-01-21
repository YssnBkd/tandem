package org.epoque.tandem.ui.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.epoque.tandem.presentation.timeline.TimelineItem
import org.epoque.tandem.presentation.timeline.TimelineViewModel
import org.epoque.tandem.ui.components.timeline.BackToTodayButton
import org.epoque.tandem.ui.components.timeline.GapIndicator
import org.epoque.tandem.ui.components.timeline.TimelineSectionHeader
import org.epoque.tandem.ui.components.timeline.TimelineWeekCard
import org.epoque.tandem.ui.theme.TandemPrimary
import org.epoque.tandem.ui.theme.TandemPrimaryContainer
import org.koin.compose.viewmodel.koinViewModel

/**
 * Timeline screen showing all weeks grouped by month/quarter.
 * Users can browse history, see completion stats, and expand weeks to view tasks.
 *
 * Note: This screen is designed to be embedded in MainScreen's Scaffold,
 * so it does NOT have its own Scaffold or TopAppBar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: TimelineViewModel = koinViewModel(),
    onWeekClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onProvideFab: ((@Composable () -> Unit)?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Track scroll position for "Back to this week" button
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                viewModel.updateScrollPosition(index)
            }
    }

    // Scroll to current week on initial load
    LaunchedEffect(uiState.currentWeekIndex, uiState.isLoading) {
        if (!uiState.isLoading && uiState.currentWeekIndex > 0) {
            listState.scrollToItem(uiState.currentWeekIndex)
        }
    }

    // Provide FAB to parent when needed
    LaunchedEffect(uiState.showBackToThisWeek) {
        if (uiState.showBackToThisWeek) {
            onProvideFab {
                BackToTodayButton(
                    visible = true,
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(uiState.currentWeekIndex)
                        }
                    }
                )
            }
        } else {
            onProvideFab(null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TandemPrimary)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Failed to load timeline",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No weeks to display",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Filter bar
                    item(key = "filter_bar") {
                        TimelineFilterBar(
                            hideEmptyWeeks = uiState.hideEmptyWeeks,
                            onToggleHideEmptyWeeks = { viewModel.toggleHideEmptyWeeks() }
                        )
                    }

                    // Timeline items with sticky headers
                    uiState.items.forEach { item ->
                        when (item) {
                            is TimelineItem.SectionHeader -> {
                                stickyHeader(key = item.key) {
                                    TimelineSectionHeader(
                                        quarter = item.quarter,
                                        month = item.month,
                                        year = item.year,
                                        modifier = Modifier.background(
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                }
                            }

                            is TimelineItem.WeekCard -> {
                                item(key = item.key) {
                                    TimelineWeekCard(
                                        week = item.week,
                                        tasks = item.tasks.ifEmpty {
                                            viewModel.getTasksForWeek(item.week.id)
                                        },
                                        totalTasks = item.totalTasks,
                                        completedTasks = item.completedTasks,
                                        isCurrentWeek = item.isCurrentWeek,
                                        isExpanded = item.isExpanded,
                                        onToggleExpand = {
                                            viewModel.toggleWeekExpanded(item.week.id)
                                        },
                                        onViewFullWeek = onWeekClick
                                    )
                                }
                            }

                            is TimelineItem.GapIndicator -> {
                                item(key = item.key) {
                                    GapIndicator(
                                        emptyWeekCount = item.emptyWeekCount,
                                        onClick = {
                                            // Could expand to show empty weeks
                                            viewModel.toggleHideEmptyWeeks()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Filter bar with "Hide empty weeks" chip.
 * Custom styled to match mockup design.
 */
@Composable
private fun TimelineFilterBar(
    hideEmptyWeeks: Boolean,
    onToggleHideEmptyWeeks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Custom filter chip that matches mockup styling
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (hideEmptyWeeks) TandemPrimaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 1.dp,
                        color = if (hideEmptyWeeks) TandemPrimary
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable { onToggleHideEmptyWeeks() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (hideEmptyWeeks) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TandemPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "Hide empty weeks",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (hideEmptyWeeks) TandemPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bottom divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline
        )
    }
}
