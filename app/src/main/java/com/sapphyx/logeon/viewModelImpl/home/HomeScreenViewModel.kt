package com.sapphyx.logeon.viewModelImpl.home

import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
) : BaseViewModel<HomeScreenState, HomeScreenAction, HomeScreenEffect, HomeScreenResult>(
    interactor = HomeScreenInteractor,
    initialUiState = HomeScreenState.Initial
) {

    override suspend fun handleResult(
        previousState: HomeScreenState,
        result: HomeScreenResult
    ): HomeScreenState {
        TODO("Not yet implemented")
    }
}

sealed interface HomeScreenState {
    data object Initial : HomeScreenState
}

sealed interface HomeScreenAction {

}

sealed interface HomeScreenEffect {

}

sealed interface HomeScreenResult {

}