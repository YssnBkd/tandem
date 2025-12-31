package org.epoque.tandem.domain.validation

import org.epoque.tandem.domain.model.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EmailValidatorTest {

    @Test
    fun `valid email returns Valid`() {
        val result = EmailValidator.validate("user@example.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `valid email with subdomain returns Valid`() {
        val result = EmailValidator.validate("user@mail.example.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `valid email with plus sign returns Valid`() {
        val result = EmailValidator.validate("user+tag@example.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `valid email with dots returns Valid`() {
        val result = EmailValidator.validate("first.last@example.com")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `blank email returns Error`() {
        val result = EmailValidator.validate("")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Email is required", result.message)
    }

    @Test
    fun `whitespace only email returns Error`() {
        val result = EmailValidator.validate("   ")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Email is required", result.message)
    }

    @Test
    fun `email without at symbol returns Error`() {
        val result = EmailValidator.validate("userexample.com")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Invalid email format", result.message)
    }

    @Test
    fun `email without domain returns Error`() {
        val result = EmailValidator.validate("user@")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Invalid email format", result.message)
    }

    @Test
    fun `email without local part returns Error`() {
        val result = EmailValidator.validate("@example.com")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Invalid email format", result.message)
    }

    @Test
    fun `email without TLD returns Error`() {
        val result = EmailValidator.validate("user@example")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Invalid email format", result.message)
    }
}
