package com.sapphyx.logeon.data.repository.util.user

class UserNameValidator {
    operator fun invoke(userName: String): UserNameValidationResult {
        return when {
            userName.isEmpty() -> UserNameValidationResult.IS_EMPTY
            userName.length < USER_NAME_MIN_LENGTH -> UserNameValidationResult.IS_TOO_SHORT
            else -> UserNameValidationResult.CORRECT
        }
    }

    private companion object {
        const val USER_NAME_MIN_LENGTH = 4
    }
}

enum class UserNameValidationResult {
    IS_EMPTY,
    IS_TOO_SHORT,
    CORRECT
}