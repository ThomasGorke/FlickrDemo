package at.thomasgorke.photofeed.ui.favorites

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


class FavoriteScreenViewModel(
    private val dataSource: FlickrDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dataSource.getFavoritesFlow()
                .onEach { result ->
                    when (result) {
                        is RepositoryResponse.Error -> _state.update {
                            it.copy(
                                dataState = DataState.ERROR,
                                feed = emptyList()
                            )
                        }

                        is RepositoryResponse.Success -> _state.update {
                            it.copy(
                                dataState = DataState.SUCCESS,
                                feed = result.data
                            )
                        }
                    }
                }
                .launchIn(this)
        }
    }

    fun execute(action: Action) {
        viewModelScope.launch {
            when (action) {
                is Action.ToggleFavorite -> dataSource.toggleFavorite(action.feedItem)
            }
        }
    }

    sealed class Action {
        data class ToggleFavorite(val feedItem: FeedItem) : Action()
    }

    data class State(
        val dataState: DataState = DataState.LOADING,
        val feed: List<FeedItem> = emptyList()
    )
}