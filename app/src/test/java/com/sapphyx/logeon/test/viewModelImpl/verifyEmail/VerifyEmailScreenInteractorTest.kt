package com.sapphyx.logeon.test.viewModelImpl.verifyEmail

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenAction
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenInteractor
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class VerifyEmailScreenInteractorTest {

    private val authRepository: AuthenticationRepository = mockk(relaxed = true)
    private val interactor = VerifyEmailScreenInteractor(authRepository)

    @Test
    fun `initResults emits EmailLoaded with current user email`() = runTest {
        val mockUser: FirebaseUser = mockk {
            every { email } returns "user@logeon.com"
        }
        every { authRepository.currentUser } returns mockUser

        interactor.initResults().test {
            assertEquals(VerifyEmailScreenResult.EmailLoaded("user@logeon.com"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `CheckVerifiedClick emits Loading then Verified when email is verified`() = runTest {
        coEvery { authRepository.checkEmailVerified() } returns true

        interactor.actionToResult(VerifyEmailScreenAction.CheckVerifiedClick).test {
            assertEquals(VerifyEmailScreenResult.Loading, awaitItem())
            assertEquals(VerifyEmailScreenResult.Verified, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `CheckVerifiedClick emits Loading then NotVerifiedYet when email is still unverified`() =
        runTest {
            coEvery { authRepository.checkEmailVerified() } returns false

            interactor.actionToResult(VerifyEmailScreenAction.CheckVerifiedClick).test {
                assertEquals(VerifyEmailScreenResult.Loading, awaitItem())
                assertEquals(VerifyEmailScreenResult.NotVerified, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `ResendEmailClick emits Loading then ResendSuccess on success`() = runTest {
        coEvery { authRepository.resendVerificationEmail() } returns Result.success(Unit)

        interactor.actionToResult(VerifyEmailScreenAction.ResendEmailClick).test {
            assertEquals(VerifyEmailScreenResult.Loading, awaitItem())
            assertEquals(VerifyEmailScreenResult.ResendSuccess, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `ResendEmailClick emits Loading then ResendError on failure`() = runTest {
        coEvery { authRepository.resendVerificationEmail() } returns Result.failure(
            RuntimeException("Too many requests")
        )

        interactor.actionToResult(VerifyEmailScreenAction.ResendEmailClick).test {
            assertEquals(VerifyEmailScreenResult.Loading, awaitItem())
            assertEquals(VerifyEmailScreenResult.ResendError("Too many requests"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `SignOutClick calls repository signOut and emits SignedOut`() = runTest {
        interactor.actionToResult(VerifyEmailScreenAction.SignOutClick).test {
            assertEquals(VerifyEmailScreenResult.SignedOut, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { authRepository.signOut() }
    }
}