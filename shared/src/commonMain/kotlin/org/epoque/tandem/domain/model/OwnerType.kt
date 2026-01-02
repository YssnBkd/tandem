package org.epoque.tandem.domain.model

/**
 * Categorizes who a task belongs to.
 */
enum class OwnerType {
    /** User's own task */
    SELF,

    /** Task assigned to/for partner */
    PARTNER,

    /** Joint responsibility */
    SHARED
}
