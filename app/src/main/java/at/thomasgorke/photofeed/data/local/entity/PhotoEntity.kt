package at.thomasgorke.photofeed.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo("media_url")
    val mediaUrl: String,

    @ColumnInfo("author")
    val author: String
)
