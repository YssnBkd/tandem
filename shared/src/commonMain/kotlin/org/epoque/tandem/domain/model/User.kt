package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a Tandem user.
 *
 * The User entity is the core identity model, containing essential
 * profile information retrieved from the authentication provider.
 */
data class User(
    /** Unique identifier from Supabase Auth (UUID format) */
    val id: String,

    /** User's email address (validated format) */
    val email: String,

    /** Display name shown in the app UI */
    val displayName: String,

    /** URL to user's avatar image (optional, from provider) */
    val avatarUrl: String? = null,

    /** Authentication provider used (EMAIL, GOOGLE) */
    val provider: AuthProvider,

    /** Timestamp of account creation */
    val createdAt: Instant
)
