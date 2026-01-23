package org.epoque.tandem.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tandem sizing system based on iOS Human Interface Guidelines.
 * Defines consistent sizes for icons, touch targets, and components.
 *
 * Reference: docs/ios-design-guidelines.md
 */
object TandemSizing {

    /**
     * Minimum touch target size per iOS HIG.
     * All interactive elements must have at least this touch area.
     * The visual element can be smaller, but the tappable area must be 44x44pt.
     */
    val minTouchTarget: Dp = 44.dp

    /**
     * Icon sizes for consistent iconography.
     */
    object Icon {
        /** Extra small icons for metadata indicators (12pt) */
        val xs: Dp = 12.dp

        /** Small icons for inline use (14pt) */
        val sm: Dp = 14.dp

        /** Medium icons for standard UI elements (16pt) */
        val md: Dp = 16.dp

        /** Default icon size for most contexts (20pt) */
        val lg: Dp = 20.dp

        /** Large icons for navigation and actions (24pt) */
        val xl: Dp = 24.dp

        /** Extra large icons for feature highlights (32pt) */
        val xxl: Dp = 32.dp
    }

    /**
     * Checkbox and selection indicator sizes.
     */
    object Checkbox {
        /** Visual size of standard checkbox (20pt) */
        val visualSize: Dp = 20.dp

        /** Visual size of large checkbox for detail views (32pt) */
        val visualSizeLarge: Dp = 32.dp

        /** Touch target size - must be 44pt minimum */
        val touchTarget: Dp = minTouchTarget
    }

    /**
     * Progress indicators and bars.
     */
    object Progress {
        /** Height of linear progress bars */
        val barHeight: Dp = 4.dp

        /** Width of compact inline progress bars */
        val barWidthCompact: Dp = 40.dp

        /** Standard circular progress size */
        val circularSize: Dp = 24.dp

        /** Large circular progress size */
        val circularSizeLarge: Dp = 48.dp
    }

    /**
     * Task and content indicators.
     */
    object Indicator {
        /** Size of task/activity indicator dots */
        val dot: Dp = 4.dp

        /** Size of status indicator dots */
        val statusDot: Dp = 6.dp

        /** Size of badge indicators */
        val badge: Dp = 8.dp
    }

    /**
     * Navigation and system component heights.
     * Based on iOS standard component sizes.
     */
    object Navigation {
        /** Status bar height on iPhone with notch/Dynamic Island */
        val statusBarHeightNotch: Dp = 54.dp

        /** Status bar height on older iPhones */
        val statusBarHeightLegacy: Dp = 20.dp

        /** Navigation bar height (without large title) */
        val navBarHeight: Dp = 44.dp

        /** Navigation bar height with large title */
        val navBarHeightLarge: Dp = 96.dp

        /** Tab bar height on iPhone */
        val tabBarHeight: Dp = 49.dp

        /** Tab bar height on iPad */
        val tabBarHeightIpad: Dp = 50.dp

        /** Home indicator safe area on notched devices */
        val homeIndicatorHeight: Dp = 34.dp

        /** Search bar height */
        val searchBarHeight: Dp = 36.dp
    }

    /**
     * Button and interactive element sizes.
     */
    object Button {
        /** Standard button height */
        val height: Dp = 44.dp

        /** Compact button height (still touch-target compliant via padding) */
        val heightCompact: Dp = 36.dp

        /** Icon button size (touch target) */
        val iconButtonSize: Dp = minTouchTarget

        /** FAB size */
        val fabSize: Dp = 56.dp

        /** Mini FAB size */
        val fabSizeMini: Dp = 40.dp
    }

    /**
     * Avatar and profile image sizes.
     */
    object Avatar {
        /** Small avatar for lists */
        val sm: Dp = 32.dp

        /** Medium avatar for cards */
        val md: Dp = 40.dp

        /** Large avatar for profiles */
        val lg: Dp = 56.dp

        /** Extra large avatar for detail views */
        val xl: Dp = 80.dp
    }

    /**
     * Border and stroke widths.
     */
    object Border {
        /** Hairline border (1pt) */
        val hairline: Dp = 1.dp

        /** Standard border (1.5pt) */
        val standard: Dp = 1.5.dp

        /** Emphasis border (2pt) */
        val emphasis: Dp = 2.dp

        /** Thick border for selection states (3pt) */
        val thick: Dp = 3.dp
    }
}
