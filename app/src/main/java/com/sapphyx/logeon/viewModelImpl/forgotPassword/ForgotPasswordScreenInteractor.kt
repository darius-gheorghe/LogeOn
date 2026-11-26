package com.sapphyx.logeon.viewModelImpl.forgotPassword

import com.sapphyx.logeon.data.repository.AuthResponse
import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ForgotPasswordScreenInteractor @Inject constructor(
    private val authRepository: AuthenticationRepository
) : BaseInteractor<ForgotPasswordScreenAction, ForgotPasswordScreenResult> {

    override fun initResults(): Flow<ForgotPasswordScreenResult> = emptyFlow()

    override fun actionToResult(action: ForgotPasswordScreenAction): Flow<ForgotPasswordScreenResult> =
        when (action) {
            is ForgotPasswordScreenAction.EmailChanged ->
                flowOf(ForgotPasswordScreenResult.EmailUpdated(action.email))

            is ForgotPasswordScreenAction.SendResetLinkClick ->
                authRepository
                    .sendPasswordResetEmail(action.email)
                    .mapToForgotPasswordResult()

            is ForgotPasswordScreenAction.NavigateBackClick ->
                flowOf(ForgotPasswordScreenResult.NavigateBack)
        }

    private fun Flow<AuthResponse>.mapToForgotPasswordResult(): Flow<ForgotPasswordScreenResult> =
        map { response ->
            when (response) {
                is AuthResponse.Loading -> ForgotPasswordScreenResult.Loading
                is AuthResponse.Success -> ForgotPasswordScreenResult.Success
                is AuthResponse.Cancelled -> ForgotPasswordScreenResult.Cancelled
                is AuthResponse.EmailValidationError ->
                    ForgotPasswordScreenResult.EmailValidationError(response.validationError)

                is AuthResponse.PasswordValidationError ->
                    ForgotPasswordScreenResult.Cancelled

                is AuthResponse.Error ->
                    ForgotPasswordScreenResult.Error(response.message)
            }
        }
}