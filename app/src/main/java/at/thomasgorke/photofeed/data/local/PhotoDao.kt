package at.thomasgorke.photofeed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import at.thomasgorke.photofeed.data.local.entity.FavoriteEntity
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("SELECT * FROM photos")
    fun getPhotos(): Flow<List<PhotoEntity>>

    @Query("DELETE FROM photos")
    suspend fun deleteAll()

    @Query("SELECT media_url FROM photos")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM photos WHERE media_url in (:ids)")
    suspend fun deleteByIds(ids: List<String>)

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