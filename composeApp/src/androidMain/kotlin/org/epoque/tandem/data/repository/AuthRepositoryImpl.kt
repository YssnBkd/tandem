package org.epoque.tandem.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.epoque.tandem.domain.model.AuthError
import org.epoque.tandem.domain.model.AuthProvider
import org.epoque.tandem.domain.model.User
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState

/**
 * Supabase implementation of [AuthRepository].
 */
class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    private val auth: Auth
        get() = supabaseClient.auth

    override val authState: Flow<AuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = status.session.user?.toUser()
                if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.RefreshFailure -> AuthState.Error(
                message = "Session refresh failed. Please sign in again.",
                isRetryable = true
            )
        }
    }

    override val currentUser: User?
        get() = auth.currentUserOrNull()?.toUser()

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            auth.currentUserOrNull()?.toUser()
                ?: throw AuthError.Unknown("Failed to get user after sign in")
        }.mapAuthError()
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("display_name", JsonPrimitive(displayName))
                }
            }
            auth.currentUserOrNull()?.toUser()
                ?: throw AuthError.Unknown("Failed to get user after registration")
        }.mapAuthError()
    }

    override suspend fun signInWithGoogle(): Result<Unit> {
        return runCatching {
            auth.signInWith(Google)
        }.mapAuthError()
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            auth.signOut()
        }.mapAuthError()
    }

    override suspend fun refreshSession(): Result<User> {
        return runCatching {
            auth.refreshCurrentSession()
            auth.currentUserOrNull()?.toUser()
                ?: throw AuthError.Unknown("Failed to get user after refresh")
        }.mapAuthError()
    }

    private fun UserInfo.toUser(): User {
        val metadata = userMetadata
        return User(
            id = id,
            email = email ?: "",
            displayName = metadata?.get("display_name")?.toString()?.trim('"')
                ?: metadata?.get("full_name")?.toString()?.trim('"')
                ?: metadata?.get("name")?.toString()?.trim('"')
                ?: email?.substringBefore("@")
                ?: "User",
            avatarUrl = metadata?.get("avatar_url")?.toString()?.trim('"'),
            provider = when {
                appMetadata?.get("provider")?.toString()?.contains("google", ignoreCase = true) == true -> AuthProvider.GOOGLE
                else -> AuthProvider.EMAIL
            },
            createdAt = createdAt?.let { Instant.parse(it.toString()) } ?: Clock.System.now()
        )
    }

    private fun <T> Result<T>.mapAuthError(): Result<T> {
        return this.recoverCatching { throwable ->
            val message = throwable.message ?: "Unknown error"
            throw when {
                throwable is AuthError -> throwable
                message.contains("Invalid login credentials", ignoreCase = true) ->
                    AuthError.InvalidCredentials()
                message.contains("User already registered", ignoreCase = true) ||
                    message.contains("already exists", ignoreCase = true) ->
                    AuthError.EmailAlreadyExists()
                message.contains("network", ignoreCase = true) ||
                    message.contains("connection", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ->
                    AuthError.NetworkError()
                message.contains("rate limit", ignoreCase = true) ->
                    AuthError.RateLimited()
                message.contains("session", ignoreCase = true) &&
                    message.contains("expired", ignoreCase = true) ->
                    AuthError.SessionExpired()
                message.contains("cancelled", ignoreCase = true) ||
                    message.contains("canceled", ignoreCase = true) ->
                    AuthError.GoogleSignInCancelled()
                else -> AuthError.Unknown(message, throwable)
            }
        }
    }
}
