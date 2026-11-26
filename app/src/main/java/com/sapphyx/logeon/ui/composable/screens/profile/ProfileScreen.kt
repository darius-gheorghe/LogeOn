package com.sapphyx.logeon.ui.composable.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapphyx.logeon.R
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.profile),
                style = MaterialTheme.typography.headlineMedium
            )

            state.userEmail?.let { email ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { processUiAction(ProfileScreenAction.SignOutClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.verify_email_screen_sign_out_button))
            }
        }
    }
}