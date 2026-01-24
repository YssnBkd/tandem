package org.epoque.tandem.domain.usecase.feed

import org.epoque.tandem.domain.model.FeedItem
import org.epoque.tandem.domain.repository.FeedRepository
import org.epoque.tandem.domain.repository.PartnerRepository

/**
 * Use case for sending a message to partner.
 * Creates a message feed item for both sender and recipient.
 */
class SendMessageUseCase(
    private val feedRepository: FeedRepository,
    private val partnerRepository: PartnerRepository
) {
    /**
     * Send a message to the current partner.
     *
     * @param senderId The sender's user ID
     * @param text The message text
     * @return The created Message feed item, or null if no partner connected
     * @throws IllegalArgumentException if message text is blank
     */
    suspend operator fun invoke(
        senderId: String,
        text: String
    ): FeedItem.Message? {
        val trimmedText = text.trim()
        require(trimmedText.isNotBlank()) { "Message text cannot be blank" }

        // Get partner using PartnerRepository
        val partner = partnerRepository.getPartner(senderId) ?: return null

        return feedRepository.sendMessage(senderId, partner.id, trimmedText)
    }
}
