package org.epoque.tandem.ui.components.week

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.epoque.tandem.presentation.week.model.TaskSection
import org.epoque.tandem.ui.theme.OverdueRed

/**
 * Section header for task groups (Overdue, Today, Tomorrow, Later this week).
 * "Overdue" is shown in red, others in default text color.
 * Matches the Todoist-inspired mockup design.
 */
@Composable
fun TaskSectionHeader(
    section: TaskSection,
    modifier: Modifier = Modifier
) {
    val title = when (section) {
        TaskSection.OVERDUE -> "Overdue"
        TaskSection.TODAY -> "Today"
        TaskSection.TOMORROW -> "Tomorrow"
        TaskSection.LATER_THIS_WEEK -> "Later this week"
        TaskSection.UNSCHEDULED -> "Unscheduled"
        TaskSection.COMPLETED -> "Completed"
    }

    val color = when (section) {
        TaskSection.OVERDUE -> OverdueRed
        else -> MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        ),
        color = color,
        modifier = modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 8.dp
        )
    )
}
