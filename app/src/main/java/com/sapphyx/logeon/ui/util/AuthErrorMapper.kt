package com.sapphyx.logeon.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sapphyx.logeon.R
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult

@Composable
fun EmailValidationResult.toErrorMessage(): String? = when(this) {
    EmailValidationResult.EMPTY -> stringResource(R.string.error_email_empty)
    EmailValidationResult.INCORRECT_FORMAT -> stringResource(R.string.error_email_invalid_format)
    EmailValidationResult.CORRECT -> null
}

@Composable
fun PasswordValidationResult.toLoginErrorMessage(): String? = when(this) {
    PasswordValidationResult.EMPTY -> stringResource(R.string.error_password_empty)
    else -> null
}

@Composable
fun PasswordValidationResult.toSignupErrorMessage(): String? = when (this) {
    PasswordValidationResult.EMPTY -> stringResource(R.string.error_password_empty)
    PasswordValidationResult.CONTAINS_WHITESPACE -> stringResource(R.string.error_password_contains_whitespace)
    PasswordValidationResult.TOO_SHORT -> stringResource(R.string.error_password_too_short)
    PasswordValidationResult.NO_UPPERCASE -> stringResource(R.string.error_password_no_uppercase)
    PasswordValidationResult.NO_LOWERCASE -> stringResource(R.string.error_password_no_lowercase)
    PasswordValidationResult.NO_DIGIT -> stringResource(R.string.error_password_no_digit)
    PasswordValidationResult.NO_SPECIAL_CHAR -> stringResource(R.string.error_password_no_special_char)
    PasswordValidationResult.CORRECT -> null
}