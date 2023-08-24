package at.thomasgorke.photofeed.data.local

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val localModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FlickrDatabase::class.java,
            DB_NAME
        ).build()
    }

    single<FlickrLocalDataSource> {
        FlickrLocalDataSourceImpl(photoDao = get<FlickrDatabase>().photoDao())
    }
}

private const val DB_NAME = "flickr_db"