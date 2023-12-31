package at.thomasgorke.photofeed.data.local

import at.thomasgorke.photofeed.data.local.entity.FavoriteEntity
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface FlickrLocalDataSource {
    fun getFeedFlow(): Flow<List<PhotoEntity>>
    suspend fun deleteExistingAndInsertNew(newFeed: List<PhotoEntity>)
    suspend fun deleteFeed()

    fun getFavoritesFlow(): Flow<List<FavoriteEntity>>
    suspend fun getFavorites(): List<String>
    suspend fun deleteAllFavorites()
    suspend fun toggleFavorite(favoriteEntity: FavoriteEntity)
}

class FlickrLocalDataSourceImpl(
    val photoDao: PhotoDao
) : FlickrLocalDataSource {

    override fun getFeedFlow(): Flow<List<PhotoEntity>> = photoDao.getPhotos()

    override suspend fun deleteExistingAndInsertNew(newFeed: List<PhotoEntity>) {
        val oldPhotos = photoDao.getAllIds()
        photoDao.insertPhotos(newFeed)
        photoDao.deleteByIds(oldPhotos)
    }

    override suspend fun deleteFeed() {
        photoDao.deleteAll()
    }

    override fun getFavoritesFlow(): Flow<List<FavoriteEntity>> = photoDao.getFavorites()

    override suspend fun deleteAllFavorites() {
        photoDao.removeAllFavorites()
    }

    override suspend fun getFavorites(): List<String> = photoDao.getAllFavorites()

    override suspend fun toggleFavorite(favoriteEntity: FavoriteEntity) {
        val favorites = getFavorites()
        when (favorites.contains(favoriteEntity.mediaUrl)) {
            true -> { // remove from favorites
                photoDao.removeFavorite(favoriteEntity.mediaUrl)
            }
            false -> { // add to favorites
                photoDao.addFavorite(favoriteEntity)
            }
        }
    }
}