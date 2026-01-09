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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
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
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.ui.theme.PriorityP1
import org.epoque.tandem.ui.theme.PriorityP2
import org.epoque.tandem.ui.theme.PriorityP3

/**
 * Priority option data for the selector.
 */
private data class PriorityOption(
    val priority: TaskPriority,
    val label: String,
    val color: Color,
    val useFilled: Boolean = true
)

private val priorityOptions = listOf(
    PriorityOption(TaskPriority.P1, "Priority 1", PriorityP1),
    PriorityOption(TaskPriority.P2, "Priority 2", PriorityP2),
    PriorityOption(TaskPriority.P3, "Priority 3", PriorityP3),
    PriorityOption(TaskPriority.P4, "No priority", Color.Gray, useFilled = false)
)

/**
 * Dropdown popover for selecting task priority.
 * Shows P1/P2/P3/P4 with colored flag icons.
 */
@Composable
fun PrioritySelectorPopover(
    expanded: Boolean,
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(0.dp, 4.dp),
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.width(180.dp)
    ) {
        priorityOptions.forEach { option ->
            PriorityOptionItem(
                option = option,
                isSelected = option.priority == selectedPriority,
                onClick = {
                    onPrioritySelected(option.priority)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun PriorityOptionItem(
    option: PriorityOption,
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
        // Flag icon colored by priority
        Icon(
            imageVector = if (option.useFilled) Icons.Filled.Flag else Icons.Outlined.Flag,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = option.color
        )

        // Label
        Text(
            text = option.label,
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
