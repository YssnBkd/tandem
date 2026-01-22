package org.epoque.tandem.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tandem Typography scale aligned with iOS Human Interface Guidelines.
 *
 * Mapping from iOS text styles to Material 3 Typography:
 * - displayLarge  → iOS Large Title (34pt)
 * - displayMedium → iOS Title 1 (28pt)
 * - displaySmall  → iOS Title 2 (22pt)
 * - headlineLarge → iOS Title 3 (20pt)
 * - headlineMedium → iOS Headline (17pt Semibold)
 * - headlineSmall → iOS Body (17pt) - alternate weight
 * - titleLarge   → iOS Callout (16pt)
 * - titleMedium  → iOS Subheadline (15pt)
 * - titleSmall   → iOS Footnote (13pt)
 * - bodyLarge    → iOS Body (17pt)
 * - bodyMedium   → iOS Callout (16pt)
 * - bodySmall    → iOS Subheadline (15pt)
 * - labelLarge   → iOS Footnote (13pt)
 * - labelMedium  → iOS Caption 1 (12pt)
 * - labelSmall   → iOS Caption 2 (11pt)
 *
 * Reference: docs/ios-design-guidelines.md
 */
val TandemTypography = Typography(
    // iOS Large Title: 34pt Regular, line height 41pt
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        letterSpacing = 0.37.sp
    ),
    // iOS Title 1: 28pt Regular, line height 34pt
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.36.sp
    ),
    // iOS Title 2: 22pt Regular, line height 28pt
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.35.sp
    ),
    // iOS Title 3: 20pt Regular
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.38.sp
    ),
    // iOS Headline: 17pt Semibold, line height 22pt
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    ),
    // iOS Body Bold variant: 17pt Medium
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    ),
    // iOS Callout: 16pt Regular
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.32).sp
    ),
    // iOS Subheadline: 15pt Regular
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    ),
    // iOS Footnote: 13pt Regular
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.08).sp
    ),
    // iOS Body: 17pt Regular, line height 22pt
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    ),
    // iOS Callout: 16pt Regular
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.32).sp
    ),
    // iOS Subheadline: 15pt Regular
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    ),
    // iOS Footnote: 13pt Regular
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.08).sp
    ),
    // iOS Caption 1: 12pt Regular, line height 16pt
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    // iOS Caption 2: 11pt Regular, line height 13pt
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.07.sp
    )
)

/**
 * Semantic typography aliases for common use cases.
 * Use these instead of directly referencing Typography styles
 * to ensure consistent usage across the app.
 */
object TandemTextStyles {

    /**
     * Page and section titles.
     */
    object Title {
        /** Large page title (34pt) - e.g., "This Week" */
        val page: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.displayLarge

        /** Section title (22pt) - e.g., timeline section headers */
        val section: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.displaySmall

        /** Card title (17pt Semibold) - e.g., week card titles */
        val card: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.headlineMedium

        /** List section header (13pt) - e.g., "Today", "Tomorrow" */
        val listSection: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.titleSmall
    }

    /**
     * Body and content text.
     */
    object Body {
        /** Primary body text (17pt) - main content */
        val primary: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.bodyLarge

        /** Secondary body text (15pt) - supporting content */
        val secondary: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.bodySmall

        /** Tertiary/metadata text (13pt) - timestamps, counts */
        val tertiary: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.labelLarge
    }

    /**
     * Labels and captions.
     */
    object Label {
        /** Standard label (13pt) */
        val standard: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.labelLarge

        /** Small label (12pt) - metadata, badges */
        val small: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.labelMedium

        /** Caption text (11pt) - smallest readable text */
        val caption: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.labelSmall

        /** Tab bar label (10pt) - minimum size */
        val tabBar: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.labelSmall.copy(fontSize = 10.sp)
    }

    /**
     * Interactive element text.
     */
    object Button {
        /** Primary button text (17pt Semibold) */
        val primary: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.headlineMedium

        /** Secondary button text (15pt Medium) */
        val secondary: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.titleMedium.copy(fontWeight = FontWeight.Medium)

        /** Text button (17pt) */
        val text: TextStyle
            @Composable @ReadOnlyComposable
            get() = TandemTypography.bodyLarge
    }
}
