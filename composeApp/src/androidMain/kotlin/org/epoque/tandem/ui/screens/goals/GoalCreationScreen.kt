package org.epoque.tandem.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import org.epoque.tandem.ui.components.SegmentedControl
import org.epoque.tandem.ui.theme.GoalPrimary
import org.epoque.tandem.ui.theme.GoalTextMain
import org.epoque.tandem.ui.theme.GoalTextMuted
import org.epoque.tandem.ui.theme.TandemBackgroundLight
import org.epoque.tandem.ui.theme.TandemOutlineLight
import org.epoque.tandem.ui.theme.TandemPrimaryContainer

// Local colors matching mockup
private val CardBackground = Color.White
private val PlaceholderColor = Color(0xFFB0B0B0)
private val DividerColor = Color(0xFFF0EBE6)
private val RadioBorderColor = Color(0xFFE0DCD6)  // Better contrast for radio dot

/**
 * Start date options for goal.
 */
enum class GoalStartDate(val label: String, val emoji: String) {
    TODAY("Today", "📅"),
    TOMORROW("Tomorrow", "🌅"),
    NEXT_WEEK("Next week", "📆"),
    NEXT_MONTH("Next month", "🗓️")
}

/**
 * Goal Creation Screen - Full screen for creating new goals.
 * Matches the goal_creation-geminirefactor.html mockup.
 */
@Composable
fun GoalCreationScreen(
    onNavigateBack: () -> Unit = {},
    onGoalCreated: (name: String, ownership: GoalOwnership, trackingType: TrackingType, stepperValue: Int, startDate: GoalStartDate, duration: GoalDuration) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var goalName by remember { mutableStateOf("") }
    var selectedOwnership by remember { mutableStateOf(GoalOwnership.TOGETHER) }
    var selectedTrackingType by remember { mutableStateOf(TrackingType.TARGET) }
    var stepperValue by remember { mutableIntStateOf(5000) }
    var selectedStartDate by remember { mutableStateOf(GoalStartDate.TODAY) }
    var selectedDuration by remember { mutableStateOf(GoalDuration.TWELVE_WEEKS) }

    val isCreateEnabled = goalName.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TandemBackgroundLight)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Header
        GoalCreationHeader(
            isCreateEnabled = isCreateEnabled,
            onCancelClick = onNavigateBack,
            onCreateClick = {
                if (isCreateEnabled) {
                    onGoalCreated(goalName, selectedOwnership, selectedTrackingType, stepperValue, selectedStartDate, selectedDuration)
                }
            }
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Ownership toggle (full width like other segment controls)
            OwnershipToggle(
                selectedOwnership = selectedOwnership,
                onOwnershipSelected = { selectedOwnership = it },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Goal name input
            GoalNameInput(
                value = goalName,
                onValueChange = { goalName = it },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tracking Type section
            SectionLabel(text = "TRACKING TYPE")
            TrackingTypeCard(
                selectedType = selectedTrackingType,
                onTypeSelected = { selectedTrackingType = it },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Goal Settings section (only for types with stepper)
            if (selectedTrackingType.stepperLabel != null) {
                SectionLabel(text = "GOAL SETTINGS")
                GoalSettingsCard(
                    label = selectedTrackingType.stepperLabel!!,
                    value = stepperValue,
                    onValueChange = { stepperValue = it },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // How Long section with start date and duration selectors
            SectionLabel(text = "HOW LONG?")
            HowLongSection(
                selectedStartDate = selectedStartDate,
                onStartDateSelected = { selectedStartDate = it },
                selectedDuration = selectedDuration,
                onDurationSelected = { selectedDuration = it }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Footer button
        FooterButton(
            enabled = isCreateEnabled,
            onClick = {
                if (isCreateEnabled) {
                    onGoalCreated(goalName, selectedOwnership, selectedTrackingType, stepperValue, selectedStartDate, selectedDuration)
                }
            }
        )
    }
}

/**
 * Header with Cancel and Create buttons.
 */
@Composable
private fun GoalCreationHeader(
    isCreateEnabled: Boolean,
    onCancelClick: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Cancel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = GoalTextMuted,
            modifier = Modifier.clickable(onClick = onCancelClick)
        )
        Text(
            text = "Create",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isCreateEnabled) GoalPrimary else GoalPrimary.copy(alpha = 0.4f),
            modifier = Modifier.clickable(enabled = isCreateEnabled, onClick = onCreateClick)
        )
    }
}

/**
 * Ownership toggle using SegmentedControl.
 * Only shows "Me" and "Together" options.
 */
@Composable
private fun OwnershipToggle(
    selectedOwnership: GoalOwnership,
    onOwnershipSelected: (GoalOwnership) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show Me and Together options
    val displayedOptions = listOf(GoalOwnership.JUST_ME, GoalOwnership.TOGETHER)
    val segments = listOf("Me", "Together")

    SegmentedControl(
        segments = segments,
        selectedIndex = displayedOptions.indexOf(selectedOwnership).coerceAtLeast(0),
        onSegmentSelected = { onOwnershipSelected(displayedOptions[it]) },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Large centered goal name input.
 */
@Composable
private fun GoalNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoalTextMain,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(GoalPrimary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Enter goal name...",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PlaceholderColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

/**
 * Section label - uppercase, small, muted.
 */
@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = GoalTextMuted,
        letterSpacing = 1.sp,
        modifier = modifier.padding(start = 4.dp, bottom = 12.dp)
    )
}

/**
 * Card containing tracking type selection items.
 */
@Composable
private fun TrackingTypeCard(
    selectedType: TrackingType,
    onTypeSelected: (TrackingType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            TrackingType.entries.forEachIndexed { index, type ->
                TrackingTypeItem(
                    type = type,
                    isSelected = type == selectedType,
                    onClick = { onTypeSelected(type) }
                )
                if (index < TrackingType.entries.size - 1) {
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                }
            }
        }
    }
}

/**
 * Single tracking type selection item.
 */
@Composable
private fun TrackingTypeItem(
    type: TrackingType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) TandemPrimaryContainer else TandemBackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.icon,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = type.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoalTextMain
            )
            Text(
                text = type.description,
                fontSize = 12.sp,
                color = GoalTextMuted
            )
        }

        // Radio dot
        RadioDot(isSelected = isSelected)
    }
}

