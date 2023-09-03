package at.thomasgorke.photofeed.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.data.FlickrDataSource
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.data.model.FeedItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedScreenViewModel(
    private val dataSource: FlickrDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    // show info when reload fails and feed is not empty to not have an empty screen
    private val _snackText = MutableSharedFlow<String>()
    val snackText = _snackText.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                dataSource.getFeedFlow(),
                state.map { it.sortByDate }.distinctUntilChanged()
            ) { feedData, sortByDate ->
                when (feedData) {
                    is RepositoryResponse.Error -> _state.update {
                        it.copy(dataState = DataState.ERROR)
                    }

                    is RepositoryResponse.Success -> _state.update { state ->
                        state.copy(
                            feed = feedData.data.let { list ->
                                if (sortByDate) list.sortedBy { it.dateTake }
                                else list
                            },
                            dataState = DataState.SUCCESS
                        )
                    }
                }
            }.launchIn(this)
        }
    }

    fun execute(action: Action) {
        viewModelScope.launch {
            when (action) {
                Action.Retry -> reloadFeed()
                is Action.Favorite -> dataSource.toggleFavorite(action.feedItem)
                Action.ToggleSortOption -> _state.update { it.copy(sortByDate = !it.sortByDate) }
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
        val dataState: DataState = DataState.LOADING,
        val sortByDate: Boolean = false
    )

    sealed class Action {
        data object Retry : Action()
        data class Favorite(val feedItem: FeedItem): Action()
        data object ToggleSortOption: Action()
    }
}