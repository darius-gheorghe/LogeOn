package com.sapphyx.logeon.viewModelImpl.verifyEmail

import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyEmailScreenViewModel @Inject constructor(
    interactor: VerifyEmailScreenInteractor
) : BaseViewModel<VerifyEmailScreenState, VerifyEmailScreenAction, VerifyEmailScreenEffect, VerifyEmailScreenResult>(
    initialUiState = VerifyEmailScreenState(),
    interactor = interactor
) {

    override suspend fun handleResult(
        previousState: VerifyEmailScreenState,
        result: VerifyEmailScreenResult
    ): VerifyEmailScreenState {
        return when (result) {
            is VerifyEmailScreenResult.EmailLoaded -> previousState.copy(
                userEmail = result.email
            )

            is VerifyEmailScreenResult.Loading -> previousState.copy(
                isLoading = true,
                statusMessage = null
            )

            is VerifyEmailScreenResult.Verified,
            is VerifyEmailScreenResult.SignedOut -> previousState.copy(
                isLoading = false
            )

            is VerifyEmailScreenResult.NotVerified -> previousState.copy(
                isLoading = false,
                statusMessage = VerifyEmailStatusMessage.NotVerified
            )

            is VerifyEmailScreenResult.ResendSuccess -> previousState.copy(
                isLoading = false,
                statusMessage = VerifyEmailStatusMessage.ResendSuccess
            )

            is VerifyEmailScreenResult.ResendError -> previousState.copy(
                isLoading = false,
                statusMessage = VerifyEmailStatusMessage.ResendError(result.message)
            )
        }
    }

}

data class VerifyEmailScreenState(
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val statusMessage: VerifyEmailStatusMessage? = null
)

sealed interface VerifyEmailStatusMessage {
    data object NotVerified : VerifyEmailStatusMessage
    data object ResendSuccess : VerifyEmailStatusMessage
    data class ResendError(val customMessage: String?) : VerifyEmailStatusMessage
}

sealed interface VerifyEmailScreenAction {
    data object CheckVerifiedClick : VerifyEmailScreenAction
    data object ResendEmailClick : VerifyEmailScreenAction
    data object SignOutClick : VerifyEmailScreenAction
}

sealed interface VerifyEmailScreenEffect

sealed interface VerifyEmailScreenResult {
    data class EmailLoaded(val email: String?) : VerifyEmailScreenResult
    data object Loading : VerifyEmailScreenResult
    data object Verified : VerifyEmailScreenResult
    data object NotVerified : VerifyEmailScreenResult
    data object ResendSuccess : VerifyEmailScreenResult
    data class ResendError(val message: String?) : VerifyEmailScreenResult
    data object SignedOut : VerifyEmailScreenResult
}