package org.epoque.tandem.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the application.
 */
sealed interface Routes {

    /**
     * Authentication routes.
     */
    sealed interface Auth : Routes {
        /** Welcome/landing screen with sign-in options */
        @Serializable
        data object Welcome : Auth

        /** Sign-in screen for returning users */
        @Serializable
        data object SignIn : Auth

        /** Registration screen for new users */
        @Serializable
        data object Register : Auth
    }

    /**
     * Main app routes (authenticated users only).
     */
    sealed interface Main : Routes {
        /** Main screen with bottom navigation */
        @Serializable
        data object Home : Main

        /** Week tab content */
        @Serializable
        data object Week : Main

        /** Progress tab content */
        @Serializable
        data object Progress : Main

        /** Goals tab content */
        @Serializable
        data object Goals : Main
    }

    /**
     * Weekly planning routes.
     */
    sealed interface Planning : Routes {
        /** Planning entry point (shown from banner) */
        @Serializable
        data object Start : Planning

        /** Planning wizard with step index (0-3) */
        @Serializable
        data class Wizard(val stepIndex: Int = 0) : Planning
    }

    /**
     * Weekly review routes.
     */
    sealed interface Review : Routes {
        /** Review entry point (shown from banner) */
        @Serializable
        data object Start : Review

        /** Mode selection screen */
        @Serializable
        data object ModeSelection : Review

        /** Rating step screen */
        @Serializable
        data object Rating : Review

        /** Task review step with index */
        @Serializable
        data class TaskReview(val taskIndex: Int = 0) : Review

        /** Summary screen */
        @Serializable
        data object Summary : Review
    }
}
