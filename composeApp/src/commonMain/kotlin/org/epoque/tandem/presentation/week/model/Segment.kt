package org.epoque.tandem.presentation.week.model

import org.epoque.tandem.domain.model.OwnerType

/**
 * Represents the segment/tab selection in the Week View.
 *
 * This is a presentation-layer enum that maps to domain OwnerType
 * with user-friendly display names.
 */
enum class Segment(val displayName: String) {
    YOU("You"),
    PARTNER("Partner"),
    SHARED("Shared");

    /**
     * Convert to domain OwnerType for repository queries.
     */
    fun toOwnerType(): OwnerType = when (this) {
        YOU -> OwnerType.SELF
        PARTNER -> OwnerType.PARTNER
        SHARED -> OwnerType.SHARED
    }

    companion object {
        /**
         * Create Segment from domain OwnerType.
         */
        fun fromOwnerType(ownerType: OwnerType): Segment = when (ownerType) {
            OwnerType.SELF -> YOU
            OwnerType.PARTNER -> PARTNER
            OwnerType.SHARED -> SHARED
        }
    }
}
