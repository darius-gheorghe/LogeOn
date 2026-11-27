package com.sapphyx.logeon.data.repository.util.user

class UsernameValidator {
    operator fun invoke(username: String): UsernameValidationResult {
        return when {
            username.isEmpty() -> UsernameValidationResult.EMPTY
            username.length < MIN_LENGTH -> UsernameValidationResult.TOO_SHORT
            username.length > MAX_LENGTH -> UsernameValidationResult.TOO_LONG
            !USERNAME_REGEX.matches(username) -> UsernameValidationResult.INVALID_CHARACTERS
            else -> UsernameValidationResult.CORRECT
        }
    }

    private companion object {
        const val MIN_LENGTH = 4
        const val MAX_LENGTH = 20
        val USERNAME_REGEX = Regex("^[a-z0-9._-]+$")
    }
}

enum class UsernameValidationResult {
    EMPTY,
    TOO_SHORT,
    TOO_LONG,
    INVALID_CHARACTERS,
    ALREADY_TAKEN,
    CORRECT
}