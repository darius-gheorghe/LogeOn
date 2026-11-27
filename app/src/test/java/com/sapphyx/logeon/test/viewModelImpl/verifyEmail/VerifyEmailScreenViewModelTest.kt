package com.sapphyx.logeon.test.viewModelImpl.verifyEmail

import app.cash.turbine.test
import com.sapphyx.logeon.util.MainDispatcherRule
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenAction
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenInteractor
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenResult
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenViewModel
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailStatusMessage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VerifyEmailScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val interactor: VerifyEmailScreenInteractor = mockk {
        every { initResults() } returns flowOf(
            VerifyEmailScreenResult.EmailLoaded("user@logeon.com")
        )
    }

    @Test
    fun `initial state loads user email from initResults`() = runTest {
        val viewModel = VerifyEmailScreenViewModel(interactor)

        assertEquals("user@logeon.com", viewModel.uiState.value.userEmail)
    }

    @Test
    fun `CheckVerifiedClick updates statusMessage to NotVerifiedYet when unverified`() = runTest {
        every {
            interactor.actionToResult(VerifyEmailScreenAction.CheckVerifiedClick)
        } returns flowOf(
            VerifyEmailScreenResult.Loading,
            VerifyEmailScreenResult.NotVerified
        )

        val viewModel = VerifyEmailScreenViewModel(interactor)

        viewModel.uiState.test {
            assertNull(awaitItem().statusMessage)

            viewModel.processUiAction(VerifyEmailScreenAction.CheckVerifiedClick)
            assertTrue(awaitItem().isLoading)

            val notVerifiedState = awaitItem()
            assertFalse(notVerifiedState.isLoading)
            assertEquals(
                VerifyEmailStatusMessage.NotVerified,
                notVerifiedState.statusMessage
            )
        }
    }

    @Test
    fun `ResendEmailClick handles both ResendSuccess and ResendError`() = runTest {
        every {
            interactor.actionToResult(VerifyEmailScreenAction.ResendEmailClick)
        } returns flowOf(
            VerifyEmailScreenResult.Loading,
            VerifyEmailScreenResult.ResendSuccess,
            VerifyEmailScreenResult.ResendError("Rate limited")
        )

        val viewModel = VerifyEmailScreenViewModel(interactor)

        viewModel.uiState.test {
            awaitItem() // Initial

            viewModel.processUiAction(VerifyEmailScreenAction.ResendEmailClick)
            assertTrue(awaitItem().isLoading)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(VerifyEmailStatusMessage.ResendSuccess, successState.statusMessage)

            val errorState = awaitItem()
            assertEquals(
                VerifyEmailStatusMessage.ResendError("Rate limited"),
                errorState.statusMessage
            )
        }
    }
}