package org.epoque.tandem.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.epoque.tandem.presentation.auth.AuthUiState
import org.epoque.tandem.presentation.auth.AuthViewModel
import org.epoque.tandem.presentation.partner.PartnerViewModel
import org.epoque.tandem.ui.planning.PlanningScreen
import org.epoque.tandem.ui.review.ReviewScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main navigation host for the Tandem app.
 * Routes between authenticated and unauthenticated flows.
 *
 * @param pendingInviteCode Invite code from deep link to process after authentication
 * @param onInviteCodeConsumed Callback when the invite code has been processed
 */
@Composable
fun TandemNavHost(
    modifier: Modifier = Modifier,
    pendingInviteCode: String? = null,
    onInviteCodeConsumed: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel(),
    partnerViewModel: PartnerViewModel = koinViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val partnerState by partnerViewModel.uiState.collectAsState()

    // Snackbar state for showing messages
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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

    // Handle deep link navigation for partner invites
    LaunchedEffect(pendingInviteCode, uiState) {
        if (pendingInviteCode != null && uiState is AuthUiState.Authenticated) {
            // Navigate to accept invite screen
            navController.navigate(Routes.Partner.AcceptInvite(pendingInviteCode))
            onInviteCodeConsumed()
        }
    }

    // Start at welcome by default - LaunchedEffect will redirect if authenticated
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.Auth.Welcome,
            modifier = Modifier.fillMaxSize()
        ) {
            // Authentication flow
            authNavGraph(
                navController = navController,
                authViewModel = authViewModel
            )

            // Main app flow (placeholder for now - will be expanded in US3)
            mainNavGraph(
                navController = navController,
                authViewModel = authViewModel,
                onNavigateToPartnerInvite = {
                    navController.navigate(Routes.Partner.Invite)
                },
                onNavigateToPartnerSettings = {
                    navController.navigate(Routes.Partner.Settings)
                }
            )

            // Partner system flow
            partnerNavGraph(
                navController = navController,
                stateProvider = { partnerState },
                sideEffects = partnerViewModel.sideEffects,
                onEvent = partnerViewModel::onEvent,
                onShowSnackbar = { message ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )

            // Progress detail navigation
            progressNavGraph(
                navController = navController
            )

            // Planning wizard flow
            composable<Routes.Planning.Start> {
                PlanningScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Review wizard flow
            composable<Routes.Review.Start> {
                ReviewScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPlanning = {
                        navController.navigate(Routes.Planning.Start) {
                            popUpTo(Routes.Review.Start) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Snackbar host for showing messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
