package org.epoque.tandem.domain.validation

import org.epoque.tandem.domain.model.ValidationResult

/**
 * Validates display names.
 */
object DisplayNameValidator {
    const val MIN_LENGTH = 1
    const val MAX_LENGTH = 100

    /**
     * Validates a display name.
     *
     * @param name The display name to validate
     * @return [ValidationResult.Valid] if the display name is valid,
     *         [ValidationResult.Error] with a message otherwise
     */
    fun validate(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Display name is required")
            name.length > MAX_LENGTH -> ValidationResult.Error(
                "Display name must be $MAX_LENGTH characters or less"
            )
            else -> ValidationResult.Valid
        }
    }
}
