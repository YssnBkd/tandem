package org.epoque.tandem.ui.partner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Settings screen for managing partner connection.
 *
 * Following Material Design 3 best practices:
 * - Minimum 48dp touch targets
 * - Accessible content descriptions
 * - Confirmation dialog for destructive actions
 *
 * @param partnerName The connected partner's name
 * @param connectedSince When the partnership was created
 * @param showDisconnectDialog Whether to show the disconnect confirmation dialog
 * @param isDisconnecting Whether the disconnect action is in progress
 * @param onShowDisconnectDialog Callback to show disconnect dialog
 * @param onDismissDisconnectDialog Callback to dismiss disconnect dialog
 * @param onConfirmDisconnect Callback to confirm disconnect
 * @param onNavigateBack Callback to navigate back
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerSettingsScreen(
    partnerName: String,
    connectedSince: Instant,
    showDisconnectDialog: Boolean,
    isDisconnecting: Boolean,
    onShowDisconnectDialog: () -> Unit,
    onDismissDisconnectDialog: () -> Unit,
    onConfirmDisconnect: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Partner Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Go back"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Partner info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Connected with",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = partnerName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Since ${formatDate(connectedSince)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Disconnect button
            Button(
                onClick = onShowDisconnectDialog,
                enabled = !isDisconnecting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = "Disconnect from partner"
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Disconnect")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Disconnecting will end the partnership. You can reconnect later with a new invite.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Disconnect confirmation dialog
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = onDismissDisconnectDialog,
            title = { Text("Disconnect from $partnerName?") },
            text = {
                Text("This will end your partnership. Any shared tasks and pending requests will be removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDisconnect,
                    enabled = !isDisconnecting,
                    modifier = Modifier.semantics {
                        contentDescription = "Confirm disconnect"
                    }
                ) {
                    Text(
                        text = "Disconnect",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissDisconnectDialog,
                    enabled = !isDisconnecting,
                    modifier = Modifier.semantics {
                        contentDescription = "Cancel"
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDate(instant: Instant): String {
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${localDate.dayOfMonth}, ${localDate.year}"
}
