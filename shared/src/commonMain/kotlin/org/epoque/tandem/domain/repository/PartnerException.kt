package org.epoque.tandem.domain.repository

/**
 * Exceptions thrown by PartnerRepository operations.
 */
sealed class PartnerException(message: String) : Exception(message) {
    /** No active partnership found for user */
    data object NoPartnership : PartnerException("No active partnership found")

    /** User already has an active partnership */
    data object AlreadyHasPartner : PartnerException("Already has an active partnership")
}
