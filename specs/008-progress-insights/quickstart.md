# Quickstart: Progress & Insights

**Feature**: 008-progress-insights
**Date**: 2026-01-04

## Overview

Progress & Insights is a read-only feature that displays streak information, completion trends, and historical week data. It queries existing Week, Task, and Partnership entities without modifying them.

## Prerequisites

Before implementing this feature, ensure:

1. **Feature 002** (Task Data Layer) is complete - provides Task entity and TaskRepository
2. **Feature 005** (Week Review) is complete - provides Week entity with reviewedAt, overallRating
3. **Feature 006** (Partner System) is complete - provides PartnerRepository

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/org/epoque/tandem/
│   └── presentation/progress/
│       ├── ProgressViewModel.kt
│       ├── ProgressUiState.kt
│       ├── ProgressEvent.kt
│       ├── ProgressSideEffect.kt
│       ├── PastWeekDetailViewModel.kt
│       └── PastWeekDetailUiState.kt
│
└── androidMain/kotlin/org/epoque/tandem/
    ├── ui/progress/
    │   ├── ProgressScreen.kt
    │   ├── StreakCard.kt
    │   ├── CompletionBars.kt
    │   ├── TrendChart.kt
    │   ├── PastWeeksList.kt
    │   └── PastWeekDetailScreen.kt
    │
    ├── data/preferences/
    │   └── ProgressPreferences.kt
    │
    └── di/
        └── ProgressModule.kt

shared/src/commonMain/kotlin/org/epoque/tandem/domain/
├── model/
│   ├── StreakResult.kt
│   ├── CompletionStats.kt
│   ├── TrendDataPoint.kt
│   ├── TrendChartData.kt
│   ├── WeekSummary.kt
│   └── PastWeekDetail.kt
│
└── usecase/progress/
    ├── CalculatePartnerStreakUseCase.kt
    ├── GetCompletionStatsUseCase.kt
    ├── GetCompletionTrendsUseCase.kt
    ├── GetPastWeeksUseCase.kt
    ├── GetPastWeekDetailUseCase.kt
    └── GetPendingMilestoneUseCase.kt
```

## Implementation Order

### Phase 1: Domain Models (Day 1)

Create domain models in `shared/src/commonMain`:

```kotlin
// 1. StreakResult.kt
data class StreakResult(
    val count: Int,
    val isPartnerStreak: Boolean,
    val pendingMilestone: Int?
)

// 2. CompletionStats.kt
data class CompletionStats(
    val completedCount: Int,
    val totalCount: Int
) {
    val percentage: Int get() = if (totalCount > 0) {
        (completedCount * 100) / totalCount
    } else 0
}

// 3. TrendDataPoint.kt, TrendChartData.kt
// 4. WeekSummary.kt, PastWeekDetail.kt
```

### Phase 2: Use Cases (Day 1-2)

Implement use cases following existing patterns:

```kotlin
// CalculatePartnerStreakUseCase.kt
class CalculatePartnerStreakUseCase(
    private val weekRepository: WeekRepository,
    private val partnerRepository: PartnerRepository,
    private val progressPreferences: ProgressPreferences
) {
    suspend operator fun invoke(userId: String): StreakResult {
        val partner = partnerRepository.getPartner(userId)

        val streak = if (partner == null) {
            calculateSoloStreak(userId)
        } else {
            calculatePartnerStreak(userId, partner.id)
        }

        val lastCelebrated = progressPreferences.lastCelebratedMilestone.first()
        val pendingMilestone = getPendingMilestone(streak, lastCelebrated)

        return StreakResult(
            count = streak,
            isPartnerStreak = partner != null,
            pendingMilestone = pendingMilestone
        )
    }

    private suspend fun calculateSoloStreak(userId: String): Int {
        // Existing logic from CalculateStreakUseCase
    }

    private suspend fun calculatePartnerStreak(userId: String, partnerId: String): Int {
        // Both must have reviewedAt != null for week to count
    }
}
```

### Phase 3: DataStore Preferences (Day 2)

```kotlin
// ProgressPreferences.kt
object ProgressPreferencesKeys {
    val LAST_CELEBRATED_MILESTONE = intPreferencesKey("last_celebrated_milestone")
}