/**
 * Custom radio dot matching mockup design.
 */
@Composable
private fun RadioDot(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(GoalPrimary)
                } else {
                    Modifier.border(2.dp, RadioBorderColor, CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Goal settings card with stepper.
 */
@Composable
private fun GoalSettingsCard(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoalTextMain
            )

            StepperControl(
                value = value,
                onValueChange = onValueChange
            )
        }
    }
}

/**
 * Stepper control with +/- buttons.
 */
@Composable
private fun StepperControl(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(TandemBackgroundLight)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minus button
        StepperButton(
            text = "−",
            onClick = { if (value > 1) onValueChange(value - 1) }
        )

        // Value
        Text(
            text = formatStepperValue(value),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoalTextMain,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )

        // Plus button
        StepperButton(
            text = "+",
            onClick = { onValueChange(value + 1) }
        )
    }
}

/**
 * Format stepper value with thousands separator (always comma).
 */
private fun formatStepperValue(value: Int): String {
    return if (value >= 1000) {
        val str = value.toString()
        val result = StringBuilder()
        var count = 0
        for (i in str.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                result.insert(0, ',')
            }
            result.insert(0, str[i])
            count++
        }
        result.toString()
    } else {
        value.toString()
    }
}

/**
 * Individual stepper button.
 */
@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(28.dp)
            .shadow(1.dp, CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoalTextMain
            )
        }
    }
}

/**
 * How Long section with start date and duration selector pills.
 */
