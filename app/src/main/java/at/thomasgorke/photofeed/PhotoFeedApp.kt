package at.thomasgorke.photofeed

import android.app.Application
import at.thomasgorke.photofeed.data.dataModules
import at.thomasgorke.photofeed.ui.uiModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PhotoFeedApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(level = Level.INFO)
            androidContext(applicationContext)
            modules(appModule + uiModules + dataModules)
        }
    }
}