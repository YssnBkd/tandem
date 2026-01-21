package org.epoque.tandem.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the bottom navigation tabs in the main app screen.
 * Updated for Feature 009: UI Redesign - now includes 4 tabs.
 */
sealed class NavigationTab(
    val route: Routes.Main,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Week : NavigationTab(
        route = Routes.Main.Week,
        title = "Week",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    data object Progress : NavigationTab(
        route = Routes.Main.Progress,
        title = "Progress",
        selectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
        unselectedIcon = Icons.AutoMirrored.Outlined.TrendingUp
    )

    data object Goals : NavigationTab(
        route = Routes.Main.Goals,
        title = "Goals",
        selectedIcon = Icons.Filled.Flag,
        unselectedIcon = Icons.Outlined.Flag
    )

    /** Timeline tab - shows all weeks history */
    data object Timeline : NavigationTab(
        route = Routes.Main.Timeline,
        title = "Timeline",
        selectedIcon = Icons.Filled.ViewTimeline,
        unselectedIcon = Icons.Outlined.ViewTimeline
    )

    companion object {
        val entries = listOf(Week, Progress, Goals, Timeline)

        /** Saver for use with rememberSaveable */
        val Saver: Saver<NavigationTab, String> = Saver(
            save = { it.title },
            restore = { title -> entries.find { it.title == title } ?: Week }
        )
    }
}
