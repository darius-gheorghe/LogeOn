package com.sapphyx.logeon.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sapphyx.logeon.ui.composable.mainScreen.MainScreen
import com.sapphyx.logeon.ui.composable.screens.add.AddScreen
import com.sapphyx.logeon.ui.composable.screens.home.HomeScreen
import com.sapphyx.logeon.ui.composable.screens.profile.ProfileScreen
import com.sapphyx.logeon.ui.composable.screens.pulse.PulseScreen

enum class LogeOnDestinations(
    val route: String,
    val hasBottomNavigation: Boolean,
    val content: @Composable () -> Unit
) {
    Main(
        route = DestinationRoutes.MAIN_ROUTE,
        hasBottomNavigation = true,
        content = { MainScreen.Content(hiltViewModel()) }),
    Home(
        route = DestinationRoutes.HOME_ROUTE,
        hasBottomNavigation = true,
        content = { HomeScreen.Content(hiltViewModel()) }),
    Pulse(
        route = DestinationRoutes.PULSE_ROUTE,
        hasBottomNavigation = true,
        content = { PulseScreen.Content(hiltViewModel()) }),
    Add(
        route = DestinationRoutes.ADD_ROUTE,
        hasBottomNavigation = true,
        content = { AddScreen.Content(hiltViewModel()) }),
    Profile(
        route = DestinationRoutes.PROFILE_ROUTE,
        hasBottomNavigation = true,
        content = { ProfileScreen.Content(hiltViewModel()) }),
}

private object DestinationRoutes {
    const val MAIN_ROUTE = "main"
    const val HOME_ROUTE = "home"
    const val PULSE_ROUTE = "pulse"
    const val ADD_ROUTE = "add"
    const val PROFILE_ROUTE = "profile"
}