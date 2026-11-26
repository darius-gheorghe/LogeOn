package com.sapphyx.logeon.viewModelImpl.signUp

import com.sapphyx.logeon.data.repository.AuthResponse
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SignUpScreenInteractor @Inject constructor(
    private val authRepository: AuthenticationRepository,
) : BaseInteractor<SignUpScreenAction, SignUpScreenResult> {

    override fun actionToResult(action: SignUpScreenAction): Flow<SignUpScreenResult> =
        when (action) {
            is SignUpScreenAction.EmailChanged ->
                flowOf(SignUpScreenResult.EmailUpdated(action.email))

            is SignUpScreenAction.PasswordChanged ->
                flowOf(SignUpScreenResult.PasswordUpdated(action.password))

            is SignUpScreenAction.ConfirmPasswordChanged ->
                flowOf(SignUpScreenResult.ConfirmPasswordUpdated(action.confirmPassword))

            is SignUpScreenAction.SignUpClick -> {
                if (action.password != action.confirmPassword) {
                    flowOf(SignUpScreenResult.ConfirmPasswordMismatch)
                } else {
                    authRepository
                        .createAccountWithEmailAndPassword(action.email, action.password)
                        .mapToSignUpScreenResult()
                }
            }

            is SignUpScreenAction.NavigateToLoginClick ->
                flowOf(SignUpScreenResult.NavigateToLoginScreen)
        }

    override fun initResults(): Flow<SignUpScreenResult> = emptyFlow()

    private fun Flow<AuthResponse>.mapToSignUpScreenResult(): Flow<SignUpScreenResult> =
        map { response ->
            when (response) {
                is AuthResponse.Loading -> SignUpScreenResult.Loading
                is AuthResponse.Success -> SignUpScreenResult.Success
                is AuthResponse.Cancelled -> SignUpScreenResult.Cancelled

                is AuthResponse.EmailValidationError ->
                    SignUpScreenResult.EmailValidationError(response.validationError)

                is AuthResponse.PasswordValidationError ->
                    SignUpScreenResult.PasswordValidationError(response.validationError)

                is AuthResponse.Error -> SignUpScreenResult.Error(response.message)
            }
        }
}