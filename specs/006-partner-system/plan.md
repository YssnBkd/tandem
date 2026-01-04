# Implementation Plan: Partner System

**Branch**: `006-partner-system` | **Date**: 2026-01-04 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/006-partner-system/spec.md`

## Summary

Implement partner invitation, connection, and real-time synchronization between coupled accounts. Partners can generate shareable invite links, connect their accounts via Universal/App Links, request tasks from each other (with explicit accept/decline workflow), and see real-time updates of partner activity.

**Technical Approach**: Supabase backend for partnership/invite storage and real-time sync, SQLDelight for local cache, Koin DI, Jetpack Compose UI with Material Design 3.

## Technical Context

**Language/Version**: Kotlin 2.1+ (Kotlin Multiplatform)
**Primary Dependencies**: Compose Multiplatform, Koin, SQLDelight, DataStore, Supabase Android SDK (Realtime)
**Storage**: SQLDelight (local cache), Supabase (remote partnership/invite data), offline-first with sync queue
**Testing**: Kotlin Test (unit), Android Instrumented Tests (UI)
**Target Platform**: Android 7.0+ (SDK 24), iOS preparation (future)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: 60 fps UI, <2s real-time sync, efficient battery usage
**Constraints**: Offline-first (core features work without network), Material Design 3 compliance, Universal Links/App Links for deep linking

**Feature-Specific Tech**:
- Supabase Realtime for partner task synchronization
- Android ShareSheet API for invite link sharing
- Firebase Cloud Messaging for push notifications
- Android App Links for deep link handling

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Core Principles Compliance

- [x] **Relationship-First Design**: Partner invites and requests strengthen connection; no surveillance mechanics
- [x] **Weekly Rhythm**: Tasks are within weekly context; partnership enables shared weekly planning
- [x] **Autonomous Partnership**: Task requests REQUIRE explicit Accept/Decline - core to FR-025/FR-026
- [x] **Celebration Over Judgment**: No shame language; connection confirmation is celebratory
- [x] **Intentional Simplicity**: No complex hierarchies; simple invite/connect/request flows

### Decision Framework

1. Does it strengthen the weekly rhythm? ✓ Partners plan and review weeks together
2. Does it respect partner autonomy? ✓ Task requests require explicit acceptance
3. Is it the simplest solution that works? ✓ Single invite code, 1:1 partnership
4. Can it work offline? ✓ Local cache with sync queue
5. Does it follow Material Design 3 patterns? ✓ Specified in tech stack

### Non-Negotiables Check

- [x] NO tracking of partner's incomplete tasks - Only completed tasks shown in real-time
- [~] NO notifications for partner's task completions (default off) - **VIOLATION DETECTED** (see Complexity Tracking)
- [x] NO assigning tasks without acceptance workflow - FR-025 requires explicit Accept/Decline
- [x] NO shame language in UI copy - Positive framing throughout
- [x] NO complex task hierarchies - Flat task model maintained

### Technical Compliance

- [x] Clean Architecture with MVI pattern
- [x] Domain layer is 100% shared code (Kotlin Multiplatform)
- [x] UI uses Jetpack Compose with Material Design 3
- [x] Offline-first architecture with SQLDelight
- [ ] Build validation: `:composeApp:compileDebugKotlinAndroid` succeeds (pending implementation)

## Project Structure

### Documentation (this feature)

```text
specs/006-partner-system/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── partner-api.md   # Supabase table/function contracts
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
shared/src/commonMain/kotlin/org/epoque/tandem/
├── domain/
│   ├── model/
│   │   ├── Partnership.kt
│   │   └── Invite.kt
│   └── repository/
│       ├── PartnerRepository.kt
│       └── InviteRepository.kt
└── data/
    └── repository/
        ├── PartnerRepositoryImpl.kt
        └── InviteRepositoryImpl.kt

composeApp/src/commonMain/kotlin/org/epoque/tandem/
└── presentation/partner/
    ├── PartnerViewModel.kt
    ├── PartnerUiState.kt
    ├── PartnerEvent.kt
    └── PartnerSideEffect.kt

composeApp/src/androidMain/kotlin/org/epoque/tandem/
├── ui/partner/
│   ├── InvitePartnerScreen.kt
│   ├── PartnerLandingScreen.kt
│   ├── ConnectionConfirmationScreen.kt
│   ├── RequestTaskSheet.kt
│   └── PartnerStatusCard.kt
├── ui/navigation/
│   └── PartnerNavGraph.kt
└── di/
    └── PartnerModule.kt
