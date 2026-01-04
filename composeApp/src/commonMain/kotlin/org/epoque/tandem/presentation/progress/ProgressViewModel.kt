package org.epoque.tandem.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.model.WeekSummary
import org.epoque.tandem.domain.usecase.progress.CalculatePartnerStreakUseCase
import org.epoque.tandem.domain.usecase.progress.GetCompletionTrendsUseCase
import org.epoque.tandem.domain.usecase.progress.GetMonthlyCompletionUseCase
import org.epoque.tandem.domain.usecase.progress.GetPastWeeksUseCase
import org.epoque.tandem.domain.usecase.progress.MarkMilestoneCelebratedUseCase

/**
 * ViewModel for Progress screen.
 *
 * Manages streak display and milestone celebrations.
 * User Story 1: View streak with partner.
 */
class ProgressViewModel(
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository,
    private val calculatePartnerStreakUseCase: CalculatePartnerStreakUseCase,
    private val markMilestoneCelebratedUseCase: MarkMilestoneCelebratedUseCase,
    private val getCompletionTrendsUseCase: GetCompletionTrendsUseCase,
    private val getMonthlyCompletionUseCase: GetMonthlyCompletionUseCase,
    private val getPastWeeksUseCase: GetPastWeeksUseCase
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ProgressSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<ProgressSideEffect> = _sideEffects.receiveAsFlow()

    private var currentUserId: String? = null
    private var currentOffset: Int = 0
    private val pageSize: Int = 10

    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // 1. Wait for auth
                val userId = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                    .user.id

                currentUserId = userId

                // 2. Get partner info
                val partner = partnerRepository.getPartner(userId)

                // 3. Calculate streak
                val streakResult = calculatePartnerStreakUseCase(userId)

                // 4. Get completion trends (last 8 weeks)
                val trends = getCompletionTrendsUseCase(userId)

                // 5. Get monthly completion stats
                val monthlyUser = getMonthlyCompletionUseCase(userId)
                val monthlyPartner = partner?.let { getMonthlyCompletionUseCase(it.id) }

                // 6. Get past weeks (first page)
                val pastWeeksResult = getPastWeeksUseCase(userId, offset = 0, limit = pageSize)
                currentOffset = pastWeeksResult.weeks.size

                // 7. Update UI state with all data
                _uiState.update { state ->
                    state.copy(
                        currentStreak = streakResult.count,
                        isPartnerStreak = streakResult.isPartnerStreak,
                        showMilestoneCelebration = streakResult.pendingMilestone != null,
                        milestoneValue = streakResult.pendingMilestone,
                        userMonthlyCompletion = monthlyUser.percentage,
                        partnerMonthlyCompletion = monthlyPartner?.percentage,
                        userMonthlyText = monthlyUser.displayText,
                        partnerMonthlyText = monthlyPartner?.displayText,
                        trendData = trends,
                        showTrendChart = !trends.insufficientData,
                        pastWeeks = pastWeeksResult.weeks.map { it.toUiModel() },
                        hasMoreWeeks = pastWeeksResult.hasMore,
                        hasPartner = partner != null,
                        partnerName = partner?.name,
                        isLoading = false
                    )
                }

                // 8. Trigger haptic if milestone
                if (streakResult.pendingMilestone != null) {
                    _sideEffects.send(ProgressSideEffect.TriggerMilestoneHaptic)
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EVENT HANDLING
    // ═══════════════════════════════════════════════════════════════════════════

    fun onEvent(event: ProgressEvent) {
        when (event) {
            is ProgressEvent.DismissMilestone -> handleDismissMilestone()
            is ProgressEvent.Retry -> handleRetry()
            is ProgressEvent.ScreenVisible -> { /* Optional: refresh data */ }
            is ProgressEvent.PastWeekTapped -> handlePastWeekTapped(event.weekId)
            is ProgressEvent.LoadMoreWeeks -> handleLoadMoreWeeks()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleLoadMoreWeeks() {
        val userId = currentUserId ?: return
        if (_uiState.value.isLoadingMoreWeeks || !_uiState.value.hasMoreWeeks) return

        _uiState.update { it.copy(isLoadingMoreWeeks = true) }

        viewModelScope.launch {
            try {
                val result = getPastWeeksUseCase(userId, offset = currentOffset, limit = pageSize)
                currentOffset += result.weeks.size

                _uiState.update { state ->
                    state.copy(
                        pastWeeks = state.pastWeeks + result.weeks.map { it.toUiModel() },
                        hasMoreWeeks = result.hasMore,
                        isLoadingMoreWeeks = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMoreWeeks = false) }
                _sideEffects.send(ProgressSideEffect.ShowSnackbar("Failed to load more weeks"))
            }
        }
    }

    private fun handleDismissMilestone() {
        val milestone = _uiState.value.milestoneValue ?: return

        viewModelScope.launch {
            try {
                markMilestoneCelebratedUseCase(milestone)
                _uiState.update { it.copy(showMilestoneCelebration = false, milestoneValue = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Silently fail - milestone will be shown again next time
            }
        }
    }

    private fun handleRetry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadInitialData()
    }

    private fun handlePastWeekTapped(weekId: String) {
        viewModelScope.launch {
            _sideEffects.send(ProgressSideEffect.NavigateToWeekDetail(weekId))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun WeekSummary.toUiModel(): WeekSummaryUiModel {
        val dateFormat = { date: kotlinx.datetime.LocalDate ->
            "${date.monthNumber}/${date.dayOfMonth}"
        }

        return WeekSummaryUiModel(
            weekId = weekId,
            dateRange = "${dateFormat(startDate)} - ${dateFormat(endDate)}",
            userCompletionText = userCompletion.displayText,
            partnerCompletionText = partnerCompletion?.displayText,
            userMoodEmoji = MoodEmojis.fromRating(userRating),
            partnerMoodEmoji = MoodEmojis.fromRating(partnerRating),
            isReviewed = isReviewed
        )
    }
}
