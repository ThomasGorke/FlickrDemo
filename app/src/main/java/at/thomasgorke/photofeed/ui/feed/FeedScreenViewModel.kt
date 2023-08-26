package at.thomasgorke.photofeed.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.data.FlickrDataSource
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
                        is RepositoryResponse.Error -> {
                            /* TODO show error */
                        }

                        is RepositoryResponse.Success -> _state.update {
                            it.copy(feed = feedData.data)
                        }
                    }
                }
                .launchIn(this)
        }
    }

    fun execute(action: Action) {
        viewModelScope.launch {
            when (action) {
                Action.Retry -> dataSource.fetchNewRemoteFeed()
            }
        }
    }

    data class State(
        val feed: List<FeedItem> = emptyList(),
        val hasError: Boolean = false
    )

    sealed class Action {
        data object Retry : Action()
    }
}