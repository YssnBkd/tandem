# Quickstart: Partner System

**Feature**: 006-partner-system
**Date**: 2026-01-04

This guide provides copy-paste code snippets for implementing the Partner System feature.

## 1. Domain Models

### Partnership.kt
```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Partnership.kt
package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Partnership(
    val id: String,
    val user1Id: String,
    val user2Id: String,
    val createdAt: Instant,
    val status: PartnershipStatus
)

@Serializable
enum class PartnershipStatus {
    ACTIVE,
    DISSOLVED
}

@Serializable
data class Partner(
    val id: String,
    val name: String,
    val email: String,
    val partnershipId: String,
    val connectedAt: Instant
)
```

### Invite.kt
```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/model/Invite.kt
package org.epoque.tandem.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Invite(
    val code: String,
    val creatorId: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val acceptedBy: String?,
    val acceptedAt: Instant?,
    val status: InviteStatus
) {
    val link: String get() = "https://tandem.app/invite/$code"
    val isExpired: Boolean get() = status == InviteStatus.EXPIRED
    val isPending: Boolean get() = status == InviteStatus.PENDING
}

@Serializable
enum class InviteStatus {
    PENDING,
    ACCEPTED,
    EXPIRED,
    CANCELLED
}

@Serializable
data class InviteInfo(
    val code: String,
    val creatorName: String,
    val creatorTaskPreview: List<TaskPreview>,
    val expiresAt: Instant
)

@Serializable
data class TaskPreview(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)
```

---

## 2. Repository Interfaces

### PartnerRepository.kt
```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/PartnerRepository.kt
package org.epoque.tandem.domain.repository

import kotlinx.coroutines.flow.Flow
import org.epoque.tandem.domain.model.Partner
import org.epoque.tandem.domain.model.Partnership

interface PartnerRepository {
    suspend fun getPartner(userId: String): Partner?
    fun observePartner(userId: String): Flow<Partner?>
    suspend fun dissolvePartnership(userId: String)
    suspend fun hasPartner(userId: String): Boolean
}

sealed class PartnerException(message: String) : Exception(message) {
    object NoPartnership : PartnerException("No active partnership found")
}
```

### InviteRepository.kt
```kotlin
// shared/src/commonMain/kotlin/org/epoque/tandem/domain/repository/InviteRepository.kt
package org.epoque.tandem.domain.repository

import org.epoque.tandem.domain.model.Invite
import org.epoque.tandem.domain.model.InviteInfo
import org.epoque.tandem.domain.model.Partnership

interface InviteRepository {
    suspend fun createInvite(userId: String): Invite
    suspend fun getActiveInvite(userId: String): Invite?
    suspend fun validateInvite(code: String): InviteInfo
    suspend fun acceptInvite(code: String, acceptorId: String): Partnership
    suspend fun cancelInvite(userId: String)
}

sealed class InviteException(message: String) : Exception(message) {
    object InvalidCode : InviteException("Invalid invite code")
    object Expired : InviteException("Invite has expired")
    object SelfInvite : InviteException("Cannot accept your own invite")
    object AlreadyHasPartner : InviteException("Already has an active partnership")
}
```

---

## 3. Presentation Layer

### PartnerUiState.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/partner/PartnerUiState.kt
package org.epoque.tandem.presentation.partner

import org.epoque.tandem.domain.model.Invite
import org.epoque.tandem.domain.model.InviteInfo
import org.epoque.tandem.domain.model.Partner

