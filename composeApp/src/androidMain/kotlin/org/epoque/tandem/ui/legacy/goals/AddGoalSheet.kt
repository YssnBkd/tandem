package org.epoque.tandem.ui.legacy.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.GoalType

/**
 * Bottom sheet for creating a new goal.
 * Includes name, icon picker, type selector, and duration selector.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGoalSheet(
    name: String,
    icon: String,
    type: GoalType,
    durationWeeks: Int?,
    isCreating: Boolean,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onTypeChange: (GoalType) -> Unit,
    onDurationChange: (Int?) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Goal",
                style = MaterialTheme.typography.headlineSmall
            )

            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Goal name") },
                placeholder = { Text("e.g., Exercise regularly") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Icon picker
            Column {
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val emojis = listOf("\uD83C\uDFAF", "\uD83D\uDCAA", "\uD83D\uDCDA", "\uD83C\uDFC3", "\uD83E\uDDD8", "\uD83D\uDCBC", "\uD83C\uDFA8", "\uD83C\uDFB5", "\u2764\uFE0F", "\u2B50", "\uD83C\uDF1F", "\uD83D\uDE80")
                    emojis.forEach { emoji ->
                        FilterChip(
                            selected = icon == emoji,
                            onClick = { onIconChange(emoji) },
                            label = { Text(emoji) }
                        )
                    }
                }
            }

            // Goal type selector
            GoalTypeSelector(
                selectedType = type,
                onTypeSelected = onTypeChange
            )

            // Duration selector
            DurationSelector(
                selectedDuration = durationWeeks,
                onDurationSelected = onDurationChange
            )

            // Create button
            Button(
                onClick = onCreate,
                enabled = name.isNotBlank() && !isCreating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Goal")
                }
            }
        }
    }
}

@Composable
private fun GoalTypeSelector(
    selectedType: GoalType,
    onTypeSelected: (GoalType) -> Unit
) {
    Column {
        Text(
            text = "Goal type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedType is GoalType.WeeklyHabit,
                onClick = { onTypeSelected(GoalType.WeeklyHabit(3)) },
                label = { Text("Weekly") }
            )
            FilterChip(
                selected = selectedType is GoalType.RecurringTask,
                onClick = { onTypeSelected(GoalType.RecurringTask) },
                label = { Text("Recurring") }
            )
            FilterChip(
                selected = selectedType is GoalType.TargetAmount,
                onClick = { onTypeSelected(GoalType.TargetAmount(10)) },
                label = { Text("Target") }
            )
        }

        // Target input for applicable types
        when (selectedType) {
            is GoalType.WeeklyHabit -> {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedType.targetPerWeek.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { target ->
                            if (target in 1..99) {
                                onTypeSelected(GoalType.WeeklyHabit(target))
                            }
                        }
                    },
                    label = { Text("Times per week") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(150.dp)
                )
            }
            is GoalType.TargetAmount -> {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedType.targetTotal.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { target ->
                            if (target in 1..9999) {
                                onTypeSelected(GoalType.TargetAmount(target))
                            }
                        }
                    },
                    label = { Text("Total target") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(150.dp)
                )
            }
            is GoalType.RecurringTask -> { /* No additional input */ }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationSelector(
    selectedDuration: Int?,
    onDurationSelected: (Int?) -> Unit
) {
    Column {
        Text(
            text = "Duration",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                4 to "4 weeks",
                8 to "8 weeks",
                12 to "12 weeks",
                null to "Ongoing"
            ).forEach { (weeks, label) ->
                FilterChip(
                    selected = selectedDuration == weeks,
                    onClick = { onDurationSelected(weeks) },
                    label = { Text(label) }
                )
            }
        }
    }
}
