package org.epoque.tandem.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Goals tab screen showing goal setting and tracking.
 * Placeholder implementation - will be expanded with actual features.
 */
@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Goals",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
