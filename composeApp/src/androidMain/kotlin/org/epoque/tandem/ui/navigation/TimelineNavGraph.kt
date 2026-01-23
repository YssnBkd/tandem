package org.epoque.tandem.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.epoque.tandem.ui.screens.timeline.TimelineScreen

/**
 * Navigation graph for Timeline feature.
 *
 * Handles navigation to timeline screen showing all weeks history.
 */
fun NavGraphBuilder.timelineNavGraph(
    navController: NavController
) {
    composable<Routes.Timeline.Home> {
        TimelineScreen(
            onWeekClick = { weekId ->
                // Navigate to week detail (could be Progress.PastWeekDetail or a new route)
                navController.navigate(Routes.Progress.PastWeekDetail(weekId))
            },
            onBackClick = { navController.popBackStack() }
        )
    }
}
