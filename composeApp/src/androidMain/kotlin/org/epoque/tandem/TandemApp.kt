package org.epoque.tandem

import android.app.Application
import org.epoque.tandem.di.appModule
import org.epoque.tandem.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for Tandem.
 *
 * Initializes Koin dependency injection on application startup.
 */
class TandemApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@TandemApp)
            modules(
                appModule,
                authModule
            )
        }
    }
}
