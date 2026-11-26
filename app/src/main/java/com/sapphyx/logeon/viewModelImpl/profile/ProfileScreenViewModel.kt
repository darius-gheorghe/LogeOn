package com.sapphyx.logeon.viewModelImpl.profile

import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    interactor: ProfileScreenInteractor
) : BaseViewModel<ProfileScreenState, ProfileScreenAction, ProfileScreenEffect, ProfileScreenResult>(
    interactor = interactor,
    initialUiState = ProfileScreenState()
) {

    override suspend fun handleResult(
        previousState: ProfileScreenState,
        result: ProfileScreenResult
    ): ProfileScreenState {
        return when (result) {
            is ProfileScreenResult.UserLoaded -> previousState.copy(
                userEmail = result.email
            )

            is ProfileScreenResult.SignedOut -> previousState
        }
    }
}

data class ProfileScreenState(
    val userEmail: String? = null
)

sealed interface ProfileScreenAction {
    data object SignOutClick : ProfileScreenAction
}

sealed interface ProfileScreenEffect

sealed interface ProfileScreenResult {
    data class UserLoaded(val email: String?) : ProfileScreenResult
    data object SignedOut : ProfileScreenResult
}