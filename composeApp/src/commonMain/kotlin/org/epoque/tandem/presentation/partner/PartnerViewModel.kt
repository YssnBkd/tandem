package org.epoque.tandem.presentation.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.epoque.tandem.domain.model.OwnerType
import org.epoque.tandem.domain.model.Partner
import org.epoque.tandem.domain.model.Task
import org.epoque.tandem.domain.model.TaskPriority
import org.epoque.tandem.domain.model.TaskStatus
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import org.epoque.tandem.domain.repository.InviteException
import org.epoque.tandem.domain.repository.InviteRepository
import org.epoque.tandem.domain.repository.PartnerException
import org.epoque.tandem.domain.repository.PartnerRepository
import org.epoque.tandem.domain.repository.TaskRepository
import org.epoque.tandem.domain.repository.WeekRepository

/**
 * ViewModel for the Partner screen.
 *
 * Follows Android best practices:
 * - Screen-level state holder (not reusable)
 * - StateFlow for reactive UI state
 * - Channel for one-time side effects
 * - viewModelScope for lifecycle-aware coroutines
 * - Unidirectional data flow (UDF) pattern
 * - No references to Context or lifecycle-aware APIs
 */
class PartnerViewModel(
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository,
    private val inviteRepository: InviteRepository,
    private val taskRepository: TaskRepository,
    private val weekRepository: WeekRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartnerUiState())
    val uiState: StateFlow<PartnerUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<PartnerSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<PartnerSideEffect> = _sideEffects.receiveAsFlow()

    private var currentPartner: Partner? = null

    init {
        initializePartner()
    }

    /**
     * Handle user events from the UI.
     * Following UDF pattern: events go UP to ViewModel.
     */
    fun onEvent(event: PartnerEvent) {
        when (event) {
            // Invite creation events
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

            // Pending request handling
            is PartnerEvent.AcceptRequest -> acceptRequest(event.taskId)
            is PartnerEvent.DeclineRequest -> declineRequest(event.taskId)

            // Disconnect events
            PartnerEvent.ShowDisconnectDialog -> showDisconnectDialog()
            PartnerEvent.DismissDisconnectDialog -> dismissDisconnectDialog()
            PartnerEvent.ConfirmDisconnect -> confirmDisconnect()

            // Error handling
            PartnerEvent.DismissError -> dismissError()
        }
    }

    /**
     * Initialize partner state on ViewModel creation.
     * Waits for authentication, then checks for existing partnership and invites.
     */
    private fun initializePartner() {
        viewModelScope.launch {
            try {
                // Wait for authentication (MUST be first)
                val userId = authRepository.authState
                    .filterIsInstance<AuthState.Authenticated>()
                    .first()
                    .user.id

                // Check for existing partnership
                val partner = partnerRepository.getPartner(userId)
                currentPartner = partner

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

                // If partner exists, observe pending requests and start realtime sync
                if (partner != null) {
                    observePendingRequests(userId)
                    partnerRepository.startPartnerTaskSync(userId, partner.id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Observe pending task requests from partner.
     */
    private fun observePendingRequests(userId: String) {
        viewModelScope.launch {
            taskRepository.observeTasksByStatus(TaskStatus.PENDING_ACCEPTANCE, userId)
                .collect { requests ->
                    _uiState.update { it.copy(pendingRequests = requests) }
                }
        }
    }

    // Invite Creation

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
            } catch (e: CancellationException) {
                throw e
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

    private fun cancelInvite() {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                inviteRepository.cancelInvite(userId)
                _uiState.update {
                    it.copy(activeInvite = null, hasActiveInvite = false)
                }
                _sideEffects.send(PartnerSideEffect.ShowSnackbar("Invite cancelled"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // Accept Invite (from deep link)

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
            } catch (e: CancellationException) {
                throw e
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

                _uiState.update {
                    it.copy(
                        partner = partner,
                        hasPartner = true,
                        inviteInfo = null,
                        isAcceptingInvite = false
                    )
                }

                // Start observing pending requests and realtime sync
                if (partner != null) {
                    observePendingRequests(userId)
                    partnerRepository.startPartnerTaskSync(userId, partner.id)
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
            } catch (e: CancellationException) {
                throw e
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

    // Task Request

    private fun showRequestSheet() {
        _uiState.update { it.copy(showRequestTaskSheet = true) }
    }

    private fun dismissRequestSheet() {
        _uiState.update {
            it.copy(
                showRequestTaskSheet = false,
                requestTaskTitle = "",
                requestTaskNote = ""
            )
        }
    }

    private fun updateRequestTitle(title: String) {
        _uiState.update { it.copy(requestTaskTitle = title) }
    }

    private fun updateRequestNote(note: String) {
        _uiState.update { it.copy(requestTaskNote = note) }
    }

    private fun submitTaskRequest() {
        val state = _uiState.value
        if (!state.canSubmitRequest) return

        val partner = currentPartner ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingRequest = true) }
            try {
                val userId = getCurrentUserId()
                val weekId = weekRepository.getCurrentWeekId()

                val task = Task(
                    id = "",  // Generated by repository
                    title = state.requestTaskTitle.trim(),
                    notes = null,
                    ownerId = partner.id,  // Partner owns the task
                    ownerType = OwnerType.SELF,
                    weekId = weekId,
                    status = TaskStatus.PENDING_ACCEPTANCE,
                    createdBy = userId,  // Current user created it
                    requestNote = state.requestTaskNote.trim().takeIf { it.isNotEmpty() },
                    repeatTarget = null,
                    repeatCompleted = 0,
                    linkedGoalId = null,
                    reviewNote = null,
                    rolledFromWeekId = null,
                    priority = TaskPriority.P4,
                    scheduledDate = null,
                    scheduledTime = null,
                    deadline = null,
                    parentTaskId = null,
                    labels = emptyList(),
                    createdAt = Instant.DISTANT_PAST,
                    updatedAt = Instant.DISTANT_PAST
                )

                taskRepository.createTask(task)

                _uiState.update {
                    it.copy(
                        showRequestTaskSheet = false,
                        requestTaskTitle = "",
                        requestTaskNote = "",
                        isSubmittingRequest = false
                    )
                }
                _sideEffects.send(PartnerSideEffect.ShowSnackbar("Task request sent to ${partner.name}"))
                _sideEffects.send(PartnerSideEffect.TriggerHapticFeedback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmittingRequest = false, error = e.message) }
            }
        }
    }

    // Pending Request Handling

    private fun acceptRequest(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskStatus(taskId, TaskStatus.PENDING)
                _sideEffects.send(PartnerSideEffect.ShowSnackbar("Task accepted"))
                _sideEffects.send(PartnerSideEffect.TriggerHapticFeedback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(PartnerSideEffect.ShowError("Failed to accept task"))
            }
        }
    }

    private fun declineRequest(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
                _sideEffects.send(PartnerSideEffect.ShowSnackbar("Task declined"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _sideEffects.send(PartnerSideEffect.ShowError("Failed to decline task"))
            }
        }
    }

    // Disconnect

    private fun showDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = true) }
    }

    private fun dismissDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = false) }
    }

    private fun confirmDisconnect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDisconnecting = true) }
            try {
                val userId = getCurrentUserId()

                // Stop realtime sync before dissolving
                partnerRepository.stopPartnerTaskSync()

                partnerRepository.dissolvePartnership(userId)

                currentPartner = null
                _uiState.update {
                    it.copy(
                        partner = null,
                        hasPartner = false,
                        showDisconnectDialog = false,
                        isDisconnecting = false,
                        pendingRequests = emptyList()
                    )
                }
                _sideEffects.send(PartnerSideEffect.ShowSnackbar("Partnership ended"))
                _sideEffects.send(PartnerSideEffect.NavigateToHome)
            } catch (e: PartnerException.NoPartnership) {
                _uiState.update {
                    it.copy(
                        error = "No active partnership to end",
                        showDisconnectDialog = false,
                        isDisconnecting = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        showDisconnectDialog = false,
                        isDisconnecting = false
                    )
                }
            }
        }
    }

    // Error Handling

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    // Helper

    private suspend fun getCurrentUserId(): String {
        return authRepository.authState
            .filterIsInstance<AuthState.Authenticated>()
            .first()
            .user.id
    }

    // Lifecycle

    override fun onCleared() {
        super.onCleared()
        // Clean up realtime sync when ViewModel is destroyed
        viewModelScope.launch {
            partnerRepository.stopPartnerTaskSync()
        }
    }
}
