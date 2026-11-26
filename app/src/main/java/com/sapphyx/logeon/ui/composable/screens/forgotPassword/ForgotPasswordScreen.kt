package com.sapphyx.logeon.ui.composable.screens.forgotPassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sapphyx.logeon.R
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.ui.util.toErrorMessage
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenAction
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenEffect
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenState
import com.sapphyx.logeon.viewModelImpl.forgotPassword.ForgotPasswordScreenViewModel
import kotlinx.coroutines.flow.SharedFlow

object ForgotPasswordScreen :
    BaseScreen<ForgotPasswordScreenState, ForgotPasswordScreenAction, ForgotPasswordScreenEffect, ForgotPasswordScreenViewModel>() {

    @Composable
    override fun Screen(
        state: ForgotPasswordScreenState,
        effect: SharedFlow<ForgotPasswordScreenEffect>,
        processUiAction: (action: ForgotPasswordScreenAction) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderIcon()

            Spacer(modifier = Modifier.height(16.dp))

            ScreenHeadline()

            Spacer(modifier = Modifier.height(12.dp))

            ScreenSubtitle()

            Spacer(modifier = Modifier.height(24.dp))

            EmailField(
                email = state.email,
                isError = state.emailError != null,
                errorMessage = state.emailError?.toErrorMessage(),
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            GeneralError(state.generalError)

            SuccessBanner(isVisible = state.isResetEmailSent)

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                SendResetLinkButton(
                    email = state.email,
                    processUiAction = processUiAction
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            BackToLoginButton(
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )
        }
    }

    @Composable
    private fun HeaderIcon() {
        Icon(
            imageVector = Icons.Outlined.LockReset,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
    }

    @Composable
    private fun ScreenHeadline() {
        Text(
            text = stringResource(R.string.forgot_password_screen_headline),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun ScreenSubtitle() {
        Text(
            text = stringResource(R.string.forgot_password_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun EmailField(
        email: String,
        isError: Boolean,
        errorMessage: String?,
        isLoading: Boolean,
        processUiAction: (action: ForgotPasswordScreenAction) -> Unit
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { processUiAction(ForgotPasswordScreenAction.EmailChanged(it)) },
            label = { Text(stringResource(R.string.auth_email_label)) },
            isError = isError,
            supportingText = errorMessage?.let { message ->
                { Text(text = message) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun GeneralError(errorMessage: String?) {
        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun SuccessBanner(isVisible: Boolean) {
        if (isVisible) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.forgot_password_screen_success_message),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun SendResetLinkButton(
        email: String,
        processUiAction: (action: ForgotPasswordScreenAction) -> Unit
    ) {
        Button(
            onClick = {
                processUiAction(ForgotPasswordScreenAction.SendResetLinkClick(email))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.forgot_password_screen_send_button))
        }
    }

    @Composable
    private fun BackToLoginButton(
        isLoading: Boolean,
        processUiAction: (action: ForgotPasswordScreenAction) -> Unit
    ) {
        TextButton(
            onClick = {
                processUiAction(ForgotPasswordScreenAction.NavigateBackClick)
            },
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.forgot_password_screen_back_to_login))
        }
    }
}