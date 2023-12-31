@file:OptIn(ExperimentalMaterial3Api::class)

package at.thomasgorke.photofeed.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.thomasgorke.photofeed.R
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.data.model.FeedItem
import at.thomasgorke.photofeed.ui.base.ContentEmptyScreen
import at.thomasgorke.photofeed.ui.base.ContentErrorScreen
import at.thomasgorke.photofeed.ui.base.ContentLoadingScreen
import at.thomasgorke.photofeed.ui.base.FeedItem
import at.thomasgorke.photofeed.ui.destinations.ImageFullScreenDestination
import at.thomasgorke.photofeed.ui.fullscreen.ImageFullScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel

@Destination
@Composable
fun SearchScreen(
    navigator: DestinationsNavigator,
    viewModel: SearchScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.title_feed)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TagSearch(
                input = state.query,
                tagModeIsAll = state.tagModeIsAll,
                update = {
                    viewModel.execute(SearchScreenViewModel.Action.Search(it))
                },
                tagSelected = {
                    viewModel.execute(SearchScreenViewModel.Action.TagSelected(it))
                },
                toggleTagMode = {
                    viewModel.execute(SearchScreenViewModel.Action.ToggleTagMode)
                }
            )

            FeedResultScreen(
                dataState = state.dataState,
                results = state.result,
                retry = { viewModel.execute(SearchScreenViewModel.Action.Retry) },
                toggleFavorite = { viewModel.execute(SearchScreenViewModel.Action.ToggleFavorite(it)) },
                open = { navigator.navigate(ImageFullScreenDestination(imgUrl = it)) }
            )
        }
    }
}

@Composable
fun FeedResultScreen(
    dataState: DataState,
    results: List<FeedItem>,
    retry: () -> Unit,
    toggleFavorite: (FeedItem) -> Unit,
    open: (String) -> Unit
) {
    when (dataState) {
        DataState.SUCCESS -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                when (results.isEmpty()) {
                    true -> ContentEmptyScreen(msgId = R.string.info_no_search_result)
                    else -> LazyColumn {
                        items(results) {
                            FeedItem(
                                feedItem = it,
                                toggleFavorite = toggleFavorite,
                                open = open
                            )
                        }
                    }
                }
            }
        }

        DataState.LOADING -> ContentLoadingScreen()
        DataState.ERROR -> ContentErrorScreen(retry = retry)
    }
}

@Composable
private fun TagSearch(
    input: String,
    tagModeIsAll: Boolean,
    update: (String) -> Unit,
    tagSelected: (String) -> Unit,
    toggleTagMode: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            value = input,
            onValueChange = update,
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(percent = 40),
            singleLine = true,
            placeholder = { Text(text = stringResource(id = R.string.placeholder_search)) },
            trailingIcon = {
                if (input.isNotEmpty()) {
                    IconButton(onClick = { update("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags) { tag ->
                TagItem(
                    label = tag,
                    isSelected = input.split(",").contains(tag),
                    click = tagSelected
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Search All")
            Checkbox(
                modifier = Modifier.padding(start = 8.dp),
                checked = tagModeIsAll,
                onCheckedChange = {
                    toggleTagMode()
                }
            )
        }
    }
}

@Composable
private fun TagItem(
    label: String,
    isSelected: Boolean,
    click: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(percent = 50))
            .let {
                when (isSelected) {
                    false -> it.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(percent = 50)
                    )

                    true -> it.background(color = MaterialTheme.colorScheme.primaryContainer)
                }
            }
            .clickable { click(label) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(17.dp),
            painter = painterResource(id = R.drawable.ic_tag),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

val tags = listOf(
    "banking",
    "android",
    "development",
    "car",
    "flowers",
    "usa",
    "travel",
    "food",
    "phones",
    "macbook"
)



