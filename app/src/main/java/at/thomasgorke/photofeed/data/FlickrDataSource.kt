package at.thomasgorke.photofeed.data

import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.data.local.FlickrLocalDataSource
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import at.thomasgorke.photofeed.data.model.FeedItem
import at.thomasgorke.photofeed.data.model.RepositoryException
import at.thomasgorke.photofeed.data.remote.FlickrRemoteDataSource
import at.thomasgorke.photofeed.data.remote.NetworkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

interface FlickrDataSource {
    fun getFeedFlow(): Flow<RepositoryResponse<List<FeedItem>>>
    suspend fun fetchNewRemoteFeed(): RepositoryResponse<Unit>
}

class FlickrDataSourceImpl(
    private val localDataSource: FlickrLocalDataSource,
    private val remoteDataSource: FlickrRemoteDataSource
) : FlickrDataSource {

    override fun getFeedFlow(): Flow<RepositoryResponse<List<FeedItem>>> = localDataSource
        .getFeedFlow()
        .catch {
            println("Errrrror")
            RepositoryResponse.Error(RepositoryException.LocalException(it))
        }
        .map { emission ->
            if (emission.isEmpty()) fetchNewRemoteFeed()
            emission.map { FeedItem(it.mediaUrl) }
        }
        .map { RepositoryResponse.Success(it) }

    override suspend fun fetchNewRemoteFeed(): RepositoryResponse<Unit> =
        remoteDataSource.fetchPhotoFeed().let { response ->
            println("fetched data")
            when (response) {
                is NetworkResponse.Error -> RepositoryResponse.Error(
                    RepositoryException.RemoteException(
                        response.throwable
                    )
                )
                is NetworkResponse.Success -> {
                    localDataSource.deleteExistingAndInsertNew(
                        response.data.items.map { PhotoEntity(0, it.media.m, it.author) }
                    )
                    RepositoryResponse.Success(Unit)
                }
            }
        }.also { println("Loaded data from api") }
}