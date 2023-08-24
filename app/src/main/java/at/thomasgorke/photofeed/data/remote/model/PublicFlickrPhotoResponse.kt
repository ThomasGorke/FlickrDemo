package at.thomasgorke.photofeed.data.remote.model

import com.google.gson.annotations.SerializedName

data class PublicFlickrPhotoResponse(
    val title: String,
    val items: List<PhotoItem>
)

data class PhotoItem(
    val title: String,
    val link: String,
    val media: PhotoMedia,
    val author: String,
    val description: String,
    @SerializedName("author_id") val authorId: String
)

data class PhotoMedia(
    val m: String
)
