package com.sapphyx.logeon.test.repository.authentication

import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.PasswordValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordValidatorTest {

    private val validator = PasswordValidator()

    @Test
    fun `invoke returns EMPTY when password is empty`() {
        assertEquals(PasswordValidationResult.EMPTY, validator(""))
    }

    @Test
    fun `invoke returns CONTAINS_WHITESPACE when password contains space`() {
        assertEquals(PasswordValidationResult.CONTAINS_WHITESPACE, validator("Valid 1!"))
    }

    @Test
    fun `invoke returns CONTAINS_WHITESPACE when password contains trailing space`() {
        assertEquals(PasswordValidationResult.CONTAINS_WHITESPACE, validator("Valid12! "))
    }

    @Test
    fun `invoke returns TOO_SHORT when password is fewer than 8 characters`() {
        assertEquals(PasswordValidationResult.TOO_SHORT, validator("Ab1!def"))
    }

    @Test
    fun `invoke returns NO_DIGIT when password has no numbers`() {
        assertEquals(PasswordValidationResult.NO_DIGIT, validator("ValidPass!"))
    }

    @Test
    fun `invoke returns NO_UPPERCASE when password has no uppercase letters`() {
        assertEquals(PasswordValidationResult.NO_UPPERCASE, validator("validpass1!"))
    }

    @Test
    fun `invoke returns NO_LOWERCASE when password has no lowercase letters`() {
        assertEquals(PasswordValidationResult.NO_LOWERCASE, validator("VALIDPASS1!"))
    }

    @Test
    fun `invoke returns NO_SPECIAL_CHAR when password has only letters and digits`() {
        assertEquals(PasswordValidationResult.NO_SPECIAL_CHAR, validator("ValidPass1"))
    }

    @Test
    fun `invoke returns CORRECT when password meets all 8-char complexity rules`() {
        assertEquals(PasswordValidationResult.CORRECT, validator("ValidPass1!"))
    }

    @Test
    fun `validateForLogin returns EMPTY when password is empty`() {
        assertEquals(PasswordValidationResult.EMPTY, validator.validateForLogin(""))
    }

    @Test
    fun `validateForLogin returns CORRECT for any non-empty password`() {
        assertEquals(PasswordValidationResult.CORRECT, validator.validateForLogin("legacy"))
        assertEquals(PasswordValidationResult.CORRECT, validator.validateForLogin("pass with space"))
    }
}