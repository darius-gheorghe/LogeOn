package com.sapphyx.logeon.viewModelImpl.main

import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(interactor: MainInteractor) :
    BaseViewModel<MainScreenState, MainScreenAction, MainScreenEffect, MainScreenResult>(
        interactor = interactor,
        initialUiState = MainScreenState.Loading
    ) {

    override suspend fun handleResult(
        previousState: MainScreenState,
        result: MainScreenResult
    ): MainScreenState {
        return when (result) {
            is MainScreenResult.LoggedIn -> {
                if (previousState is MainScreenState.NotLoggedIn) {
                    emitEffect(MainScreenEffect.AuthNavigation(LogeOnDestinations.Home.route))
                }
                MainScreenState.LoggedIn
            }

            is MainScreenResult.NotLoggedIn -> {
                if (previousState is MainScreenState.LoggedIn) {
                    emitEffect(MainScreenEffect.AuthNavigation(LogeOnDestinations.Login.route))
                }
                MainScreenState.NotLoggedIn
            }

            is MainScreenResult.NavigateWithDestination -> {
                emitEffect(MainScreenEffect.NavigateWithDestination(result.destination.route))
                previousState
            }

            is MainScreenResult.NavigateWithNavBar -> {
                emitEffect(MainScreenEffect.NavigateWithNavBar(result.route))
                previousState
            }

            is MainScreenResult.PopBack -> {
                emitEffect(MainScreenEffect.PopBack)
                previousState
            }
        }
    }
}

sealed interface MainScreenState {
    data object Loading : MainScreenState
    data object LoggedIn : MainScreenState
    data object NotLoggedIn : MainScreenState
}

sealed interface MainScreenAction {
    data class BottomBarClick(val route: String) : MainScreenAction
}

sealed interface MainScreenEffect {
    data class AuthNavigation(val route: String) : MainScreenEffect
    data class NavigateWithDestination(val route: String) : MainScreenEffect
    data class NavigateWithNavBar(val route: String) : MainScreenEffect
    data object PopBack : MainScreenEffect
}

sealed interface MainScreenResult {
    data object LoggedIn : MainScreenResult
    data object NotLoggedIn : MainScreenResult
    data class NavigateWithDestination(val destination: LogeOnDestinations) : MainScreenResult
    data class NavigateWithNavBar(val route: String) : MainScreenResult
    data object PopBack : MainScreenResult
}