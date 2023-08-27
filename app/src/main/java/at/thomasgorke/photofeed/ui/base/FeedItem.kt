package at.thomasgorke.photofeed.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import at.thomasgorke.photofeed.data.model.FeedItem
import coil.compose.AsyncImage

@Composable
fun FeedItem(
    feedItem: FeedItem,
    toggleFavorite: (FeedItem) -> Unit,
    open: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null)
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = feedItem.author,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Box {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { open(feedItem.imgUrl) },
                contentDescription = "Feed entry",
                contentScale = ContentScale.FillWidth,
                model = feedItem.imgUrl
            )

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                onClick = { toggleFavorite(feedItem) }
            ) {
                Icon(
                    imageVector = when (feedItem.isFavored) {
                        false -> Icons.Outlined.FavoriteBorder
                        true -> Icons.Filled.Favorite
                    },
                    contentDescription = "Favorite"
                )
            }
        }
    }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = feedItem.title,
        style = MaterialTheme.typography.bodyMedium
    )
}