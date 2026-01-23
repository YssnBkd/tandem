package org.epoque.tandem.ui.components.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Floating button that appears when scrolled away from the current week.
 * Clicking it scrolls back to the current week.
 * Uses dark background with white text to match mockup design.
 */
@Composable
fun BackToTodayButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            shape = RoundedCornerShape(50.dp),
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    text = "Back to this week",
                    fontWeight = FontWeight.SemiBold
                )
            }
        )
    }
}
