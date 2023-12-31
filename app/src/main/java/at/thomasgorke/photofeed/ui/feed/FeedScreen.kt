@file:OptIn(ExperimentalMaterial3Api::class)

package at.thomasgorke.photofeed.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.thomasgorke.photofeed.R
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.data.model.FeedItem
import at.thomasgorke.photofeed.ui.base.ContentErrorScreen
import at.thomasgorke.photofeed.ui.base.ContentLoadingScreen
import at.thomasgorke.photofeed.ui.base.FeedItem
import at.thomasgorke.photofeed.ui.destinations.FavoriteScreenDestination
import at.thomasgorke.photofeed.ui.destinations.ImageFullScreenDestination
import at.thomasgorke.photofeed.ui.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.coroutines.EmptyCoroutineContext

@RootNavGraph(start = true)
@Destination
@Composable
fun FeedScreen(
    navigator: DestinationsNavigator,
    viewModel: FeedScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.snackText
            .onEach {
                scope.launch {
                    snackbarHostState.showSnackbar(message = it)
                }
            }
            .flowOn(EmptyCoroutineContext)
            .launchIn(this)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(text = stringResource(id = R.string.title_feed)) },
                actions = {
                    IconButton(onClick = { navigator.navigate(FavoriteScreenDestination) }) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites"
                        )
                    }
                    IconButton(onClick = { navigator.navigate(SearchScreenDestination) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.execute(FeedScreenViewModel.Action.Retry) }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar {
                    Text(text = data.visuals.message)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (state.dataState) {
                DataState.ERROR -> ContentErrorScreen { viewModel.execute(FeedScreenViewModel.Action.Retry) }
                DataState.LOADING -> ContentLoadingScreen()
                DataState.SUCCESS -> Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort by date",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Switch(
                            modifier = Modifier.padding(start = 16.dp),
                            checked = state.sortByDate,
                            onCheckedChange = {
                                viewModel.execute(FeedScreenViewModel.Action.ToggleSortOption)
                            }
                        )
                    }
                    FeedContent(
                        feed = state.feed,
                        toggleFavorite = { viewModel.execute(FeedScreenViewModel.Action.Favorite(it)) },
                        open = { navigator.navigate(ImageFullScreenDestination(imgUrl = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedContent(
    feed: List<FeedItem>,
    toggleFavorite: (FeedItem) -> Unit,
    open: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(feed) {
                FeedItem(
                    feedItem = it,
                    toggleFavorite = toggleFavorite,
                    open = open
                )
            }
        }
    }
}
