package com.sapphyx.logeon.viewModelImpl.signUp

import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    interactor: SignUpScreenInteractor
) : BaseViewModel<SignUpScreenState, SignUpScreenAction, SignUpScreenEffect, SignUpScreenResult>(
    initialUiState = SignUpScreenState(),
    interactor = interactor
) {

    override suspend fun handleResult(
        previousState: SignUpScreenState,
        result: SignUpScreenResult
    ): SignUpScreenState {
        return when (result) {
            is SignUpScreenResult.EmailUpdated -> previousState.copy(
                email = result.email,
                emailError = null,
                generalError = null
            )

            is SignUpScreenResult.PasswordUpdated -> previousState.copy(
                password = result.password,
                passwordError = null,
                confirmPasswordError = false,
                generalError = null
            )

            is SignUpScreenResult.ConfirmPasswordUpdated -> previousState.copy(
                confirmPassword = result.confirmPassword,
                confirmPasswordError = false,
                generalError = null
            )

            is SignUpScreenResult.Loading -> previousState.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                confirmPasswordError = false,
                generalError = null
            )

            is SignUpScreenResult.Success,
            is SignUpScreenResult.Cancelled -> previousState.copy(
                isLoading = false
            )

            is SignUpScreenResult.EmailValidationError -> previousState.copy(
                isLoading = false,
                emailError = result.error,
                generalError = null
            )

            is SignUpScreenResult.PasswordValidationError -> previousState.copy(
                isLoading = false,
                passwordError = result.error,
                generalError = null
            )

            is SignUpScreenResult.ConfirmPasswordMismatch -> previousState.copy(
                isLoading = false,
                confirmPasswordError = true,
                generalError = null
            )

            is SignUpScreenResult.Error -> previousState.copy(
                isLoading = false,
                emailError = null,
                passwordError = null,
                confirmPasswordError = false,
                generalError = result.message
            )

            is SignUpScreenResult.NavigateToLoginScreen -> {
                navigationManager.navigateBack()
                previousState
            }
        }
    }
}

data class SignUpScreenState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val emailError: EmailValidationResult? = null,
    val passwordError: PasswordValidationResult? = null,
    val confirmPasswordError: Boolean = false,
    val generalError: String? = null
)

sealed interface SignUpScreenAction {
    data class EmailChanged(val email: String) : SignUpScreenAction
    data class PasswordChanged(val password: String) : SignUpScreenAction
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpScreenAction
    data class SignUpClick(
        val email: String,
        val password: String,
        val confirmPassword: String
    ) : SignUpScreenAction

    data object NavigateToLoginClick : SignUpScreenAction
}

sealed interface SignUpScreenEffect

sealed interface SignUpScreenResult {
    data object Loading : SignUpScreenResult
    data object Success : SignUpScreenResult
    data object Cancelled : SignUpScreenResult
    data class EmailUpdated(val email: String) : SignUpScreenResult
    data class PasswordUpdated(val password: String) : SignUpScreenResult
    data class ConfirmPasswordUpdated(val confirmPassword: String) : SignUpScreenResult
    data class EmailValidationError(val error: EmailValidationResult) : SignUpScreenResult
    data class PasswordValidationError(val error: PasswordValidationResult) : SignUpScreenResult
    data object ConfirmPasswordMismatch : SignUpScreenResult
    data class Error(val message: String) : SignUpScreenResult
    data object NavigateToLoginScreen : SignUpScreenResult
}