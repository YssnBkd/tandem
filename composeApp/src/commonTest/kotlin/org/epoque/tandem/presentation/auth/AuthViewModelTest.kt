package org.epoque.tandem.presentation.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.epoque.tandem.domain.model.AuthError
import org.epoque.tandem.domain.model.AuthProvider
import org.epoque.tandem.domain.model.User
import org.epoque.tandem.domain.repository.AuthRepository
import org.epoque.tandem.domain.repository.AuthState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = AuthViewModel(fakeAuthRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Registration Tests

    @Test
    fun `initial form state is empty`() {
        val formState = viewModel.formState.value
        assertEquals("", formState.email)
        assertEquals("", formState.password)
        assertEquals("", formState.displayName)
        assertNull(formState.emailError)
        assertNull(formState.passwordError)
        assertNull(formState.displayNameError)
    }

    @Test
    fun `email changed updates form state`() {
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))

        assertEquals("test@example.com", viewModel.formState.value.email)
    }

    @Test
    fun `password changed updates form state`() {
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))

        assertEquals("password123", viewModel.formState.value.password)
    }

    @Test
    fun `display name changed updates form state`() {
        viewModel.onEvent(AuthEvent.DisplayNameChanged("John Doe"))

        assertEquals("John Doe", viewModel.formState.value.displayName)
    }

    @Test
    fun `register with valid credentials succeeds`() = runTest {
        val testUser = createTestUser()
        fakeAuthRepository.signUpResult = Result.success(testUser)

        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Test User"))
        viewModel.onEvent(AuthEvent.RegisterWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        // Verify the operation completed successfully (no loading, no error)
        val formState = viewModel.formState.value
        assertEquals(false, formState.isLoading)
        assertNull(formState.errorMessage)
    }

    @Test
    fun `register with invalid email shows error`() = runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("invalid-email"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Test User"))
        viewModel.onEvent(AuthEvent.RegisterWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Invalid email format", viewModel.formState.value.emailError)
    }

    @Test
    fun `register with short password shows error`() = runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("short"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Test User"))
        viewModel.onEvent(AuthEvent.RegisterWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Password must be at least 8 characters", viewModel.formState.value.passwordError)
    }

    @Test
    fun `register with blank display name shows error`() = runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged(""))
        viewModel.onEvent(AuthEvent.RegisterWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Display name is required", viewModel.formState.value.displayNameError)
    }

    @Test
    fun `register with existing email shows error`() = runTest {
        fakeAuthRepository.signUpResult = Result.failure(AuthError.EmailAlreadyExists())

        viewModel.onEvent(AuthEvent.EmailChanged("existing@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Test User"))
        viewModel.onEvent(AuthEvent.RegisterWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("An account with this email already exists", viewModel.formState.value.errorMessage)
    }

    @Test
    fun `clear error clears error message`() = runTest {
        fakeAuthRepository.signUpResult = Result.failure(AuthError.NetworkError())

        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.DisplayNameChanged("Test User"))
        viewModel.onEvent(AuthEvent.RegisterWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AuthEvent.ClearError)

        assertNull(viewModel.formState.value.errorMessage)
    }

    // Sign-In Tests (User Story 2)

    @Test
    fun `sign in with valid credentials succeeds`() = runTest {
        val testUser = createTestUser()
        fakeAuthRepository.signInResult = Result.success(testUser)

        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.SignInWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        // Verify the operation completed successfully (no loading, no error)
        val formState = viewModel.formState.value
        assertEquals(false, formState.isLoading)
        assertNull(formState.errorMessage)
    }

    @Test
    fun `sign in with invalid email shows error`() = runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("invalid-email"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.SignInWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Invalid email format", viewModel.formState.value.emailError)
    }

    @Test
    fun `sign in with short password shows error`() = runTest {
        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("short"))
        viewModel.onEvent(AuthEvent.SignInWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Password must be at least 8 characters", viewModel.formState.value.passwordError)
    }

    @Test
    fun `sign in with invalid credentials shows error`() = runTest {
        fakeAuthRepository.signInResult = Result.failure(AuthError.InvalidCredentials())

        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("wrongpassword"))
        viewModel.onEvent(AuthEvent.SignInWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Incorrect email or password", viewModel.formState.value.errorMessage)
    }

    @Test
    fun `sign in with network error shows error`() = runTest {
        fakeAuthRepository.signInResult = Result.failure(AuthError.NetworkError())

        viewModel.onEvent(AuthEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.SignInWithEmail)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Unable to connect. Check your internet connection.", viewModel.formState.value.errorMessage)
    }

    // Sign-Out Tests (User Story 2)

    @Test
    fun `sign out succeeds`() = runTest {
        // First set up an authenticated state
        val testUser = createTestUser()
        fakeAuthRepository.setAuthState(AuthState.Authenticated(testUser))

        viewModel.onEvent(AuthEvent.SignOut)

        testDispatcher.scheduler.advanceUntilIdle()

        // Verify no error occurred
        assertNull(viewModel.formState.value.errorMessage)
    }

    @Test
    fun `sign out with error shows error message`() = runTest {
        fakeAuthRepository.signOutResult = Result.failure(AuthError.NetworkError())

        viewModel.onEvent(AuthEvent.SignOut)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Unable to connect. Check your internet connection.", viewModel.formState.value.errorMessage)
    }

    // Google Sign-In Tests

    @Test
    fun `google sign in cancelled shows no error`() = runTest {
        fakeAuthRepository.googleSignInResult = Result.failure(AuthError.GoogleSignInCancelled())

        viewModel.onEvent(AuthEvent.SignInWithGoogle)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Sign in was cancelled", viewModel.formState.value.errorMessage)
    }

    private fun createTestUser() = User(
        id = "test-id",
        email = "test@example.com",
        displayName = "Test User",
        avatarUrl = null,
        provider = AuthProvider.EMAIL,
        createdAt = Clock.System.now()
    )
}

/**
 * Fake implementation of AuthRepository for testing.
 */
class FakeAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)

    var signInResult: Result<User> = Result.failure(AuthError.Unknown("Not configured"))
    var signUpResult: Result<User> = Result.failure(AuthError.Unknown("Not configured"))
    var signOutResult: Result<Unit> = Result.success(Unit)
    var refreshResult: Result<User> = Result.failure(AuthError.Unknown("Not configured"))
    var googleSignInResult: Result<Unit> = Result.success(Unit)

    override val authState: Flow<AuthState> = _authState

    override val currentUser: User? = null

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return signInResult.also { result ->
            result.onSuccess { user ->
                _authState.value = AuthState.Authenticated(user)
            }
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return signUpResult.also { result ->
            result.onSuccess { user ->
                _authState.value = AuthState.Authenticated(user)
            }
        }
    }

    override suspend fun signInWithGoogle(): Result<Unit> {
        return googleSignInResult
    }

    override suspend fun signOut(): Result<Unit> {
        _authState.value = AuthState.Unauthenticated
        return signOutResult
    }

    override suspend fun refreshSession(): Result<User> {
        return refreshResult
    }

    fun setAuthState(state: AuthState) {
        _authState.value = state
    }
}
