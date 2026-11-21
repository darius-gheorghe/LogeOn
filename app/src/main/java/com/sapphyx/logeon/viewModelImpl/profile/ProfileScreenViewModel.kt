package com.sapphyx.logeon.viewModelImpl.profile

import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val navigationManager: NavigationManager
) : BaseViewModel<ProfileScreenState, ProfileScreenAction, ProfileScreenEffect, ProfileScreenResult>(
    interactor = ProfileScreenInteractor,
    initialUiState = ProfileScreenState.Initial
) {

    override suspend fun handleResult(
        previousState: ProfileScreenState,
        result: ProfileScreenResult
    ): ProfileScreenState {
        TODO("Not yet implemented")
    }
}

sealed interface ProfileScreenState {
    object Initial : ProfileScreenState
}

sealed interface ProfileScreenAction {

}

sealed interface ProfileScreenEffect {

}

sealed interface ProfileScreenResult {

}