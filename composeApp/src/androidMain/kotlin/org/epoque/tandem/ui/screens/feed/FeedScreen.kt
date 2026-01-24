package org.epoque.tandem.ui.screens.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import org.epoque.tandem.presentation.feed.FeedEvent
import org.epoque.tandem.presentation.feed.FeedSideEffect
import org.epoque.tandem.presentation.feed.FeedUiState
import org.epoque.tandem.presentation.feed.FeedViewModel
import org.epoque.tandem.presentation.feed.model.FeedUiItem
import org.epoque.tandem.ui.components.feed.AiPromptCard
import org.epoque.tandem.ui.components.feed.CaughtUpSeparator
import org.epoque.tandem.ui.components.feed.FeedEmptyState
import org.epoque.tandem.ui.components.feed.FeedFilterBar
import org.epoque.tandem.ui.components.feed.FeedTopBar
import org.epoque.tandem.ui.components.feed.MessageCard
import org.epoque.tandem.ui.components.feed.MessageInputBar
import org.epoque.tandem.ui.components.feed.PartnerJoinedCard
import org.epoque.tandem.ui.components.feed.StickyDayHeader
import org.epoque.tandem.ui.components.feed.TaskAssignmentCard
import org.epoque.tandem.ui.components.feed.TaskCompletionCard
import org.epoque.tandem.ui.components.feed.TaskResponseCard
import org.epoque.tandem.ui.components.feed.WeekEventCard
import org.epoque.tandem.ui.theme.TandemBackgroundLight
import org.epoque.tandem.ui.theme.TandemSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * Feed screen showing activity stream with partner.
 *
 * Note: This screen does NOT use its own Scaffold. It receives padding
 * from MainScreen's Scaffold to ensure proper NavigationBar spacing.
 * This follows the same pattern as WeekScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    contentPadding: PaddingValues,
    viewModel: FeedViewModel = koinViewModel(),
    onNavigateToPlanning: () -> Unit = {},
    onNavigateToReview: (String) -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    onNavigateToWeek: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collectLatest { effect ->
            when (effect) {
                is FeedSideEffect.NavigateToPlanning -> onNavigateToPlanning()
                is FeedSideEffect.NavigateToReview -> onNavigateToReview(effect.weekId)
                is FeedSideEffect.NavigateToTaskDetail -> onNavigateToTaskDetail(effect.taskId)
                is FeedSideEffect.NavigateToWeek -> onNavigateToWeek()
                is FeedSideEffect.ShowSnackbar -> {
                    // Snackbar is handled by MainScreen's snackbarHostState
                }
                is FeedSideEffect.TriggerHapticFeedback -> {
                    // Platform-specific haptic feedback
                }
                is FeedSideEffect.ClearMessageInput -> {
                    // Input is managed by state, so this is informational
                }
                is FeedSideEffect.ShowMoreOptionsMenu -> {
                    // TODO: Show options menu
                }
            }
        }
    }

    // Track scroll position to mark items as read
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { /* Could track for read marking */ }
    }

    // Main layout using contentPadding from parent Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TandemBackgroundLight)
            .padding(contentPadding)
    ) {
        // Fixed header: TopBar + FilterBar
        FeedTopBar(
            onMoreOptionsTapped = { viewModel.onEvent(FeedEvent.MoreOptionsTapped) }
        )
        FeedFilterBar(
            activeFilter = uiState.activeFilter,
            onFilterSelected = { filter ->
                viewModel.onEvent(FeedEvent.FilterSelected(filter))
            }
        )

        // Main content area - takes remaining space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                uiState.showEmptyState -> {
                    FeedEmptyState(
                        hasPartner = uiState.hasPartner,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.onEvent(FeedEvent.RefreshRequested) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        FeedContent(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            listState = listState,
                            // Add padding for message bar
                            messageBarPadding = 64.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // iOS-style message input bar - part of Column layout, not overlay
        Column {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                MessageInputBar(
                    text = uiState.messageText,
                    placeholder = uiState.messageInputPlaceholder,
                    isEnabled = true,
                    isSending = uiState.isSendingMessage,
                    onTextChanged = { viewModel.onEvent(FeedEvent.MessageTextChanged(it)) },
                    onSendClicked = { viewModel.onEvent(FeedEvent.SendMessageTapped) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeedContent(
    uiState: FeedUiState,
    onEvent: (FeedEvent) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    messageBarPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = messageBarPadding + TandemSpacing.md),
        modifier = modifier
    ) {
        var globalItemIndex = 0

        uiState.feedGroups.forEach { group ->
            // Sticky day header
            stickyHeader(key = "header-${group.date}") {
                StickyDayHeader(
                    dayLabel = group.dayLabel,
                    dateLabel = group.dateLabel
                )
            }

            // Items in this group
            itemsIndexed(
                items = group.items,
                key = { _, item -> item.id }
            ) { localIndex, item ->
                val currentGlobalIndex = globalItemIndex + localIndex

                // Show "caught up" separator after the last unread item
                if (currentGlobalIndex == uiState.lastReadIndex) {
                    CaughtUpSeparator(
                        modifier = Modifier.padding(vertical = TandemSpacing.xs)
                    )
                }

                FeedItemCard(
                    item = item,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = TandemSpacing.Screen.horizontalPadding,
                            vertical = TandemSpacing.xs
                        )
                        .animateItem()
                )
            }

            globalItemIndex += group.items.size
        }

        // End of feed spacer
        item {
            Spacer(modifier = Modifier.height(TandemSpacing.lg))
        }
    }
}

@Composable
private fun FeedItemCard(
    item: FeedUiItem,
    onEvent: (FeedEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (item) {
        is FeedUiItem.TaskCompleted -> TaskCompletionCard(
            item = item,
            onCardClick = { onEvent(FeedEvent.ItemTapped(item.id)) },
            onCheckboxClick = { onEvent(FeedEvent.TaskCheckboxTapped(item.taskId)) },
            modifier = modifier
        )

        is FeedUiItem.TaskAssigned -> TaskAssignmentCard(
            item = item,
            onAccept = { onEvent(FeedEvent.AcceptTaskAssignment(item.id)) },
            onDecline = { onEvent(FeedEvent.DeclineTaskAssignment(item.id)) },
            modifier = modifier
        )

        is FeedUiItem.TaskAccepted -> TaskResponseCard(
            item = item,
            onCardClick = { onEvent(FeedEvent.ItemTapped(item.id)) },
            modifier = modifier
        )

        is FeedUiItem.TaskDeclined -> TaskResponseCard(
            item = item,
            onCardClick = { onEvent(FeedEvent.ItemTapped(item.id)) },
            modifier = modifier
        )

        is FeedUiItem.Message -> MessageCard(
            item = item,
            modifier = modifier
        )

        is FeedUiItem.WeekPlanned -> WeekEventCard(
            item = item,
            onCardClick = { onEvent(FeedEvent.ItemTapped(item.id)) },
            modifier = modifier
        )

        is FeedUiItem.WeekReviewed -> WeekEventCard(
            item = item,
            onCardClick = { onEvent(FeedEvent.ItemTapped(item.id)) },
            modifier = modifier
        )

        is FeedUiItem.PartnerJoined -> PartnerJoinedCard(
            item = item,
            modifier = modifier
        )

        is FeedUiItem.AiPlanPrompt -> AiPromptCard(
            item = item,
            onActionClick = { onEvent(FeedEvent.StartPlanningTapped(item.id)) },
            onDismissClick = { onEvent(FeedEvent.DismissAiPrompt(item.id)) },
            modifier = modifier
        )

        is FeedUiItem.AiReviewPrompt -> AiPromptCard(
            item = item,
            onActionClick = { onEvent(FeedEvent.StartReviewTapped(item.id, item.weekId)) },
            onDismissClick = { onEvent(FeedEvent.DismissAiPrompt(item.id)) },
            modifier = modifier
        )
    }
}
