package org.epoque.tandem.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.usecase.feed.AcceptTaskAssignmentUseCase
import org.epoque.tandem.domain.usecase.feed.DeclineTaskAssignmentUseCase
import org.epoque.tandem.domain.usecase.feed.DismissAiPromptUseCase
import org.epoque.tandem.domain.usecase.feed.FeedFilter as DomainFeedFilter
import org.epoque.tandem.domain.usecase.feed.GetFeedItemsUseCase
import org.epoque.tandem.domain.usecase.feed.MarkFeedItemReadUseCase
import org.epoque.tandem.domain.usecase.feed.SendMessageUseCase
import org.epoque.tandem.presentation.feed.model.FeedDayGroup
import org.epoque.tandem.presentation.feed.model.FeedFilter
import org.epoque.tandem.presentation.feed.model.FeedUiItem

/**
 * ViewModel for the Feed screen.
 *
 * Follows Android best practices:
 * - Screen-level state holder (not reusable)
 * - StateFlow for reactive UI state
 * - Channel for one-time side effects
 * - viewModelScope for lifecycle-aware coroutines
 * - Unidirectional data flow (UDF) pattern
 */
class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository,
    private val getFeedItemsUseCase: GetFeedItemsUseCase,
    private val markFeedItemReadUseCase: MarkFeedItemReadUseCase,
    private val acceptTaskAssignmentUseCase: AcceptTaskAssignmentUseCase,
    private val declineTaskAssignmentUseCase: DeclineTaskAssignmentUseCase,
    private val dismissAiPromptUseCase: DismissAiPromptUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<FeedSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<FeedSideEffect> = _sideEffects.receiveAsFlow()

    // Cache current user ID
    private var currentUserId: String? = null

    init {
        loadInitialData()
        observeFeed()
        observePartner()
        observeUnreadCount()
    }

    /**
     * Handle user events from the UI.
     * Following UDF pattern: events go UP to ViewModel.
     */
    fun onEvent(event: FeedEvent) {
        when (event) {
            // Filter
            is FeedEvent.FilterSelected -> handleFilterSelected(event.filter)

            // Item interactions
            is FeedEvent.ItemTapped -> handleItemTapped(event.itemId)
            is FeedEvent.ItemsScrolledPast -> handleItemsScrolledPast(event.lastSeenItemId)
            is FeedEvent.ReachedEndOfFeed -> handleReachedEndOfFeed()

            // Task assignments
            is FeedEvent.AcceptTaskAssignment -> handleAcceptTaskAssignment(event.itemId)
            is FeedEvent.DeclineTaskAssignment -> handleDeclineTaskAssignment(event.itemId)

            // AI prompts
            is FeedEvent.StartPlanningTapped -> handleStartPlanningTapped(event.itemId)
            is FeedEvent.StartReviewTapped -> handleStartReviewTapped(event.itemId, event.weekId)
            is FeedEvent.DismissAiPrompt -> handleDismissAiPrompt(event.itemId)

            // Messages
            is FeedEvent.MessageTextChanged -> handleMessageTextChanged(event.text)
            is FeedEvent.SendMessageTapped -> handleSendMessageTapped()

            // Task checkbox
            is FeedEvent.TaskCheckboxTapped -> handleTaskCheckboxTapped(event.taskId)

            // Refresh
            is FeedEvent.RefreshRequested -> handleRefreshRequested()

            // Navigation
            is FeedEvent.MoreOptionsTapped -> handleMoreOptionsTapped()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    private fun loadInitialData() {
        viewModelScope.launch {
            // Wait for authentication
            val userId = authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .first()
                .user.id

            currentUserId = userId
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun observeFeed() {
        viewModelScope.launch {
            val userId = authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .first()
                .user.id

            // Combine filter selection with feed items
            combine(
                _uiState,
                feedRepository.observeFeedItems(userId)
            ) { state, items ->
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                // Filter items based on active filter
                val filteredItems = when (state.activeFilter) {
                    FeedFilter.ALL -> items
                    FeedFilter.TASKS -> items.filter { item ->
                        item is org.epoque.tandem.domain.model.FeedItem.TaskCompleted ||
                        item is org.epoque.tandem.domain.model.FeedItem.TaskAssigned ||
                        item is org.epoque.tandem.domain.model.FeedItem.TaskAccepted ||
                        item is org.epoque.tandem.domain.model.FeedItem.TaskDeclined
                    }
                    FeedFilter.MESSAGES -> items.filter { item ->
                        item is org.epoque.tandem.domain.model.FeedItem.Message
                    }
                }

                // Group by day
                val grouped = filteredItems.groupBy { item ->
                    item.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
                }

                // Convert to UI models
                val feedGroups = grouped.map { (date, dayItems) ->
                    FeedDayGroup(
                        date = date,
                        dayLabel = getDayLabel(date, today),
                        dateLabel = getDateLabel(date, today),
                        items = dayItems
                            .sortedByDescending { it.timestamp }
                            .map { FeedUiItem.fromDomain(it, userId) }
                    )
                }.sortedByDescending { it.date }

                // Find "caught up" separator position
                val lastReadIndex = findLastReadIndex(feedGroups)

                feedGroups to lastReadIndex
            }.collect { (feedGroups, lastReadIndex) ->
                _uiState.update { state ->
                    state.copy(
                        feedGroups = feedGroups,
                        lastReadIndex = lastReadIndex,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    private fun observePartner() {
        viewModelScope.launch {
            val userId = authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .first()
                .user.id

            partnerRepository.observePartner(userId).collect { partner ->
                _uiState.update { state ->
                    state.copy(
                        hasPartner = partner != null,
                        partnerName = partner?.name
                    )
                }
            }
        }
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            val userId = authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .first()
                .user.id

            feedRepository.observeUnreadCount(userId).collect { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleFilterSelected(filter: FeedFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    private fun handleItemTapped(itemId: String) {
        viewModelScope.launch {
            // Mark as read
            markFeedItemReadUseCase.markSingleAsRead(itemId)

            // Navigate based on item type
            val item = _uiState.value.feedGroups
                .flatMap { it.items }
                .find { it.id == itemId }

            when (item) {
                is FeedUiItem.TaskCompleted -> {
                    _sideEffects.send(FeedSideEffect.NavigateToTaskDetail(item.taskId))
                }
                is FeedUiItem.TaskAssigned -> {
                    // Don't navigate - handled by Accept/Decline buttons
                }
                is FeedUiItem.TaskAccepted,
                is FeedUiItem.TaskDeclined -> {
                    _sideEffects.send(FeedSideEffect.NavigateToTaskDetail(
                        (item as? FeedUiItem.TaskAccepted)?.taskId
                            ?: (item as FeedUiItem.TaskDeclined).taskId
                    ))
                }
                is FeedUiItem.Message -> {
                    // Messages don't have detail view
                }
                is FeedUiItem.WeekPlanned,
                is FeedUiItem.WeekReviewed -> {
                    _sideEffects.send(FeedSideEffect.NavigateToWeek)
                }
                is FeedUiItem.PartnerJoined -> {
                    // Partner joined doesn't have detail view
                }
                is FeedUiItem.AiPlanPrompt,
                is FeedUiItem.AiReviewPrompt -> {
                    // AI prompts handled by their CTA buttons
                }
                null -> { /* Item not found */ }
            }
        }
    }

    private fun handleItemsScrolledPast(lastSeenItemId: String) {
        viewModelScope.launch {
            markFeedItemReadUseCase.markSingleAsRead(lastSeenItemId)
        }
    }

    private fun handleReachedEndOfFeed() {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            markFeedItemReadUseCase.markAllAsRead(userId)
        }
    }

    private fun handleAcceptTaskAssignment(itemId: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                val result = acceptTaskAssignmentUseCase(itemId, userId)
                if (result != null) {
                    _sideEffects.send(FeedSideEffect.TriggerHapticFeedback)
                    _sideEffects.send(FeedSideEffect.ShowSnackbar("Task accepted"))
                } else {
                    _sideEffects.send(FeedSideEffect.ShowSnackbar("Failed to accept task"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(FeedSideEffect.ShowSnackbar("Failed to accept task"))
            }
        }
    }

    private fun handleDeclineTaskAssignment(itemId: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                val result = declineTaskAssignmentUseCase(itemId, userId)
                if (result != null) {
                    _sideEffects.send(FeedSideEffect.ShowSnackbar("Task declined"))
                } else {
                    _sideEffects.send(FeedSideEffect.ShowSnackbar("Failed to decline task"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(FeedSideEffect.ShowSnackbar("Failed to decline task"))
            }
        }
    }

    private fun handleStartPlanningTapped(itemId: String) {
        viewModelScope.launch {
            markFeedItemReadUseCase.markSingleAsRead(itemId)
            _sideEffects.send(FeedSideEffect.NavigateToPlanning)
        }
    }

    private fun handleStartReviewTapped(itemId: String, weekId: String) {
        viewModelScope.launch {
            markFeedItemReadUseCase.markSingleAsRead(itemId)
            _sideEffects.send(FeedSideEffect.NavigateToReview(weekId))
        }
    }

    private fun handleDismissAiPrompt(itemId: String) {
        viewModelScope.launch {
            try {
                dismissAiPromptUseCase(itemId)
                _sideEffects.send(FeedSideEffect.TriggerHapticFeedback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Silently fail - prompt will reappear if dismiss failed
            }
        }
    }

    private fun handleMessageTextChanged(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    private fun handleSendMessageTapped() {
        val text = _uiState.value.messageText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSendingMessage = true) }

                val userId = currentUserId ?: return@launch
                val result = sendMessageUseCase(userId, text)

                if (result != null) {
                    _uiState.update { it.copy(messageText = "", isSendingMessage = false) }
                    _sideEffects.send(FeedSideEffect.ClearMessageInput)
                    _sideEffects.send(FeedSideEffect.TriggerHapticFeedback)
                } else {
                    _uiState.update { it.copy(isSendingMessage = false) }
                    _sideEffects.send(FeedSideEffect.ShowSnackbar("Failed to send message"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isSendingMessage = false) }
                _sideEffects.send(FeedSideEffect.ShowSnackbar("Message cannot be empty"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isSendingMessage = false) }
                _sideEffects.send(FeedSideEffect.ShowSnackbar("Failed to send message"))
            }
        }
    }

    private fun handleTaskCheckboxTapped(taskId: String) {
        viewModelScope.launch {
            _sideEffects.send(FeedSideEffect.TriggerHapticFeedback)
            // Task checkbox toggle would be implemented via TaskRepository
            // This is for allowing users to un-complete a task from the feed
        }
    }

    private fun handleRefreshRequested() {
        _uiState.update { it.copy(isRefreshing = true) }
        // Feed updates automatically via flows
        viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Brief delay for visual feedback
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun handleMoreOptionsTapped() {
        viewModelScope.launch {
            _sideEffects.send(FeedSideEffect.ShowMoreOptionsMenu)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Find the index where the "caught up" separator should be shown.
     * Returns the index after the last unread item, or -1 if all items are read.
     */
    private fun findLastReadIndex(groups: List<FeedDayGroup>): Int {
        var index = 0
        var lastUnreadIndex = -1

        for (group in groups) {
            for (item in group.items) {
                if (!item.isRead) {
                    lastUnreadIndex = index
                }
                index++
            }
        }

        return if (lastUnreadIndex >= 0) lastUnreadIndex + 1 else -1
    }

    private fun getDayLabel(date: LocalDate, today: LocalDate): String {
        val yesterday = today.minus(DatePeriod(days = 1))

        return when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val daysAgo = today.toEpochDays() - date.toEpochDays()
                if (daysAgo in 2..6) {
                    date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercaseChar() }
                } else {
                    formatMonthDay(date)
                }
            }
        }
    }

    private fun getDateLabel(date: LocalDate, today: LocalDate): String {
        val yesterday = today.minus(DatePeriod(days = 1))

        return when (date) {
            today -> formatFullDate(date)
            yesterday -> formatFullDate(date)
            else -> {
                val daysAgo = today.toEpochDays() - date.toEpochDays()
                if (daysAgo in 2..6) {
                    formatFullDate(date)
                } else {
                    formatMonthDay(date)
                }
            }
        }
    }

    private fun formatFullDate(date: LocalDate): String {
        val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercaseChar() }
        val monthDay = formatMonthDay(date)
        return "$dayName, $monthDay"
    }

    private fun formatMonthDay(date: LocalDate): String {
        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }
        return "$month ${date.dayOfMonth}"
    }
}
