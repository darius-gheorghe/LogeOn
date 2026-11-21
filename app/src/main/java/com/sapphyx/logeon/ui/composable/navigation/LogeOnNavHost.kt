package com.sapphyx.logeon.ui.composable.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.navigation.bottomNavigation.BottomNavItem

@Composable
fun LogeOnNavHost(
    navController: NavHostController,
    startRoute: String,
    action: (BottomNavItem) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        LogeOnDestinations.entries.forEach { destination ->
            composable(destination.route) {
                if (destination.hasBottomNavigation) {
                    Scaffold(
                        bottomBar = {
                            LogeOnBottomNavigationBar(
                                navController = navController,
                                action = action
                            )
                        }
                    ) { padding ->
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)) {
                            destination.content()
                        }
                    }
                } else {
                    destination.content()
                }
            }
        }
    }
}