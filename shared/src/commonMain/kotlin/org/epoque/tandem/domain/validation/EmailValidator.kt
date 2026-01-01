package org.epoque.tandem.domain.validation

import org.epoque.tandem.domain.model.ValidationResult

/**
 * Validates email addresses.
 */
object EmailValidator {
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
    )

    /**
     * Validates an email address.
     *
     * @param email The email address to validate
     * @return [ValidationResult.Valid] if the email is valid,
     *         [ValidationResult.Error] with a message otherwise
     */
    fun validate(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !EMAIL_REGEX.matches(email) -> ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Valid
        }
    }
}
