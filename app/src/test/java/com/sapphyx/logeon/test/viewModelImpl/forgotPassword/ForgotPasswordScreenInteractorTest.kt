package com.sapphyx.logeon.test.viewModelImpl.forgotPassword

import app.cash.turbine.test
import com.sapphyx.logeon.data.repository.AuthResponse
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenAction
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenInteractor
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ForgotPasswordScreenInteractorTest {

    private val authRepository: AuthenticationRepository = mockk()
    private val interactor = ForgotPasswordScreenInteractor(authRepository)

    @Test
    fun `SendResetLinkClick maps repository AuthResponse emissions`() = runTest {
        every {
            authRepository.sendPasswordResetEmail("user@logeon.com")
        } returns flowOf(
            AuthResponse.Loading,
            AuthResponse.EmailValidationError(EmailValidationResult.EMPTY),
            AuthResponse.Error("Network error"),
            AuthResponse.Success
        )

        interactor.actionToResult(ForgotPasswordScreenAction.SendResetLinkClick("user@logeon.com"))
            .test {
                assertEquals(ForgotPasswordScreenResult.Loading, awaitItem())
                assertEquals(
                    ForgotPasswordScreenResult.EmailValidationError(EmailValidationResult.EMPTY),
                    awaitItem()
                )
                assertEquals(ForgotPasswordScreenResult.Error("Network error"), awaitItem())
                assertEquals(ForgotPasswordScreenResult.Success, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun `NavigateBackClick emits NavigateBack`() = runTest {
        interactor.actionToResult(ForgotPasswordScreenAction.NavigateBackClick).test {
            assertEquals(ForgotPasswordScreenResult.NavigateBack, awaitItem())
            awaitComplete()
        }
    }
}