# Data Model: Core Infrastructure

**Feature**: 001-core-infrastructure
**Date**: 2025-12-31
**Status**: Complete

## Overview

This document defines the data entities, their relationships, and validation rules for the Core Infrastructure feature.

---

## Entities

### 1. User

Represents a registered user in the Tandem application.

```kotlin
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

enum class AuthProvider {
    EMAIL,
    GOOGLE
}
```

#### Field Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| id | Non-empty UUID format | "Invalid user ID" |
| email | Valid email format (RFC 5322) | "Invalid email address" |
| displayName | 1-100 characters, non-blank | "Display name is required" |
| avatarUrl | Valid URL or null | "Invalid avatar URL" |

#### Data Source
- **Primary**: Supabase Auth `session.user` object
- **Persistence**: Not stored locally (fetched from session)

---

### 2. AuthState

Represents the current authentication state of the application.

```kotlin
/**
 * Sealed interface representing all possible authentication states.
 * Used by AuthViewModel to drive UI state.
 */
sealed interface AuthState {
    /** Initial state while checking stored session */
    data object Loading : AuthState

    /** User is authenticated with valid session */
    data class Authenticated(
        val user: User,
        val sessionExpiresAt: Instant
    ) : AuthState

    /** No valid session exists */
    data object Unauthenticated : AuthState

    /** Authentication error occurred */
    data class Error(
        val message: String,
        val isRetryable: Boolean = true
    ) : AuthState
}
```

#### State Transitions

```
┌─────────────────────────────────────────────────────────────┐
│                         Loading                              │
│                      (app startup)                           │
└─────────────────────────────────────────────────────────────┘
                            │
         ┌──────────────────┼──────────────────┐
         │                  │                  │
         ▼                  ▼                  ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Authenticated   │ │ Unauthenticated │ │     Error       │
│ (session valid) │ │ (no session)    │ │ (network/other) │
└─────────────────┘ └─────────────────┘ └─────────────────┘
         │                  │                  │
         │                  │                  │
         ▼                  ▼                  ▼
    ┌─────────┐        ┌─────────┐        ┌─────────┐
    │ Sign Out│        │ Sign In │        │  Retry  │
    └─────────┘        └─────────┘        └─────────┘
         │                  │                  │
         │                  │                  │
         ▼                  ▼                  ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Unauthenticated │ │ Authenticated   │ │   Loading       │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

---

### 3. NavigationTab

Represents the main navigation destinations.

```kotlin
/**
 * Sealed class representing main navigation tabs.
 * Used for bottom navigation state management.
 */
sealed class NavigationTab(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Week : NavigationTab(
        route = "week",
        title = "Week",
        icon = Icons.Outlined.CalendarToday,
        selectedIcon = Icons.Filled.CalendarToday
    )

    data object Progress : NavigationTab(
        route = "progress",
        title = "Progress",
        icon = Icons.Outlined.TrendingUp,
        selectedIcon = Icons.Filled.TrendingUp
    )

    data object Goals : NavigationTab(
        route = "goals",
        title = "Goals",
        icon = Icons.Outlined.Flag,
        selectedIcon = Icons.Filled.Flag
    )

    companion object {
        val DEFAULT = Week
        val ALL = listOf(Week, Progress, Goals)
    }
}
```

---

### 4. Persisted Preferences

Key-value pairs stored in DataStore for app state persistence.

```kotlin
/**
 * Preference keys for DataStore persistence.
 */
object PreferencesKeys {
    /** Cached user ID for quick session validation */
    val USER_ID = stringPreferencesKey("user_id")

    /** Cached display name for offline display */
    val DISPLAY_NAME = stringPreferencesKey("display_name")

    /** Cached email for offline display */
    val EMAIL = stringPreferencesKey("email")

