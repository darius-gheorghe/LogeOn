package com.sapphyx.logeon.viewModelImpl.main

import com.sapphyx.logeon.navigation.NavigationCommands
import com.sapphyx.logeon.navigation.NavigationManager
import com.sapphyx.logeon.viewModel.BaseInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

class MainInteractor @Inject constructor(
    private val navigationManager: NavigationManager
) : BaseInteractor<MainScreenAction, MainScreenResult> {
    override fun actionToResult(action: MainScreenAction): Flow<MainScreenResult> {
        return when (action) {
            is MainScreenAction.BottomBarClick -> {
                flowOf(MainScreenResult.NavigateWithNavBar(action.route))
            }
        }
    }

    override fun initResults(): Flow<MainScreenResult> {
        val usersFlow: Flow<MainScreenResult> = emptyFlow()

        val navigationFlow = navigationManager.navigateCommands.map {
            when (it) {
                is NavigationCommands.NavigateCommand -> MainScreenResult.NavigateWithDestination(it.destination)
                is NavigationCommands.PopBackCommand -> MainScreenResult.PopBack
            }
        }

        val loadedFlow = flowOf(MainScreenResult.Loaded)

        return merge(usersFlow, navigationFlow, loadedFlow)
    }
}