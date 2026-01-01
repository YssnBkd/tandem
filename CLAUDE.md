# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tandem is a Kotlin Multiplatform project targeting Android and iOS, built with Compose Multiplatform for shared UI.

## Build Commands

```bash
# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Compile Kotlin for Android (faster for type checking)
./gradlew :composeApp:compileDebugKotlinAndroid

# Run all unit tests
./gradlew :composeApp:testDebugUnitTest

# Run a specific test class
./gradlew :composeApp:testDebugUnitTest --tests "org.epoque.tandem.domain.validation.EmailValidatorTest"

# Clean build
./gradlew clean
```

For iOS, open `/iosApp` in Xcode and run from there.

## Architecture

### Module Structure

- **composeApp**: Main application module with platform-specific implementations
  - `commonMain`: Shared Kotlin code (ViewModels, UI state)
  - `androidMain`: Android-specific code (UI screens, DI, repository implementations)
  - `commonTest`: Shared unit tests

- **shared**: Pure Kotlin library shared across all platforms
  - Domain models (`User`, `AuthError`, `ValidationResult`)
  - Repository interfaces (`AuthRepository`)
  - Validation logic (`EmailValidator`, `PasswordValidator`, `DisplayNameValidator`)

### Key Patterns

**Dependency Injection**: Koin modules defined in `composeApp/src/androidMain/kotlin/org/epoque/tandem/di/`
- `AppModule.kt`: Supabase client configuration
- `AuthModule.kt`: Auth repository and ViewModel bindings

**State Management**: Unidirectional data flow
- ViewModels in `presentation/` expose `UiState` and handle `Event`s
- Example: `AuthViewModel` with `AuthUiState` and `AuthEvent`

**Navigation**: Jetpack Navigation Compose with nav graphs in `ui/navigation/`

## Configuration

Secrets are loaded from `local.properties` (not committed):
```properties
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_anon_key
GOOGLE_WEB_CLIENT_ID=your_google_client_id
```

## Dependencies

Key libraries (versions in `gradle/libs.versions.toml`):
- Kotlin 2.3.0, Compose Multiplatform 1.9.3
- Supabase 3.1.4 (auth, compose-auth)
- Koin 4.0.4 (DI)
- Ktor 3.1.3 (networking)
- kotlinx-coroutines, kotlinx-serialization, kotlinx-datetime
