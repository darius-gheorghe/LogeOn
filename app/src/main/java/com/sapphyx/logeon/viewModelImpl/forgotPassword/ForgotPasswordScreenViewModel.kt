package com.sapphyx.logeon.viewModelImpl.forgotPassword

import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordScreenViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    interactor: ForgotPasswordScreenInteractor
) : BaseViewModel<ForgotPasswordScreenState, ForgotPasswordScreenAction, ForgotPasswordScreenEffect, ForgotPasswordScreenResult>(
    initialUiState = ForgotPasswordScreenState(),
    interactor = interactor
) {

    override suspend fun handleResult(
        previousState: ForgotPasswordScreenState,
        result: ForgotPasswordScreenResult
    ): ForgotPasswordScreenState {
        return when (result) {
            is ForgotPasswordScreenResult.EmailUpdated -> previousState.copy(
                email = result.email,
                emailError = null,
                generalError = null,
                isResetEmailSent = false
            )

            is ForgotPasswordScreenResult.Loading -> previousState.copy(
                isLoading = true,
                emailError = null,
                generalError = null,
                isResetEmailSent = false
            )

            is ForgotPasswordScreenResult.Success -> previousState.copy(
                isLoading = false,
                isResetEmailSent = true
            )

            is ForgotPasswordScreenResult.Cancelled -> previousState.copy(
                isLoading = false
            )

            is ForgotPasswordScreenResult.EmailValidationError -> previousState.copy(
                isLoading = false,
                emailError = result.error,
                generalError = null
            )

            is ForgotPasswordScreenResult.Error -> previousState.copy(
                isLoading = false,
                emailError = null,
                generalError = result.message
            )

            is ForgotPasswordScreenResult.NavigateBack -> {
                navigationManager.navigateBack()
                previousState
            }
        }
    }
}

data class ForgotPasswordScreenState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isResetEmailSent: Boolean = false,
    val emailError: EmailValidationResult? = null,
    val generalError: String? = null
)

sealed interface ForgotPasswordScreenAction {
    data class EmailChanged(val email: String) : ForgotPasswordScreenAction
    data class SendResetLinkClick(val email: String) : ForgotPasswordScreenAction
    data object NavigateBackClick : ForgotPasswordScreenAction
}

sealed interface ForgotPasswordScreenEffect

sealed interface ForgotPasswordScreenResult {
    data object Loading : ForgotPasswordScreenResult
    data object Success : ForgotPasswordScreenResult
    data object Cancelled : ForgotPasswordScreenResult
    data class EmailUpdated(val email: String) : ForgotPasswordScreenResult
    data class EmailValidationError(val error: EmailValidationResult) : ForgotPasswordScreenResult
    data class Error(val message: String) : ForgotPasswordScreenResult
    data object NavigateBack : ForgotPasswordScreenResult
}