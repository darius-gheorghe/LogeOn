package com.sapphyx.logeon.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sapphyx.logeon.R
import com.sapphyx.logeon.data.repository.util.user.UsernameValidationResult

@Composable
fun UsernameValidationResult.toErrorMessage(): String? = when (this) {
    UsernameValidationResult.EMPTY -> stringResource(R.string.error_username_empty)
    UsernameValidationResult.TOO_SHORT -> stringResource(R.string.error_username_too_short)
    UsernameValidationResult.TOO_LONG -> stringResource(R.string.error_username_too_long)
    UsernameValidationResult.INVALID_CHARACTERS -> stringResource(R.string.error_username_invalid_chars)
    UsernameValidationResult.ALREADY_TAKEN -> stringResource(R.string.error_username_taken)
    UsernameValidationResult.CORRECT -> null
}