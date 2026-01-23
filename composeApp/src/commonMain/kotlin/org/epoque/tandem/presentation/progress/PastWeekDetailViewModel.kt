package org.epoque.tandem.presentation.progress

import androidx.lifecycle.SavedStateHandle
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
import org.epoque.tandem.domain.model.PastWeekDetail
import org.epoque.tandem.domain.model.ReviewDetail
import org.epoque.tandem.domain.model.TaskOutcome
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.usecase.progress.GetPastWeekDetailUseCase

/**
 * ViewModel for Past Week Detail screen.
 *
 * Loads and displays detailed information for a specific past week,
 * including review data and task outcomes for both user and partner.
 */
class PastWeekDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val getPastWeekDetailUseCase: GetPastWeekDetailUseCase
) : ViewModel() {

    private val weekId: String = savedStateHandle.get<String>("weekId")
        ?: throw IllegalArgumentException("weekId is required")

    private val _uiState = MutableStateFlow(PastWeekDetailUiState(weekId = weekId))
    val uiState: StateFlow<PastWeekDetailUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<PastWeekDetailSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<PastWeekDetailSideEffect> = _sideEffects.receiveAsFlow()

    private var currentUserId: String? = null

    init {
        loadWeekDetail()
    }

    private fun loadWeekDetail() {
        viewModelScope.launch {
            try {
                // Get authenticated user
                val userId = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                    .user.id
                currentUserId = userId

                // Load week detail
                val detail = getPastWeekDetailUseCase(weekId, userId)

                _uiState.update { state ->
                    state.copy(
                        dateRange = formatDateRange(detail),
                        userReview = detail.userReview.toUiModel("You"),
                        partnerReview = detail.partnerReview?.toUiModel("Partner"),
                        tasks = detail.tasks.map { it.toUiModel() },
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onEvent(event: PastWeekDetailEvent) {
        when (event) {
            is PastWeekDetailEvent.Back -> handleBack()
            is PastWeekDetailEvent.Retry -> handleRetry()
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _sideEffects.send(PastWeekDetailSideEffect.NavigateBack)
        }
    }

    private fun handleRetry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadWeekDetail()
    }

    private fun formatDateRange(detail: PastWeekDetail): String {
        val start = detail.startDate
        val end = detail.endDate
        return "${start.monthNumber}/${start.dayOfMonth} - ${end.monthNumber}/${end.dayOfMonth}"
    }

    private fun ReviewDetail.toUiModel(name: String): ReviewSummaryUiModel {
        return ReviewSummaryUiModel(
            name = name,
            moodEmoji = MoodEmojis.fromRating(rating),
            completionText = completion.displayText,
            completionPercentage = completion.percentage,
            note = note,
            isReviewed = isReviewed
        )
    }

    private fun TaskOutcome.toUiModel(): TaskOutcomeUiModel {
        return TaskOutcomeUiModel(
            taskId = taskId,
            title = title,
            priority = priority,
            isCompleted = userStatus == TaskStatus.COMPLETED,
            isSkipped = userStatus == TaskStatus.SKIPPED || userStatus == TaskStatus.DECLINED,
            partnerCompleted = partnerStatus?.let { it == TaskStatus.COMPLETED },
            partnerSkipped = partnerStatus?.let { it == TaskStatus.SKIPPED || it == TaskStatus.DECLINED }
        )
    }
}