```

## Complexity Tracking

| Violation | Why Needed | Resolution |
|-----------|------------|------------|
| FR-035: Push notification for task completions | Spec requests notifications for all partner actions | **MODIFIED**: Task completion notifications will be OFF by default (user can enable in settings). This aligns with constitution while preserving user choice. |
| FR-036: Push notification for task edits | Same as above | **MODIFIED**: Task edit notifications will be OFF by default. |

**Resolution Applied**: Update FR-035 and FR-036 to specify "opt-in" behavior with default OFF, satisfying the constitution's "default off" requirement while still providing the capability for users who want it.

## Integration Points

### From Feature 001 (Core Infrastructure)
| Method/API | Purpose | Preconditions |
|------------|---------|---------------|
| `authRepository.authState` | Get authenticated user | Wait for `AuthState.Authenticated` |
| `authRepository.currentUser` | User details for invites | User authenticated |

### From Feature 002 (Task Data Layer)
| Method/API | Purpose | Preconditions |
|------------|---------|---------------|
| `taskRepository.createTask()` | Create task requests | Partnership exists |
| `taskRepository.updateTask()` | Accept/decline requests | Task has PENDING_ACCEPTANCE status |
| `Task.status` | Extended with PENDING_ACCEPTANCE | Database migration needed |
| `Task.createdBy` | Track request origin | New field addition |

### New Repositories
| Repository | Purpose | Key Methods |
|------------|---------|-------------|
| `PartnerRepository` | Partnership lifecycle | `getPartner()`, `observePartner()`, `createPartnership()`, `dissolvePartnership()` |
| `InviteRepository` | Invite management | `createInvite()`, `getActiveInvite()`, `validateInvite()`, `acceptInvite()` |

## Initialization Sequence (CRITICAL)

```kotlin
// PartnerViewModel.init
viewModelScope.launch {
    // 1. Wait for authentication
    val userId = authRepository.authState
        .filterIsInstance<AuthState.Authenticated>()
        .first()
        .user.id

    // 2. Check for existing partnership
    val partner = partnerRepository.getPartner(userId)

    // 3. Setup realtime subscription if partner exists
    if (partner != null) {
        setupRealtimeSync(partner.id)
    }

    // 4. Update state
    _uiState.update {
        it.copy(
            partner = partner,
            hasPartner = partner != null,
            isLoading = false
        )
    }
}
```

## Supabase Realtime Setup

```kotlin
private fun setupRealtimeSync(partnerId: String) {
    supabase.realtime
        .channel("partner-tasks-$partnerId")
        .on<Task>("tasks") { event ->
            when (event) {
                is PostgresAction.Insert -> handleTaskCreated(event.record)
                is PostgresAction.Update -> handleTaskUpdated(event.record)
                is PostgresAction.Delete -> handleTaskDeleted(event.oldRecord)
            }
        }
        .subscribe()
}
```

## Implementation Patterns

### Side Effect Channel - Single Collector Only
```kotlin
private val _sideEffects = Channel<PartnerSideEffect>(Channel.BUFFERED)
val sideEffects = _sideEffects.receiveAsFlow()

// In UI: Single LaunchedEffect collector
LaunchedEffect(Unit) {
    viewModel.sideEffects.collect { effect ->
        when (effect) {
            is PartnerSideEffect.NavigateToConfirmation -> navController.navigate(...)
            is PartnerSideEffect.ShowShareSheet -> shareInviteLink(effect.link)
            is PartnerSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
        }
    }
}
```

### stateProvider Pattern for Navigation
```kotlin
// Avoid state capture issues in NavGraphBuilder
fun NavGraphBuilder.partnerNavGraph(
    navController: NavController,
    stateProvider: () -> PartnerUiState,
    onEvent: (PartnerEvent) -> Unit
) {
    composable<Routes.Partner.Invite> {
        val state = stateProvider()
        InvitePartnerScreen(
            inviteLink = state.inviteLink,
            hasActiveInvite = state.hasActiveInvite,
            onGenerateInvite = { onEvent(PartnerEvent.GenerateInvite) },
            // ...
        )
    }
}
```

### UI Affordance - Always Visible Actions
All primary actions have visible buttons with 48dp minimum touch targets.
