package com.sapphyx.logeon.viewModelImpl.add

import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddScreenViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
) : BaseViewModel<AddScreenState, AddScreenAction, AddScreenEffect, AddScreenResult>(
    interactor = AddScreenInteractor,
    initialUiState = AddScreenState.Initial
) {

    override suspend fun handleResult(
        previousState: AddScreenState,
        result: AddScreenResult
    ): AddScreenState {
        TODO("Not yet implemented")
    }
}

sealed interface AddScreenState {
    data object Initial : AddScreenState
}

sealed interface AddScreenAction {

}

sealed interface AddScreenEffect {

}

sealed interface AddScreenResult {

}