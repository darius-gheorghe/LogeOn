package com.sapphyx.logeon.viewModelImpl.main

import com.sapphyx.logeon.data.repository.AuthenticationRepository
import com.sapphyx.logeon.navigation.NavigationCommands
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

class MainInteractor @Inject constructor(
    private val navigationManager: NavigationManager,
    private val authRepository: AuthenticationRepository
) : BaseInteractor<MainScreenAction, MainScreenResult> {
    override fun actionToResult(action: MainScreenAction): Flow<MainScreenResult> {
        return when (action) {
            is MainScreenAction.BottomBarClick -> {
                flowOf(MainScreenResult.NavigateWithNavBar(action.route))
            }
        }
    }

    override fun initResults(): Flow<MainScreenResult> {
        val userFlow: Flow<MainScreenResult> = authRepository.observeAuthState().map {
            when (it) {
                true -> MainScreenResult.LoggedIn
                false -> MainScreenResult.NotLoggedIn
            }
        }

        val navigationFlow = navigationManager.navigateCommands.map {
            when (it) {
                is NavigationCommands.NavigateCommand -> MainScreenResult.NavigateWithDestination(it.destination)
                is NavigationCommands.PopBackCommand -> MainScreenResult.PopBack
            }
        }

        return merge(userFlow, navigationFlow)
    }
}