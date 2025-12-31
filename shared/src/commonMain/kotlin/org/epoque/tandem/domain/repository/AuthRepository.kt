package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.User

/**
 * Sealed interface representing all possible authentication states.
 * Used by AuthViewModel to drive UI state.
 */
sealed interface AuthState {
    /** Initial state while checking stored session */
    data object Loading : AuthState

    /** User is authenticated with valid session */
    data class Authenticated(val user: User) : AuthState

    /** No valid session exists */
    data object Unauthenticated : AuthState

    /** Authentication error occurred */
    data class Error(
        val message: String,
        val isRetryable: Boolean = true
    ) : AuthState
}

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
