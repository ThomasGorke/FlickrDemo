package at.thomasgorke.photofeed.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity

@Database(
    entities = [
        PhotoEntity::class
    ],
    version = 1
)
abstract class FlickrDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}