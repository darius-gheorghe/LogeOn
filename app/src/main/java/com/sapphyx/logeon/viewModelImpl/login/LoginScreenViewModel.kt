package com.sapphyx.logeon.viewModelImpl.login

import android.content.Context
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    interactor: LoginScreenInteractor
) : BaseViewModel<LoginScreenState, LoginScreenAction, LoginScreenEffect, LoginScreenResult>(
    initialUiState = LoginScreenState(),
    interactor = interactor
) {

    override suspend fun handleResult(
        previousState: LoginScreenState,
        result: LoginScreenResult
    ): LoginScreenState {
        return when (result) {
            is LoginScreenResult.EmailUpdated -> previousState.copy(
                email = result.email,
                emailError = null,
                generalError = null,
            )

            is LoginScreenResult.PasswordUpdated -> previousState.copy(
                password = result.password,
                passwordError = null,
                generalError = null,
            )

            is LoginScreenResult.Loading -> previousState.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                generalError = null
            )

            is LoginScreenResult.Success,
            is LoginScreenResult.Cancelled -> previousState.copy(
                isLoading = false
            )

            is LoginScreenResult.EmailValidationError -> previousState.copy(
                isLoading = false,
                emailError = result.error,
                generalError = null
            )

            is LoginScreenResult.PasswordValidationError -> previousState.copy(
                isLoading = false,
                passwordError = result.error,
                generalError = null
            )

            is LoginScreenResult.Error -> previousState.copy(
                isLoading = false,
                emailError = null,
                passwordError = null,
                generalError = result.message
            )

            is LoginScreenResult.NavigateToSignUpScreen -> {
                navigationManager.navigate(LogeOnDestinations.SignUp)
                previousState
            }

            is LoginScreenResult.NavigateToForgotPasswordScreen -> {
                navigationManager.navigate(LogeOnDestinations.ForgotPassword)
                previousState
            }
        }
    }
}

data class LoginScreenState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: EmailValidationResult? = null,
    val passwordError: PasswordValidationResult? = null,
    val generalError: String? = null
)

sealed interface LoginScreenAction {
    data class EmailChanged(val email: String) : LoginScreenAction
    data class PasswordChanged(val password: String) : LoginScreenAction
    data class LoginClick(val email: String, val password: String) : LoginScreenAction
    data class GoogleSignInClick(val context: Context) : LoginScreenAction
    data object NavigateToSignUpClick : LoginScreenAction
    data object NavigateToForgotPasswordClick : LoginScreenAction
}

sealed interface LoginScreenEffect

sealed interface LoginScreenResult {
    data object Loading : LoginScreenResult
    data object Success : LoginScreenResult
    data object Cancelled : LoginScreenResult
    data class EmailUpdated(val email: String) : LoginScreenResult
    data class PasswordUpdated(val password: String) : LoginScreenResult
    data class EmailValidationError(val error: EmailValidationResult) : LoginScreenResult
    data class PasswordValidationError(val error: PasswordValidationResult) : LoginScreenResult
    data class Error(val message: String) : LoginScreenResult
    data object NavigateToSignUpScreen : LoginScreenResult
    data object NavigateToForgotPasswordScreen : LoginScreenResult
}