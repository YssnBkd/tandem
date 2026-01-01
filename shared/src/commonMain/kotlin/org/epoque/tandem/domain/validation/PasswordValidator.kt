package org.epoque.tandem.domain.validation

import org.epoque.tandem.domain.model.ValidationResult

/**
 * Validates passwords.
 */
object PasswordValidator {
    const val MIN_LENGTH = 8

    /**
     * Validates a password.
     *
     * @param password The password to validate
     * @return [ValidationResult.Valid] if the password is valid,
     *         [ValidationResult.Error] with a message otherwise
     */
    fun validate(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < MIN_LENGTH -> ValidationResult.Error(
                "Password must be at least $MIN_LENGTH characters"
            )
            else -> ValidationResult.Valid
        }
    }
}
