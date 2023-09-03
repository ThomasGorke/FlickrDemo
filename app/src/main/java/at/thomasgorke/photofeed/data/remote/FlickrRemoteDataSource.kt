package at.thomasgorke.photofeed.data.remote

import at.thomasgorke.photofeed.data.remote.model.PublicFlickrPhotoResponse
import at.thomasgorke.photofeed.data.remote.model.TagMode

interface FlickrRemoteDataSource {
    suspend fun fetchPhotoFeed(): NetworkResponse<PublicFlickrPhotoResponse>
    suspend fun fetchPhotoFeedByTags(tags: String, tagMode: TagMode = TagMode.ALL): NetworkResponse<PublicFlickrPhotoResponse>
}

class FlickrRemoteDataSourceImpl(
    private val flickrApi: FlickrApi
) : FlickrRemoteDataSource {

    override suspend fun fetchPhotoFeed(): NetworkResponse<PublicFlickrPhotoResponse> =
        safeApiCall { flickrApi.getPublicPhotos() }

    override suspend fun fetchPhotoFeedByTags(tags: String, tagMode: TagMode): NetworkResponse<PublicFlickrPhotoResponse> =
        safeApiCall { flickrApi.getPublicPhotos(tags = tags, tagMode = tagMode.value) }
}