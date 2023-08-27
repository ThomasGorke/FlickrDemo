package at.thomasgorke.photofeed.data.local

import at.thomasgorke.photofeed.data.local.entity.FavoriteEntity
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface FlickrLocalDataSource {
    fun getFeedFlow(): Flow<List<PhotoEntity>>
    suspend fun deleteExistingAndInsertNew(newFeed: List<PhotoEntity>)

    fun getFavoritesFlow(): Flow<List<FavoriteEntity>>
    suspend fun deleteAllFavorites()
    suspend fun addFavorite(favoriteEntity: FavoriteEntity)
    suspend fun removeFavorite(url: String)
    suspend fun toggleFavorite(favoriteEntity: FavoriteEntity)
}

class FlickrLocalDataSourceImpl(
    val photoDao: PhotoDao
) : FlickrLocalDataSource {

    override fun getFeedFlow(): Flow<List<PhotoEntity>> = photoDao.getPhotos()

    override suspend fun deleteExistingAndInsertNew(newFeed: List<PhotoEntity>) {
        photoDao.deleteAll()
        photoDao.insertPhotos(newFeed)
    }

    override fun getFavoritesFlow(): Flow<List<FavoriteEntity>> = photoDao.getFavorites()

    override suspend fun deleteAllFavorites() {
        photoDao.deleteAll()
    }

    override suspend fun addFavorite(favoriteEntity: FavoriteEntity) {
        photoDao.addFavorite(favoriteEntity)
    }

    override suspend fun removeFavorite(url: String) {
        photoDao.removeFavorite(url)
    }

    override suspend fun toggleFavorite(favoriteEntity: FavoriteEntity) {
        val favorites = photoDao.getAllFavorites()
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