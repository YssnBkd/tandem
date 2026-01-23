package org.epoque.tandem.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tandem spacing system based on iOS Human Interface Guidelines.
 * Uses an 8pt grid with exceptions for fine-tuned internal spacing.
 *
 * Reference: docs/ios-design-guidelines.md
 */
object TandemSpacing {
    // Base 8pt grid scale
    val xxxs: Dp = 2.dp   // Fine-tuned internal spacing only
    val xxs: Dp = 4.dp    // Icon gaps, indicator dots
    val xs: Dp = 8.dp     // Related item spacing, small gaps
    val sm: Dp = 12.dp    // Internal component padding
    val md: Dp = 16.dp    // Standard margins, card padding (iPhone horizontal margin)
    val lg: Dp = 24.dp    // Section spacing, iPad horizontal margin
    val xl: Dp = 32.dp    // Large section breaks
    val xxl: Dp = 40.dp   // Page-level spacing

    /**
     * Screen-level spacing for consistent page layouts.
     */
    object Screen {
        /** Standard horizontal padding for iPhone screens (16pt) */
        val horizontalPadding: Dp = md

        /** Standard vertical padding at top of scrollable content */
        val topPadding: Dp = sm

        /** Bottom padding to clear FAB and tab bar */
        val bottomPaddingWithFab: Dp = 80.dp
    }

    /**
     * List and collection spacing.
     */
    object List {
        /** Vertical padding inside list items */
        val itemVerticalPadding: Dp = sm  // 12.dp

        /** Horizontal padding inside list items */
        val itemHorizontalPadding: Dp = md  // 16.dp

        /** Space above section headers */
        val sectionHeaderTopPadding: Dp = md  // 16.dp

        /** Space below section headers */
        val sectionHeaderBottomPadding: Dp = xs  // 8.dp

        /** Space between list items */
        val itemSpacing: Dp = xs  // 8.dp
    }

    /**
     * Card component spacing.
     */
    object Card {
        /** Internal padding for cards */
        val padding: Dp = md  // 16.dp

        /** Vertical spacing between cards */
        val verticalSpacing: Dp = xs  // 8.dp

        /** Horizontal margin for cards in a list */
        val horizontalMargin: Dp = md  // 16.dp
    }

    /**
     * Inline content spacing (icons, badges, metadata).
     */
    object Inline {
        /** Gap between icon and text */
        val iconTextGap: Dp = xxs  // 4.dp

        /** Gap between inline metadata items */
        val metadataGap: Dp = xs  // 8.dp

        /** Gap between checkbox and content */
        val checkboxGap: Dp = sm  // 12.dp
    }

    /**
     * Chip and tag spacing.
     */
    object Chip {
        /** Horizontal padding inside chips */
        val horizontalPadding: Dp = sm  // 12.dp

        /** Vertical padding inside chips (minimum for touch target compliance) */
        val verticalPadding: Dp = xs  // 8.dp

        /** Smaller vertical padding for non-interactive chips */
        val verticalPaddingCompact: Dp = xxs  // 4.dp
    }

    /**
     * Sheet and modal spacing.
     */
    object Sheet {
        /** Horizontal padding inside bottom sheets */
        val horizontalPadding: Dp = md  // 16.dp

        /** Top padding inside bottom sheets (below handle) */
        val topPadding: Dp = xs  // 8.dp

        /** Bottom padding inside bottom sheets */
        val bottomPadding: Dp = xl  // 32.dp
    }
}
