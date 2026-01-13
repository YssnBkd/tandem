package org.epoque.tandem.ui.components.week

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import org.epoque.tandem.ui.theme.CoralDark

/**
 * Expandable FAB menu for Week screen actions.
 * Uses Material 3 Expressive FAB Menu components.
 *
 * Menu items (bottom to top when expanded):
 * 1. Add Task - Opens add task modal (primary action)
 * 2. Plan Week - Navigates to planning workflow
 * 3. Review Week - Navigates to review workflow
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeekFabMenu(
    onAddTaskClick: () -> Unit,
    onPlanWeekClick: () -> Unit,
    onReviewWeekClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Handle back gesture to collapse menu
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    FloatingActionButtonMenu(
        expanded = fabMenuExpanded,
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                checked = fabMenuExpanded,
                onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close
                        else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        // Review Week - Top of menu
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                onReviewWeekClick()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            text = { Text("Review Week") }
        )

        // Plan Week - Middle of menu
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                onPlanWeekClick()
            },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            text = { Text("Plan Week") }
        )

        // Add Task - Bottom of menu (primary action, closest to FAB)
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                onAddTaskClick()
            },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Task") }
        )
    }
}

/**
 * Simple floating action button for adding new tasks.
 * Uses coral color and rounded rectangle shape.
 *
 * @deprecated Use [WeekFabMenu] instead for expandable menu with Plan/Review options.
 */
@Composable
fun WeekFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(52.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = CoralDark,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add task",
            modifier = Modifier.size(24.dp)
        )
    }
}
