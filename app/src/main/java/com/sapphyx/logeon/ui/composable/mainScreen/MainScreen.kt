package com.sapphyx.logeon.ui.composable.mainScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.ui.composable.navigation.LogeOnNavHost
import com.sapphyx.logeon.viewModelImpl.main.MainScreenAction
import com.sapphyx.logeon.viewModelImpl.main.MainScreenEffect
import com.sapphyx.logeon.viewModelImpl.main.MainScreenState
import com.sapphyx.logeon.viewModelImpl.main.MainViewModel
import kotlinx.coroutines.flow.SharedFlow

object MainScreen :
    BaseScreen<MainScreenState, MainScreenAction, MainScreenEffect, MainViewModel>() {

    @Composable
    override fun Screen(
        state: MainScreenState,
        effect: SharedFlow<MainScreenEffect>,
        processUiAction: (action: MainScreenAction) -> Unit
    ) {
        val navController = rememberNavController()

        val startRoute = when(state) {
            is MainScreenState.Initial -> LogeOnDestinations.Main.route
            is MainScreenState.Loaded -> LogeOnDestinations.Home.route
        }

        LogeOnNavHost(
            navController = navController,
            startRoute = startRoute,
            action = {
                processUiAction(MainScreenAction.BottomBarClick(it.route))
            }
        )

        LaunchedEffect(Unit) {
            effect.collect {
                when(it) {
                    is MainScreenEffect.NavigateWithDestination -> {
                        navController.popBackStack(it.route, inclusive = true)
                        navController.navigate(it.route)
                    }

                    is MainScreenEffect.NavigateWithNavBar -> {
                        when(it.route) {
                            LogeOnDestinations.Home.route -> {
                                navController.popBackStack()
                            }

                            else -> {
                                navController.navigate(it.route) {
                                    popUpTo(LogeOnDestinations.Home.route)
                                }
                            }
                        }
                    }

                    is MainScreenEffect.PopBack -> {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}