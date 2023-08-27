package at.thomasgorke.photofeed.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.data.FlickrDataSource
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.data.model.FeedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedScreenViewModel(
    private val dataSource: FlickrDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dataSource.getFeedFlow()
                .onEach { feedData ->
                    when (feedData) {
                        is RepositoryResponse.Error -> _state.update {
                            it.copy(dataState = DataState.ERROR)
                        }

                        is RepositoryResponse.Success -> _state.update {
                            it.copy(feed = feedData.data, dataState = DataState.SUCCESS)
                        }
                    }
                }
                .launchIn(this)
        }
    }

    fun execute(action: Action) {
        viewModelScope.launch {
            when (action) {
                Action.Retry -> reloadFeed()
                is Action.Favorite -> dataSource.toggleFavorite(action.feedItem)
            }
        }
    }

    private suspend fun reloadFeed() {
        _state.update { it.copy(dataState = DataState.LOADING) }
        when (dataSource.fetchNewRemoteFeed()) {
            is RepositoryResponse.Error -> {
                when (state.value.feed.isEmpty()) {
                    false -> _state.update { it.copy(dataState = DataState.SUCCESS) }
                    true -> _state.update { it.copy(dataState = DataState.ERROR) }
                }
            }
            is RepositoryResponse.Success -> { /* nothing to do is the flow gets refreshed */ }
        }
    }

    data class State(
        val feed: List<FeedItem> = emptyList(),
        val dataState: DataState = DataState.LOADING
    )

    sealed class Action {
        data object Retry : Action()
        data class Favorite(val feedItem: FeedItem): Action()
    }
}