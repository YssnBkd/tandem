# Research: Core Infrastructure

**Feature**: 001-core-infrastructure
**Date**: 2025-12-31
**Status**: Complete

## Summary

This document consolidates research findings for the Core Infrastructure feature, resolving all technical decisions needed for implementation.

---

## 1. Supabase Kotlin SDK

### Decision
Use **supabase-kt v3.1.4** with the Compose Auth plugin for native authentication flows.

### Rationale
- Full Kotlin Multiplatform support (Android, iOS, JVM, WASM)
- Native Google Sign-In integration via Compose Auth plugin
- Session auto-refresh and persistence built-in
- Active community maintenance with regular releases
- Aligns with project's KMP architecture

### Alternatives Considered
| Alternative | Rejected Because |
|-------------|------------------|
| Firebase Auth | Not KMP-compatible, would require expect/actual for each platform |
| Custom JWT auth | Over-engineering for MVP, Supabase provides backend |
| AppAuth/OAuth directly | More complex setup, Supabase abstracts this |

### Key Configuration

```kotlin
// Client setup pattern
val supabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Auth) {
        autoRefreshToken = true
        alwaysAutoRefresh = true
    }
    install(ComposeAuth) {
        nativeGoogleLogin(BuildConfig.GOOGLE_WEB_CLIENT_ID)
    }
}
```

### Dependencies Required
```toml
[versions]
supabase = "3.1.4"
ktor = "3.1.3"

[libraries]
supabase-bom = { module = "io.github.jan-tennert.supabase:bom", version.ref = "supabase" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt" }
supabase-compose-auth = { module = "io.github.jan-tennert.supabase:compose-auth" }
supabase-compose-auth-ui = { module = "io.github.jan-tennert.supabase:compose-auth-ui" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
```

### Gotchas
1. Module renamed from `gotrue-kt` to `auth-kt` in v3.0.0+
2. Requires Kotlin 2.1.0+ (project has 2.3.0)
3. Requires Ktor 3.0.0+ (use 3.1.3)
4. Use Web Client ID in code, not Android Client ID
5. Session persistence is automatic via internal storage

---

## 2. Jetpack Navigation Compose Type-Safe Routes

### Decision
Use **Navigation Compose 2.8.x** with `@Serializable` route definitions using kotlinx.serialization.

### Rationale
- Type-safe navigation eliminates string-based route errors
- Kotlin serialization integration is stable (since 2.8.0)
- Aligns with project's Kotlin-first approach
- Better IDE support and compile-time safety
- Official Google-supported approach

### Alternatives Considered
| Alternative | Rejected Because |
|-------------|------------------|
| String-based routes | Type-unsafe, prone to runtime errors |
| Voyager | Third-party, less documentation, uncertain long-term support |
| Decompose | Over-complex for current needs, primarily for deep nesting |

### Implementation Pattern

```kotlin
// Route definitions
@Serializable object Welcome
@Serializable object SignIn
@Serializable object Register
@Serializable object Main

// Navigation graph
NavHost(
    navController = navController,
    startDestination = if (isAuthenticated) Main else Welcome
) {
    composable<Welcome> { WelcomeScreen(...) }
    composable<SignIn> { SignInScreen(...) }
    composable<Register> { RegisterScreen(...) }
    composable<Main> { MainScreen(...) }
}

// Type-safe navigation
navController.navigate(SignIn) {
    popUpTo(Welcome) { inclusive = true }
}
```

### Dependencies Required
```toml
[versions]
navigation = "2.8.5"
serialization = "1.7.3"

[libraries]
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
```

### Gotchas
1. Requires `kotlin.plugin.serialization` plugin
2. All route types must be `@Serializable`
3. Complex types need custom NavType implementation
4. Pass IDs, not full objects (follow REST patterns)

---

## 3. DataStore in KMP

### Decision
Use **DataStore Preferences 1.1.1** in commonMain with platform-specific path providers.

### Rationale
- Official KMP support since 1.1.0
- Simple key-value storage sufficient for auth tokens
- Coroutines Flow integration aligns with architecture
- Type-safe preference keys

### Alternatives Considered
| Alternative | Rejected Because |
|-------------|------------------|
| SharedPreferences (Android) | Not KMP-compatible, platform-specific |
| NSUserDefaults (iOS) | Not KMP-compatible, platform-specific |
| SQLDelight | Over-engineering for simple key-value storage |
| EncryptedSharedPreferences | Not KMP-compatible, v1.1 consideration |

### Implementation Pattern

```kotlin
// commonMain - Factory
internal const val DATA_STORE_FILE_NAME = "tandem_preferences.preferences_pb"

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

object PreferencesKeys {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val USER_ID = stringPreferencesKey("user_id")
    val DISPLAY_NAME = stringPreferencesKey("display_name")
}

// androidMain
fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
)
```

### Dependencies Required
```toml
[versions]
datastore = "1.1.1"

[libraries]
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
```

### Gotchas
1. Preferences DataStore only (Proto not directly supported in KMP)
2. No WASM/JS support (not needed for mobile)
3. Android requires Context for file path
4. Must be singleton to avoid file corruption
5. Use Koin `single {}` scope

