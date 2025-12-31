package org.epoque.tandem.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TandemPrimary,
    onPrimary = TandemOnPrimary,
    primaryContainer = TandemPrimaryContainer,
    onPrimaryContainer = TandemOnPrimaryContainer,
    secondary = TandemSecondary,
    onSecondary = TandemOnSecondary,
    secondaryContainer = TandemSecondaryContainer,
    onSecondaryContainer = TandemOnSecondaryContainer,
    tertiary = TandemTertiary,
    onTertiary = TandemOnTertiary,
    tertiaryContainer = TandemTertiaryContainer,
    onTertiaryContainer = TandemOnTertiaryContainer,
    error = TandemError,
    onError = TandemOnError,
    errorContainer = TandemErrorContainer,
    onErrorContainer = TandemOnErrorContainer,
    background = TandemBackgroundLight,
    onBackground = TandemOnBackgroundLight,
    surface = TandemSurfaceLight,
    onSurface = TandemOnSurfaceLight,
    surfaceVariant = TandemSurfaceVariantLight,
    onSurfaceVariant = TandemOnSurfaceVariantLight,
    outline = TandemOutlineLight,
    outlineVariant = TandemOutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TandemPrimaryDark,
    onPrimary = TandemOnPrimaryDark,
    primaryContainer = TandemPrimaryContainerDark,
    onPrimaryContainer = TandemOnPrimaryContainerDark,
    secondary = TandemSecondaryDark,
    onSecondary = TandemOnSecondaryDark,
    secondaryContainer = TandemSecondaryContainerDark,
    onSecondaryContainer = TandemOnSecondaryContainerDark,
    tertiary = TandemTertiaryDark,
    onTertiary = TandemOnTertiaryDark,
    tertiaryContainer = TandemTertiaryContainerDark,
    onTertiaryContainer = TandemOnTertiaryContainerDark,
    error = TandemErrorDark,
    onError = TandemOnErrorDark,
    errorContainer = TandemErrorContainerDark,
    onErrorContainer = TandemOnErrorContainerDark,
    background = TandemBackgroundDark,
    onBackground = TandemOnBackgroundDark,
    surface = TandemSurfaceDark,
    onSurface = TandemOnSurfaceDark,
    surfaceVariant = TandemSurfaceVariantDark,
    onSurfaceVariant = TandemOnSurfaceVariantDark,
    outline = TandemOutlineDark,
    outlineVariant = TandemOutlineVariantDark
)

/**
 * Tandem Material Design 3 theme.
 *
 * @param darkTheme Whether to use dark theme, defaults to system setting
 * @param dynamicColor Whether to use dynamic colors on Android 12+, defaults to true
 * @param content The composable content to be themed
 */
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
