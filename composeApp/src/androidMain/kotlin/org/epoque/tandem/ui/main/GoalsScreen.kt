package org.epoque.tandem.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.epoque.tandem.ui.legacy.goals.GoalsScreen as GoalsScreenImpl

/**
 * Goals tab screen showing goal setting and tracking.
 * Delegates to the full GoalsScreen implementation.
 */
@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier
) {
    GoalsScreenImpl(modifier = modifier)
}
