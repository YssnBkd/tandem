# Contract: AuthRepository

**Feature**: 001-core-infrastructure
**Date**: 2025-12-31
**Type**: Repository Interface

## Overview

The AuthRepository defines the contract for authentication operations in the Tandem application. It abstracts the Supabase Auth SDK to enable testability and potential provider switching.

---

## Interface Definition

```kotlin
package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.User
import org.epoque.tandem.domain.model.AuthState

/**
 * Repository interface for authentication operations.
 *
 * This interface defines the contract for all authentication-related
 * operations in the Tandem application. Implementations handle the
 * specific authentication provider (Supabase).
 */
interface AuthRepository {

    /**
     * Observable authentication state.
     *
     * Emits the current authentication state whenever it changes.
     * UI should collect this flow to reactively update based on auth status.
     *
     * @return Flow of [AuthState] representing current auth status
     */
    val authState: Flow<AuthState>

    /**
     * Current authenticated user, if any.
     *
     * Returns the currently authenticated user or null if not authenticated.
     * This is a synchronous check against the cached session.
     *
     * @return Current [User] or null
     */
    val currentUser: User?

    /**
     * Sign in with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return [Result] containing [User] on success or error on failure
     * @throws IllegalArgumentException if email or password is blank
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /**
     * Register a new account with email and password.
     *
     * @param email User's email address
     * @param password User's password (minimum 8 characters)
     * @param displayName User's display name
     * @return [Result] containing [User] on success or error on failure
     * @throws IllegalArgumentException if any parameter is invalid
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<User>

    /**
     * Initiate Google Sign-In flow.
     *
     * This triggers the native Google credential picker on Android.
     * The result is delivered via the [authState] flow.
     *
     * @return [Result] indicating whether the flow was initiated successfully
     */
    suspend fun signInWithGoogle(): Result<Unit>

    /**
     * Sign out the current user.
     *
     * Clears the session and triggers [AuthState.Unauthenticated] emission.
     *
     * @return [Result] indicating success or failure
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Refresh the current session.
     *
     * Attempts to refresh the authentication token. Called automatically
     * by the SDK, but can be triggered manually if needed.
     *
     * @return [Result] containing refreshed [User] or error
     */
    suspend fun refreshSession(): Result<User>
}
```

---

## Error Handling

### AuthError Sealed Class

```kotlin
package org.epoque.tandem.domain.model

/**
 * Sealed class representing authentication errors.
 */
sealed class AuthError(
    open val message: String,
    open val isRetryable: Boolean = true
) {
    /** Invalid email or password */
    data class InvalidCredentials(
        override val message: String = "Incorrect email or password"
    ) : AuthError(message, isRetryable = true)

    /** Email already registered */
    data class EmailAlreadyExists(
        override val message: String = "An account with this email already exists"
    ) : AuthError(message, isRetryable = false)

    /** Network error */
    data class NetworkError(
        override val message: String = "Unable to connect. Check your internet connection."
    ) : AuthError(message, isRetryable = true)

    /** Rate limited */
    data class RateLimited(
        override val message: String = "Too many attempts. Please try again later.",
        val retryAfterSeconds: Int? = null
    ) : AuthError(message, isRetryable = true)

    /** Session expired */
    data class SessionExpired(
        override val message: String = "Your session has expired. Please sign in again."
    ) : AuthError(message, isRetryable = false)

    /** Google Sign-In cancelled by user */
    data class GoogleSignInCancelled(
        override val message: String = "Sign in was cancelled"
    ) : AuthError(message, isRetryable = true)

    /** Unknown error */
    data class Unknown(
        override val message: String = "Something went wrong. Please try again.",
        val cause: Throwable? = null
    ) : AuthError(message, isRetryable = true)
}
```

---

## Result Extension

```kotlin
/**
 * Extension to convert Result to AuthError-aware handling.
 */
inline fun <T> Result<T>.onAuthError(action: (AuthError) -> Unit): Result<T> {
    exceptionOrNull()?.let { error ->
        when (error) {
            is AuthError -> action(error)
            else -> action(AuthError.Unknown(error.message ?: "Unknown error", error))
        }
    }
    return this
}
```

---

## Implementation Notes

### Supabase Mapping

| AuthRepository Method | Supabase SDK Method |
|-----------------------|---------------------|
| `signInWithEmail` | `auth.signInWith(Email) { ... }` |
| `signUpWithEmail` | `auth.signUpWith(Email) { ... }` |
| `signInWithGoogle` | `composeAuth.loginWithGoogle()` |
| `signOut` | `auth.signOut()` |
| `refreshSession` | `auth.refreshCurrentSession()` |
| `authState` | `auth.sessionStatus.map { ... }` |
| `currentUser` | `auth.currentUserOrNull()?.toUser()` |

### Session Status Mapping

| Supabase SessionStatus | AuthState |
|------------------------|-----------|
| `SessionStatus.LoadingFromStorage` | `AuthState.Loading` |
| `SessionStatus.Authenticated` | `AuthState.Authenticated(user)` |
| `SessionStatus.NotAuthenticated` | `AuthState.Unauthenticated` |
| `SessionStatus.NetworkError` | `AuthState.Error(message)` |

---

## Usage Examples

### Observing Auth State

```kotlin
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )
}
```

### Sign In

```kotlin
suspend fun signIn(email: String, password: String) {
    authRepository.signInWithEmail(email, password)
        .onSuccess { user ->
            // Navigate to main screen
        }
        .onAuthError { error ->
            _uiState.update { it.copy(error = error.message) }
        }
}
```

### Sign Up

```kotlin
suspend fun signUp(email: String, password: String, displayName: String) {
    authRepository.signUpWithEmail(email, password, displayName)
        .onSuccess { user ->
            // Navigate to main screen
        }
        .onAuthError { error ->
            _uiState.update { it.copy(error = error.message) }
        }
}
```

---

## Testing Contract

```kotlin
interface AuthRepository {
    // Test scenarios that implementations must handle:

    // 1. Sign in with valid credentials → returns User
    // 2. Sign in with invalid credentials → returns InvalidCredentials error
    // 3. Sign in with network error → returns NetworkError error
    // 4. Sign up with new email → returns User
    // 5. Sign up with existing email → returns EmailAlreadyExists error
    // 6. Sign out → clears session, emits Unauthenticated
    // 7. Session expired → emits Unauthenticated after refresh fails
    // 8. Google sign in cancelled → returns GoogleSignInCancelled error
}
```

---

## Dependencies

- `kotlinx.coroutines.flow.Flow` - For reactive state
- `kotlin.Result` - For operation results
- Supabase Auth SDK (implementation only)
