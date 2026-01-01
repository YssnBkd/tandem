package org.epoque.tandem.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Progress tab screen showing progress tracking.
 * Placeholder implementation - will be expanded with actual features.
 */
@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
