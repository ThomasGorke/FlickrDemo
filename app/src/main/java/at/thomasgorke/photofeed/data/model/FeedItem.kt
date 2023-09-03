package at.thomasgorke.photofeed.data.model

import java.util.Date

data class FeedItem(
    val imgUrl: String,
    val title: String,
    val author: String,
    val isFavored: Boolean,
    val dateTake: Date
)
