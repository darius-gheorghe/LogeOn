package com.sapphyx.logeon.ui.composable.screens.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenAction
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenEffect
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenState
import com.sapphyx.logeon.viewModelImpl.profile.ProfileScreenViewModel
import kotlinx.coroutines.flow.SharedFlow

object ProfileScreen :
    BaseScreen<ProfileScreenState, ProfileScreenAction, ProfileScreenEffect, ProfileScreenViewModel>() {

    @Composable
    override fun Screen(
        state: ProfileScreenState,
        effect: SharedFlow<ProfileScreenEffect>,
        processUiAction: (action: ProfileScreenAction) -> Unit
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Profile Screen",
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