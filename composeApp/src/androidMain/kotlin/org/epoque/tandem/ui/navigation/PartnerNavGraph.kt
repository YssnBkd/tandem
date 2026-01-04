package org.epoque.tandem.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.epoque.tandem.presentation.partner.PartnerEvent
import org.epoque.tandem.presentation.partner.PartnerUiState
import org.epoque.tandem.ui.partner.ConnectionConfirmationScreen
import org.epoque.tandem.ui.partner.InvitePartnerScreen
import org.epoque.tandem.ui.partner.PartnerLandingScreen
import org.epoque.tandem.ui.partner.PartnerSettingsScreen

/**
 * Navigation graph for partner-related screens.
 *
 * Uses stateProvider lambda to avoid state capture issues.
 * This pattern ensures each composable gets fresh state on each recomposition,
 * rather than capturing a stale snapshot when the navigation graph is built.
 *
 * @param navController Navigation controller for navigation actions
 * @param stateProvider Lambda that returns the current UI state
 * @param onEvent Callback to handle UI events
 */
fun NavGraphBuilder.partnerNavGraph(
    navController: NavController,
    stateProvider: () -> PartnerUiState,
    onEvent: (PartnerEvent) -> Unit
) {
    // Invite partner screen
    composable<Routes.Partner.Invite> {
        val state = stateProvider()
        InvitePartnerScreen(
            hasActiveInvite = state.hasActiveInvite,
            inviteLink = state.inviteLink,
            onGenerateInvite = { onEvent(PartnerEvent.GenerateInvite) },
            onShareInvite = { onEvent(PartnerEvent.ShareInvite) },
            onCopyInvite = { /* Copy to clipboard handled via side effect */ },
            onSkip = { navController.popBackStack() }
        )
    }

    // Accept invite screen (from deep link)
    composable<Routes.Partner.AcceptInvite> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Partner.AcceptInvite>()
        val state = stateProvider()

        // Load invite on entry
        LaunchedEffect(route.code) {
            onEvent(PartnerEvent.LoadInvite(route.code))
        }

        PartnerLandingScreen(
            inviteInfo = state.inviteInfo,
            isLoading = state.isLoading,
            isAccepting = state.isAcceptingInvite,
            error = state.error,
            onAccept = { onEvent(PartnerEvent.AcceptInvite) },
            onDecline = { onEvent(PartnerEvent.DeclineInvite) }
        )
    }

    // Connection confirmation screen
    composable<Routes.Partner.Confirmation> {
        val state = stateProvider()
        ConnectionConfirmationScreen(
            partnerName = state.partner?.name ?: "",
            onPlanWeek = {
                navController.navigate(Routes.Planning.Start) {
                    popUpTo(Routes.Partner.Confirmation) { inclusive = true }
                }
            },
            onDone = {
                navController.navigate(Routes.Main.Home) {
                    popUpTo(Routes.Partner.Confirmation) { inclusive = true }
                }
            }
        )
    }

    // Partner settings screen
    composable<Routes.Partner.Settings> {
        val state = stateProvider()
        val partner = state.partner

        if (partner != null) {
            PartnerSettingsScreen(
                partnerName = partner.name,
                connectedSince = partner.connectedAt,
                showDisconnectDialog = state.showDisconnectDialog,
                isDisconnecting = state.isDisconnecting,
                onShowDisconnectDialog = { onEvent(PartnerEvent.ShowDisconnectDialog) },
                onDismissDisconnectDialog = { onEvent(PartnerEvent.DismissDisconnectDialog) },
                onConfirmDisconnect = { onEvent(PartnerEvent.ConfirmDisconnect) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
