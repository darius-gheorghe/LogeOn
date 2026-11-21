package com.sapphyx.logeon.ui.composable.screens.pulse

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.viewModelImpl.pulse.PulseScreenAction
import com.sapphyx.logeon.viewModelImpl.pulse.PulseScreenEffect
import com.sapphyx.logeon.viewModelImpl.pulse.PulseScreenState
import com.sapphyx.logeon.viewModelImpl.pulse.PulseScreenViewModel
import kotlinx.coroutines.flow.SharedFlow

object PulseScreen :
    BaseScreen<PulseScreenState, PulseScreenAction, PulseScreenEffect, PulseScreenViewModel>() {

    @Composable
    override fun Screen(
        state: PulseScreenState,
        effect: SharedFlow<PulseScreenEffect>,
        processUiAction: (action: PulseScreenAction) -> Unit
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Pulse Screen",
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