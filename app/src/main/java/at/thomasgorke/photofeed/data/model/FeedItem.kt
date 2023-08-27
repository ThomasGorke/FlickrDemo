package at.thomasgorke.photofeed.data.model

data class FeedItem(
    val imgUrl: String,
    val title: String,
    val author: String,
    val isFavored: Boolean
)
