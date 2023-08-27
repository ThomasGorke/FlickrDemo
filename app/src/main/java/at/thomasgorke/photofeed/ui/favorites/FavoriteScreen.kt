@file:OptIn(ExperimentalMaterial3Api::class)

package at.thomasgorke.photofeed.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.thomasgorke.photofeed.R
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.ui.base.ContentEmptyScreen
import at.thomasgorke.photofeed.ui.base.ContentErrorScreen
import at.thomasgorke.photofeed.ui.base.ContentLoadingScreen
import at.thomasgorke.photofeed.ui.base.FeedItem
import at.thomasgorke.photofeed.ui.destinations.ImageFullScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel

@Destination
@Composable
fun FavoriteScreen(
    navigator: DestinationsNavigator,
    viewModel: FavoriteScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.title_favorite)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when (state.dataState) {
            DataState.SUCCESS -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                ) {
                    when (state.feed.isEmpty()) {
                        true -> ContentEmptyScreen(msgId = R.string.info_no_favorites)
                        else -> LazyColumn(modifier = Modifier.padding(padding)) {
                            items(state.feed) { item ->
                                FeedItem(
                                    feedItem = item,
                                    toggleFavorite = {
                                        viewModel.execute(
                                            FavoriteScreenViewModel.Action.ToggleFavorite(
                                                it
                                            )
                                        )
                                    },
                                    open = { navigator.navigate(ImageFullScreenDestination(imgUrl = it)) }
                                )
                            }
                        }
                    }
                }
            }

            DataState.LOADING -> ContentLoadingScreen()
            DataState.ERROR -> ContentErrorScreen {
                /* handle error */
            }
        }
    }
}