@Composable
private fun HowLongSection(
    selectedStartDate: GoalStartDate,
    onStartDateSelected: (GoalStartDate) -> Unit,
    selectedDuration: GoalDuration,
    onDurationSelected: (GoalDuration) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePopover by remember { mutableStateOf(false) }
    var showDurationPopover by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Start date selector pill
        SelectorPill(
            icon = Icons.Outlined.CalendarToday,
            label = selectedStartDate.label,
            expanded = showStartDatePopover,
            onClick = { showStartDatePopover = true },
            modifier = Modifier.weight(1f)
        ) {
            StartDatePopover(
                expanded = showStartDatePopover,
                selectedDate = selectedStartDate,
                onDateSelected = {
                    onStartDateSelected(it)
                    showStartDatePopover = false
                },
                onDismiss = { showStartDatePopover = false }
            )
        }

        // Duration selector pill
        SelectorPill(
            icon = Icons.Outlined.Schedule,
            label = selectedDuration.label,
            expanded = showDurationPopover,
            onClick = { showDurationPopover = true },
            modifier = Modifier.weight(1f)
        ) {
            DurationPopover(
                expanded = showDurationPopover,
                selectedDuration = selectedDuration,
                onDurationSelected = {
                    onDurationSelected(it)
                    showDurationPopover = false
                },
                onDismiss = { showDurationPopover = false }
            )
        }
    }
}

/**
 * Reusable selector pill with icon, label, and dropdown slot.
 */
@Composable
private fun SelectorPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dropdownContent: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = CardBackground,
            shadowElevation = 4.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = GoalPrimary
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoalTextMain,
                    modifier = Modifier.weight(1f)
                )
                // Dropdown arrow
                Text(
                    text = "▼",
                    fontSize = 10.sp,
                    color = GoalTextMuted
                )
            }
        }
        dropdownContent()
    }
}

/**
 * Popover for start date selection.
 */
@Composable
private fun StartDatePopover(
    expanded: Boolean,
    selectedDate: GoalStartDate,
    onDateSelected: (GoalStartDate) -> Unit,
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
        GoalStartDate.entries.forEach { option ->
            PopoverOptionItem(
                emoji = option.emoji,
                label = option.label,
                isSelected = option == selectedDate,
                onClick = { onDateSelected(option) }
            )
        }
    }
}

/**
 * Popover for duration selection.
 */
@Composable
private fun DurationPopover(
    expanded: Boolean,
    selectedDuration: GoalDuration,
    onDurationSelected: (GoalDuration) -> Unit,
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
        GoalDuration.entries.forEach { option ->
            PopoverOptionItem(
                emoji = when (option) {
                    GoalDuration.FOUR_WEEKS -> "🏃"
                    GoalDuration.TWELVE_WEEKS -> "📈"
                    GoalDuration.SIX_MONTHS -> "🎯"
                    GoalDuration.ONGOING -> "♾️"
                },
                label = option.label,
                isSelected = option == selectedDuration,
                onClick = { onDurationSelected(option) }
            )
        }
    }
}

/**
 * Single option item in a popover menu.
 */
@Composable
private fun PopoverOptionItem(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) TandemPrimaryContainer else Color.Transparent
    val contentColor = if (isSelected) GoalPrimary else GoalTextMain

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 16.sp
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(18.dp),
                tint = GoalPrimary
            )
        } else {
            Spacer(modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Footer submit button.
 */
@Composable
private fun FooterButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (enabled) GoalTextMain else GoalTextMain.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = if (enabled) 4.dp else 0.dp,
                    shape = RoundedCornerShape(percent = 50),
                    ambientColor = Color(0x334A4238),
                    spotColor = Color(0x334A4238)
                )
                .clickable(enabled = enabled, onClick = onClick),
            shape = RoundedCornerShape(percent = 50),
            color = backgroundColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Let's grow together",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
