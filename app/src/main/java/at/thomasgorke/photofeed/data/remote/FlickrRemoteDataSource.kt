package at.thomasgorke.photofeed.data.remote

import at.thomasgorke.photofeed.data.remote.model.PublicFlickrPhotoResponse

interface FlickrRemoteDataSource {
    suspend fun fetchPhotoFeed(): NetworkResponse<PublicFlickrPhotoResponse>
    suspend fun fetchPhotoFeedByTags(tags: String): NetworkResponse<PublicFlickrPhotoResponse>
}

class FlickrRemoteDataSourceImpl(
    private val flickrApi: FlickrApi
) : FlickrRemoteDataSource {

    override suspend fun fetchPhotoFeed(): NetworkResponse<PublicFlickrPhotoResponse> =
        safeApiCall { flickrApi.getPublicPhotos() }

    override suspend fun fetchPhotoFeedByTags(tags: String): NetworkResponse<PublicFlickrPhotoResponse> =
        safeApiCall { flickrApi.getPublicPhotos(tags = tags) }
}