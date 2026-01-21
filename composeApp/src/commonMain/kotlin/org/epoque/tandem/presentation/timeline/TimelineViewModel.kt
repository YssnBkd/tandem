package org.epoque.tandem.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.WeekWithStats
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * ViewModel for the Timeline screen.
 *
 * Manages:
 * - Loading all weeks with task statistics
 * - Building timeline items (section headers, week cards, gap indicators)
 * - Expanding/collapsing week cards
 * - Filtering empty weeks
 * - Scroll state for "Back to this week" button
 */
class TimelineViewModel(
    private val weekRepository: WeekRepository,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * UI State for the Timeline screen.
     */
    data class TimelineUiState(
        val items: List<TimelineItem> = emptyList(),
        val isLoading: Boolean = true,
        val hideEmptyWeeks: Boolean = true,
        val expandedWeekIds: Set<String> = emptySet(),
        val currentWeekId: String? = null,
        val currentWeekIndex: Int = 0,
        val showBackToThisWeek: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    // Cache for tasks by week ID
    private val weekTasksCache = mutableMapOf<String, List<Task>>()

    init {
        loadTimeline()
    }

    private fun loadTimeline() {
        viewModelScope.launch {
            try {
                // Wait for authenticated user
                val authState = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                val userId = authState.user.id

                // Get current week ID and auto-expand it
                val currentWeekId = weekRepository.getCurrentWeekId()

                _uiState.update {
                    it.copy(
                        currentWeekId = currentWeekId,
                        expandedWeekIds = setOf(currentWeekId)
                    )
                }

                // Observe weeks with stats
                weekRepository.observeWeeksWithStats(userId)
                    .combine(_uiState.map { it.hideEmptyWeeks to it.expandedWeekIds }) { weeks, (hideEmpty, expanded) ->
                        buildTimelineItems(weeks, hideEmpty, expanded, currentWeekId)
                    }
                    .collect { items ->
                        val currentIndex = items.indexOfFirst { item ->
                            item is TimelineItem.WeekCard && item.week.id == currentWeekId
                        }.coerceAtLeast(0)

                        _uiState.update {
                            it.copy(
                                items = items,
                                isLoading = false,
                                currentWeekIndex = currentIndex
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load timeline"
                    )
                }
            }
        }
    }

    /**
     * Toggle the expanded state of a week card.
     */
    fun toggleWeekExpanded(weekId: String) {
        _uiState.update { state ->
            val newExpanded = if (weekId in state.expandedWeekIds) {
                state.expandedWeekIds - weekId
            } else {
                state.expandedWeekIds + weekId
            }
            state.copy(expandedWeekIds = newExpanded)
        }

        // Load tasks for the week if expanding and not cached
        if (weekId in _uiState.value.expandedWeekIds && weekId !in weekTasksCache) {
            loadTasksForWeek(weekId)
        }
    }

    /**
     * Toggle the hide empty weeks filter.
     */
    fun toggleHideEmptyWeeks() {
        _uiState.update { it.copy(hideEmptyWeeks = !it.hideEmptyWeeks) }
    }

    /**
     * Update the scroll position to show/hide "Back to this week" button.
     */
    fun updateScrollPosition(firstVisibleItemIndex: Int) {
        val currentWeekIndex = _uiState.value.currentWeekIndex
        val showButton = firstVisibleItemIndex > currentWeekIndex + 2 ||
                         firstVisibleItemIndex < currentWeekIndex - 2
        _uiState.update { it.copy(showBackToThisWeek = showButton) }
    }

    /**
     * Get tasks for a specific week (for display in expanded cards).
     */
    fun getTasksForWeek(weekId: String): List<Task> {
        return weekTasksCache[weekId] ?: emptyList()
    }

    private fun loadTasksForWeek(weekId: String) {
        viewModelScope.launch {
            try {
                val authState = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                val userId = authState.user.id

                taskRepository.observeTasksForWeek(weekId, userId)
                    .collect { tasks ->
                        weekTasksCache[weekId] = tasks
                        // Trigger UI update by refreshing items
                        refreshItems()
                    }
            } catch (e: Exception) {
                // Silently fail - tasks just won't show
            }
        }
    }

    private fun refreshItems() {
        val state = _uiState.value
        // Rebuild items with current state to update task lists
        viewModelScope.launch {
            try {
                val authState = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                val userId = authState.user.id

                weekRepository.observeWeeksWithStats(userId)
                    .first()
                    .let { weeks ->
                        val items = buildTimelineItems(
                            weeks,
                            state.hideEmptyWeeks,
                            state.expandedWeekIds,
                            state.currentWeekId ?: ""
                        )
                        _uiState.update { it.copy(items = items) }
                    }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Build timeline items from weeks, adding section headers and gap indicators.
     */
    private fun buildTimelineItems(
        weeks: List<WeekWithStats>,
        hideEmptyWeeks: Boolean,
        expandedWeekIds: Set<String>,
        currentWeekId: String
    ): List<TimelineItem> {
        if (weeks.isEmpty()) return emptyList()

        val items = mutableListOf<TimelineItem>()
        var currentSectionKey: String? = null
        var consecutiveEmptyWeeks = mutableListOf<WeekWithStats>()

        for (weekWithStats in weeks) {
            val week = weekWithStats.week
            val isCurrentWeek = week.id == currentWeekId
            val isEmpty = weekWithStats.isEmpty

            // Handle empty weeks
            if (isEmpty && hideEmptyWeeks && !isCurrentWeek) {
                consecutiveEmptyWeeks.add(weekWithStats)
                continue
            }

            // Flush any accumulated empty weeks as a gap indicator
            if (consecutiveEmptyWeeks.isNotEmpty()) {
                items.add(
                    TimelineItem.GapIndicator(
                        emptyWeekCount = consecutiveEmptyWeeks.size,
                        startWeekId = consecutiveEmptyWeeks.first().week.id,
                        endWeekId = consecutiveEmptyWeeks.last().week.id
                    )
                )
                consecutiveEmptyWeeks.clear()
            }

            // Add section header if needed (based on quarter/month)
            val sectionKey = getSectionKey(week.startDate.month, week.startDate.year)
            if (sectionKey != currentSectionKey) {
                currentSectionKey = sectionKey
                items.add(
                    TimelineItem.SectionHeader(
                        quarter = getQuarter(week.startDate.month),
                        month = week.startDate.month.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        year = week.startDate.year
                    )
                )
            }

            // Add week card - current week starts expanded but can be toggled
            val isExpanded = week.id in expandedWeekIds
            items.add(
                TimelineItem.WeekCard(
                    week = week,
                    tasks = if (isExpanded) weekTasksCache[week.id] ?: emptyList() else emptyList(),
                    totalTasks = weekWithStats.totalTasks,
                    completedTasks = weekWithStats.completedTasks,
                    isCurrentWeek = isCurrentWeek,
                    isExpanded = isExpanded
                )
            )

            // Load tasks for current week automatically
            if (isCurrentWeek && week.id !in weekTasksCache) {
                loadTasksForWeek(week.id)
            }
        }

        // Don't forget trailing empty weeks
        if (consecutiveEmptyWeeks.isNotEmpty()) {
            items.add(
                TimelineItem.GapIndicator(
                    emptyWeekCount = consecutiveEmptyWeeks.size,
                    startWeekId = consecutiveEmptyWeeks.first().week.id,
                    endWeekId = consecutiveEmptyWeeks.last().week.id
                )
            )
        }

        return items
    }

    private fun getSectionKey(month: Month, year: Int): String {
        return "${year}_Q${getQuarter(month)}_${month.name}"
    }

    private fun getQuarter(month: Month): Int {
        return when (month) {
            Month.JANUARY, Month.FEBRUARY, Month.MARCH -> 1
            Month.APRIL, Month.MAY, Month.JUNE -> 2
            Month.JULY, Month.AUGUST, Month.SEPTEMBER -> 3
            Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER -> 4
        }
    }
}
