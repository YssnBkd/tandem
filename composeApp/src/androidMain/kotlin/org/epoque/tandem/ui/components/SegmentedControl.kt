package org.epoque.tandem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.epoque.tandem.ui.theme.GoalPrimary
import org.epoque.tandem.ui.theme.GoalTextMuted
import org.epoque.tandem.ui.theme.TandemShapes
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTextStyles

/**
 * A reusable segmented control component with pill-style selection.
 *
 * @param segments List of segment labels to display
 * @param selectedIndex Currently selected segment index
 * @param onSegmentSelected Callback when a segment is selected
 * @param modifier Modifier for the component
 * @param selectedColor Color for selected segment text
 * @param unselectedColor Color for unselected segment text
 * @param backgroundColor Background color of the control container
 * @param pillElevation Elevation of the selected pill
 */
@Composable
fun SegmentedControl(
    segments: List<String>,
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = GoalPrimary,
    unselectedColor: Color = GoalTextMuted,
    backgroundColor: Color = Color(0x08000000),
    pillElevation: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(TandemShapes.pill)
            .background(backgroundColor)
            .padding(TandemSpacing.xxxs)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            segments.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }

                if (isSelected) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onSegmentSelected(index) },
                        shape = TandemShapes.pill,
                        color = Color.White,
                        shadowElevation = pillElevation
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = TandemSpacing.sm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = TandemTextStyles.Label.small,
                                fontWeight = FontWeight.SemiBold,
                                color = selectedColor
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(TandemShapes.pill)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onSegmentSelected(index) }
                            .padding(vertical = TandemSpacing.sm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = TandemTextStyles.Label.small,
                            fontWeight = FontWeight.SemiBold,
                            color = unselectedColor
                        )
                    }
                }
            }
        }
    }
}
