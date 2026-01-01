# Implementation Plan: Core Infrastructure

**Branch**: `001-core-infrastructure` | **Date**: 2025-12-31 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-core-infrastructure/spec.md`

## Summary

Set up the foundational architecture for Tandem including authentication (email/password + Google Sign-In), navigation shell (bottom navigation with Week/Progress/Goals tabs), dependency injection (Koin), and theming (Material Design 3 with light/dark mode support). This feature establishes the patterns and infrastructure for all subsequent features.

## Technical Context

**Language/Version**: Kotlin 2.3.0 (Kotlin Multiplatform)
**Primary Dependencies**:
- Compose Multiplatform 1.9.3
- Supabase Auth (Android SDK) - for authentication
- Jetpack Navigation Compose - type-safe routes
- Koin - dependency injection
- DataStore - auth state persistence
- Kotlin Coroutines + Flow - async operations

**Storage**: DataStore (auth tokens, preferences)
**Testing**: Kotlin Test (unit), Android Instrumented Tests (UI)
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: 60 fps UI, <300ms tab switching, <500ms theme changes
**Constraints**: Offline-capable navigation (auth required online), Material Design 3 compliance
**Scale/Scope**: Individual users initially, partner features in later features

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [x] **Relationship-First Design**: Core Infrastructure is foundational - authentication and navigation do not introduce surveillance or control dynamics. This feature enables individual user access only.
- [x] **Weekly Rhythm**: Navigation shell includes Week tab as default destination, centering the app on weekly planning.
- [x] **Autonomous Partnership**: N/A for this feature - no partner interactions implemented yet. Future features will build on this foundation.
- [x] **Celebration Over Judgment**: N/A for this feature - no task status or outcomes displayed. UI copy will use neutral language (Sign In, Create Account).
- [x] **Intentional Simplicity**: Feature scope is minimal - only authentication, navigation, and theming. No task management complexity.

### Decision Framework

1. Does it strengthen the weekly rhythm? **YES** - Week tab is default destination
2. Does it respect partner autonomy? **N/A** - No partner features in this scope
3. Is it the simplest solution that works? **YES** - Standard auth patterns, navigation, theming
4. Can it work offline? **PARTIAL** - Navigation works offline; authentication requires network
5. Does it follow Material Design 3 patterns? **YES** - Using MD3 components, dynamic colors, theming

### Non-Negotiables Check

- [x] NO tracking of partner's incomplete tasks - N/A (no partner features)
- [x] NO notifications for partner's task completions - N/A (no notifications)
- [x] NO assigning tasks without acceptance workflow - N/A (no task features)
- [x] NO shame language in UI copy - Using neutral: "Sign In", "Create Account", "Error"
- [x] NO complex task hierarchies - N/A (no task features)

### Technical Compliance

- [x] Clean Architecture with MVI pattern - ViewModels with UI State + Events
- [x] Domain layer is 100% shared code (Kotlin Multiplatform) - User model, AuthRepository interface in shared
- [x] UI uses Jetpack Compose with Material Design 3 - Using compose.material3
- [x] Offline-first architecture with SQLDelight - N/A for auth (DataStore for token persistence)
- [x] Build validation: `:composeApp:compileDebugKotlinAndroid` succeeds

## Project Structure

### Documentation (this feature)

```text
specs/001-core-infrastructure/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (auth API contracts)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

**Module Structure for Core Infrastructure:**

```text
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/
│   │   └── User.kt                    # User entity (id, email, displayName)
│   └── repository/
│       └── AuthRepository.kt          # Auth repository interface
└── data/
    └── repository/
        └── AuthRepositoryImpl.kt      # Supabase auth implementation

composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/
│   ├── theme/
│   │   ├── Theme.kt                   # MD3 theme with dynamic colors
│   │   ├── Color.kt                   # Brand colors + light/dark palettes
│   │   └── Type.kt                    # Typography scale
│   ├── navigation/
│   │   ├── TandemNavHost.kt           # Main NavHost
│   │   ├── AuthNavGraph.kt            # Welcome, SignIn, Register routes
│   │   ├── MainNavGraph.kt            # BottomNav with Week, Progress, Goals
│   │   └── Routes.kt                  # Type-safe route definitions
│   ├── auth/
│   │   ├── WelcomeScreen.kt           # Landing with sign-in options
│   │   ├── SignInScreen.kt            # Email/password sign-in
│   │   ├── RegisterScreen.kt          # Account creation
│   │   └── AuthViewModel.kt           # Auth state management
│   └── main/
│       ├── MainScreen.kt              # Scaffold with BottomNav
│       ├── WeekScreen.kt              # Placeholder for Week tab
│       ├── ProgressScreen.kt          # Placeholder for Progress tab
│       └── GoalsScreen.kt             # Placeholder for Goals tab
├── di/
│   ├── AppModule.kt                   # Koin root module
│   └── AuthModule.kt                  # Auth-specific dependencies
└── TandemApp.kt                       # Application class with Koin init

composeApp/src/commonMain/kotlin/org/epoque/tandem/
├── presentation/
│   └── auth/
│       ├── AuthUiState.kt             # Sealed interface for auth states
│       └── AuthEvent.kt               # Auth-related events
```

**Build Validation**: `:composeApp:compileDebugKotlinAndroid`

## Complexity Tracking

No constitution violations requiring justification. All decisions follow the simplest solution principle.

## Dependencies to Add

The following dependencies need to be added to `gradle/libs.versions.toml` and `composeApp/build.gradle.kts`:

