package com.sapphyx.logeon.viewModelImpl.profile

import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ProfileScreenInteractor @Inject constructor(
    private val authRepository: AuthenticationRepository
) : BaseInteractor<ProfileScreenAction, ProfileScreenResult> {

    override fun initResults(): Flow<ProfileScreenResult> =
        flowOf(ProfileScreenResult.UserLoaded(authRepository.currentUser?.email))

    override fun actionToResult(action: ProfileScreenAction): Flow<ProfileScreenResult> =
        when (action) {
            is ProfileScreenAction.SignOutClick -> {
                authRepository.signOut()
                flowOf(ProfileScreenResult.SignedOut)
            }
        }
}