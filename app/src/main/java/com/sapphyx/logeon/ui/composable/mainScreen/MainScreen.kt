package com.sapphyx.logeon.ui.composable.mainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.sapphyx.logeon.R
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
        when (state) {
            is MainScreenState.Loading -> LoadingScreen()

            is MainScreenState.Error -> ErrorScreen(
                state = state,
                processUiAction = processUiAction
            )

            else -> LoadingCompletedScreen(
                state = state,
                effect = effect,
                processUiAction = processUiAction
            )
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun ErrorScreen(
        state: MainScreenState.Error,
        processUiAction: (action: MainScreenAction) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { processUiAction(MainScreenAction.RetryProfileLoad) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.onboarding_error_retry_button))
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { processUiAction(MainScreenAction.SignOutClick) }
            ) {
                Text(stringResource(R.string.onboarding_sign_out_button))
            }
        }
    }

    @Composable
    private fun LoadingCompletedScreen(
        state: MainScreenState,
        effect: SharedFlow<MainScreenEffect>,
        processUiAction: (action: MainScreenAction) -> Unit
    ) {
        val navController = rememberNavController()

        val startRoute = when (state) {
            is MainScreenState.LoggedIn -> LogeOnDestinations.Home.route
            is MainScreenState.UnverifiedEmail -> LogeOnDestinations.VerifyEmail.route
            is MainScreenState.OnboardingRequired -> LogeOnDestinations.Onboarding.route
            else -> LogeOnDestinations.Login.route
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
                when (it) {
                    is MainScreenEffect.AuthNavigation -> {
                        navController.navigate(it.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    is MainScreenEffect.NavigateWithDestination -> {
                        navController.popBackStack(it.route, inclusive = true)
                        navController.navigate(it.route)
                    }

                    is MainScreenEffect.NavigateWithNavBar -> {
                        when (it.route) {
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