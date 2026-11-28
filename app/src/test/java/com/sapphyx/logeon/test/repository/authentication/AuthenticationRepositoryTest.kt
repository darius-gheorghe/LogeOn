package com.sapphyx.logeon.test.repository.authentication

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.sapphyx.logeon.R
import com.sapphyx.logeon.data.repository.AccountSecurityResponse
import com.sapphyx.logeon.data.repository.AuthResponse
import com.sapphyx.logeon.data.repository.AuthStatus
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidator
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationRepositoryTest {

    private val appContext: Context = mockk(relaxed = true)
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val emailValidator: EmailValidator = mockk()
    private val passwordValidator: PasswordValidator = mockk()

    private lateinit var repository: AuthenticationRepository

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        mockkObject(CredentialManager.Companion)
        mockkObject(GoogleIdTokenCredential.Companion)
        mockkStatic(GoogleAuthProvider::class)
        mockkStatic(EmailAuthProvider::class)

        repository = AuthenticationRepository(
            appContext = appContext,
            externalScope = testScope,
            auth = firebaseAuth,
            emailValidator = emailValidator,
            passwordValidator = passwordValidator
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
        unmockkObject(CredentialManager.Companion)
        unmockkObject(GoogleIdTokenCredential.Companion)
        unmockkStatic(GoogleAuthProvider::class)
        unmockkStatic(EmailAuthProvider::class)
    }

    // --- currentUser ---

    @Test
    fun `currentUser delegates directly to FirebaseAuth currentUser`() {
        val mockUser: FirebaseUser = mockk()
        every { firebaseAuth.currentUser } returns mockUser
        assertEquals(mockUser, repository.currentUser)

        every { firebaseAuth.currentUser } returns null
        assertEquals(null, repository.currentUser)
    }

    // --- observeAuthState ---

    @Test
    fun `observeAuthState emits LoggedOut when currentUser is null`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { firebaseAuth.currentUser } returns null
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onAuthStateChanged(firebaseAuth)
        }

        repository.observeAuthState().test {
            assertEquals(AuthStatus.LoggedOut, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAuthState emits UnverifiedEmail then Verified after checkEmailVerified succeeds`() =
        runTest {
            val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
            val mockUser: FirebaseUser = mockk()
            val reloadTask: Task<Void> = mockk()

            every { firebaseAuth.currentUser } returns mockUser
            every { mockUser.isEmailVerified } returns false
            every { mockUser.reload() } returns reloadTask
            coEvery { reloadTask.await() } returns mockk()

            every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onAuthStateChanged(firebaseAuth)
            }

            repository.observeAuthState().test {
                assertEquals(AuthStatus.UnverifiedEmail, awaitItem())

                // Simulate user clicking verification link in inbox
                every { mockUser.isEmailVerified } returns true
                val verified = repository.checkEmailVerified()

                assertTrue(verified)
                assertEquals(AuthStatus.Verified, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- createAccountWithEmailAndPassword ---

    @Test
    fun `createAccount emits EmailValidationError when email is invalid`() = runTest {
        every { emailValidator("bad-email") } returns EmailValidationResult.INCORRECT_FORMAT

        repository.createAccountWithEmailAndPassword("bad-email", "Password1!").test {
            assertEquals(
                AuthResponse.EmailValidationError(EmailValidationResult.INCORRECT_FORMAT),
                awaitItem()
            )
            awaitComplete()
        }

        verify(exactly = 0) { firebaseAuth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `createAccount emits PasswordValidationError when password is weak`() = runTest {
        every { emailValidator("user@example.com") } returns EmailValidationResult.CORRECT
        every { passwordValidator("weak") } returns PasswordValidationResult.TOO_SHORT

        repository.createAccountWithEmailAndPassword("user@example.com", "weak").test {
            assertEquals(
                AuthResponse.PasswordValidationError(PasswordValidationResult.TOO_SHORT),
                awaitItem()
            )
            awaitComplete()
        }

        verify(exactly = 0) { firebaseAuth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `createAccount trims email, creates user, sends verification email, and emits Success`() =
        runTest {
            val authTask: Task<AuthResult> = mockk()
            val authResult: AuthResult = mockk()
            val mockUser: FirebaseUser = mockk()
            val verifyTask: Task<Void> = mockk()

            every { emailValidator("user@example.com") } returns EmailValidationResult.CORRECT
            every { passwordValidator("Password1!") } returns PasswordValidationResult.CORRECT
            every {
                firebaseAuth.createUserWithEmailAndPassword("user@example.com", "Password1!")
            } returns authTask
            coEvery { authTask.await() } returns authResult
            every { authResult.user } returns mockUser
            every { mockUser.sendEmailVerification() } returns verifyTask
            coEvery { verifyTask.await() } returns mockk()

            repository.createAccountWithEmailAndPassword("  user@example.com  ", "Password1!")
                .test {
                    assertEquals(AuthResponse.Loading, awaitItem())
                    assertEquals(AuthResponse.Success, awaitItem())
                    awaitComplete()
                }

            coVerify(exactly = 1) { verifyTask.await() }
        }

    // --- loginWithEmailAndPassword ---

    @Test
    fun `loginWithEmailAndPassword emits Error when Firebase throws exception`() = runTest {
        val authTask: Task<AuthResult> = mockk()

        every { emailValidator("user@example.com") } returns EmailValidationResult.CORRECT
        every { passwordValidator.validateForLogin("wrongpass") } returns PasswordValidationResult.CORRECT
        every {
            firebaseAuth.signInWithEmailAndPassword("user@example.com", "wrongpass")
        } returns authTask
        coEvery { authTask.await() } throws RuntimeException("Invalid credentials")

        repository.loginWithEmailAndPassword("user@example.com", "wrongpass").test {
            assertEquals(AuthResponse.Loading, awaitItem())
            assertEquals(AuthResponse.Error("Invalid credentials"), awaitItem())
            awaitComplete()
        }
    }

    // --- resendVerificationEmail & checkEmailVerified ---

    @Test
    fun `resendVerificationEmail returns failure when sendEmailVerification throws`() = runTest {
        val mockUser: FirebaseUser = mockk()
        val verifyTask: Task<Void> = mockk()

        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.sendEmailVerification() } returns verifyTask
        coEvery { verifyTask.await() } throws RuntimeException("Too many requests")

        val result = repository.resendVerificationEmail()

        assertTrue(result.isFailure)
        assertEquals("Too many requests", result.exceptionOrNull()?.message)
    }

    @Test
    fun `checkEmailVerified returns false when reload throws exception`() = runTest {
        val mockUser: FirebaseUser = mockk()
        val reloadTask: Task<Void> = mockk()

        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.reload() } returns reloadTask
        coEvery { reloadTask.await() } throws RuntimeException("Network error")

        assertFalse(repository.checkEmailVerified())
    }

    // --- sendPasswordResetEmail ---

    @Test
    fun `sendPasswordResetEmail trims email and emits Loading then Success`() = runTest {
        val resetTask: Task<Void> = mockk()

        every { emailValidator("user@example.com") } returns EmailValidationResult.CORRECT
        every { firebaseAuth.sendPasswordResetEmail("user@example.com") } returns resetTask
        coEvery { resetTask.await() } returns mockk()

        repository.sendPasswordResetEmail("  user@example.com ").test {
            assertEquals(AuthResponse.Loading, awaitItem())
            assertEquals(AuthResponse.Success, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `sendPasswordResetEmail emits EmailValidationError when email is empty`() = runTest {
        every { emailValidator("") } returns EmailValidationResult.EMPTY

        repository.sendPasswordResetEmail("   ").test {
            assertEquals(
                AuthResponse.EmailValidationError(EmailValidationResult.EMPTY),
                awaitItem()
            )
            awaitComplete()
        }

        verify(exactly = 0) { firebaseAuth.sendPasswordResetEmail(any()) }
    }

    // --- signInWithGoogle ---

    @Test
    fun `signInWithGoogle emits Error when context is not an Activity`() = runTest {
        val nonActivityContext: Context = mockk(relaxed = true)

        repository.signInWithGoogle(nonActivityContext).test {
            assertEquals(
                AuthResponse.Error("Sign in with google account - context error"),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun `signInWithGoogle emits Loading then Success when valid Google ID token is returned`() =
        runTest {
            val activity: Activity = mockk(relaxed = true)
            val credentialManager: CredentialManager = mockk()
            val getCredentialResponse: GetCredentialResponse = mockk()
            val customCredential: CustomCredential = mockk()
            val googleIdTokenCredential: GoogleIdTokenCredential = mockk()
            val firebaseCredential: AuthCredential = mockk()
            val authTask: Task<AuthResult> = mockk()

            every { activity.getString(R.string.default_web_client_id) } returns "server-client-id"
            every { CredentialManager.create(activity) } returns credentialManager
            coEvery { credentialManager.getCredential(activity, any<GetCredentialRequest>()) } returns getCredentialResponse
            every { getCredentialResponse.credential } returns customCredential
            every { customCredential.type } returns GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            every { customCredential.data } returns mockk(relaxed = true)
            every { GoogleIdTokenCredential.createFrom(any()) } returns googleIdTokenCredential
            every { googleIdTokenCredential.idToken } returns "valid-id-token"
            every { GoogleAuthProvider.getCredential("valid-id-token", null) } returns firebaseCredential
            every { firebaseAuth.signInWithCredential(firebaseCredential) } returns authTask
            coEvery { authTask.await() } returns mockk()

            repository.signInWithGoogle(activity).test {
                assertEquals(AuthResponse.Loading, awaitItem())
                assertEquals(AuthResponse.Success, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `signInWithGoogle emits Loading then Cancelled when user dismisses bottom sheet`() =
        runTest {
            val activity: Activity = mockk(relaxed = true)
            val credentialManager: CredentialManager = mockk()

            every { activity.getString(R.string.default_web_client_id) } returns "server-client-id"
            every { CredentialManager.create(activity) } returns credentialManager
            coEvery {
                credentialManager.getCredential(activity, any<GetCredentialRequest>())
            } throws GetCredentialCancellationException("User cancelled")

            repository.signInWithGoogle(activity).test {
                assertEquals(AuthResponse.Loading, awaitItem())
                assertEquals(AuthResponse.Cancelled, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `signInWithGoogle emits Loading then Error when GoogleIdTokenParsingException is thrown`() =
        runTest {
            val activity: Activity = mockk(relaxed = true)
            val credentialManager: CredentialManager = mockk()
            val getCredentialResponse: GetCredentialResponse = mockk()
            val customCredential: CustomCredential = mockk()

            every { activity.getString(R.string.default_web_client_id) } returns "server-client-id"
            every { CredentialManager.create(activity) } returns credentialManager
            coEvery { credentialManager.getCredential(activity, any<GetCredentialRequest>()) } returns getCredentialResponse
            every { getCredentialResponse.credential } returns customCredential
            every { customCredential.type } returns GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            every { customCredential.data } returns mockk(relaxed = true)
            every {
                GoogleIdTokenCredential.createFrom(any())
            } throws GoogleIdTokenParsingException(RuntimeException("Malformed token"))

            repository.signInWithGoogle(activity).test {
                assertEquals(AuthResponse.Loading, awaitItem())
                assertEquals(AuthResponse.Error("java.lang.RuntimeException: Malformed token"), awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `signInWithGoogle attaches a valid 64-char SHA-256 hex nonce to GetGoogleIdOption`() =
        runTest {
            val activity: Activity = mockk(relaxed = true)
            val credentialManager: CredentialManager = mockk()
            val requestSlot = slot<GetCredentialRequest>()

            every { activity.getString(R.string.default_web_client_id) } returns "server-client-id"
            every { CredentialManager.create(activity) } returns credentialManager
            coEvery {
                credentialManager.getCredential(activity, capture(requestSlot))
            } throws GetCredentialCancellationException("Cancelled")

            repository.signInWithGoogle(activity).test {
                assertEquals(AuthResponse.Loading, awaitItem())
                assertEquals(AuthResponse.Cancelled, awaitItem())
                awaitComplete()
            }

            val googleOption = requestSlot.captured.credentialOptions.first() as GetGoogleIdOption
            val nonce = googleOption.nonce
            assertNotNull(nonce)
            assertEquals(64, nonce!!.length)
            assertTrue(nonce.matches(Regex("^[0-9a-f]{64}$")))
        }

    // --- signOut ---

    @Test
    fun `signOut calls Firebase signOut and clears CredentialManager state`() = runTest {
        val credentialManager: CredentialManager = mockk()
        every { CredentialManager.create(appContext) } returns credentialManager
        coEvery { credentialManager.clearCredentialState(any()) } returns Unit

        repository.signOut()

        verify(exactly = 1) { firebaseAuth.signOut() }
        coVerify(exactly = 1) { credentialManager.clearCredentialState(any()) }
    }

    @Test
    fun `signOut still completes Firebase signOut if clearCredentialState throws`() = runTest {
        val credentialManager: CredentialManager = mockk()
        every { CredentialManager.create(appContext) } returns credentialManager
        coEvery {
            credentialManager.clearCredentialState(any())
        } throws RuntimeException("Clear failed")

        repository.signOut()

        verify(exactly = 1) { firebaseAuth.signOut() }
    }

    // --- changePassword ---

    @Test
    fun `changePassword emits validation errors before calling Firebase`() = runTest {
        repository.changePassword("", "NewPass1!", "NewPass1!").test {
            assertEquals(AccountSecurityResponse.CurrentPasswordEmpty, awaitItem())
            awaitComplete()
        }

        every { passwordValidator("weak") } returns PasswordValidationResult.TOO_SHORT
        repository.changePassword("OldPass1!", "weak", "weak").test {
            assertEquals(
                AccountSecurityResponse.NewPasswordError(PasswordValidationResult.TOO_SHORT),
                awaitItem()
            )
            awaitComplete()
        }

        every { passwordValidator("NewPass1!") } returns PasswordValidationResult.CORRECT
        repository.changePassword("OldPass1!", "NewPass1!", "Mismatch1!").test {
            assertEquals(AccountSecurityResponse.PasswordsDoNotMatch, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `changePassword emits Error when currentUser is null`() = runTest {
        every { passwordValidator("NewPass1!") } returns PasswordValidationResult.CORRECT
        every { firebaseAuth.currentUser } returns null

        repository.changePassword("OldPass1!", "NewPass1!", "NewPass1!").test {
            assertEquals(
                AccountSecurityResponse.Error("User session expired. Please sign in again."),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun `changePassword reauthenticates and updates password successfully`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { email } returns "user@logeon.com"
        }
        val credential: AuthCredential = mockk()
        val reauthorizeTask: Task<Void> = mockk()
        val updateTask: Task<Void> = mockk()

        every { passwordValidator("NewPass1!") } returns PasswordValidationResult.CORRECT
        every { firebaseAuth.currentUser } returns mockUser
        every { EmailAuthProvider.getCredential("user@logeon.com", "OldPass1!") } returns credential
        every { mockUser.reauthenticate(credential) } returns reauthorizeTask
        every { mockUser.updatePassword("NewPass1!") } returns updateTask
        coEvery { reauthorizeTask.await() } returns mockk()
        coEvery { updateTask.await() } returns mockk()

        repository.changePassword("OldPass1!", "NewPass1!", "NewPass1!").test {
            assertEquals(AccountSecurityResponse.Loading, awaitItem())
            assertEquals(AccountSecurityResponse.Success, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `changePassword emits Error when reauthentication fails`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { email } returns "user@logeon.com"
        }
        val credential: AuthCredential = mockk()
        val reauthorizeTask: Task<Void> = mockk()

        every { passwordValidator("NewPass1!") } returns PasswordValidationResult.CORRECT
        every { firebaseAuth.currentUser } returns mockUser
        every { EmailAuthProvider.getCredential("user@logeon.com", "WrongPass!") } returns credential
        every { mockUser.reauthenticate(credential) } returns reauthorizeTask
        coEvery { reauthorizeTask.await() } throws RuntimeException("The password is invalid")

        repository.changePassword("WrongPass!", "NewPass1!", "NewPass1!").test {
            assertEquals(AccountSecurityResponse.Loading, awaitItem())
            assertEquals(AccountSecurityResponse.Error("The password is invalid"), awaitItem())
            awaitComplete()
        }
    }

    // --- deleteAccount ---

    @Test
    fun `deleteAccount emits CurrentPasswordEmpty when password is blank`() = runTest {
        repository.deleteAccount("") {}.test {
            assertEquals(AccountSecurityResponse.CurrentPasswordEmpty, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `deleteAccount reauthenticates, invokes cleanup callback, and deletes user`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { uid } returns "uid-1"
            every { email } returns "user@logeon.com"
        }
        val credential: AuthCredential = mockk()
        val reauthTask: Task<Void> = mockk()
        val deleteTask: Task<Void> = mockk()
        var cleanedUpUid: String? = null

        every { firebaseAuth.currentUser } returns mockUser
        every { EmailAuthProvider.getCredential("user@logeon.com", "Pass123!") } returns credential
        every { mockUser.reauthenticate(credential) } returns reauthTask
        every { mockUser.delete() } returns deleteTask
        coEvery { reauthTask.await() } returns mockk()
        coEvery { deleteTask.await() } returns mockk()

        repository.deleteAccount("Pass123!") { uid ->
            cleanedUpUid = uid
        }.test {
            assertEquals(AccountSecurityResponse.Loading, awaitItem())
            assertEquals(AccountSecurityResponse.Success, awaitItem())
            awaitComplete()
        }

        assertEquals("uid-1", cleanedUpUid)
    }

    @Test
    fun `deleteAccount aborts user deletion if cleanup callback throws`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { uid } returns "uid-1"
            every { email } returns "user@logeon.com"
        }
        val credential: AuthCredential = mockk()
        val reauthorizeTask: Task<Void> = mockk()

        every { firebaseAuth.currentUser } returns mockUser
        every { EmailAuthProvider.getCredential("user@logeon.com", "Pass123!") } returns credential
        every { mockUser.reauthenticate(credential) } returns reauthorizeTask
        coEvery { reauthorizeTask.await() } returns mockk()

        repository.deleteAccount("Pass123!") {
            throw RuntimeException("Firestore cleanup failed")
        }.test {
            assertEquals(AccountSecurityResponse.Loading, awaitItem())
            assertEquals(AccountSecurityResponse.Error("Firestore cleanup failed"), awaitItem())
            awaitComplete()
        }

        verify(exactly = 0) { mockUser.delete() }
    }
}