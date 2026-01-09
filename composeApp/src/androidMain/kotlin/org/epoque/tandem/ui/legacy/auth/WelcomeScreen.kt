package org.epoque.tandem.ui.legacy.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import org.epoque.tandem.presentation.auth.AuthEvent
import org.epoque.tandem.presentation.auth.AuthFormState
import org.epoque.tandem.presentation.auth.AuthViewModel
import org.koin.compose.koinInject

/**
 * Welcome screen with sign-in options.
 * Entry point for unauthenticated users.
 */
@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel,
    onNavigateToSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    composeAuth: ComposeAuth = koinInject()
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Native Google Sign-in using Credential Manager
    val googleSignInState = composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                NativeSignInResult.Success -> {
                    // Auth state flow handles navigation automatically
                    viewModel.onEvent(AuthEvent.SetGoogleSignInLoading(false))
                }
                NativeSignInResult.ClosedByUser -> {
                    viewModel.onEvent(AuthEvent.SetGoogleSignInLoading(false))
                }
                is NativeSignInResult.Error -> {
                    viewModel.onEvent(AuthEvent.SetGoogleSignInError(result.message))
                }
                is NativeSignInResult.NetworkError -> {
                    viewModel.onEvent(AuthEvent.SetGoogleSignInError(result.message))
                }
            }
        },
        fallback = {
            // Fallback to OAuth if native sign-in is unavailable
            viewModel.onEvent(AuthEvent.SignInWithGoogle)
        }
    )

    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(AuthEvent.ClearError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        WelcomeContent(
            formState = formState,
            onSignInWithGoogle = {
                viewModel.onEvent(AuthEvent.SetGoogleSignInLoading(true))
                googleSignInState.startFlow()
            },
            onNavigateToSignIn = onNavigateToSignIn,
            onNavigateToRegister = onNavigateToRegister,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun WelcomeContent(
    formState: AuthFormState,
    onSignInWithGoogle: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Icon
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App name
        Text(
            text = "Tandem",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tagline
        Text(
            text = "Weekly planning, together",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Loading indicator
        if (formState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sign in with Google button
        OutlinedButton(
            onClick = onSignInWithGoogle,
            enabled = !formState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continue with Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create account button
        Button(
            onClick = onNavigateToRegister,
            enabled = !formState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Already have account link
        TextButton(
            onClick = onNavigateToSignIn,
            enabled = !formState.isLoading
        ) {
            Text("Already have an account? Sign In")
        }
    }
}
