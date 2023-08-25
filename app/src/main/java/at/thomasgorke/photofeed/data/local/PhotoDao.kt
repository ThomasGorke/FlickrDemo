package at.thomasgorke.photofeed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("SELECT * FROM photos")
    fun getPhotos(): Flow<List<PhotoEntity>>

    @Query("DELETE FROM photos")
    suspend fun deleteAll()
}