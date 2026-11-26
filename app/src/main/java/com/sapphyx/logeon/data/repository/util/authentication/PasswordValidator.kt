package com.sapphyx.logeon.data.repository.util.authentication

class PasswordValidator {
    operator fun invoke(password: String): PasswordValidationResult {
        return when {
            password.isEmpty() -> PasswordValidationResult.EMPTY
            password.any(Char::isWhitespace) -> PasswordValidationResult.CONTAINS_WHITESPACE
            password.length < MIN_PASSWORD_LENGTH -> PasswordValidationResult.TOO_SHORT
            password.count(Char::isDigit) < MIN_CASE_COUNT -> PasswordValidationResult.NO_DIGIT
            password.count(Char::isUpperCase) < MIN_CASE_COUNT -> PasswordValidationResult.NO_UPPERCASE
            password.count(Char::isLowerCase) < MIN_CASE_COUNT -> PasswordValidationResult.NO_LOWERCASE
            password.count(Char::isLetterOrDigit) == password.length -> PasswordValidationResult.NO_SPECIAL_CHAR

            else -> PasswordValidationResult.CORRECT
        }
    }

    // Minimal validation for Login (avoids leaking password rules or locking out legacy passwords)
    fun validateForLogin(password: String): PasswordValidationResult {
        return if (password.isEmpty()) {
            PasswordValidationResult.EMPTY
        } else {
            PasswordValidationResult.CORRECT
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MIN_CASE_COUNT = 1
    }
}

enum class PasswordValidationResult {
    EMPTY,
    CONTAINS_WHITESPACE,
    TOO_SHORT,
    NO_DIGIT,
    NO_UPPERCASE,
    NO_LOWERCASE,
    NO_SPECIAL_CHAR,
    CORRECT
}