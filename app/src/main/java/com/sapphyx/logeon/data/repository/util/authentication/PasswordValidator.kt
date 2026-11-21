package com.sapphyx.logeon.data.repository.util.authentication

class PasswordValidator {
    operator fun invoke(password: String): PasswordValidationResult {
        return when {
            password.any(Char::isWhitespace) -> PasswordValidationResult.CONTAINS_WHITESPACE
            password.length < MIN_PASSWORD_LENGTH -> PasswordValidationResult.NOT_LONG_ENOUGH
            !password.contains(DIGITS_STRING.toRegex()) -> PasswordValidationResult.NOT_ENOUGH_DIGITS
            password.count(Char::isUpperCase) < MIN_UPPER_CASE_COUNT -> PasswordValidationResult.NOT_ENOUGH_UPPER_CASE
            password.count(Char::isLetterOrDigit) == password.length -> PasswordValidationResult.NOT_ENOUGH_SPECIAL_CHARACTERS

            else -> PasswordValidationResult.CORRECT
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val DIGITS_STRING = "[0-9]"
        const val MIN_UPPER_CASE_COUNT = 1
    }
}

enum class PasswordValidationResult {
    CONTAINS_WHITESPACE,
    NOT_LONG_ENOUGH,
    NOT_ENOUGH_DIGITS,
    NOT_ENOUGH_UPPER_CASE,
    NOT_ENOUGH_SPECIAL_CHARACTERS,
    CORRECT
}