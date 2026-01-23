package org.epoque.tandem.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Tandem shape system for consistent corner radii and shapes.
 * Based on iOS design patterns with Material Design 3 integration.
 *
 * Reference: docs/ios-design-guidelines.md
 */
object TandemShapes {

    // Base corner radius scale
    /** No rounding (0pt) */
    val none: Shape = RoundedCornerShape(0.dp)

    /** Extra small radius for subtle rounding (4pt) */
    val xs: Shape = RoundedCornerShape(4.dp)

    /** Small radius for buttons and inputs (8pt) */
    val sm: Shape = RoundedCornerShape(8.dp)

    /** Medium radius for cards and containers (12pt) - iOS default card radius */
    val md: Shape = RoundedCornerShape(12.dp)

    /** Large radius for prominent cards (16pt) */
    val lg: Shape = RoundedCornerShape(16.dp)

    /** Extra large radius for modal sheets (20pt) */
    val xl: Shape = RoundedCornerShape(20.dp)

    /** Fully rounded pill shape for chips and buttons */
    val pill: Shape = RoundedCornerShape(percent = 50)

    /** Circle shape for avatars and FABs */
    val circle: Shape = CircleShape

    /**
     * Card-specific shapes.
     */
    object Card {
        /** Default card corner radius (12pt) */
        val default: Shape = md

        /** Elevated card with slightly larger radius */
        val elevated: Shape = lg
    }

    /**
     * Chip and badge shapes.
     */
    object Chip {
        /** Standard chip shape - fully rounded */
        val default: Shape = pill

        /** Compact chip with smaller radius */
        val compact: Shape = sm
    }

    /**
     * Button shapes.
     */
    object Button {
        /** Standard button shape */
        val default: Shape = sm

        /** Rounded button shape */
        val rounded: Shape = pill

        /** Icon button shape */
        val icon: Shape = circle
    }

    /**
     * Sheet and modal shapes.
     */
    object Sheet {
        /** Bottom sheet top corners */
        val bottomSheet: Shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        /** Dialog shape */
        val dialog: Shape = lg
    }

    /**
     * Input field shapes.
     */
    object Input {
        /** Text field shape */
        val textField: Shape = sm

        /** Search bar shape */
        val searchBar: Shape = pill
    }

    /**
     * Selection indicator shapes.
     */
    object Selection {
        /** Checkbox border radius */
        val checkbox: Shape = xs

        /** Day selector chip */
        val dayChip: Shape = md
    }
}

/**
 * Raw corner radius values for cases where Shape isn't appropriate.
 * Use these when you need Dp values directly (e.g., for clip modifiers with custom shapes).
 */
object TandemCornerRadius {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
}
