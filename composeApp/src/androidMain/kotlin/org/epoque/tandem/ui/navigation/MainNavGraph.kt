package org.epoque.tandem.ui.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.epoque.tandem.presentation.auth.AuthEvent
import org.epoque.tandem.presentation.auth.AuthUiState
import org.epoque.tandem.presentation.auth.AuthViewModel
import org.epoque.tandem.ui.main.MainScreen

/**
 * Navigation graph for main app screens (authenticated users).
 */
fun NavGraphBuilder.mainNavGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    onNavigateToPartnerInvite: () -> Unit = {},
    onNavigateToPartnerSettings: () -> Unit = {}
) {
    composable<Routes.Main.Home> {
        val uiState by authViewModel.uiState.collectAsState()
        val user = (uiState as? AuthUiState.Authenticated)?.user

        MainScreen(
            user = user,
            onSignOut = {
                authViewModel.onEvent(AuthEvent.SignOut)
            },
            onNavigateToPlanning = {
                navController.navigate(Routes.Planning.Start)
            },
            onNavigateToReview = {
                navController.navigate(Routes.Review.Start)
            },
            onNavigateToPartnerInvite = onNavigateToPartnerInvite,
            onNavigateToPartnerSettings = onNavigateToPartnerSettings
        )
    }
}
