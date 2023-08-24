package at.thomasgorke.photofeed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insertPhotos(vararg photos: PhotoEntity)

    @Query("SELECT * FROM photos")
    fun getPhotos(): Flow<PhotoEntity>

    @Query("DELETE FROM photos")
    suspend fun deleteAll()
}