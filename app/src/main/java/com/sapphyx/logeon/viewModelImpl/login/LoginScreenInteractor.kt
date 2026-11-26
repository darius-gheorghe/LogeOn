package com.sapphyx.logeon.viewModelImpl.login

import com.sapphyx.logeon.data.repository.AuthResponse
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoginScreenInteractor @Inject constructor(
    private val authRepository: AuthenticationRepository,
) : BaseInteractor<LoginScreenAction, LoginScreenResult> {

    override fun actionToResult(action: LoginScreenAction): Flow<LoginScreenResult> =
        when (action) {
            is LoginScreenAction.EmailChanged ->
                flowOf(LoginScreenResult.EmailUpdated(action.email))

            is LoginScreenAction.PasswordChanged ->
                flowOf(LoginScreenResult.PasswordUpdated(action.password))

            is LoginScreenAction.LoginClick -> authRepository
                .loginWithEmailAndPassword(action.email, action.password)
                .mapToLoginScreenResult()

            is LoginScreenAction.GoogleSignInClick -> authRepository
                .signInWithGoogle(action.context)
                .mapToLoginScreenResult()

            is LoginScreenAction.NavigateToSignUpClick ->
                flowOf(LoginScreenResult.NavigateToSignUpScreen)

            is LoginScreenAction.NavigateToForgotPasswordClick ->
                flowOf(LoginScreenResult.NavigateToForgotPasswordScreen)
        }

    override fun initResults(): Flow<LoginScreenResult> = emptyFlow()

    private fun Flow<AuthResponse>.mapToLoginScreenResult(): Flow<LoginScreenResult> =
        map { response ->
            when (response) {
                is AuthResponse.Loading -> LoginScreenResult.Loading
                is AuthResponse.Success -> LoginScreenResult.Success
                is AuthResponse.Cancelled -> LoginScreenResult.Cancelled
                is AuthResponse.EmailValidationError -> LoginScreenResult.EmailValidationError(
                    response.validationError
                )

                is AuthResponse.PasswordValidationError -> LoginScreenResult.PasswordValidationError(
                    response.validationError
                )

                is AuthResponse.Error -> LoginScreenResult.Error(response.message)
            }
        }
}