package org.epoque.tandem.ui.screens.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Task ownership segment for filtering.
 */
enum class OwnerSegment(val displayName: String) {
    YOU("You"),
    PARTNER("Partner"),
    TOGETHER("Together")
}

// Light gray background for segment container
private val SegmentBackground = Color(0xFFF0F0F0)

/**
 * Custom segmented control matching the Todoist-inspired mockup design.
 * Features a "pill within a pill" appearance:
 * - Outer container: light gray rounded rectangle
 * - Selected segment: white pill floating inside
 * - No borders between segments
 * - Checkmark icon on "You" when selected
 */
@Composable
fun SegmentedControl(
    selectedSegment: OwnerSegment,
    onSegmentSelected: (OwnerSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val segments = OwnerSegment.entries

    // Outer container with light gray background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SegmentBackground)
            .padding(4.dp) // Inner padding for floating pill effect
            .semantics {
                contentDescription = "Task view selector. Current selection: ${selectedSegment.displayName}"
            }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            segments.forEach { segment ->
                val isSelected = segment == selectedSegment
                val interactionSource = remember { MutableInteractionSource() }

                // Each segment as a selectable pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = if (isSelected) Color.White else Color.Transparent
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null // No ripple for clean look
                        ) { onSegmentSelected(segment) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Show checkmark only for "You" when selected
                        if (segment == OwnerSegment.YOU && isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = segment.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
