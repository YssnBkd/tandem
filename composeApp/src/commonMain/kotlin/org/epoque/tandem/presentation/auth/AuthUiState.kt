package org.epoque.tandem.presentation.auth

import org.epoque.tandem.domain.model.User

/**
 * Represents the UI state for authentication screens.
 */
sealed interface AuthUiState {
    /** Initial loading state while checking session */
    data object Loading : AuthUiState

    /** User is not authenticated, show auth screens */
    data object Unauthenticated : AuthUiState

    /** User is authenticated */
    data class Authenticated(val user: User) : AuthUiState

    /** Error occurred during authentication */
    data class Error(val message: String) : AuthUiState
}

/**
 * Represents the state of authentication form fields.
 */
data class AuthFormState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val displayName: String = "",
    val displayNameError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isEmailValid: Boolean
        get() = emailError == null && email.isNotBlank()

    val isPasswordValid: Boolean
        get() = passwordError == null && password.isNotBlank()

    val isDisplayNameValid: Boolean
        get() = displayNameError == null && displayName.isNotBlank()

    val canSubmitSignIn: Boolean
        get() = isEmailValid && isPasswordValid && !isLoading

    val canSubmitRegister: Boolean
        get() = isEmailValid && isPasswordValid && isDisplayNameValid && !isLoading
}
