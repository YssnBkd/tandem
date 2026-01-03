package org.epoque.tandem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.epoque.tandem.presentation.auth.AuthUiState
import org.epoque.tandem.presentation.auth.AuthViewModel
import org.epoque.tandem.ui.planning.PlanningScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main navigation host for the Tandem app.
 * Routes between authenticated and unauthenticated flows.
 */
@Composable
fun TandemNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    // React to auth state changes for navigation
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Authenticated -> {
                // Navigate to main app, clearing auth backstack
                navController.navigate(Routes.Main.Home) {
                    popUpTo(Routes.Auth.Welcome) { inclusive = true }
                }
            }
            is AuthUiState.Unauthenticated -> {
                // Navigate to welcome, clearing main backstack
                navController.navigate(Routes.Auth.Welcome) {
                    popUpTo(Routes.Main.Home) { inclusive = true }
                }
            }
            is AuthUiState.Loading -> {
                // Stay on current screen while loading
            }
            is AuthUiState.Error -> {
                // Stay on current screen to display error
            }
        }
    }

    // Start at welcome by default - LaunchedEffect will redirect if authenticated
    NavHost(
        navController = navController,
        startDestination = Routes.Auth.Welcome,
        modifier = modifier
    ) {
        // Authentication flow
        authNavGraph(
            navController = navController,
            authViewModel = authViewModel
        )

        // Main app flow (placeholder for now - will be expanded in US3)
        mainNavGraph(
            navController = navController,
            authViewModel = authViewModel
        )

        // Planning wizard flow
        composable<Routes.Planning.Start> {
            PlanningScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
