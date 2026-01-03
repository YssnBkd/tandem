package org.epoque.tandem.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.epoque.tandem.presentation.planning.PlanningViewModel

/**
 * Navigation graph for weekly planning wizard.
 *
 * TODO: Replace placeholder screens with actual implementations in Phase 3-7.
 */
fun NavGraphBuilder.planningNavGraph(
    navController: NavController,
    planningViewModel: PlanningViewModel
) {
    composable<Routes.Planning.Start> {
        // TODO: Implement PlanningScreen in Phase 3 (US1)
        PlaceholderPlanningScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable<Routes.Planning.Wizard> { backStackEntry ->
        // TODO: Implement wizard step screens in Phase 3-7
        PlaceholderPlanningScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}

/**
 * Placeholder screen for planning wizard.
 * Will be replaced with actual implementation in Phase 3-7.
 */
@Composable
private fun PlaceholderPlanningScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Planning Screen - To be implemented in Phase 3-7")
    }
}
