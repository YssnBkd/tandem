package org.epoque.tandem.ui.legacy.partner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

/**
 * Screen for generating and sharing an invite link.
 *
 * Following Material Design 3 best practices:
 * - Minimum 48dp touch targets for all interactive elements
 * - Accessible content descriptions
 * - Clear visual hierarchy
 *
 * @param hasActiveInvite Whether an invite link has been generated
 * @param inviteLink The generated invite link (if any)
 * @param onGenerateInvite Callback to generate a new invite
 * @param onShareInvite Callback to share the invite link
 * @param onCopyInvite Callback to copy the invite link to clipboard
 * @param onSkip Callback when user taps "I'll do this later"
 * @param modifier Modifier for customization
 */
@Composable
fun InvitePartnerScreen(
    hasActiveInvite: Boolean,
    inviteLink: String?,
    onGenerateInvite: () -> Unit,
    onShareInvite: () -> Unit,
    onCopyInvite: () -> Unit = {},
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Invite Your Partner",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tandem works best together. Share an invite link with your partner to start planning your weeks as a team.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (hasActiveInvite && inviteLink != null) {
            // Show existing invite
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your invite link is ready:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inviteLink,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onCopyInvite,
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    contentDescription = "Copy invite link"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onShareInvite,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = "Share invite link"
                    }
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Invite Link")
            }
        } else {
            // Generate new invite
            Button(
                onClick = onGenerateInvite,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = "Generate invite link"
                    }
            ) {
                Text("Generate Invite Link")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .height(48.dp)
                .semantics {
                    contentDescription = "Skip and do this later"
                }
        ) {
            Text("I'll do this later")
        }
    }
}