data class PartnerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Partner state
    val partner: Partner? = null,
    val hasPartner: Boolean = false,

    // Invite state
    val activeInvite: Invite? = null,
    val hasActiveInvite: Boolean = false,

    // Invite acceptance (from deep link)
    val inviteInfo: InviteInfo? = null,
    val isAcceptingInvite: Boolean = false,

    // Task request
    val showRequestTaskSheet: Boolean = false,
    val requestTaskTitle: String = "",
    val requestTaskNote: String = "",
    val isSubmittingRequest: Boolean = false,

    // Disconnect
    val showDisconnectDialog: Boolean = false,
    val isDisconnecting: Boolean = false
) {
    val inviteLink: String? get() = activeInvite?.link
    val canSubmitRequest: Boolean get() = requestTaskTitle.isNotBlank() && !isSubmittingRequest
}
```

### PartnerEvent.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/partner/PartnerEvent.kt
package org.epoque.tandem.presentation.partner

sealed interface PartnerEvent {
    // Invite
    object GenerateInvite : PartnerEvent
    object ShareInvite : PartnerEvent
    object CancelInvite : PartnerEvent

    // Accept invite (from deep link)
    data class LoadInvite(val code: String) : PartnerEvent
    object AcceptInvite : PartnerEvent
    object DeclineInvite : PartnerEvent

    // Task request
    object ShowRequestTaskSheet : PartnerEvent
    object DismissRequestTaskSheet : PartnerEvent
    data class UpdateRequestTitle(val title: String) : PartnerEvent
    data class UpdateRequestNote(val note: String) : PartnerEvent
    object SubmitTaskRequest : PartnerEvent

    // Disconnect
    object ShowDisconnectDialog : PartnerEvent
    object DismissDisconnectDialog : PartnerEvent
    object ConfirmDisconnect : PartnerEvent

    // Error handling
    object DismissError : PartnerEvent
}
```

### PartnerSideEffect.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/partner/PartnerSideEffect.kt
package org.epoque.tandem.presentation.partner

sealed interface PartnerSideEffect {
    data class ShowShareSheet(val link: String) : PartnerSideEffect
    object NavigateToConfirmation : PartnerSideEffect
    object NavigateToHome : PartnerSideEffect
    object NavigateBack : PartnerSideEffect
    data class ShowError(val message: String) : PartnerSideEffect
    data class ShowSnackbar(val message: String) : PartnerSideEffect
}
```

### PartnerViewModel.kt
```kotlin
// composeApp/src/commonMain/kotlin/org/epoque/tandem/presentation/partner/PartnerViewModel.kt
package org.epoque.tandem.presentation.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.epoque.tandem.domain.model.Partner
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.repository.*
import org.epoque.tandem.presentation.auth.AuthState

