package com.sapphyx.logeon.test.viewModelImpl.signUp

import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.util.MainDispatcherRule
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenAction
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenInteractor
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenResult
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenViewModel
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

class SignUpScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navigationManager: NavigationManager = mockk(relaxed = true)
    private val interactor: SignUpScreenInteractor = mockk {
        every { initResults() } returns emptyFlow()
    }

    @Test
    fun `ConfirmPasswordMismatch sets confirmPasswordError and typing clears it`() = runTest {
        every {
            interactor.actionToResult(SignUpScreenAction.SignUpClick("a@b.com", "Pass1!", "Pass2!"))
        } returns flowOf(SignUpScreenResult.ConfirmPasswordMismatch)
        every {
            interactor.actionToResult(SignUpScreenAction.ConfirmPasswordChanged("Pass1!"))
        } returns flowOf(SignUpScreenResult.ConfirmPasswordUpdated("Pass1!"))

        val viewModel = SignUpScreenViewModel(navigationManager, interactor)

        viewModel.uiState.test {
            assertFalse(awaitItem().confirmPasswordError)

            viewModel.processUiAction(SignUpScreenAction.SignUpClick("a@b.com", "Pass1!", "Pass2!"))
            assertTrue(awaitItem().confirmPasswordError)

            viewModel.processUiAction(SignUpScreenAction.ConfirmPasswordChanged("Pass1!"))
            val updated = awaitItem()
            assertEquals("Pass1!", updated.confirmPassword)
            assertFalse(updated.confirmPasswordError)
        }
    }

    @Test
    fun `validation and general errors update state cleanly`() = runTest {
        every {
            interactor.actionToResult(SignUpScreenAction.SignUpClick("a@b.com", "Pass1!", "Pass1!"))
        } returns flowOf(
            SignUpScreenResult.Loading,
            SignUpScreenResult.EmailValidationError(EmailValidationResult.EMPTY),
            SignUpScreenResult.PasswordValidationError(PasswordValidationResult.NO_DIGIT),
            SignUpScreenResult.Error("Network error")
        )

        val viewModel = SignUpScreenViewModel(navigationManager, interactor)

        viewModel.uiState.test {
            awaitItem() // Initial

            viewModel.processUiAction(SignUpScreenAction.SignUpClick("a@b.com", "Pass1!", "Pass1!"))
            assertTrue(awaitItem().isLoading)

            val emailErr = awaitItem()
            assertFalse(emailErr.isLoading)
            assertEquals(EmailValidationResult.EMPTY, emailErr.emailError)

            val passErr = awaitItem()
            assertEquals(PasswordValidationResult.NO_DIGIT, passErr.passwordError)

            val generalErr = awaitItem()
            assertNull(generalErr.emailError)
            assertNull(generalErr.passwordError)
            assertEquals("Network error", generalErr.generalError)
        }
    }

    @Test
    fun `NavigateToLoginClick calls navigationManager navigateBack`() = runTest {
        every {
            interactor.actionToResult(SignUpScreenAction.NavigateToLoginClick)
        } returns flowOf(SignUpScreenResult.NavigateToLoginScreen)

        val viewModel = SignUpScreenViewModel(navigationManager, interactor)
        viewModel.processUiAction(SignUpScreenAction.NavigateToLoginClick)

        verify(exactly = 1) { navigationManager.navigateBack() }
    }
}
