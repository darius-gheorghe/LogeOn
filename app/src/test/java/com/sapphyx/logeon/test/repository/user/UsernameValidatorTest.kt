package com.sapphyx.logeon.test.repository.user

import com.sapphyx.logeon.data.repository.util.user.UsernameValidationResult
import com.sapphyx.logeon.data.repository.util.user.UsernameValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class UsernameValidatorTest {

    private val validator = UsernameValidator()

    @Test
    fun `invoke returns EMPTY when username is empty`() {
        assertEquals(UsernameValidationResult.EMPTY, validator(""))
    }

    @Test
    fun `invoke returns TOO_SHORT when username has fewer than 4 characters`() {
        assertEquals(UsernameValidationResult.TOO_SHORT, validator("abc"))
    }

    @Test
    fun `invoke returns TOO_LONG when username has more than 20 characters`() {
        assertEquals(UsernameValidationResult.TOO_LONG, validator("a".repeat(21)))
    }

    @Test
    fun `invoke returns INVALID_CHARACTERS when username contains uppercase letters`() {
        assertEquals(UsernameValidationResult.INVALID_CHARACTERS, validator("User_1"))
    }

    @Test
    fun `invoke returns INVALID_CHARACTERS when username contains spaces or special symbols`() {
        assertEquals(UsernameValidationResult.INVALID_CHARACTERS, validator("user name"))
        assertEquals(UsernameValidationResult.INVALID_CHARACTERS, validator("user@name"))
    }

    @Test
    fun `invoke returns CORRECT for valid lowercase alphanumeric username with allowed symbols`() {
        assertEquals(UsernameValidationResult.CORRECT, validator("abcd"))
        assertEquals(UsernameValidationResult.CORRECT, validator("user_name.01-test"))
        assertEquals(UsernameValidationResult.CORRECT, validator("a".repeat(20)))
    }
}