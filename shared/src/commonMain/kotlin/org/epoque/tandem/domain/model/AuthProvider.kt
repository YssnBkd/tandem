package org.epoque.tandem.domain.model

/**
 * Authentication providers supported by the application.
 */
enum class AuthProvider {
    /** Email and password authentication */
    EMAIL,
    /** Google OAuth authentication */
    GOOGLE
}
