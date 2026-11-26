package com.sapphyx.logeon.data.repository.util.authentication

import android.util.Patterns

class EmailValidator {
    operator fun invoke(email: String): EmailValidationResult {
        return when {
            email.isEmpty() -> EmailValidationResult.EMPTY
            Patterns.EMAIL_ADDRESS.matcher(email).matches() -> EmailValidationResult.CORRECT
            else -> EmailValidationResult.INCORRECT_FORMAT
        }
    }
}

enum class EmailValidationResult {
    EMPTY,
    INCORRECT_FORMAT,
    CORRECT
}