package org.epoque.tandem.domain.validation

import org.epoque.tandem.domain.model.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PasswordValidatorTest {

    @Test
    fun `valid password returns Valid`() {
        val result = PasswordValidator.validate("password123")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `password with exactly 8 characters returns Valid`() {
        val result = PasswordValidator.validate("12345678")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `long password returns Valid`() {
        val result = PasswordValidator.validate("thisIsAVeryLongPasswordThatShouldBeValid123!")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `blank password returns Error`() {
        val result = PasswordValidator.validate("")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Password is required", result.message)
    }

    @Test
    fun `whitespace only password returns Error`() {
        val result = PasswordValidator.validate("   ")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Password is required", result.message)
    }

    @Test
    fun `password with 7 characters returns Error`() {
        val result = PasswordValidator.validate("1234567")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Password must be at least 8 characters", result.message)
    }

    @Test
    fun `password with 1 character returns Error`() {
        val result = PasswordValidator.validate("a")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Password must be at least 8 characters", result.message)
    }

    @Test
    fun `MIN_LENGTH constant is 8`() {
        assertEquals(8, PasswordValidator.MIN_LENGTH)
    }
}
