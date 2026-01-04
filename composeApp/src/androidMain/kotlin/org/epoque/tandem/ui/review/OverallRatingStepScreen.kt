package org.epoque.tandem.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.epoque.tandem.ui.review.components.EmojiRatingSelector

/**
 * Overall rating step screen - Step 1 of the review wizard.
 *
 * Users rate their overall week satisfaction using a 5-emoji scale
 * and can optionally add a note about their week.
 *
 * Requirements:
 * - FR-005: 5-point emoji scale
 * - FR-006: Rating required before Continue
 * - FR-007: Optional note field
 *
 * @param selectedRating Currently selected rating (1-5), or null
 * @param note Optional note text
 * @param canContinue Whether the Continue button should be enabled
 * @param onRatingSelected Callback when user selects a rating
 * @param onNoteChanged Callback when user updates the note
 * @param onContinue Callback when user taps Continue
 * @param onQuickFinish Callback when user taps Quick Finish
 */
@Composable
fun OverallRatingStepScreen(
    selectedRating: Int?,
    note: String,
    canContinue: Boolean,
    onRatingSelected: (Int) -> Unit,
    onNoteChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onQuickFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "How was your week?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Emoji rating selector
        EmojiRatingSelector(
            selectedRating = selectedRating,
            onRatingSelected = onRatingSelected
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Optional note field
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChanged,
            label = { Text("Add a note about your week (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            singleLine = false
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Continue button - enabled only when rating is selected
            Button(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Quick Finish option
            TextButton(
                onClick = onQuickFinish
            ) {
                Text(
                    text = "Quick Finish",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