class PartnerViewModel(
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository,
    private val inviteRepository: InviteRepository,
    private val taskRepository: TaskRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartnerUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = Channel<PartnerSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private var realtimeJob: Job? = null
    private var currentPartner: Partner? = null

    init {
        initializePartner()
    }

    private fun initializePartner() {
        viewModelScope.launch {
            try {
                // Wait for authentication
                val userId = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                    .user.id

                // Check for existing partnership
                val partner = partnerRepository.getPartner(userId)
                currentPartner = partner

                // Setup realtime if partner exists
                if (partner != null) {
                    setupRealtimeSync(partner.id)
                }

                // Check for active invite
                val invite = inviteRepository.getActiveInvite(userId)

                _uiState.update {
                    it.copy(
                        partner = partner,
                        hasPartner = partner != null,
                        activeInvite = invite,
                        hasActiveInvite = invite != null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onEvent(event: PartnerEvent) {
        when (event) {
            // Invite events
            PartnerEvent.GenerateInvite -> generateInvite()
            PartnerEvent.ShareInvite -> shareInvite()
            PartnerEvent.CancelInvite -> cancelInvite()

            // Accept invite events
            is PartnerEvent.LoadInvite -> loadInvite(event.code)
            PartnerEvent.AcceptInvite -> acceptInvite()
            PartnerEvent.DeclineInvite -> declineInvite()

            // Task request events
            PartnerEvent.ShowRequestTaskSheet -> showRequestSheet()
            PartnerEvent.DismissRequestTaskSheet -> dismissRequestSheet()
            is PartnerEvent.UpdateRequestTitle -> updateRequestTitle(event.title)
            is PartnerEvent.UpdateRequestNote -> updateRequestNote(event.note)
            PartnerEvent.SubmitTaskRequest -> submitTaskRequest()

            // Disconnect events
            PartnerEvent.ShowDisconnectDialog -> showDisconnectDialog()
            PartnerEvent.DismissDisconnectDialog -> dismissDisconnectDialog()
            PartnerEvent.ConfirmDisconnect -> confirmDisconnect()

            PartnerEvent.DismissError -> dismissError()
        }
    }

    private fun generateInvite() {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                val invite = inviteRepository.createInvite(userId)
                _uiState.update {
                    it.copy(activeInvite = invite, hasActiveInvite = true)
                }
            } catch (e: InviteException.AlreadyHasPartner) {
                _sideEffects.send(PartnerSideEffect.ShowError("You already have a partner"))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun shareInvite() {
        val link = _uiState.value.inviteLink ?: return
        viewModelScope.launch {
            _sideEffects.send(PartnerSideEffect.ShowShareSheet(link))
        }
    }

    private fun loadInvite(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val info = inviteRepository.validateInvite(code)
                _uiState.update {
                    it.copy(inviteInfo = info, isLoading = false)
                }
            } catch (e: InviteException.InvalidCode) {
                _uiState.update { it.copy(error = "Invalid invite link", isLoading = false) }
            } catch (e: InviteException.Expired) {
                _uiState.update { it.copy(error = "This invite has expired", isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun acceptInvite() {
        val info = _uiState.value.inviteInfo ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAcceptingInvite = true) }
            try {
                val userId = getCurrentUserId()
                val partnership = inviteRepository.acceptInvite(info.code, userId)

                // Get partner details
                val partner = partnerRepository.getPartner(userId)
                currentPartner = partner

                if (partner != null) {
                    setupRealtimeSync(partner.id)
                }

                _uiState.update {
                    it.copy(
                        partner = partner,
                        hasPartner = true,
                        inviteInfo = null,
                        isAcceptingInvite = false
                    )
                }
                _sideEffects.send(PartnerSideEffect.NavigateToConfirmation)
            } catch (e: InviteException.SelfInvite) {
                _uiState.update {
                    it.copy(error = "You cannot accept your own invite", isAcceptingInvite = false)
                }
            } catch (e: InviteException.AlreadyHasPartner) {
                _uiState.update {
                    it.copy(error = "You already have a partner", isAcceptingInvite = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isAcceptingInvite = false) }
            }
        }
    }

    private fun declineInvite() {
        _uiState.update { it.copy(inviteInfo = null) }
        viewModelScope.launch {
            _sideEffects.send(PartnerSideEffect.NavigateBack)
        }
    }

    private fun submitTaskRequest() {
        val partner = currentPartner ?: return
        val title = _uiState.value.requestTaskTitle
        val note = _uiState.value.requestTaskNote.takeIf { it.isNotBlank() }

        if (title.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingRequest = true) }
            try {
                val userId = getCurrentUserId()
                taskRepository.createTaskRequest(
                    title = title,
                    note = note,
                    ownerId = partner.id,
                    createdBy = userId
                )
                _uiState.update {
                    it.copy(
                        showRequestTaskSheet = false,
                        requestTaskTitle = "",
                        requestTaskNote = "",
                        isSubmittingRequest = false
                    )
                }
                _sideEffects.send(PartnerSideEffect.ShowSnackbar("Request sent to ${partner.name}"))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSubmittingRequest = false) }
            }
        }
    }

    private fun confirmDisconnect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDisconnecting = true) }
            try {
                val userId = getCurrentUserId()
                partnerRepository.dissolvePartnership(userId)

                // Cleanup realtime
                realtimeJob?.cancel()
                currentPartner = null

                _uiState.update {
                    it.copy(
                        partner = null,
                        hasPartner = false,
                        showDisconnectDialog = false,
                        isDisconnecting = false
                    )
                }
                _sideEffects.send(PartnerSideEffect.ShowSnackbar("Partnership ended"))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isDisconnecting = false) }
            }
        }
    }

    private fun setupRealtimeSync(partnerId: String) {
        realtimeJob?.cancel()

        val channel = supabase.channel("partner-tasks-$partnerId")

        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "Task"
            filter = "owner_id=eq.$partnerId"
        }

        realtimeJob = changes
            .onEach { action ->
                when (action) {
                    is PostgresAction.Insert -> handlePartnerTaskCreated(action.record)
                    is PostgresAction.Update -> handlePartnerTaskUpdated(action.record)
                    is PostgresAction.Delete -> handlePartnerTaskDeleted(action.oldRecord)
                    else -> {}
                }
            }
            .catch { e ->
                // Handle realtime errors
                _uiState.update { it.copy(error = "Sync error: ${e.message}") }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            channel.subscribe()
        }
    }

    private suspend fun handlePartnerTaskCreated(record: Task) {
        // Notify UI of new partner task
        _sideEffects.send(
            PartnerSideEffect.ShowSnackbar("${currentPartner?.name} added: ${record.title}")
        )
    }

    private suspend fun handlePartnerTaskUpdated(record: Task) {
        if (record.status.isCompleted) {
            _sideEffects.send(
                PartnerSideEffect.ShowSnackbar("${currentPartner?.name} completed: ${record.title}")
            )
        }
    }

    private suspend fun handlePartnerTaskDeleted(record: Task) {
        // Task deleted - no notification needed
    }

    // Helper functions
    private fun showRequestSheet() {
        _uiState.update { it.copy(showRequestTaskSheet = true) }
    }

    private fun dismissRequestSheet() {
        _uiState.update {
            it.copy(showRequestTaskSheet = false, requestTaskTitle = "", requestTaskNote = "")
        }
    }

    private fun updateRequestTitle(title: String) {
        _uiState.update { it.copy(requestTaskTitle = title) }
    }

    private fun updateRequestNote(note: String) {
        _uiState.update { it.copy(requestTaskNote = note) }
    }

    private fun showDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = true) }
    }

    private fun dismissDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = false) }
    }

    private fun cancelInvite() {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                inviteRepository.cancelInvite(userId)
                _uiState.update { it.copy(activeInvite = null, hasActiveInvite = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun getCurrentUserId(): String {
        return authRepository.authState
            .filterIsInstance<AuthState.Authenticated>()
            .first()
            .user.id
    }

    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
    }
}
```

---

## 4. UI Components

### InvitePartnerScreen.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/partner/InvitePartnerScreen.kt
package org.epoque.tandem.ui.partner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun InvitePartnerScreen(
    hasActiveInvite: Boolean,
    inviteLink: String?,
    onGenerateInvite: () -> Unit,
    onShareInvite: () -> Unit,
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
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onShareInvite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Invite Link")
            }
        } else {
            // Generate new invite
            Button(
                onClick = onGenerateInvite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Invite Link")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSkip) {
            Text("I'll do this later")
        }
    }
}
```

### PartnerLandingScreen.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/partner/PartnerLandingScreen.kt
package org.epoque.tandem.ui.partner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.epoque.tandem.domain.model.InviteInfo
import org.epoque.tandem.domain.model.TaskPreview

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
                    TextButton(onClick = onDecline) {
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Connect with ${inviteInfo.creatorName}")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDecline,
                        enabled = !isAccepting
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
```

### RequestTaskSheet.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/partner/RequestTaskSheet.kt
package org.epoque.tandem.ui.partner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestTaskSheet(
    partnerName: String,
    title: String,
    note: String,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Request a Task",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("What do you need help with?") },
                placeholder = { Text("e.g., Pick up groceries") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Add a note (optional)") },
                placeholder = { Text("Any details or context...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$partnerName will see this as a request",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send Request")
                }
            }
        }
    }
}
```

---

## 5. Navigation

### PartnerNavGraph.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/ui/navigation/PartnerNavGraph.kt
package org.epoque.tandem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.epoque.tandem.presentation.partner.PartnerEvent
import org.epoque.tandem.presentation.partner.PartnerUiState
import org.epoque.tandem.ui.partner.*

/**
 * Navigation graph for partner-related screens.
 *
 * Uses stateProvider lambda to avoid state capture issues.
 */
fun NavGraphBuilder.partnerNavGraph(
    navController: NavController,
    stateProvider: () -> PartnerUiState,
    onEvent: (PartnerEvent) -> Unit
) {
    composable<Routes.Partner.Invite> {
        val state = stateProvider()
        InvitePartnerScreen(
            hasActiveInvite = state.hasActiveInvite,
            inviteLink = state.inviteLink,
            onGenerateInvite = { onEvent(PartnerEvent.GenerateInvite) },
            onShareInvite = { onEvent(PartnerEvent.ShareInvite) },
            onSkip = { navController.popBackStack() }
        )
    }

    composable<Routes.Partner.AcceptInvite> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Partner.AcceptInvite>()
        val state = stateProvider()

        // Load invite on entry
        androidx.compose.runtime.LaunchedEffect(route.code) {
            onEvent(PartnerEvent.LoadInvite(route.code))
        }

        PartnerLandingScreen(
            inviteInfo = state.inviteInfo,
            isLoading = state.isLoading,
            isAccepting = state.isAcceptingInvite,
            error = state.error,
            onAccept = { onEvent(PartnerEvent.AcceptInvite) },
            onDecline = { onEvent(PartnerEvent.DeclineInvite) }
        )
    }

    composable<Routes.Partner.Confirmation> {
        val state = stateProvider()
        ConnectionConfirmationScreen(
            partnerName = state.partner?.name ?: "",
            onPlanWeek = {
                navController.navigate(Routes.Planning.Start) {
                    popUpTo(Routes.Partner.Confirmation) { inclusive = true }
                }
            },
            onDone = {
                navController.navigate(Routes.Main.Home) {
                    popUpTo(Routes.Partner.Confirmation) { inclusive = true }
                }
            }
        )
    }
}
```

### Routes Extension
```kotlin
// Add to Routes.kt
sealed interface Partner : Routes {
    @Serializable
    data object Invite : Partner

    @Serializable
    data class AcceptInvite(val code: String) : Partner

    @Serializable
    data object Confirmation : Partner

    @Serializable
    data object Settings : Partner
}
```

---

## 6. Koin Module

### PartnerModule.kt
```kotlin
// composeApp/src/androidMain/kotlin/org/epoque/tandem/di/PartnerModule.kt
package org.epoque.tandem.di

import org.epoque.tandem.data.repository.InviteRepositoryImpl
import org.epoque.tandem.data.repository.PartnerRepositoryImpl
import org.epoque.tandem.domain.repository.InviteRepository
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.presentation.partner.PartnerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val partnerModule = module {
    // Repositories
    single<PartnerRepository> { PartnerRepositoryImpl(get(), get()) }
    single<InviteRepository> { InviteRepositoryImpl(get(), get()) }

    // ViewModel
    viewModel {
        PartnerViewModel(
            authRepository = get(),
            partnerRepository = get(),
            inviteRepository = get(),
            taskRepository = get(),
            supabase = get()
        )
    }
}
```

---

## 7. Deep Link Handling

### MainActivity Update
```kotlin
// In MainActivity.onCreate()
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Handle deep link
    handleDeepLink(intent)

    setContent { /* ... */ }
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleDeepLink(intent)
}

private fun handleDeepLink(intent: Intent?) {
    intent?.data?.let { uri ->
        if (uri.host == "tandem.app" && uri.pathSegments.firstOrNull() == "invite") {
            val code = uri.pathSegments.getOrNull(1)
            if (code != null) {
                // Navigate to invite acceptance
                // This will be handled by TandemNavHost
            }
        }
    }
}
```

---

## 8. AndroidManifest Updates

```xml
<!-- Add to MainActivity -->
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="https"
        android:host="tandem.app"
        android:pathPrefix="/invite/" />
</intent-filter>
```

---

## Summary

This quickstart provides the essential code for:

1. **Domain Models**: Partnership, Invite, Partner
2. **Repository Interfaces**: PartnerRepository, InviteRepository
3. **ViewModel**: Complete MVI implementation with realtime sync
4. **UI Screens**: Invite, Landing, Request Task Sheet
5. **Navigation**: Type-safe routes with stateProvider pattern
6. **DI**: Koin module configuration
7. **Deep Linking**: AndroidManifest and intent handling

Refer to `data-model.md` for schema details and `contracts/partner-api.md` for Supabase setup.
