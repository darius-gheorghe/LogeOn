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
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.ui.util.toErrorMessage
import com.sapphyx.logeon.ui.util.toLoginErrorMessage
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenAction
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenEffect
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenState
import com.sapphyx.logeon.viewModelImpl.login.LoginScreenViewModel
import kotlinx.coroutines.flow.SharedFlow

object LoginScreen :
    BaseScreen<LoginScreenState, LoginScreenAction, LoginScreenEffect, LoginScreenViewModel>() {

    @Composable
    override fun Screen(
        state: LoginScreenState,
        effect: SharedFlow<LoginScreenEffect>,
        processUiAction: (action: LoginScreenAction) -> Unit
    ) {
        val context = LocalContext.current
        var passwordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { processUiAction(LoginScreenAction.EmailChanged(it)) },
                label = { Text("Email") },
                isError = state.emailError != null,
                supportingText = state.emailError?.toErrorMessage()?.let { errorMessage ->
                    { Text(text = errorMessage) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { processUiAction(LoginScreenAction.PasswordChanged(it)) },
                label = { Text("Password") },
                isError = state.passwordError != null,
                supportingText = state.passwordError?.toLoginErrorMessage()?.let { errorMessage ->
                    { Text(text = errorMessage) }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        enabled = !state.isLoading,
                        onClick = { passwordVisible = !passwordVisible },
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            state.generalError?.let { errorMessage ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        processUiAction(
                            LoginScreenAction.LoginClick(state.email, state.password)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        processUiAction(LoginScreenAction.GoogleSignInClick(context))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with Google")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    processUiAction(LoginScreenAction.NavigateToSignUpClick)
                },
                enabled = !state.isLoading
            ) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}