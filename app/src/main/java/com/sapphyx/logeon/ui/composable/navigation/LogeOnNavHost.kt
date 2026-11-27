package com.sapphyx.logeon.ui.composable.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sapphyx.logeon.navigation.LogeOnDestinations
import com.sapphyx.logeon.navigation.bottomNavigation.BottomNavItem

private const val TRANSITION_DURATION_MS = 300
private const val FADE_DURATION_MS = 220

@Composable
fun LogeOnNavHost(
    navController: NavHostController,
    startRoute: String,
    action: (BottomNavItem) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startRoute

    val bottomBarRoutes = remember {
        LogeOnDestinations.entries
            .filter { it.hasBottomNavigation }
            .map { it.route }
            .toSet()
    }

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                LogeOnBottomNavigationBar(
                    navController = navController,
                    action = action
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { resolveEnterTransition(bottomBarRoutes) },
            exitTransition = { resolveExitTransition(bottomBarRoutes) },
            popEnterTransition = { resolvePopEnterTransition(bottomBarRoutes) },
            popExitTransition = { resolvePopExitTransition(bottomBarRoutes) }
        ) {
            LogeOnDestinations.entries.forEach { destination ->
                composable(destination.route) {
                    destination.content()
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.resolveEnterTransition(
    bottomBarRoutes: Set<String>
): EnterTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return when {
        fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes -> {
            fadeIn(animationSpec = tween(FADE_DURATION_MS))
        }

        toRoute == LogeOnDestinations.Login.route -> {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
        }

        else -> {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.resolveExitTransition(
    bottomBarRoutes: Set<String>
): ExitTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return when {
        fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes -> {
            fadeOut(animationSpec = tween(FADE_DURATION_MS))
        }

        toRoute == LogeOnDestinations.Login.route -> {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        }

        else -> {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.resolvePopEnterTransition(
    bottomBarRoutes: Set<String>
): EnterTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return if (fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes) {
        fadeIn(animationSpec = tween(FADE_DURATION_MS))
    } else {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.resolvePopExitTransition(
    bottomBarRoutes: Set<String>
): ExitTransition {
    val fromRoute = initialState.destination.route
    val toRoute = targetState.destination.route

    return if (fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes) {
        fadeOut(animationSpec = tween(FADE_DURATION_MS))
    } else {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
    }
}