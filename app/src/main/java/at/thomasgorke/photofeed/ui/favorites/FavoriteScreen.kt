@file:OptIn(ExperimentalMaterial3Api::class)

package at.thomasgorke.photofeed.ui.favorites

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.thomasgorke.photofeed.R
import at.thomasgorke.photofeed.data.model.DataState
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
                }
            )
        }
    ) { padding ->
        when (state.dataState) {
            DataState.SUCCESS -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
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

            DataState.LOADING -> ContentLoadingScreen()
            DataState.ERROR -> ContentErrorScreen {
                /* handle error */
            }
        }
    }
}