    /** Last selected navigation tab index (0=Week, 1=Progress, 2=Goals) */
    val LAST_TAB_INDEX = intPreferencesKey("last_tab_index")
}
```

#### Persistence Rules

| Key | When Written | When Read | Cleared |
|-----|--------------|-----------|---------|
| USER_ID | On successful auth | App startup | On sign out |
| DISPLAY_NAME | On successful auth | App startup | On sign out |
| EMAIL | On successful auth | App startup | On sign out |
| LAST_TAB_INDEX | On tab change | After auth | Never (preserved) |

---

## Relationships

```
┌─────────────────────────────────────────────────────────────┐
│                      Supabase Auth                           │
│                    (External Service)                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ provides session
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                         User                                 │
│  - id (from Supabase)                                       │
│  - email                                                     │
│  - displayName                                               │
│  - provider                                                  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ drives
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       AuthState                              │
│  - Loading → Authenticated(user) / Unauthenticated / Error  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ determines
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Navigation                               │
│  AuthState.Authenticated → MainGraph (NavigationTabs)       │
│  AuthState.Unauthenticated → AuthGraph (Welcome/SignIn)     │
└─────────────────────────────────────────────────────────────┘
```

---

## Data Flow

### Authentication Flow

```
1. App Launch
   └── Check Supabase session status
       ├── Session exists and valid
       │   └── Emit AuthState.Authenticated(user)
       │       └── Navigate to MainGraph
       │           └── Show last selected tab (or Week)
       ├── Session expired
       │   └── Attempt refresh
       │       ├── Refresh successful → Authenticated
       │       └── Refresh failed → Unauthenticated
       └── No session
           └── Emit AuthState.Unauthenticated
               └── Navigate to AuthGraph (Welcome)

2. Sign In (Email)
   └── Validate input (email format, password length)
       ├── Invalid → Show validation error
       └── Valid → Call Supabase signIn
           ├── Success → Emit Authenticated, persist user info
           └── Failure → Emit Error(message)

3. Sign In (Google)
   └── Launch native Google credential picker
       ├── User cancels → No state change
       └── User selects account → Exchange token with Supabase
           ├── Success → Emit Authenticated
           └── Failure → Emit Error(message)

4. Sign Out
   └── Call Supabase signOut
       └── Clear persisted preferences
           └── Emit Unauthenticated
               └── Navigate to AuthGraph
```

### Navigation State Flow

```
1. Tab Selection
   └── User taps navigation tab
       └── Update UI state (selectedTab)
           └── Persist tab index to DataStore
               └── Navigate to tab content

2. Configuration Change (rotation)
   └── Activity recreated
       └── ViewModel retained (survives config change)
           └── UI state restored from ViewModel
               └── Same tab displayed

3. Process Death
   └── App killed by system
       └── On restart: Load tab index from DataStore
           └── Restore last selected tab
```

---

## Validation Schemas

### Email Validation

```kotlin
object EmailValidator {
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    fun validate(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !EMAIL_REGEX.matches(email) -> ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Valid
        }
    }
}
```

### Password Validation

```kotlin
object PasswordValidator {
    const val MIN_LENGTH = 8

    fun validate(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < MIN_LENGTH -> ValidationResult.Error(
                "Password must be at least $MIN_LENGTH characters"
            )
            else -> ValidationResult.Valid
        }
    }
}
```

### Display Name Validation

```kotlin
object DisplayNameValidator {
    const val MIN_LENGTH = 1
    const val MAX_LENGTH = 100

    fun validate(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Display name is required")
            name.length > MAX_LENGTH -> ValidationResult.Error(
                "Display name must be $MAX_LENGTH characters or less"
            )
            else -> ValidationResult.Valid
        }
    }
}
```

### Validation Result

```kotlin
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Error(val message: String) : ValidationResult
}
```

---

## Error Messages

### Authentication Errors

| Error Code | User Message | Retryable |
|------------|--------------|-----------|
| invalid_credentials | "Incorrect email or password" | Yes |
| email_not_confirmed | "Please verify your email address" | No |
| user_already_exists | "An account with this email already exists" | No |
| network_error | "Unable to connect. Check your internet connection." | Yes |
| rate_limited | "Too many attempts. Please try again later." | Yes (with delay) |
| unknown | "Something went wrong. Please try again." | Yes |

### Validation Errors

| Field | Error Message |
|-------|---------------|
| email_required | "Email is required" |
| email_invalid | "Invalid email format" |
| password_required | "Password is required" |
| password_too_short | "Password must be at least 8 characters" |
| name_required | "Display name is required" |
| name_too_long | "Display name must be 100 characters or less" |

---

## Notes

1. **No local user database**: User data is fetched from Supabase session, not stored locally in SQLDelight. DataStore only caches essential fields for offline display.

2. **Session management**: Supabase SDK handles token refresh automatically. App only needs to observe session status.

3. **Partner data**: Not included in this feature. Partner relationship will be added in Feature 006.

4. **Theme preference**: Follows system setting only in v1.0. User override preference deferred to v1.1.
