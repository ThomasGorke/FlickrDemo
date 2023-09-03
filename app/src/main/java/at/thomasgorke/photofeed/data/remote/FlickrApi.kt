package at.thomasgorke.photofeed.data.remote

import at.thomasgorke.photofeed.data.remote.model.PublicFlickrPhotoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {

    @GET("feeds/photos_public.gne")
    suspend fun getPublicPhotos(
        @Query("format") format: String = "json",
        @Query("nojsoncallback") nojsoncallback: Boolean = true,
        @Query("tags") tags: String? = null,
        @Query("tagmode") tagMode: String? = null
    ): PublicFlickrPhotoResponse
}