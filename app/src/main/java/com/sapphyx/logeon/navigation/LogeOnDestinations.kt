package com.sapphyx.logeon.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sapphyx.logeon.ui.composable.screens.add.AddScreen
import com.sapphyx.logeon.ui.composable.screens.home.HomeScreen
import com.sapphyx.logeon.ui.composable.screens.profile.ProfileScreen
import com.sapphyx.logeon.ui.composable.screens.pulse.PulseScreen

enum class LogeOnDestinations(
    val route: String,
    val hasBottomNavigation: Boolean,
    val content: @Composable () -> Unit
) {
    SignUp(
        route = DestinationRoutes.SIGNUP_ROUTE,
        hasBottomNavigation = false,
        content = {}
    ),
    VerifyEmail(
        route = DestinationRoutes.VERIFY_EMAIL_ROUTE,
        hasBottomNavigation = false,
        content = {}
    ),
    Login(
        route = DestinationRoutes.LOGIN_ROUTE,
        hasBottomNavigation = false,
        content = {}
    ),
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
    const val SIGNUP_ROUTE = "signup"
    const val VERIFY_EMAIL_ROUTE = "verify-email"
    const val LOGIN_ROUTE = "login"
    const val HOME_ROUTE = "home"
    const val PULSE_ROUTE = "pulse"
    const val ADD_ROUTE = "add"
    const val PROFILE_ROUTE = "profile"
}