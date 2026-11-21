package com.sapphyx.logeon.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationManager {
    private val _commands = MutableSharedFlow<NavigationCommands>(extraBufferCapacity = 1)
    val navigateCommands = _commands.asSharedFlow()

    fun navigate(directions: LogeOnDestinations) {
        val navigationCommand = NavigationCommands.NavigateCommand(directions)
        _commands.tryEmit(navigationCommand)
    }

    fun navigateBack() {
        val popBackCommand = NavigationCommands.PopBackCommand
        _commands.tryEmit(popBackCommand)
    }
}

sealed interface NavigationCommands {
    data class NavigateCommand(val destination: LogeOnDestinations) : NavigationCommands
    object PopBackCommand : NavigationCommands
}