package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.epoque.tandem.presentation.progress.ReviewSummaryUiModel
import org.epoque.tandem.ui.theme.TandemSpacing
import org.epoque.tandem.ui.theme.TandemTextStyles
import org.epoque.tandem.ui.theme.TandemTypography

/**
 * Card displaying review summary for a single user.
 *
 * Shows name, mood emoji, completion bar, and optional reflection note.
 * Uses design tokens for consistent styling.
 */
@Composable
fun ReviewSummaryCard(
    review: ReviewSummaryUiModel,
    modifier: Modifier = Modifier
) {
    val accessibilityLabel = buildString {
        append("${review.name}'s review: ${review.completionText} completed")
        review.moodEmoji?.let { append(", mood: $it") }
        if (!review.isReviewed) append(", not yet reviewed")
    }

    Card(
        modifier = modifier.semantics { contentDescription = accessibilityLabel },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TandemSpacing.Card.padding),
            verticalArrangement = Arrangement.spacedBy(TandemSpacing.xs)
        ) {
            // Header: Name and mood emoji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.name,
                    style = TandemTextStyles.Title.card,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                review.moodEmoji?.let { emoji ->
                    Text(
                        text = emoji,
                        style = TandemTypography.titleMedium
                    )
                }
            }

            // Completion progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(TandemSpacing.xxs)
            ) {
                LinearProgressIndicator(
                    progress = { review.completionPercentage / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Text(
                    text = review.completionText,
                    style = TandemTextStyles.Label.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reflection note (if present)
            review.note?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    text = note,
                    style = TandemTextStyles.Body.secondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }

            // Not reviewed indicator
            if (!review.isReviewed) {
                Text(
                    text = "Not yet reviewed",
                    style = TandemTextStyles.Label.small,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
