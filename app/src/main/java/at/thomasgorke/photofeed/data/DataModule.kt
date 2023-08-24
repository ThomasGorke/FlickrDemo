package at.thomasgorke.photofeed.data

import at.thomasgorke.photofeed.data.local.localModule
import at.thomasgorke.photofeed.data.remote.remoteModule
import com.google.gson.Gson
import org.koin.dsl.module

internal val dataModule = module {
    single { Gson() }

    single<FlickrDataSource> {
        FlickrDataSourceImpl(
            localDataSource = get(),
            remoteDataSource = get()
        )
    }
}

internal val dataModules = dataModule + localModule + remoteModule