# Quickstart: Core Infrastructure

**Feature**: 001-core-infrastructure
**Date**: 2025-12-31

This guide covers the setup steps required before implementing the Core Infrastructure feature.

---

## Prerequisites

### 1. Development Environment

- [ ] Android Studio Ladybug (2024.2.1) or later
- [ ] JDK 17+
- [ ] Kotlin 2.3.0 (configured in project)
- [ ] Android SDK 24+ (configured in project)

### 2. Supabase Project

You need a Supabase project for authentication. If you don't have one:

1. Go to [supabase.com](https://supabase.com) and create an account
2. Create a new project
3. Note down:
   - **Project URL**: `https://YOUR_PROJECT_ID.supabase.co`
   - **Anon/Public Key**: Found in Settings > API

### 3. Google Cloud Console (for Google Sign-In)

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create or select a project
3. Enable the **Google Identity** API
4. Configure **OAuth Consent Screen**:
   - User Type: External
   - App name: Tandem
   - Support email: Your email
   - Scopes: `email`, `profile`, `openid`

5. Create OAuth 2.0 Client IDs:

   **Web Application** (required for Supabase):
   - Name: "Tandem Web Client"
   - Authorized redirect URI: `https://YOUR_PROJECT_ID.supabase.co/auth/v1/callback`
   - Save the **Client ID** and **Client Secret**

   **Android** (required for native sign-in):
   - Name: "Tandem Android"
   - Package name: `org.epoque.tandem`
   - SHA-1 fingerprint: (see below)

### 4. Get SHA-1 Fingerprint

For **debug** builds:
```bash
# macOS/Linux
keytool -keystore ~/.android/debug.keystore -list -v \
  -alias androiddebugkey -storepass android -keypass android

# Or use Gradle
./gradlew signingReport
```

Copy the SHA-1 fingerprint and add it to the Android OAuth client in Google Cloud Console.

---

## Supabase Configuration

### Enable Google Provider

1. In Supabase Dashboard, go to **Authentication** > **Providers**
2. Find **Google** and enable it
3. Enter:
   - **Client ID**: Web Client ID from Google Console
   - **Client Secret**: Web Client Secret from Google Console
4. In **Authorized Client IDs**, add the Android Client ID

---

## Project Configuration

### 1. Create Secrets File

Create `local.properties` (already gitignored) with your credentials:

```properties
# Supabase
SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
SUPABASE_KEY=your_anon_key_here

# Google Sign-In
GOOGLE_WEB_CLIENT_ID=your_web_client_id.apps.googleusercontent.com
```

### 2. Configure BuildConfig

Update `composeApp/build.gradle.kts` to read secrets:

```kotlin
import java.util.Properties

// Load local.properties
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

android {
    defaultConfig {
        // Add BuildConfig fields
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProperties.getProperty("SUPABASE_URL", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_KEY",
            "\"${localProperties.getProperty("SUPABASE_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }
}
```

### 3. Add Dependencies

Update `gradle/libs.versions.toml`:

```toml
[versions]
supabase = "3.1.4"
ktor = "3.1.3"
koin = "4.0.0"
navigation = "2.8.5"
datastore = "1.1.1"
serialization = "1.7.3"

[libraries]
# Supabase
supabase-bom = { module = "io.github.jan-tennert.supabase:bom", version.ref = "supabase" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt" }
supabase-compose-auth = { module = "io.github.jan-tennert.supabase:compose-auth" }
supabase-compose-auth-ui = { module = "io.github.jan-tennert.supabase:compose-auth-ui" }

# Ktor (required by Supabase)
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }

# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

# Navigation
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Serialization
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

[plugins]
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

Update `composeApp/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization) // Add this
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Supabase
            implementation(platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.compose.auth.ui)
            implementation(libs.ktor.client.android)

            // Koin
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation
            implementation(libs.navigation.compose)

            // DataStore
            implementation(libs.datastore.preferences)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            // Koin core
            implementation(libs.koin.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
```

---

## Verification Steps

After setup, verify everything works:

### 1. Sync Gradle

```bash
./gradlew --refresh-dependencies
```

### 2. Build Check

```bash
./gradlew :composeApp:compileDebugKotlinAndroid
```

Should complete without errors.

### 3. Verify BuildConfig

Create a temporary file to test BuildConfig access:

```kotlin
// Temporary - delete after verification
fun verifyConfig() {
    println("Supabase URL: ${BuildConfig.SUPABASE_URL}")
    println("Has Google Client: ${BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()}")
}
```

---

## Directory Structure After Setup

```
tandem/
├── local.properties          # Secrets (gitignored)
├── gradle/
│   └── libs.versions.toml    # Updated with new deps
├── composeApp/
│   ├── build.gradle.kts      # Updated with new deps
│   └── src/
│       └── androidMain/
│           └── kotlin/org/epoque/tandem/
│               ├── TandemApp.kt       # To be created
│               ├── di/                 # To be created
│               ├── ui/                 # To be created
│               └── ...
└── shared/
    └── src/
        └── commonMain/
            └── kotlin/org/epoque/tandem/
                ├── domain/             # To be created
                └── data/               # To be created
```

---

## Troubleshooting

### Supabase Connection Failed

- Verify `SUPABASE_URL` starts with `https://`
- Check that the anon key is correct (not the service role key)
- Ensure your IP is not blocked in Supabase settings

### Google Sign-In DEVELOPER_ERROR

- Verify SHA-1 fingerprint matches debug keystore
- Ensure package name is exactly `org.epoque.tandem`
- Check that Android Client ID is added to Supabase authorized clients
- Use **Web Client ID** in code, not Android Client ID

### Gradle Sync Errors

- Clear Gradle cache: `./gradlew clean --refresh-dependencies`
- Invalidate Android Studio caches: File > Invalidate Caches
- Check Kotlin version compatibility (needs 2.1.0+)

---

## Next Steps

Once setup is verified, proceed to implementation:

1. Run `/speckit.tasks` to generate implementation tasks
2. Follow the task list to implement each component
3. Verify build after each major component
4. Test on device after completing authentication flow

---

## Security Notes

- **Never commit** `local.properties` to version control
- Use **anon key** for client, not service role key
- SHA-1 fingerprints for **release** builds should be added before production
- Consider using Android Keystore for production credential storage
