package com.sapphyx.logeon.ui.composable.screens.verifyEmail

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sapphyx.logeon.R
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenAction
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenEffect
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenState
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailScreenViewModel
import com.sapphyx.logeon.viewModelImpl.verifyEmail.VerifyEmailStatusMessage
import kotlinx.coroutines.flow.SharedFlow

object VerifyEmailScreen :
    BaseScreen<VerifyEmailScreenState, VerifyEmailScreenAction, VerifyEmailScreenEffect, VerifyEmailScreenViewModel>() {

    @Composable
    override fun Screen(
        state: VerifyEmailScreenState,
        effect: SharedFlow<VerifyEmailScreenEffect>,
        processUiAction: (action: VerifyEmailScreenAction) -> Unit
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
            EmailHeaderIcon()

            Spacer(modifier = Modifier.height(16.dp))

            ScreenHeadline()

            Spacer(modifier = Modifier.height(12.dp))

            ScreenSubtitle(userEmail = state.userEmail)

            StatusBanner(statusMessage = state.statusMessage)

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                CheckVerifiedButton(processUiAction = processUiAction)

                Spacer(modifier = Modifier.height(12.dp))

                ResendEmailButton(processUiAction = processUiAction)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SignOutButton(
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )
        }
    }

    @Composable
    private fun EmailHeaderIcon() {
        Icon(
            imageVector = Icons.Outlined.MarkEmailUnread,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
    }

    @Composable
    private fun ScreenHeadline() {
        Text(
            text = stringResource(R.string.verify_email_screen_headline),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun ScreenSubtitle(userEmail: String?) {
        val subtitleText = if (!userEmail.isNullOrBlank()) {
            stringResource(R.string.verify_email_screen_subtitle_with_email, userEmail)
        } else {
            stringResource(R.string.verify_email_screen_subtitle)
        }

        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun StatusBanner(statusMessage: VerifyEmailStatusMessage?) {
        statusMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))

            val (text, color) = when (message) {
                is VerifyEmailStatusMessage.NotVerified -> {
                    stringResource(R.string.verify_email_screen_not_verified_yet) to
                            MaterialTheme.colorScheme.error
                }

                is VerifyEmailStatusMessage.ResendSuccess -> {
                    stringResource(R.string.verify_email_screen_resend_success) to
                            MaterialTheme.colorScheme.primary
                }

                is VerifyEmailStatusMessage.ResendError -> {
                    (message.customMessage
                        ?: stringResource(R.string.verify_email_screen_resend_error)) to
                            MaterialTheme.colorScheme.error
                }
            }

            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun CheckVerifiedButton(
        processUiAction: (action: VerifyEmailScreenAction) -> Unit
    ) {
        Button(
            onClick = {
                processUiAction(VerifyEmailScreenAction.CheckVerifiedClick)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.verify_email_screen_check_button))
        }
    }

    @Composable
    private fun ResendEmailButton(
        processUiAction: (action: VerifyEmailScreenAction) -> Unit
    ) {
        OutlinedButton(
            onClick = {
                processUiAction(VerifyEmailScreenAction.ResendEmailClick)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.verify_email_screen_resend_button))
        }
    }

    @Composable
    private fun SignOutButton(
        isLoading: Boolean,
        processUiAction: (action: VerifyEmailScreenAction) -> Unit
    ) {
        TextButton(
            onClick = {
                processUiAction(VerifyEmailScreenAction.SignOutClick)
            },
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.verify_email_screen_sign_out_button))
        }
    }
}