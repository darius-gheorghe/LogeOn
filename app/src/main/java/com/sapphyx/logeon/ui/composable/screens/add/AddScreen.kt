package com.sapphyx.logeon.ui.composable.screens.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.viewModelImpl.add.AddScreenAction
import com.sapphyx.logeon.viewModelImpl.add.AddScreenEffect
import com.sapphyx.logeon.viewModelImpl.add.AddScreenState
import com.sapphyx.logeon.viewModelImpl.add.AddScreenViewModel
import kotlinx.coroutines.flow.SharedFlow

object AddScreen :
    BaseScreen<AddScreenState, AddScreenAction, AddScreenEffect, AddScreenViewModel>() {

    @Composable
    override fun Screen(
        state: AddScreenState,
        effect: SharedFlow<AddScreenEffect>,
        processUiAction: (action: AddScreenAction) -> Unit
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Add Screen",
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