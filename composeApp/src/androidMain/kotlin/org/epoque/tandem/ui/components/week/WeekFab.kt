package org.epoque.tandem.ui.components.week

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.epoque.tandem.ui.theme.CoralDark

/**
 * Floating action button for adding new tasks.
 * Uses coral color and rounded rectangle shape.
 * Matches the Todoist-inspired mockup design.
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
