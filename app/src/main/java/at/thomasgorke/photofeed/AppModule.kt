package at.thomasgorke.photofeed

import org.koin.dsl.module

internal val appModule = module {
    single {
        AppInfo(
            isDebug = BuildConfig.DEBUG,
            baseUrl = BuildConfig.BASE_URL
        )
    }
}

data class AppInfo(
    val isDebug: Boolean,
    val baseUrl: String
)
