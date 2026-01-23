package org.epoque.tandem.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.epoque.tandem.presentation.auth.AuthViewModel
import org.epoque.tandem.ui.legacy.auth.RegisterScreen
import org.epoque.tandem.ui.legacy.auth.SignInScreen
import org.epoque.tandem.ui.legacy.auth.WelcomeScreen

/**
 * Navigation graph for authentication screens.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    composable<Routes.Auth.Welcome> {
        WelcomeScreen(
            viewModel = authViewModel,
            onNavigateToSignIn = {
                navController.navigate(Routes.Auth.SignIn)
            },
            onNavigateToRegister = {
                navController.navigate(Routes.Auth.Register)
            }
        )
    }

    composable<Routes.Auth.SignIn> {
        SignInScreen(
            viewModel = authViewModel,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToRegister = {
                navController.navigate(Routes.Auth.Register) {
                    popUpTo(Routes.Auth.Welcome)
                }
            }
        )
    }

    composable<Routes.Auth.Register> {
        RegisterScreen(
            viewModel = authViewModel,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToSignIn = {
                navController.navigate(Routes.Auth.SignIn) {
                    popUpTo(Routes.Auth.Welcome)
                }
            }
        )
    }
}
