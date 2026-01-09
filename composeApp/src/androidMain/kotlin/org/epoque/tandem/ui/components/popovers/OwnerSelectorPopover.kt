package org.epoque.tandem.ui.components.popovers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

/**
 * Owner type for task assignment.
 */
enum class OwnerType(val displayName: String, val emoji: String) {
    ME("Me", "\uD83D\uDC64"),
    PARTNER("Partner", "\uD83D\uDC91"),
    TOGETHER("Together", "\uD83D\uDC65")
}

/**
 * Dropdown popover for selecting task owner.
 * Shows Me/Partner/Together options with checkmark on selected.
 */
@Composable
fun OwnerSelectorPopover(
    expanded: Boolean,
    selectedOwner: OwnerType,
    onOwnerSelected: (OwnerType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false),
        offset = DpOffset(0.dp, 4.dp),
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.width(180.dp)
    ) {
        OwnerType.entries.forEach { owner ->
            OwnerOptionItem(
                owner = owner,
                isSelected = owner == selectedOwner,
                onClick = {
                    onOwnerSelected(owner)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun OwnerOptionItem(
    owner: OwnerType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji icon
        Text(
            text = owner.emoji,
            fontSize = 16.sp
        )

        // Label
        Text(
            text = owner.displayName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        // Checkmark for selected
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Box(modifier = Modifier.size(18.dp))
        }
    }
}
