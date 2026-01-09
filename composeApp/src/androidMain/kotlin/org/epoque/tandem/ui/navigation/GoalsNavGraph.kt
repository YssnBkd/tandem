package org.epoque.tandem.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.epoque.tandem.ui.screens.goals.GoalCreationScreen

/**
 * Navigation graph for goals feature screens.
 */
fun NavGraphBuilder.goalsNavGraph(
    navController: NavController
) {
    composable<Routes.Goals.Create> {
        GoalCreationScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onGoalCreated = { name, ownership, trackingType, stepperValue, startDate, duration ->
                // TODO: Save goal and navigate back or to detail
                navController.popBackStack()
            }
        )
    }

    // Goal detail route (placeholder for future)
    composable<Routes.Goals.Detail> { backStackEntry ->
        // TODO: Implement goal detail screen
        // val goalId = backStackEntry.toRoute<Routes.Goals.Detail>().goalId
    }
}
