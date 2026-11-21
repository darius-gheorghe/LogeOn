package com.sapphyx.logeon.viewModelImpl.pulse

import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PulseScreenViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
) : BaseViewModel<PulseScreenState, PulseScreenAction, PulseScreenEffect, PulseScreenResult>(
    interactor = PulseScreenInteractor,
    initialUiState = PulseScreenState.Initial
) {

    override suspend fun handleResult(
        previousState: PulseScreenState,
        result: PulseScreenResult
    ): PulseScreenState {
        TODO("Not yet implemented")
    }
}

sealed interface PulseScreenState {
    object Initial : PulseScreenState
}

sealed interface PulseScreenAction {

}

sealed interface PulseScreenEffect {

}

sealed interface PulseScreenResult {

}