package com.sapphyx.logeon.navigation.bottomNavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import com.sapphyx.logeon.R
import com.sapphyx.logeon.navigation.LogeOnDestinations

sealed class BottomNavItem(
    val route: String,
    val title: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = LogeOnDestinations.Home.route,
        title = R.string.home,
        icon = Icons.Default.Home
    )

    object Pulse : BottomNavItem(
        route = LogeOnDestinations.Pulse.route,
        title = R.string.pulse,
        icon = Icons.Default.PlayArrow
    )

    object Add : BottomNavItem(
        route = LogeOnDestinations.Add.route,
        title = R.string.add,
        icon = Icons.Default.Add
    )

    object Profile : BottomNavItem(
        route = LogeOnDestinations.Profile.route,
        title = R.string.profile,
        icon = Icons.Default.AccountCircle
    )
}