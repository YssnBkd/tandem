package org.epoque.tandem

import android.app.Application
import org.epoque.tandem.di.appModule
import org.epoque.tandem.di.authModule
import org.epoque.tandem.di.goalsModule
import org.epoque.tandem.di.partnerModule
import org.epoque.tandem.di.planningModule
import org.epoque.tandem.di.progressModule
import org.epoque.tandem.di.reviewModule
import org.epoque.tandem.di.seedModule
import org.epoque.tandem.di.taskModule
import org.epoque.tandem.di.timelineModule
import org.epoque.tandem.di.weekModule
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
                listOfNotNull(
                    appModule,
                    authModule,
                    taskModule,
                    weekModule,
                    planningModule,
                    reviewModule,
                    partnerModule,
                    goalsModule,
                    progressModule,
                    timelineModule,
                    // DEBUG only: Mock data seeder module
                    if (BuildConfig.DEBUG) seedModule else null
                )
            )
        }
    }
}
