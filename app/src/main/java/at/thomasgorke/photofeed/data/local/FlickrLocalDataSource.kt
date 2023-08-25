package at.thomasgorke.photofeed.data.local

import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface FlickrLocalDataSource {
    fun getFeedFlow(): Flow<List<PhotoEntity>>
    suspend fun deleteExistingAndInsertNew(newFeed: List<PhotoEntity>)
}

class FlickrLocalDataSourceImpl(
    val photoDao: PhotoDao
) : FlickrLocalDataSource {

    override fun getFeedFlow(): Flow<List<PhotoEntity>> = photoDao.getPhotos()

    override suspend fun deleteExistingAndInsertNew(newFeed: List<PhotoEntity>) {
        photoDao.deleteAll()
        println("Insert new feed")
        photoDao.insertPhotos(newFeed)
    }
}