package org.epoque.tandem.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.epoque.tandem.domain.model.User
import org.epoque.tandem.ui.navigation.NavigationTab
import org.epoque.tandem.ui.legacy.progress.ProgressScreen
import org.epoque.tandem.ui.screens.seasons.SeasonsScreen
import org.epoque.tandem.ui.screens.week.WeekScreen

/**
 * Main app screen with bottom navigation for authenticated users.
 *
 * Note: WeekScreen has its own WeekHeader, so we hide the TopAppBar
 * when on the Week tab to avoid duplicate headers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: User?,
    onSignOut: () -> Unit,
    onNavigateToPlanning: () -> Unit = {},
    onNavigateToReview: () -> Unit = {},
    onNavigateToPartnerInvite: () -> Unit = {},
    onNavigateToPartnerSettings: () -> Unit = {},
    onNavigateToPastWeekDetail: (String) -> Unit = {},
    onNavigateToAddGoal: () -> Unit = {},
    onNavigateToGoalDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable(stateSaver = NavigationTab.Saver) { mutableStateOf(NavigationTab.Week) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    // Week and Goals tabs have their own headers, so we don't show TopAppBar for them
    val showTopBar = selectedTab != NavigationTab.Week && selectedTab != NavigationTab.Goals

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                NavigationTab.Progress -> "Progress"
                                NavigationTab.Goals -> "Goals"
                                NavigationTab.Seasons -> "Seasons"
                                else -> user?.displayName ?: "Tandem"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sign Out") },
                                    onClick = {
                                        showMenu = false
                                        onSignOut()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                NavigationTab.Week -> WeekScreen(
                    onNavigateToPartnerInvite = onNavigateToPartnerInvite,
                    onNavigateToSeasons = { selectedTab = NavigationTab.Seasons }
                )
                NavigationTab.Progress -> ProgressScreen(
                    onNavigateToWeekDetail = onNavigateToPastWeekDetail
                )
                NavigationTab.Goals -> GoalsScreen(
                    onNavigateToAddGoal = onNavigateToAddGoal,
                    onNavigateToGoalDetail = onNavigateToGoalDetail
                )
                NavigationTab.Seasons -> SeasonsScreen()
            }
        }
    }
}
