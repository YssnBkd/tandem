package org.epoque.tandem.ui.review.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Rating data for each emoji option.
 */
private data class EmojiRating(
    val value: Int,
    val emoji: String,
    val description: String
)

/**
 * 5-point emoji rating scale: 😫 😕 😐 🙂 🎉 (maps to 1-5)
 *
 * Design rationale:
 * - Intuitive: Emotions are universal, no explanation needed
 * - Celebration Over Judgment: Emojis feel lighter than numbers/stars
 * - Quick selection: Large touch targets (≥48dp), single tap
 * - Accessibility: Each emoji has content description for screen readers
 */
private val RATINGS = listOf(
    EmojiRating(1, "😫", "Terrible week"),
    EmojiRating(2, "😕", "Bad week"),
    EmojiRating(3, "😐", "Okay week"),
    EmojiRating(4, "🙂", "Good week"),
    EmojiRating(5, "🎉", "Great week")
)

/**
 * Emoji-based rating selector for week satisfaction.
 *
 * @param selectedRating Currently selected rating (1-5), or null if none
 * @param onRatingSelected Callback when user selects a rating
 * @param modifier Modifier for the component
 */
@Composable
fun EmojiRatingSelector(
    selectedRating: Int?,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        RATINGS.forEach { rating ->
            EmojiButton(
                emoji = rating.emoji,
                contentDescription = rating.description,
                isSelected = selectedRating == rating.value,
                onClick = { onRatingSelected(rating.value) }
            )
        }
    }
}

/**
 * Individual emoji button with selection state.
 * Minimum touch target: 64dp (exceeds 48dp requirement)
 */
@Composable
private fun EmojiButton(
    emoji: String,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
        }
    }
}
