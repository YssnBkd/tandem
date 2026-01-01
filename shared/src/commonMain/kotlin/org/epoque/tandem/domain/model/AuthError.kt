package org.epoque.tandem.domain.model

/**
 * Sealed class representing authentication errors.
 * Extends Exception to allow throwing in Result operations.
 */
sealed class AuthError(
    override val message: String,
    open val isRetryable: Boolean = true
) : Exception(message) {
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
        override val cause: Throwable? = null
    ) : AuthError(message, isRetryable = true)
}
