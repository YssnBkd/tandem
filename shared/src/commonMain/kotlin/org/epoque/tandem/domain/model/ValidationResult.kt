package org.epoque.tandem.domain.model

/**
 * Represents the result of a validation operation.
 */
sealed interface ValidationResult {
    /** Validation passed */
    data object Valid : ValidationResult

    /** Validation failed with an error message */
    data class Error(val message: String) : ValidationResult
}

/** Returns true if this result is [ValidationResult.Valid] */
val ValidationResult.isValid: Boolean
    get() = this is ValidationResult.Valid

/** Returns the error message if this is [ValidationResult.Error], null otherwise */
val ValidationResult.errorMessage: String?
    get() = (this as? ValidationResult.Error)?.message
