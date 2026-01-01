package org.epoque.tandem.presentation.auth

/**
 * Events that can be triggered from authentication UI.
 */
sealed interface AuthEvent {
    /** Update email field */
    data class EmailChanged(val email: String) : AuthEvent

    /** Update password field */
    data class PasswordChanged(val password: String) : AuthEvent

    /** Update display name field */
    data class DisplayNameChanged(val displayName: String) : AuthEvent

    /** Submit sign in with email/password */
    data object SignInWithEmail : AuthEvent

    /** Submit registration with email/password */
    data object RegisterWithEmail : AuthEvent

    /** Initiate Google Sign-In (OAuth fallback) */
    data object SignInWithGoogle : AuthEvent

    /** Set loading state for native Google Sign-in */
    data class SetGoogleSignInLoading(val isLoading: Boolean) : AuthEvent

    /** Set error from native Google Sign-in */
    data class SetGoogleSignInError(val message: String) : AuthEvent

    /** Sign out current user */
    data object SignOut : AuthEvent

    /** Clear current error message */
    data object ClearError : AuthEvent
}
