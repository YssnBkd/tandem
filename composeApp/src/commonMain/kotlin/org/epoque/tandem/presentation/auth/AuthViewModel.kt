package org.epoque.tandem.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.epoque.tandem.domain.model.AuthError
import org.epoque.tandem.domain.model.ValidationResult
import org.epoque.tandem.domain.model.errorMessage
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.validation.DisplayNameValidator
import org.epoque.tandem.domain.validation.EmailValidator
import org.epoque.tandem.domain.validation.PasswordValidator

/**
 * ViewModel for authentication screens.
 * Handles sign-in, registration, and sign-out operations.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * UI state derived from the repository's auth state.
     */
    val uiState: StateFlow<AuthUiState> = authRepository.authState
        .map { authState ->
            when (authState) {
                is AuthState.Loading -> AuthUiState.Loading
                is AuthState.Authenticated -> AuthUiState.Authenticated(authState.user)
                is AuthState.Unauthenticated -> AuthUiState.Unauthenticated
                is AuthState.Error -> AuthUiState.Error(authState.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthUiState.Loading
        )

    private val _formState = MutableStateFlow(AuthFormState())

    /**
     * Form state for input fields and validation errors.
     */
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    /**
     * Handle authentication events from the UI.
     */
    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                _formState.update { it.copy(email = event.email, emailError = null) }
            }
            is AuthEvent.PasswordChanged -> {
                _formState.update { it.copy(password = event.password, passwordError = null) }
            }
            is AuthEvent.DisplayNameChanged -> {
                _formState.update { it.copy(displayName = event.displayName, displayNameError = null) }
            }
            is AuthEvent.SignInWithEmail -> signInWithEmail()
            is AuthEvent.RegisterWithEmail -> registerWithEmail()
            is AuthEvent.SignInWithGoogle -> signInWithGoogle()
            is AuthEvent.SetGoogleSignInLoading -> setGoogleSignInLoading(event.isLoading)
            is AuthEvent.SetGoogleSignInError -> setGoogleSignInError(event.message)
            is AuthEvent.SignOut -> signOut()
            is AuthEvent.ClearError -> clearError()
        }
    }

    private fun signInWithEmail() {
        val state = _formState.value

        // Validate inputs
        val emailResult = EmailValidator.validate(state.email)
        val passwordResult = PasswordValidator.validate(state.password)

        if (emailResult is ValidationResult.Error || passwordResult is ValidationResult.Error) {
            _formState.update {
                it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage
                )
            }
            return
        }

        _formState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.signInWithEmail(state.email, state.password)
                .onSuccess {
                    _formState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = (error as? AuthError)?.message ?: error.message
                        )
                    }
                }
        }
    }

    private fun registerWithEmail() {
        val state = _formState.value

        // Validate inputs
        val emailResult = EmailValidator.validate(state.email)
        val passwordResult = PasswordValidator.validate(state.password)
        val displayNameResult = DisplayNameValidator.validate(state.displayName)

        if (emailResult is ValidationResult.Error ||
            passwordResult is ValidationResult.Error ||
            displayNameResult is ValidationResult.Error
        ) {
            _formState.update {
                it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage,
                    displayNameError = displayNameResult.errorMessage
                )
            }
            return
        }

        _formState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.signUpWithEmail(state.email, state.password, state.displayName)
                .onSuccess {
                    _formState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = (error as? AuthError)?.message ?: error.message
                        )
                    }
                }
        }
    }

    private fun signInWithGoogle() {
        _formState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.signInWithGoogle()
                .onSuccess {
                    _formState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = (error as? AuthError)?.message ?: error.message
                        )
                    }
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
                .onFailure { error ->
                    _formState.update {
                        it.copy(
                            errorMessage = (error as? AuthError)?.message ?: error.message
                        )
                    }
                }
        }
    }

    private fun clearError() {
        _formState.update { it.copy(errorMessage = null) }
    }

    private fun setGoogleSignInLoading(isLoading: Boolean) {
        _formState.update { it.copy(isLoading = isLoading, errorMessage = null) }
    }

    private fun setGoogleSignInError(message: String) {
        _formState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
