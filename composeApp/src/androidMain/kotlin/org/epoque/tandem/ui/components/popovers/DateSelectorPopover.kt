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
 * Quick date options for task scheduling.
 */
sealed class QuickDate(val id: String, val label: String, val emoji: String) {
    data object Today : QuickDate("today", "Today", "\uD83D\uDCC5")
    data object Tomorrow : QuickDate("tomorrow", "Tomorrow", "\uD83C\uDF05")
    data object NextWeek : QuickDate("next_week", "Next week", "\uD83D\uDCC6")
    data object PickDate : QuickDate("pick", "Pick a date...", "\uD83D\uDDD3\uFE0F")

    companion object {
        val options = listOf(Today, Tomorrow, NextWeek, PickDate)

        fun fromId(id: String): QuickDate? = when (id) {
            "today" -> Today
            "tomorrow" -> Tomorrow
            "next_week" -> NextWeek
            "pick" -> PickDate
            else -> null
        }
    }
}

/**
 * Dropdown popover for selecting task date.
 * Shows Today/Tomorrow/Next week/Pick a date options.
 */
@Composable
fun DateSelectorPopover(
    expanded: Boolean,
    selectedDate: String,
    onDateSelected: (QuickDate) -> Unit,
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
        modifier = modifier.width(200.dp)
    ) {
        QuickDate.options.forEach { option ->
            DateOptionItem(
                option = option,
                isSelected = option.label == selectedDate || option.id == selectedDate,
                onClick = {
                    onDateSelected(option)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun DateOptionItem(
    option: QuickDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected && option !is QuickDate.PickDate) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (isSelected && option !is QuickDate.PickDate) {
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
            text = option.emoji,
            fontSize = 16.sp
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

        // Checkmark for selected (not for "Pick a date")
        if (isSelected && option !is QuickDate.PickDate) {
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