---

## 4. Google Sign-In with Supabase

### Decision
Use **Compose Auth plugin** with native Google login flow, falling back to OAuth if needed.

### Rationale
- Best UX with native credential picker
- Supabase handles token exchange
- Single-tap sign-in support
- Automatic session management

### Alternatives Considered
| Alternative | Rejected Because |
|-------------|------------------|
| Manual Credential Manager | More complex, plugin handles this |
| OAuth web flow only | Worse UX, opens browser |
| Firebase Auth + Supabase | Redundant auth systems |

### Configuration Requirements

1. **Google Cloud Console**:
   - Create Web Application OAuth client → Get Web Client ID
   - Create Android OAuth client → Add SHA-1 fingerprints
   - Configure OAuth consent screen

2. **Supabase Dashboard**:
   - Enable Google provider in Authentication > Providers
   - Add Web Client ID and Client Secret
   - Add Android Client ID to authorized clients

3. **Android App**:
   - Add Web Client ID to BuildConfig/secrets
   - Configure Supabase client with ComposeAuth plugin

### Implementation Pattern

```kotlin
// In Composable
val authState = supabaseClient.composeAuth.rememberLoginWithGoogle(
    onResult = { result ->
        when (result) {
            NativeSignInResult.Success -> onSuccess()
            NativeSignInResult.ClosedByUser -> { /* Cancelled */ }
            is NativeSignInResult.Error -> onError(result.message)
            is NativeSignInResult.NetworkError -> onError("Network error")
        }
    },
    fallback = {
        // Fallback to OAuth if native fails
        supabaseClient.auth.signInWith(Google)
    }
)

Button(onClick = { authState.startFlow() }) {
    Text("Continue with Google")
}
```

### Common Issues
| Issue | Solution |
|-------|----------|
| `DEVELOPER_ERROR` | Wrong SHA-1 or Client ID mismatch |
| Sign-in not working | Use Web Client ID, not Android Client ID in code |
| `12500` error | Google Play Services outdated |

---

## 5. Koin Dependency Injection

### Decision
Use **Koin 4.0.0** for dependency injection with module-per-feature organization.

### Rationale
- Pure Kotlin, no code generation
- KMP-compatible out of the box
- Simple DSL, low learning curve
- Compose integration built-in
- Aligns with constitution's DI guidelines

### Alternatives Considered
| Alternative | Rejected Because |
|-------------|------------------|
| Hilt/Dagger | Not KMP-compatible, Android-only |
| Kodein | Less popular, smaller community |
| Manual DI | Not scalable for multi-feature app |

### Implementation Pattern

```kotlin
// App module
val appModule = module {
    single { createDataStore(androidContext()) }
    single { createSupabaseClient(...) }
}

// Auth module
val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthViewModel(get()) }
}

// Application class
class TandemApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TandemApp)
            modules(appModule, authModule)
        }
    }
}

// Usage in Composable
@Composable
fun AuthScreen() {
    val viewModel: AuthViewModel = koinViewModel()
    // ...
}
```

### Dependencies Required
```toml
[versions]
koin = "4.0.0"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
```

---

## 6. Material Design 3 Theming

### Decision
Use Compose Material 3 with dynamic colors and custom brand accent.

### Rationale
- Already included in project (compose.material3)
- Dynamic colors enhance native feel
- Constitution requires MD3 compliance
- Light/dark mode support built-in

### Implementation Pattern

```kotlin
@Composable
fun TandemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TandemTypography,
        content = content
    )
}
```

### Brand Colors (Placeholder)
```kotlin
// Light scheme with brand accent
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),      // Brand primary - TBD from design
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    // ... other colors
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),      // Brand primary dark variant
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
    // ... other colors
)
```

---

## Dependency Version Summary

| Library | Version | Purpose |
|---------|---------|---------|
| supabase-kt | 3.1.4 | Authentication backend |
| ktor-client | 3.1.3 | HTTP client for Supabase |
| navigation-compose | 2.8.5 | Type-safe navigation |
| kotlinx-serialization | 1.7.3 | Route serialization |
| datastore-preferences | 1.1.1 | Auth token persistence |
| koin | 4.0.0 | Dependency injection |
| compose-material3 | (bundled) | UI theming |

---

## Outstanding Items

1. **Brand accent color**: Awaiting design specifications (using MD3 defaults)
2. **Supabase project setup**: Requires project URL and anon key
3. **Google OAuth configuration**: Requires Cloud Console setup
4. **SHA-1 fingerprints**: Need debug and release keystores

---

## References

- [supabase-kt GitHub](https://github.com/supabase-community/supabase-kt)
- [Navigation Compose Type Safety](https://developer.android.com/guide/navigation/design/type-safety)
- [DataStore for KMP](https://developer.android.com/kotlin/multiplatform/datastore)
- [Supabase Google Auth](https://supabase.com/docs/guides/auth/social-login/auth-google)
- [Koin Documentation](https://insert-koin.io/docs/quickstart/kotlin)
