package org.epoque.tandem.ui.legacy.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.progress.ReviewSummaryUiModel

/**
 * Side-by-side display of user and partner review summaries.
 *
 * Shows user card on left, partner card on right (if available).
 * Cards take equal width when both present.
 */
@Composable
fun ReviewSummaryCards(
    userReview: ReviewSummaryUiModel,
    partnerReview: ReviewSummaryUiModel?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReviewSummaryCard(
            review = userReview,
            modifier = Modifier.weight(1f)
        )

        partnerReview?.let { partner ->
            ReviewSummaryCard(
                review = partner,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
