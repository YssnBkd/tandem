package org.epoque.tandem.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.epoque.tandem.ui.screens.goals.GoalsScreen as GoalsScreenImpl

/**
 * Goals tab screen showing goal setting and tracking.
 * Delegates to the new redesigned GoalsScreen implementation.
 */
@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier
) {
    GoalsScreenImpl(modifier = modifier)
}
