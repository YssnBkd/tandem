package org.epoque.tandem.ui.legacy.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.epoque.tandem.presentation.week.model.TaskUiModel
import org.epoque.tandem.ui.legacy.planning.components.PlanningCard

/**
 * Screen for reviewing partner task requests.
 *
 * Shows each request as a full-screen card with Accept/Discuss buttons.
 * Automatically advances when all requests are processed.
 *
 * Following Material Design 3 best practices:
 * - Full-screen card display for focus
 * - Clear primary (Accept) and secondary (Discuss) actions
 * - 48dp+ touch targets per FR-023
 * - Accessible content descriptions per FR-024
 * - Progress indicator showing current position
 *
 * @param currentRequest The request currently being reviewed (null if all processed)
 * @param currentIndex Current position in the list (0-based)
 * @param totalRequests Total number of requests to review
 * @param onAccept Callback when user accepts the request
 * @param onDiscuss Callback when user wants to discuss (placeholder)
 * @param onStepComplete Callback when all requests are processed
 * @param modifier Modifier for customization
 */
@Composable
fun PartnerRequestsStepScreen(
    currentRequest: TaskUiModel?,
    currentIndex: Int,
    totalRequests: Int,
    onAccept: () -> Unit,
    onDiscuss: () -> Unit,
    onStepComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-complete when all requests processed
    LaunchedEffect(currentIndex, totalRequests) {
        if (currentIndex >= totalRequests && totalRequests > 0) {
            onStepComplete()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // No partner requests - show empty state
            totalRequests == 0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No partner requests",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your partner hasn't requested any tasks from you this week",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onStepComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics {
                                contentDescription = "Continue to next step"
                            }
                    ) {
                        Text("Continue")
                    }
                }
            }

            // Current request to review
            currentRequest != null -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Progress text
                    Text(
                        text = "Request ${currentIndex + 1} of $totalRequests",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Request card
                    PlanningCard(
                        title = currentRequest.title,
                        notes = currentRequest.notes,
                        primaryAction = {
                            Button(
                                onClick = onAccept,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .semantics {
                                        contentDescription = "Accept this task request"
                                    }
                            ) {
                                Text("Accept")
                            }
                        },
                        secondaryAction = {
                            OutlinedButton(
                                onClick = onDiscuss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .semantics {
                                        contentDescription = "Discuss this request with partner"
                                    }
                            ) {
                                Text("Discuss")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // All requests processed - waiting for navigation
            else -> {
                // This state should be brief as onStepComplete will be called
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "All requests reviewed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
