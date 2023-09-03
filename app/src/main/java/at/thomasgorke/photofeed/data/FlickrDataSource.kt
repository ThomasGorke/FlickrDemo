package at.thomasgorke.photofeed.data

import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.data.local.FlickrLocalDataSource
import at.thomasgorke.photofeed.data.local.entity.FavoriteEntity
import at.thomasgorke.photofeed.data.local.entity.PhotoEntity
import at.thomasgorke.photofeed.data.model.FeedItem
import at.thomasgorke.photofeed.data.model.RepositoryException
import at.thomasgorke.photofeed.data.remote.FlickrRemoteDataSource
import at.thomasgorke.photofeed.data.remote.NetworkResponse
import at.thomasgorke.photofeed.data.remote.model.TagMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface FlickrDataSource {
    fun getFeedFlow(): Flow<RepositoryResponse<List<FeedItem>>>
    fun getFavoritesFlow(): Flow<RepositoryResponse<List<FeedItem>>>

    suspend fun fetchNewRemoteFeed(): RepositoryResponse<Unit>
    suspend fun fetchFeedByTags(tags: String, tagMode: TagMode = TagMode.ALL): RepositoryResponse<List<FeedItem>>
    suspend fun toggleFavorite(feedItem: FeedItem)
}

class FlickrDataSourceImpl(
    private val localDataSource: FlickrLocalDataSource,
    private val remoteDataSource: FlickrRemoteDataSource
) : FlickrDataSource {

    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN)
    private fun String.toDate(): Date = dateTimeFormatter.parse(this)
    private fun Date.convertToString(): String = dateTimeFormatter.format(this)

    override fun getFeedFlow(): Flow<RepositoryResponse<List<FeedItem>>> =
        combine(
            localDataSource.getFeedFlow(),
            localDataSource.getFavoritesFlow()
        ) { response, favorites ->
            if (response.isEmpty()) fetchNewRemoteFeed()

            response.map { responseItem ->
                FeedItem(
                    imgUrl = responseItem.mediaUrl,
                    title = responseItem.title,
                    author = responseItem.author,
                    isFavored = favorites.any { it.mediaUrl == responseItem.mediaUrl },
                    dateTake = responseItem.dateTaken.toDate()
                )
            }
        }.map { RepositoryResponse.Success(it) }

    override fun getFavoritesFlow(): Flow<RepositoryResponse<List<FeedItem>>> =
        localDataSource.getFavoritesFlow()
            .map { favorites ->
                favorites.map {
                    FeedItem(
                        imgUrl = it.mediaUrl,
                        title = it.title,
                        author = it.author,
                        isFavored = true,
                        dateTake = it.dateTaken.toDate()
                    )
                }
            }
            .map { RepositoryResponse.Success(it) }

    override suspend fun fetchNewRemoteFeed(): RepositoryResponse<Unit> =
        remoteDataSource.fetchPhotoFeed().let { response ->
            when (response) {
                is NetworkResponse.Error -> RepositoryResponse.Error(
                    RepositoryException.RemoteException(
                        response.throwable
                    )
                )

                is NetworkResponse.Success -> {
                    localDataSource.deleteExistingAndInsertNew(
                        response.data.items.map {
                            PhotoEntity(
                                it.media.m,
                                it.author,
                                it.title,
                                it.dateTaken
                            )
                        }
                    )
                    RepositoryResponse.Success(Unit)
                }
            }
        }

    override suspend fun fetchFeedByTags(tags: String, tagMode: TagMode): RepositoryResponse<List<FeedItem>> =
        remoteDataSource.fetchPhotoFeedByTags(tags, tagMode).let { response ->
            when (response) {
                is NetworkResponse.Error -> RepositoryResponse.Error(
                    RepositoryException.RemoteException(
                        response.throwable
                    )
                )

                is NetworkResponse.Success -> {
                    RepositoryResponse.Success(
                        response.data.items.map {
                            FeedItem(
                                imgUrl = it.media.m,
                                title = it.title,
                                author = it.author,
                                isFavored = false,
                                dateTake = it.dateTaken.toDate()
                            )
                        }
                    )
                }
            }
        }

    override suspend fun toggleFavorite(feedItem: FeedItem) {
        localDataSource.toggleFavorite(
            FavoriteEntity(
                mediaUrl = feedItem.imgUrl,
                author = feedItem.author,
                title = feedItem.title,
                dateTaken = feedItem.dateTake.convertToString()
            )
        )
    }
}