```toml
# libs.versions.toml additions
[versions]
supabase = "3.1.4"
koin = "4.0.4"
navigation = "2.9.0-alpha10"
datastore = "1.1.7"
ktor = "3.1.3"

[libraries]
# Supabase
supabase-gotrue = { module = "io.github.jan-tennert.supabase:gotrue-kt", version.ref = "supabase" }
supabase-compose-auth = { module = "io.github.jan-tennert.supabase:compose-auth", version.ref = "supabase" }
supabase-compose-auth-ui = { module = "io.github.jan-tennert.supabase:compose-auth-ui", version.ref = "supabase" }

# Ktor (required by Supabase)
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }

# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }

# Navigation
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
```

## Authentication Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        App Launch                                │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Check DataStore    │
                    │  for auth token     │
                    └─────────────────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                  │
              ▼                                  ▼
    ┌─────────────────┐              ┌─────────────────────┐
    │  Token exists   │              │   No token found    │
    └─────────────────┘              └─────────────────────┘
              │                                  │
              ▼                                  ▼
    ┌─────────────────┐              ┌─────────────────────┐
    │ Validate with   │              │   WelcomeScreen     │
    │   Supabase      │              │  (Sign In options)  │
    └─────────────────┘              └─────────────────────┘
              │                                  │
      ┌───────┴───────┐                          │
      │               │                          │
      ▼               ▼                          ▼
┌──────────┐   ┌──────────────┐        ┌─────────────────┐
│  Valid   │   │   Invalid    │        │ Email/Password  │
│  Token   │   │ (expired)    │        │ or Google Auth  │
└──────────┘   └──────────────┘        └─────────────────┘
      │               │                          │
      │               └──────────────────────────┘
      │                                          │
      ▼                                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      MainScreen                                  │
│  ┌─────────┐  ┌──────────┐  ┌─────────┐                        │
│  │  Week   │  │ Progress │  │  Goals  │   ← BottomNavigation   │
│  │ (start) │  │          │  │         │                        │
│  └─────────┘  └──────────┘  └─────────┘                        │
└─────────────────────────────────────────────────────────────────┘
```

## Navigation Structure

```
NavHost
├── AuthGraph (startDestination when unauthenticated)
│   ├── Welcome
│   ├── SignIn
│   └── Register
│
└── MainGraph (startDestination when authenticated)
    └── MainScreen (Scaffold with BottomNavigation)
        ├── Week (default tab)
        ├── Progress
        └── Goals
```

## State Management

### AuthViewModel State

```kotlin
sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface AuthEvent {
    data class SignInWithEmail(val email: String, val password: String) : AuthEvent
    data class RegisterWithEmail(val email: String, val password: String, val displayName: String) : AuthEvent
    data object SignInWithGoogle : AuthEvent
    data object SignOut : AuthEvent
    data object ClearError : AuthEvent
}
```

## Files to Create (Estimated: 18-22 files)

| Layer | File | Purpose |
|-------|------|---------|
| Domain | `shared/.../domain/model/User.kt` | User entity |
| Domain | `shared/.../domain/repository/AuthRepository.kt` | Auth interface |
| Data | `shared/.../data/repository/AuthRepositoryImpl.kt` | Supabase implementation |
| Presentation | `composeApp/.../presentation/auth/AuthUiState.kt` | UI state sealed class |
| Presentation | `composeApp/.../presentation/auth/AuthEvent.kt` | Auth events |
| UI | `composeApp/.../ui/theme/Theme.kt` | MD3 theme |
| UI | `composeApp/.../ui/theme/Color.kt` | Color definitions |
| UI | `composeApp/.../ui/theme/Type.kt` | Typography |
| UI | `composeApp/.../ui/navigation/Routes.kt` | Route definitions |
| UI | `composeApp/.../ui/navigation/TandemNavHost.kt` | Main nav host |
| UI | `composeApp/.../ui/navigation/AuthNavGraph.kt` | Auth routes |
| UI | `composeApp/.../ui/navigation/MainNavGraph.kt` | Main routes |
| UI | `composeApp/.../ui/auth/WelcomeScreen.kt` | Welcome screen |
| UI | `composeApp/.../ui/auth/SignInScreen.kt` | Sign in form |
| UI | `composeApp/.../ui/auth/RegisterScreen.kt` | Register form |
| UI | `composeApp/.../ui/auth/AuthViewModel.kt` | Auth ViewModel |
| UI | `composeApp/.../ui/main/MainScreen.kt` | Main scaffold |
| UI | `composeApp/.../ui/main/WeekScreen.kt` | Week placeholder |
| UI | `composeApp/.../ui/main/ProgressScreen.kt` | Progress placeholder |
| UI | `composeApp/.../ui/main/GoalsScreen.kt` | Goals placeholder |
| DI | `composeApp/.../di/AppModule.kt` | Koin app module |
| DI | `composeApp/.../di/AuthModule.kt` | Auth module |
| App | `composeApp/.../TandemApp.kt` | Application class |

## Next Steps

1. **Phase 0**: Generate `research.md` with decisions on:
   - Supabase SDK integration patterns for KMP
   - Navigation Compose type-safe routes setup
   - DataStore integration for auth persistence
   - Google Sign-In configuration

2. **Phase 1**: Generate design artifacts:
   - `data-model.md` - User entity, session model
   - `contracts/` - Supabase auth API contracts
   - `quickstart.md` - Setup instructions

3. **Phase 2** (`/speckit.tasks`): Generate implementation tasks
