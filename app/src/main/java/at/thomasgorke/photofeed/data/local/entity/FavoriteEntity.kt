package at.thomasgorke.photofeed.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo("media_url")
    val mediaUrl: String,

    @ColumnInfo("author")
    val author: String,

    @ColumnInfo("title")
    val title: String
)
