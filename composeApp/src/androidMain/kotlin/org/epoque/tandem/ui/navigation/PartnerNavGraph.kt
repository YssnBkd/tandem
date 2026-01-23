package org.epoque.tandem.ui.navigation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.presentation.partner.PartnerEvent
import org.epoque.tandem.presentation.partner.PartnerSideEffect
import org.epoque.tandem.presentation.partner.PartnerUiState
import org.epoque.tandem.ui.legacy.partner.ConnectionConfirmationScreen
import org.epoque.tandem.ui.legacy.partner.InvitePartnerScreen
import org.epoque.tandem.ui.legacy.partner.PartnerLandingScreen
import org.epoque.tandem.ui.legacy.partner.PartnerSettingsScreen

/**
 * Navigation graph for partner-related screens.
 *
 * Uses stateProvider lambda to avoid state capture issues.
 * This pattern ensures each composable gets fresh state on each recomposition,
 * rather than capturing a stale snapshot when the navigation graph is built.
 *
 * @param navController Navigation controller for navigation actions
 * @param stateProvider Lambda that returns the current UI state
 * @param sideEffects Flow of one-time side effects from the ViewModel
 * @param onEvent Callback to handle UI events
 * @param onShowSnackbar Callback to show a snackbar message
 */
fun NavGraphBuilder.partnerNavGraph(
    navController: NavController,
    stateProvider: () -> PartnerUiState,
    sideEffects: Flow<PartnerSideEffect>,
    onEvent: (PartnerEvent) -> Unit,
    onShowSnackbar: (String) -> Unit = {}
) {
    // Invite partner screen
    composable<Routes.Partner.Invite> {
        val state = stateProvider()
        val context = LocalContext.current
        val hapticFeedback = LocalHapticFeedback.current

        // Handle side effects
        LaunchedEffect(Unit) {
            sideEffects.collect { effect ->
                when (effect) {
                    is PartnerSideEffect.ShowShareSheet -> {
                        shareInviteLink(context, effect.link)
                    }
                    is PartnerSideEffect.ShowSnackbar -> {
                        onShowSnackbar(effect.message)
                    }
                    is PartnerSideEffect.TriggerHapticFeedback -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    is PartnerSideEffect.NavigateToConfirmation -> {
                        navController.navigate(Routes.Partner.Confirmation) {
                            popUpTo(Routes.Partner.Invite) { inclusive = true }
                        }
                    }
                    is PartnerSideEffect.NavigateToHome -> {
                        navController.navigate(Routes.Main.Home) {
                            popUpTo(Routes.Partner.Invite) { inclusive = true }
                        }
                    }
                    is PartnerSideEffect.NavigateBack -> {
                        navController.popBackStack()
                    }
                    else -> { /* Handled elsewhere or not applicable */ }
                }
            }
        }

        InvitePartnerScreen(
            hasActiveInvite = state.hasActiveInvite,
            inviteLink = state.inviteLink,
            onGenerateInvite = { onEvent(PartnerEvent.GenerateInvite) },
            onShareInvite = { onEvent(PartnerEvent.ShareInvite) },
            onCopyInvite = {
                state.inviteLink?.let { link ->
                    copyToClipboard(context, link)
                    onShowSnackbar("Link copied to clipboard")
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            },
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

/**
 * Opens the Android share sheet with the invite link.
 */
private fun shareInviteLink(context: Context, link: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Join me on Tandem! $link")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share invite link")
    context.startActivity(shareIntent)
}

/**
 * Copies the invite link to the system clipboard.
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Tandem Invite Link", text)
    clipboardManager.setPrimaryClip(clip)
}
