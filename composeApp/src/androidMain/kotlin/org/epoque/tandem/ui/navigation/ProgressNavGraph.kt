package org.epoque.tandem.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.epoque.tandem.ui.legacy.progress.PastWeekDetailScreen

/**
 * Navigation graph for Progress feature.
 *
 * Handles navigation to past week detail screens.
 */
fun NavGraphBuilder.progressNavGraph(
    navController: NavController
) {
    composable<Routes.Progress.PastWeekDetail> {
        PastWeekDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
