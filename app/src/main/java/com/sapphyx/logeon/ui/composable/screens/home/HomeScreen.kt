package com.sapphyx.logeon.ui.composable.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.viewModelImpl.home.HomeScreenAction
import com.sapphyx.logeon.viewModelImpl.home.HomeScreenEffect
import com.sapphyx.logeon.viewModelImpl.home.HomeScreenState
import com.sapphyx.logeon.viewModelImpl.home.HomeScreenViewModel
import kotlinx.coroutines.flow.SharedFlow

object HomeScreen :
    BaseScreen<HomeScreenState, HomeScreenAction, HomeScreenEffect, HomeScreenViewModel>() {

    @Composable
    override fun Screen(
        state: HomeScreenState,
        effect: SharedFlow<HomeScreenEffect>,
        processUiAction: (action: HomeScreenAction) -> Unit
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Home Screen",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    @Composable
    private fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}