package com.sapphyx.logeon.viewModelImpl.main

import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(interactor: MainInteractor) :
    BaseViewModel<MainScreenState, MainScreenAction, MainScreenEffect, MainScreenResult>(
        interactor = interactor,
        initialUiState = MainScreenState.Initial
    ) {

    override suspend fun handleResult(
        previousState: MainScreenState,
        result: MainScreenResult
    ): MainScreenState {
        return when (result) {
            is MainScreenResult.Loaded -> {
                MainScreenState.Loaded
            }

            is MainScreenResult.NavigateWithDestination -> {
                emmitEffect(MainScreenEffect.NavigateWithDestination(result.destination.route))
                previousState
            }

            is MainScreenResult.NavigateWithNavBar -> {
                emmitEffect(MainScreenEffect.NavigateWithNavBar(result.route))
                previousState
            }

            is MainScreenResult.PopBack -> {
                emmitEffect(MainScreenEffect.PopBack)
                previousState
            }
        }
    }
}

sealed interface MainScreenState {
    object Initial : MainScreenState

    object Loaded : MainScreenState
}

sealed interface MainScreenAction {
    data class BottomBarClick(val route: String) : MainScreenAction
}

sealed interface MainScreenEffect {
    data class NavigateWithDestination(val route: String) : MainScreenEffect
    data class NavigateWithNavBar(val route: String) : MainScreenEffect
    object PopBack : MainScreenEffect
}

sealed interface MainScreenResult {
    object Loaded : MainScreenResult
    data class NavigateWithDestination(val destination: LogeOnDestinations) : MainScreenResult
    data class NavigateWithNavBar(val route: String) : MainScreenResult
    object PopBack : MainScreenResult
}