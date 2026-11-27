package com.sapphyx.logeon.test.viewModelImpl.forgotPassword

import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.util.MainDispatcherRule
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenAction
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenInteractor
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenResult
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForgotPasswordScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navigationManager: NavigationManager = mockk(relaxed = true)
    private val interactor: ForgotPasswordScreenInteractor = mockk {
        every { initResults() } returns emptyFlow()
    }

    @Test
    fun `SendResetLinkClick sets isResetEmailSent on Success and resets when email is edited`() =
        runTest {
            every {
                interactor.actionToResult(ForgotPasswordScreenAction.SendResetLinkClick("a@b.com"))
            } returns flowOf(
                ForgotPasswordScreenResult.Loading,
                ForgotPasswordScreenResult.Success
            )
            every {
                interactor.actionToResult(ForgotPasswordScreenAction.EmailChanged("new@b.com"))
            } returns flowOf(ForgotPasswordScreenResult.EmailUpdated("new@b.com"))

            val viewModel = ForgotPasswordScreenViewModel(navigationManager, interactor)

            viewModel.uiState.test {
                assertFalse(awaitItem().isResetEmailSent)

                viewModel.processUiAction(ForgotPasswordScreenAction.SendResetLinkClick("a@b.com"))
                assertTrue(awaitItem().isLoading)

                val sentState = awaitItem()
                assertFalse(sentState.isLoading)
                assertTrue(sentState.isResetEmailSent)

                viewModel.processUiAction(ForgotPasswordScreenAction.EmailChanged("new@b.com"))
                val editedState = awaitItem()
                assertEquals("new@b.com", editedState.email)
                assertFalse(editedState.isResetEmailSent)
            }
        }

    @Test
    fun `EmailValidationError updates emailError state`() = runTest {
        every {
            interactor.actionToResult(ForgotPasswordScreenAction.SendResetLinkClick(""))
        } returns flowOf(
            ForgotPasswordScreenResult.EmailValidationError(EmailValidationResult.EMPTY)
        )

        val viewModel = ForgotPasswordScreenViewModel(navigationManager, interactor)

        viewModel.uiState.test {
            awaitItem()
            viewModel.processUiAction(ForgotPasswordScreenAction.SendResetLinkClick(""))
            assertEquals(EmailValidationResult.EMPTY, awaitItem().emailError)
        }
    }

    @Test
    fun `NavigateBackClick calls navigationManager navigateBack`() = runTest {
        every {
            interactor.actionToResult(ForgotPasswordScreenAction.NavigateBackClick)
        } returns flowOf(ForgotPasswordScreenResult.NavigateBack)

        val viewModel = ForgotPasswordScreenViewModel(navigationManager, interactor)
        viewModel.processUiAction(ForgotPasswordScreenAction.NavigateBackClick)

        verify(exactly = 1) { navigationManager.navigateBack() }
    }
}