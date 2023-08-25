package at.thomasgorke.photofeed.data.remote

import at.thomasgorke.photofeed.data.remote.model.PublicFlickrPhotoResponse

interface FlickrRemoteDataSource {
    suspend fun fetchPhotoFeed(): NetworkResponse<PublicFlickrPhotoResponse>
}

class FlickrRemoteDataSourceImpl(
    private val flickrApi: FlickrApi
): FlickrRemoteDataSource {

    override suspend fun fetchPhotoFeed(): NetworkResponse<PublicFlickrPhotoResponse> =
        safeApiCall { flickrApi.getPublicPhotos() }
}