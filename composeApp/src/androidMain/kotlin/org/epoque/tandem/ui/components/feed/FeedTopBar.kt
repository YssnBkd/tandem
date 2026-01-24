package org.epoque.tandem.ui.components.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.epoque.tandem.ui.theme.TandemBackgroundLight
import org.epoque.tandem.ui.theme.TandemOnBackgroundLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemSurfaceLight
import org.epoque.tandem.ui.theme.TandemTextStyles

/**
 * Top navigation bar for the Feed screen.
 * Shows "Feed" title and more options button.
 */
@Composable
fun FeedTopBar(
    onMoreOptionsTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TandemBackgroundLight,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TandemSpacing.Screen.horizontalPadding,
                    end = TandemSpacing.Screen.horizontalPadding,
                    top = TandemSpacing.lg,
                    bottom = TandemSpacing.md
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Feed",
                style = TandemTextStyles.Title.page,
                color = TandemOnBackgroundLight
            )

            Surface(
                onClick = onMoreOptionsTapped,
                shape = CircleShape,
                color = TandemSurfaceLight,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = TandemOnBackgroundLight,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
        }
    }
}
