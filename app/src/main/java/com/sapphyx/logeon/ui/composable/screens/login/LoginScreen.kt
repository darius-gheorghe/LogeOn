package com.sapphyx.logeon.ui.composable.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sapphyx.logeon.R
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.ui.util.toErrorMessage
import com.sapphyx.logeon.ui.util.toLoginErrorMessage
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenAction
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenEffect
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenState
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenViewModel
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.res.stringResource

object LoginScreen :
    BaseScreen<LoginScreenState, LoginScreenAction, LoginScreenEffect, LoginScreenViewModel>() {

    @Composable
    override fun Screen(
        state: LoginScreenState,
        effect: SharedFlow<LoginScreenEffect>,
        processUiAction: (action: LoginScreenAction) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeadline()

            Spacer(modifier = Modifier.height(24.dp))

            EmailField(
                email = state.email,
                isError = state.emailError != null,
                errorMessage = state.emailError?.toErrorMessage(),
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordField(
                password = state.password,
                isError = state.passwordError != null,
                errorMessage = state.passwordError?.toLoginErrorMessage(),
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            GeneralError(state.generalError)

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                SignInButton(
                    email = state.email,
                    password = state.password,
                    processUiAction = processUiAction
                )

                Spacer(modifier = Modifier.height(12.dp))

                ContinueWithGoogleButton(processUiAction = processUiAction)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SignUpButton(
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )
        }
    }

    @Composable
    private fun ScreenHeadline() {
        Text(
            text = stringResource(R.string.login_screen_headline),
            style = MaterialTheme.typography.headlineMedium
        )
    }

    @Composable
    private fun EmailField(
        email: String,
        isError: Boolean,
        errorMessage: String?,
        isLoading: Boolean,
        processUiAction: (action: LoginScreenAction) -> Unit
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { processUiAction(LoginScreenAction.EmailChanged(it)) },
            label = { Text(stringResource(R.string.auth_email_label)) },
            isError = isError,
            supportingText = errorMessage?.let { message ->
                { Text(text = message) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun PasswordField(
        password: String,
        isError: Boolean,
        errorMessage: String?,
        isLoading: Boolean,
        processUiAction: (action: LoginScreenAction) -> Unit
    ) {
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = { processUiAction(LoginScreenAction.PasswordChanged(it)) },
            label = { Text(stringResource(R.string.auth_password_label)) },
            isError = isError,
            supportingText = errorMessage?.let { message ->
                { Text(text = message) }
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    enabled = !isLoading,
                    onClick = { passwordVisible = !passwordVisible },
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            stringResource(R.string.auth_hide_password)
                        } else {
                            stringResource(R.string.auth_show_password)
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
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
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    @Composable
    private fun SignInButton(
        email: String,
        password: String,
        processUiAction: (action: LoginScreenAction) -> Unit
    ) {
        Button(
            onClick = {
                processUiAction(
                    LoginScreenAction.LoginClick(email, password)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_screen_sign_in))
        }
    }

    @Composable
    private fun ContinueWithGoogleButton(processUiAction: (action: LoginScreenAction) -> Unit) {
        val context = LocalContext.current

        OutlinedButton(
            onClick = {
                processUiAction(LoginScreenAction.GoogleSignInClick(context))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_screen_continue_with_google))
        }
    }

    @Composable
    private fun SignUpButton(
        isLoading: Boolean,
        processUiAction: (action: LoginScreenAction) -> Unit
    ) {
        TextButton(
            onClick = {
                processUiAction(LoginScreenAction.NavigateToSignUpClick)
            },
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.login_screen_sign_up))
        }
    }
}