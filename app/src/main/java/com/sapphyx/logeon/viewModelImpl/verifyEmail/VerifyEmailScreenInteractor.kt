package com.sapphyx.logeon.viewModelImpl.verifyEmail

import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class VerifyEmailScreenInteractor @Inject constructor(
    private val authRepository: AuthenticationRepository
) : BaseInteractor<VerifyEmailScreenAction, VerifyEmailScreenResult> {
    override fun actionToResult(action: VerifyEmailScreenAction): Flow<VerifyEmailScreenResult> =
        when (action) {
            is VerifyEmailScreenAction.CheckVerifiedClick -> flow {
                emit(VerifyEmailScreenResult.Loading)
                val isVerified = authRepository.checkEmailVerified()
                if (isVerified) {
                    emit(VerifyEmailScreenResult.Verified)
                } else {
                    emit(VerifyEmailScreenResult.NotVerified)
                }
            }

            is VerifyEmailScreenAction.ResendEmailClick -> flow {
                emit(VerifyEmailScreenResult.Loading)
                val result = authRepository.resendVerificationEmail()
                result.fold(
                    onSuccess = { emit(VerifyEmailScreenResult.ResendSuccess) },
                    onFailure = { error ->
                        emit(VerifyEmailScreenResult.ResendError(error.message))
                    }
                )
            }

            is VerifyEmailScreenAction.SignOutClick -> {
                authRepository.signOut()
                flowOf(VerifyEmailScreenResult.SignedOut)
            }
        }


    override fun initResults(): Flow<VerifyEmailScreenResult> =
        flowOf(VerifyEmailScreenResult.EmailLoaded(authRepository.currentUser?.email))
}