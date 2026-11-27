package com.sapphyx.logeon.test.viewModelImpl.signUp

import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.AuthResponse
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenAction
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenInteractor
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SignUpScreenInteractorTest {

    private val authRepository: AuthenticationRepository = mockk()
    private val interactor = SignUpScreenInteractor(authRepository)

    @Test
    fun `initResults returns empty flow`() = runTest {
        interactor.initResults().test {
            awaitComplete()
        }
    }

    @Test
    fun `field change actions emit updated values`() = runTest {
        interactor.actionToResult(SignUpScreenAction.EmailChanged("a@b.com")).test {
            assertEquals(SignUpScreenResult.EmailUpdated("a@b.com"), awaitItem())
            awaitComplete()
        }
        interactor.actionToResult(SignUpScreenAction.PasswordChanged("Pass1!")).test {
            assertEquals(SignUpScreenResult.PasswordUpdated("Pass1!"), awaitItem())
            awaitComplete()
        }
        interactor.actionToResult(SignUpScreenAction.ConfirmPasswordChanged("Pass1!")).test {
            assertEquals(SignUpScreenResult.ConfirmPasswordUpdated("Pass1!"), awaitItem())
            awaitComplete()
        }
    }

    @Suppress("UnusedFlow")
    @Test
    fun `SignUpClick emits ConfirmPasswordMismatch and skips repository when passwords differ`() =
        runTest {
            interactor.actionToResult(
                SignUpScreenAction.SignUpClick("a@b.com", "Password1!", "Different1!")
            ).test {
                assertEquals(SignUpScreenResult.ConfirmPasswordMismatch, awaitItem())
                awaitComplete()
            }

            verify(exactly = 0) { authRepository.createAccountWithEmailAndPassword(any(), any()) }
        }

    @Test
    fun `SignUpClick maps repository AuthResponse emissions when passwords match`() = runTest {
        every {
            authRepository.createAccountWithEmailAndPassword("a@b.com", "Password1!")
        } returns flowOf(
            AuthResponse.Loading,
            AuthResponse.EmailValidationError(EmailValidationResult.INCORRECT_FORMAT),
            AuthResponse.PasswordValidationError(PasswordValidationResult.TOO_SHORT),
            AuthResponse.Error("Email already in use"),
            AuthResponse.Cancelled,
            AuthResponse.Success
        )

        interactor.actionToResult(
            SignUpScreenAction.SignUpClick("a@b.com", "Password1!", "Password1!")
        ).test {
            assertEquals(SignUpScreenResult.Loading, awaitItem())
            assertEquals(
                SignUpScreenResult.EmailValidationError(EmailValidationResult.INCORRECT_FORMAT),
                awaitItem()
            )
            assertEquals(
                SignUpScreenResult.PasswordValidationError(PasswordValidationResult.TOO_SHORT),
                awaitItem()
            )
            assertEquals(SignUpScreenResult.Error("Email already in use"), awaitItem())
            assertEquals(SignUpScreenResult.Cancelled, awaitItem())
            assertEquals(SignUpScreenResult.Success, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `NavigateToLoginClick emits NavigateToLoginScreen`() = runTest {
        interactor.actionToResult(SignUpScreenAction.NavigateToLoginClick).test {
            assertEquals(SignUpScreenResult.NavigateToLoginScreen, awaitItem())
            awaitComplete()
        }
    }
}