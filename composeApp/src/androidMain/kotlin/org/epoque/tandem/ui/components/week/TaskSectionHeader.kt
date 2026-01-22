package org.epoque.tandem.ui.components.week

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.epoque.tandem.presentation.week.model.TaskSection
import org.epoque.tandem.ui.theme.OverdueRed
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTextStyles

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
        style = TandemTextStyles.Title.listSection.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier = modifier.padding(
            start = TandemSpacing.List.itemHorizontalPadding,
            end = TandemSpacing.List.itemHorizontalPadding,
            top = TandemSpacing.List.sectionHeaderTopPadding,
            bottom = TandemSpacing.List.sectionHeaderBottomPadding
        )
    )
}
