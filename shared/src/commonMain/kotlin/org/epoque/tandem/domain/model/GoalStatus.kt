package org.epoque.tandem.domain.model

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED;

    companion object {
        fun fromString(value: String): GoalStatus {
            return entries.find { it.name == value } ?: ACTIVE
        }
    }
}
