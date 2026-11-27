package com.sapphyx.logeon.test.repository.authentication

import com.sapphyx.logeon.data.repository.util.authentication.EmailValidationResult
import com.sapphyx.logeon.data.repository.util.authentication.EmailValidator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmailValidatorTest {

    private val validator = EmailValidator()

    @Test
    fun `invoke returns EMPTY when email is empty`() {
        assertEquals(EmailValidationResult.EMPTY, validator(""))
    }

    @Test
    fun `invoke returns INCORRECT_FORMAT when email contains only whitespace`() {
        assertEquals(EmailValidationResult.INCORRECT_FORMAT, validator("   "))
    }

    @Test
    fun `invoke returns INCORRECT_FORMAT when email lacks at symbol`() {
        assertEquals(EmailValidationResult.INCORRECT_FORMAT, validator("userexample.com"))
    }

    @Test
    fun `invoke returns INCORRECT_FORMAT when email lacks domain`() {
        assertEquals(EmailValidationResult.INCORRECT_FORMAT, validator("user@"))
    }

    @Test
    fun `invoke returns CORRECT for valid standard email`() {
        assertEquals(EmailValidationResult.CORRECT, validator("user@example.com"))
    }
}