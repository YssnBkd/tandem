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
}
