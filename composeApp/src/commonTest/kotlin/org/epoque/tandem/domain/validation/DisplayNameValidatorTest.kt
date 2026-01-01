package org.epoque.tandem.domain.validation

import org.epoque.tandem.domain.model.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DisplayNameValidatorTest {

    @Test
    fun `valid display name returns Valid`() {
        val result = DisplayNameValidator.validate("John Doe")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `single character display name returns Valid`() {
        val result = DisplayNameValidator.validate("J")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `display name with 100 characters returns Valid`() {
        val name = "a".repeat(100)
        val result = DisplayNameValidator.validate(name)
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `display name with special characters returns Valid`() {
        val result = DisplayNameValidator.validate("John-Doe O'Brien")
        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `blank display name returns Error`() {
        val result = DisplayNameValidator.validate("")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Display name is required", result.message)
    }

    @Test
    fun `whitespace only display name returns Error`() {
        val result = DisplayNameValidator.validate("   ")
        assertIs<ValidationResult.Error>(result)
        assertEquals("Display name is required", result.message)
    }

    @Test
    fun `display name with 101 characters returns Error`() {
        val name = "a".repeat(101)
        val result = DisplayNameValidator.validate(name)
        assertIs<ValidationResult.Error>(result)
        assertEquals("Display name must be 100 characters or less", result.message)
    }

    @Test
    fun `display name with 200 characters returns Error`() {
        val name = "a".repeat(200)
        val result = DisplayNameValidator.validate(name)
        assertIs<ValidationResult.Error>(result)
        assertEquals("Display name must be 100 characters or less", result.message)
    }

    @Test
    fun `MIN_LENGTH constant is 1`() {
        assertEquals(1, DisplayNameValidator.MIN_LENGTH)
    }

    @Test
    fun `MAX_LENGTH constant is 100`() {
        assertEquals(100, DisplayNameValidator.MAX_LENGTH)
    }
}
