package com.sapphyx.logeon.test.viewModelImpl.login

import android.content.Context
import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.AuthResponse
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenAction
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenInteractor
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginScreenInteractorTest {

    private val authRepository: AuthenticationRepository = mockk()
    private val interactor = LoginScreenInteractor(authRepository)

    @Test
    fun `initResults returns empty flow`() = runTest {
        interactor.initResults().test {
            awaitComplete()
        }
    }

    @Test
    fun `EmailChanged emits EmailUpdated`() = runTest {
        interactor.actionToResult(LoginScreenAction.EmailChanged("user@test.com")).test {
            assertEquals(LoginScreenResult.EmailUpdated("user@test.com"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `PasswordChanged emits PasswordUpdated`() = runTest {
        interactor.actionToResult(LoginScreenAction.PasswordChanged("secret")).test {
            assertEquals(LoginScreenResult.PasswordUpdated("secret"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `LoginClick maps all AuthResponse states properly`() = runTest {
        every {
            authRepository.loginWithEmailAndPassword("user@test.com", "secret")
        } returns flowOf(
            AuthResponse.Loading,
            AuthResponse.EmailValidationError(EmailValidationResult.EMPTY),
            AuthResponse.PasswordValidationError(PasswordValidationResult.EMPTY),
            AuthResponse.Error("Invalid login"),
            AuthResponse.Cancelled,
            AuthResponse.Success
        )

        interactor.actionToResult(LoginScreenAction.LoginClick("user@test.com", "secret")).test {
            assertEquals(LoginScreenResult.Loading, awaitItem())
            assertEquals(LoginScreenResult.EmailValidationError(EmailValidationResult.EMPTY), awaitItem())
            assertEquals(LoginScreenResult.PasswordValidationError(PasswordValidationResult.EMPTY), awaitItem())
            assertEquals(LoginScreenResult.Error("Invalid login"), awaitItem())
            assertEquals(LoginScreenResult.Cancelled, awaitItem())
            assertEquals(LoginScreenResult.Success, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `GoogleSignInClick maps Loading and Success from repository`() = runTest {
        val context: Context = mockk()
        every { authRepository.signInWithGoogle(context) } returns flowOf(
            AuthResponse.Loading,
            AuthResponse.Success
        )

        interactor.actionToResult(LoginScreenAction.GoogleSignInClick(context)).test {
            assertEquals(LoginScreenResult.Loading, awaitItem())
            assertEquals(LoginScreenResult.Success, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Navigation actions emit corresponding navigation results`() = runTest {
        interactor.actionToResult(LoginScreenAction.NavigateToSignUpClick).test {
            assertEquals(LoginScreenResult.NavigateToSignUpScreen, awaitItem())
            awaitComplete()
        }

        interactor.actionToResult(LoginScreenAction.NavigateToForgotPasswordClick).test {
            assertEquals(LoginScreenResult.NavigateToForgotPasswordScreen, awaitItem())
            awaitComplete()
        }
    }
}