package at.thomasgorke.photofeed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import at.thomasgorke.photofeed.data.local.entity.FavoriteEntity
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

    @Insert
    suspend fun addFavorite(favorites: FavoriteEntity)

    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("DELETE FROM favorites WHERE media_url = :mediaUrl")
    suspend fun removeFavorite(mediaUrl: String)

    @Query("DELETE FROM favorites")
    suspend fun removeAllFavorites()

    @Query("SELECT media_url FROM favorites")
    suspend fun getAllFavorites(): List<String>
}