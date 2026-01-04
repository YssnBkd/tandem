package org.epoque.tandem.ui.partner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.InviteInfo
import org.epoque.tandem.domain.model.TaskPreview

/**
 * Landing screen shown when a user opens an invite link.
 * Shows the inviter's name, their task preview, and connect/decline options.
 *
 * Following Material Design 3 best practices:
 * - Minimum 48dp touch targets for all interactive elements
 * - Accessible content descriptions
 * - Loading and error states handled
 *
 * @param inviteInfo The validated invite information
 * @param isLoading Whether the invite is being loaded
 * @param isAccepting Whether the invite is being accepted
 * @param error Error message to display (if any)
 * @param onAccept Callback when user taps Connect button
 * @param onDecline Callback when user taps Not now
 * @param modifier Modifier for customization
 */
@Composable
fun PartnerLandingScreen(
    inviteInfo: InviteInfo?,
    isLoading: Boolean,
    isAccepting: Boolean,
    error: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .height(48.dp)
                            .semantics {
                                contentDescription = "Go back"
                            }
                    ) {
                        Text("Go Back")
                    }
                }
            }
            inviteInfo != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${inviteInfo.creatorName} invited you!",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Here's what they're working on this week:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Task preview
                    if (inviteInfo.creatorTaskPreview.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(inviteInfo.creatorTaskPreview) { task ->
                                TaskPreviewCard(task)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks yet - you can plan together!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onAccept,
                        enabled = !isAccepting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .semantics {
                                contentDescription = "Connect with ${inviteInfo.creatorName}"
                            }
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Connect with ${inviteInfo.creatorName}")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDecline,
                        enabled = !isAccepting,
                        modifier = Modifier
                            .height(48.dp)
                            .semantics {
                                contentDescription = "Decline invite"
                            }
                    ) {
                        Text("Not now")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskPreviewCard(task: TaskPreview) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = null,
                enabled = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
