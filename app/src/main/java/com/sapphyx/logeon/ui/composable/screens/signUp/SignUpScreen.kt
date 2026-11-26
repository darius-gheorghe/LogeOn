package com.sapphyx.logeon.ui.composable.screens.signUp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sapphyx.logeon.R
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.ui.util.toErrorMessage
import com.sapphyx.logeon.ui.util.toSignupErrorMessage
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenAction
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenEffect
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenState
import com.sapphyx.logeon.viewModelImpl.signUp.SignUpScreenViewModel
import kotlinx.coroutines.flow.SharedFlow

object SignUpScreen :
    BaseScreen<SignUpScreenState, SignUpScreenAction, SignUpScreenEffect, SignUpScreenViewModel>() {

    @Composable
    override fun Screen(
        state: SignUpScreenState,
        effect: SharedFlow<SignUpScreenEffect>,
        processUiAction: (action: SignUpScreenAction) -> Unit
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
                errorMessage = state.passwordError?.toSignupErrorMessage(),
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfirmPasswordField(
                confirmPassword = state.confirmPassword,
                isError = state.confirmPasswordError,
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            GeneralError(state.generalError)

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                CreateAccountButton(
                    email = state.email,
                    password = state.password,
                    confirmPassword = state.confirmPassword,
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
    private fun ScreenHeadline() {
        Text(
            text = stringResource(R.string.signup_screen_headline),
            style = MaterialTheme.typography.headlineMedium
        )
    }

    @Composable
    private fun EmailField(
        email: String,
        isError: Boolean,
        errorMessage: String?,
        isLoading: Boolean,
        processUiAction: (action: SignUpScreenAction) -> Unit
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { processUiAction(SignUpScreenAction.EmailChanged(it)) },
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
        processUiAction: (action: SignUpScreenAction) -> Unit
    ) {
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = { processUiAction(SignUpScreenAction.PasswordChanged(it)) },
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
                    onClick = { passwordVisible = !passwordVisible }
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
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun ConfirmPasswordField(
        confirmPassword: String,
        isError: Boolean,
        isLoading: Boolean,
        processUiAction: (action: SignUpScreenAction) -> Unit
    ) {
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { processUiAction(SignUpScreenAction.ConfirmPasswordChanged(it)) },
            label = { Text(stringResource(R.string.signup_confirm_password_label)) },
            isError = isError,
            supportingText = if (isError) {
                { Text(text = stringResource(R.string.signup_error_confirm_password_mismatch)) }
            } else {
                null
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    enabled = !isLoading,
                    onClick = { passwordVisible = !passwordVisible }
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
    private fun CreateAccountButton(
        email: String,
        password: String,
        confirmPassword: String,
        processUiAction: (action: SignUpScreenAction) -> Unit
    ) {
        Button(
            onClick = {
                processUiAction(
                    SignUpScreenAction.SignUpClick(email, password, confirmPassword)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.signup_screen_create_account))
        }
    }

    @Composable
    private fun BackToLoginButton(
        isLoading: Boolean,
        processUiAction: (action: SignUpScreenAction) -> Unit
    ) {
        TextButton(
            onClick = {
                processUiAction(SignUpScreenAction.NavigateToLoginClick)
            },
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.signup_screen_back_to_login))
        }
    }
}