class ProgressPreferences(
    private val dataStore: DataStore<Preferences>
) {
    val lastCelebratedMilestone: Flow<Int> = dataStore.data
        .map { it[LAST_CELEBRATED_MILESTONE] ?: 0 }

    suspend fun setLastCelebratedMilestone(milestone: Int) {
        dataStore.edit { it[LAST_CELEBRATED_MILESTONE] = milestone }
    }
}
```

### Phase 4: ViewModel (Day 2-3)

```kotlin
// ProgressViewModel.kt
class ProgressViewModel(
    private val calculateStreakUseCase: CalculatePartnerStreakUseCase,
    private val getCompletionTrendsUseCase: GetCompletionTrendsUseCase,
    private val getPastWeeksUseCase: GetPastWeeksUseCase,
    private val getMonthlyCompletionUseCase: GetMonthlyCompletionUseCase,
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository,
    private val progressPreferences: ProgressPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ProgressSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<ProgressSideEffect> = _sideEffects.receiveAsFlow()

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

                // 2. Get partner info
                val partner = partnerRepository.getPartner(userId)

                // 3. Calculate streak
                val streakResult = calculateStreakUseCase(userId)

                // 4. Get trends
                val trends = getCompletionTrendsUseCase(userId)

                // 5. Get monthly completion
                val monthlyUser = getMonthlyCompletionUseCase(userId)
                val monthlyPartner = partner?.let { getMonthlyCompletionUseCase(it.id) }

                // 6. Get initial past weeks
                val pastWeeks = getPastWeeksUseCase(userId)

                // 7. Update state
                _uiState.update { state ->
                    state.copy(
                        currentStreak = streakResult.count,
                        isPartnerStreak = streakResult.isPartnerStreak,
                        showMilestoneCelebration = streakResult.pendingMilestone != null,
                        milestoneValue = streakResult.pendingMilestone,
                        trendData = trends,
                        showTrendChart = trends.weekCount >= 4,
                        userMonthlyCompletion = monthlyUser.percentage,
                        userMonthlyText = monthlyUser.displayText,
                        partnerMonthlyCompletion = monthlyPartner?.percentage,
                        partnerMonthlyText = monthlyPartner?.displayText,
                        pastWeeks = pastWeeks.weeks.map { it.toUiModel() },
                        hasMoreWeeks = pastWeeks.hasMore,
                        hasPartner = partner != null,
                        partnerName = partner?.name,
                        isLoading = false
                    )
                }

                // Trigger haptic if milestone
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

    fun onEvent(event: ProgressEvent) {
        when (event) {
            is ProgressEvent.PastWeekTapped -> navigateToWeekDetail(event.weekId)
            is ProgressEvent.LoadMoreWeeks -> loadMoreWeeks()
            is ProgressEvent.DismissMilestone -> dismissMilestone()
            is ProgressEvent.Retry -> loadInitialData()
            is ProgressEvent.ScreenVisible -> { /* Optional: refresh data */ }
        }
    }
}
```

### Phase 5: UI Components (Day 3-4)

```kotlin
// ProgressScreen.kt
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = koinViewModel(),
    onNavigateToWeekDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ProgressSideEffect.NavigateToWeekDetail ->
                    onNavigateToWeekDetail(effect.weekId)
                is ProgressSideEffect.TriggerMilestoneHaptic ->
                    // Trigger device haptic
                is ProgressSideEffect.ShowSnackbar ->
                    // Show snackbar
            }
        }
    }

    ProgressContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ProgressContent(
    uiState: ProgressUiState,
    onEvent: (ProgressEvent) -> Unit
) {
    LazyColumn {
        // Streak Card
        item {
            StreakCard(
                streakCount = uiState.currentStreak,
                isPartnerStreak = uiState.isPartnerStreak,
                showCelebration = uiState.showMilestoneCelebration,
                milestoneValue = uiState.milestoneValue,
                onDismissCelebration = { onEvent(ProgressEvent.DismissMilestone) }
            )
        }

        // Completion Bars
        item {
            CompletionBars(
                userPercentage = uiState.userMonthlyCompletion,
                partnerPercentage = uiState.partnerMonthlyCompletion,
                userText = uiState.userMonthlyText,
                partnerText = uiState.partnerMonthlyText,
                partnerName = uiState.partnerName
            )
        }

        // Trend Chart
        if (uiState.showTrendChart && uiState.trendData != null) {
            item {
                TrendChart(trendData = uiState.trendData)
            }
        }

        // Past Weeks Header
        item {
            Text("Past Weeks", style = MaterialTheme.typography.titleMedium)
        }

        // Past Weeks List
        items(uiState.pastWeeks, key = { it.weekId }) { week ->
            PastWeekItem(
                week = week,
                onClick = { onEvent(ProgressEvent.PastWeekTapped(week.weekId)) }
            )
        }

        // Load More
        if (uiState.hasMoreWeeks) {
            item {
                LaunchedEffect(Unit) {
                    onEvent(ProgressEvent.LoadMoreWeeks)
                }
                CircularProgressIndicator()
            }
        }
    }
}
```

### Phase 6: TrendChart Canvas Implementation (Day 4)

```kotlin
// TrendChart.kt
@Composable
fun TrendChart(
    trendData: TrendChartData,
    modifier: Modifier = Modifier
) {
    val userColor = MaterialTheme.colorScheme.primary
    val partnerColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val padding = 32.dp.toPx()

        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)

        val points = trendData.dataPoints
        val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)

        // Draw user line
        val userPath = Path()
        points.forEachIndexed { index, point ->
            val x = padding + (index * stepX)
            val y = padding + chartHeight - (point.userPercentage / 100f * chartHeight)

            if (index == 0) {
                userPath.moveTo(x, y)
            } else {
                userPath.lineTo(x, y)
            }
        }
        drawPath(userPath, userColor, style = Stroke(width = 4.dp.toPx()))

        // Draw partner line (if applicable)
        if (trendData.hasPartner) {
            val partnerPath = Path()
            points.forEachIndexed { index, point ->
                point.partnerPercentage?.let { pct ->
                    val x = padding + (index * stepX)
                    val y = padding + chartHeight - (pct / 100f * chartHeight)

                    if (index == 0) {
                        partnerPath.moveTo(x, y)
                    } else {
                        partnerPath.lineTo(x, y)
                    }
                }
            }
            drawPath(partnerPath, partnerColor, style = Stroke(width = 4.dp.toPx()))
        }
    }
}
```

### Phase 7: DI Module (Day 4)

```kotlin
// ProgressModule.kt
val progressModule = module {
    // Use Cases
    factory { CalculatePartnerStreakUseCase(get(), get(), get()) }
    factory { GetCompletionStatsUseCase(get()) }
    factory { GetCompletionTrendsUseCase(get(), get(), get()) }
    factory { GetPastWeeksUseCase(get(), get()) }
    factory { GetPastWeekDetailUseCase(get(), get(), get()) }
    factory { GetMonthlyCompletionUseCase(get(), get()) }

    // Preferences
    single { ProgressPreferences(get()) }

    // ViewModels
    viewModel { ProgressViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { params -> PastWeekDetailViewModel(params.get(), get(), get(), get()) }
}
```

### Phase 8: Navigation Integration (Day 5)

Add to existing navigation graph:

```kotlin
// In MainNavGraph.kt or TandemNavHost.kt
composable("progress") {
    ProgressScreen(
        onNavigateToWeekDetail = { weekId ->
            navController.navigate("progress/week/$weekId")
        }
    )
}

composable(
    route = "progress/week/{weekId}",
    arguments = listOf(navArgument("weekId") { type = NavType.StringType })
) { backStackEntry ->
    val weekId = backStackEntry.arguments?.getString("weekId") ?: return@composable
    PastWeekDetailScreen(
        weekId = weekId,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

## Testing Checklist

### Unit Tests

- [ ] `CalculatePartnerStreakUseCaseTest` - solo and partner streak calculations
- [ ] `GetCompletionStatsUseCaseTest` - percentage calculations, edge cases
- [ ] `GetCompletionTrendsUseCaseTest` - 8 weeks, <8 weeks, no data
- [ ] `ProgressViewModelTest` - state transitions, events, side effects

### UI Tests

- [ ] Progress screen loads with streak card
- [ ] Completion bars display correctly
- [ ] Trend chart renders with data points
- [ ] Past weeks list scrolls and loads more
- [ ] Past week detail shows correct data
- [ ] Milestone celebration auto-dismisses

### Manual Testing

1. **Streak Display**: Complete reviews for 3+ weeks, verify count
2. **Milestone**: Reach 5-week streak, verify celebration shows once
3. **Solo User**: Test without partner connected
4. **Offline**: Disconnect network, verify data displays
5. **Pagination**: Create 15+ weeks of history, verify load more

## Build Validation

After implementation, run:

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:testDebugUnitTest
```

## Key Patterns to Follow

1. **Auth Dependency**: Always wait for `AuthState.Authenticated` before data operations
2. **CancellationException**: Always re-throw, never consume
3. **State Updates**: Use `.update { }` for thread-safe mutations
4. **Side Effects**: Use `Channel` for one-time effects, collect only once in UI
5. **Pagination**: Use offset-based with automatic load on scroll-to-bottom
