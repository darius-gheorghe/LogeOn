package com.sapphyx.logeon.test.viewModelImpl.login

import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.util.MainDispatcherRule
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenAction
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenInteractor
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenResult
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navigationManager: NavigationManager = mockk(relaxed = true)
    private val interactor: LoginScreenInteractor = mockk {
        every { initResults() } returns emptyFlow()
    }

    @Test
    fun `typing email or password clears previous field and general errors`() = runTest {
        every {
            interactor.actionToResult(LoginScreenAction.LoginClick("", ""))
        } returns flowOf(
            LoginScreenResult.EmailValidationError(EmailValidationResult.EMPTY),
            LoginScreenResult.PasswordValidationError(PasswordValidationResult.EMPTY),
            LoginScreenResult.Error("Auth failed")
        )
        every {
            interactor.actionToResult(LoginScreenAction.EmailChanged("a@b.com"))
        } returns flowOf(LoginScreenResult.EmailUpdated("a@b.com"))
        every {
            interactor.actionToResult(LoginScreenAction.PasswordChanged("123"))
        } returns flowOf(LoginScreenResult.PasswordUpdated("123"))

        val viewModel = LoginScreenViewModel(navigationManager, interactor)

        viewModel.uiState.test {
            awaitItem() // Initial

            viewModel.processUiAction(LoginScreenAction.LoginClick("", ""))
            awaitItem() // Email error
            awaitItem() // Password error
            val errorState = awaitItem()
            assertEquals("Auth failed", errorState.generalError)

            viewModel.processUiAction(LoginScreenAction.EmailChanged("a@b.com"))
            val emailCleared = awaitItem()
            assertEquals("a@b.com", emailCleared.email)
            assertNull(emailCleared.emailError)
            assertNull(emailCleared.generalError)

            viewModel.processUiAction(LoginScreenAction.PasswordChanged("123"))
            val passwordCleared = awaitItem()
            assertEquals("123", passwordCleared.password)
            assertNull(passwordCleared.passwordError)
        }
    }

    @Test
    fun `Loading transitions to Success and resets isLoading`() = runTest {
        every {
            interactor.actionToResult(LoginScreenAction.LoginClick("a@b.com", "123"))
        } returns flowOf(LoginScreenResult.Loading, LoginScreenResult.Success)

        val viewModel = LoginScreenViewModel(navigationManager, interactor)

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)

            viewModel.processUiAction(LoginScreenAction.LoginClick("a@b.com", "123"))
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isLoading)
        }
    }

    @Test
    fun `navigation results call NavigationManager with expected destinations`() = runTest {
        every {
            interactor.actionToResult(LoginScreenAction.NavigateToSignUpClick)
        } returns flowOf(LoginScreenResult.NavigateToSignUpScreen)
        every {
            interactor.actionToResult(LoginScreenAction.NavigateToForgotPasswordClick)
        } returns flowOf(LoginScreenResult.NavigateToForgotPasswordScreen)

        val viewModel = LoginScreenViewModel(navigationManager, interactor)

        viewModel.processUiAction(LoginScreenAction.NavigateToSignUpClick)
        verify(exactly = 1) { navigationManager.navigate(LogeOnDestinations.SignUp) }

        viewModel.processUiAction(LoginScreenAction.NavigateToForgotPasswordClick)
        verify(exactly = 1) { navigationManager.navigate(LogeOnDestinations.ForgotPassword) }
    }
}