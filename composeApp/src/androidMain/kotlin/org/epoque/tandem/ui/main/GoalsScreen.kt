package org.epoque.tandem.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import org.epoque.tandem.ui.screens.goals.GoalsScreen as GoalsScreenImpl

/**
 * Goals tab screen showing goal setting and tracking.
 * Delegates to the new redesigned GoalsScreen implementation.
 */
@Composable
fun GoalsScreen(
    contentPadding: PaddingValues,
    onNavigateToAddGoal: () -> Unit = {},
    onNavigateToGoalDetail: (String) -> Unit = {},
    onClearFab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Clear FAB when this screen is shown (Goals has no FAB)
    DisposableEffect(Unit) {
        onClearFab()
        onDispose { }
    }

    GoalsScreenImpl(
        contentPadding = contentPadding,
        onNavigateToGoalDetail = onNavigateToGoalDetail,
        onNavigateToAddGoal = onNavigateToAddGoal,
        modifier = modifier
    )
}
