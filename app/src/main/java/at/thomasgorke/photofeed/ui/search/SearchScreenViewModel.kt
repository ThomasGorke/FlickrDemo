package at.thomasgorke.photofeed.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.data.FlickrDataSource
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.data.model.FeedItem
import at.thomasgorke.photofeed.data.remote.model.TagMode
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchScreenViewModel(
    private val flickrDataSource: FlickrDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                state.map { it.query }.distinctUntilChanged().debounce(200).filter { it.isBlank() || it.length > 2 },
                state.map { it.tagModeIsAll }.distinctUntilChanged()
            ) { query, tagModeIsAll ->
                when (query.isEmpty()) {
                    true -> _state.update { it.copy(result = emptyList()) }
                    false -> loadData(query, tagModeIsAll)
                }
            }.launchIn(this)

            combine(
                state.map { it.result }.distinctUntilChanged(),
                flickrDataSource.getFavoritesFlow()
            ) { results, favorites ->
                when (favorites) {
                    is RepositoryResponse.Error -> results
                    is RepositoryResponse.Success -> {
                        results.map { item ->
                            when (favorites.data.any { it.imgUrl == item.imgUrl }) {
                                true -> item.copy(isFavored = true)
                                false -> item
                            }
                        }
                    }
                }
            }
                .onEach { result ->
                    _state.update { it.copy(result = result) }
                }
                .launchIn(this)
        }
    }

    fun execute(action: Action) {
        when (action) {
            Action.Retry -> {
                if (state.value.query.criteriaFulfilled()) {
                    loadData(state.value.query, state.value.tagModeIsAll)
                }
            }

            is Action.Search -> _state.update { it.copy(query = action.query) }
            is Action.TagSelected -> tagSelected(action.tag)
            is Action.ToggleFavorite -> toggleFavorite(action.feedItem)
            Action.ToggleTagMode -> _state.update { it.copy(tagModeIsAll = !it.tagModeIsAll) }
        }
    }

    private fun toggleFavorite(feedItem: FeedItem) {
        viewModelScope.launch {
            flickrDataSource.toggleFavorite(feedItem)
        }
    }

    private fun loadData(query: String, tagModeIsAll: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(dataState = DataState.LOADING) }
            val delayJob = async { delay(1000) }
            when (val response = flickrDataSource.fetchFeedByTags(query, tagModeIsAll.toTagMode())) {
                is RepositoryResponse.Error -> {
                    delayJob.await()
                    _state.update {
                        it.copy(
                            result = emptyList(),
                            dataState = when (it.query.criteriaFulfilled()) {
                                true -> DataState.ERROR // only show error with retry when query is valid
                                false -> DataState.SUCCESS // show empty list
                            }
                        )
                    }
                }

                is RepositoryResponse.Success -> {
                    delayJob.await()
                    _state.update {
                        it.copy(result = response.data, dataState = DataState.SUCCESS)
                    }
                }
            }
        }
    }

    private fun tagSelected(tag: String) {
        when (tag.isAdded()) {
            true -> getTagsAsList().filter { it != tag } // unselect tag
            false -> getTagsAsList().apply { this += tag }
        }.joinToString(separator = ",").also { newQuery ->
            _state.update { it.copy(query = newQuery) }
        }
    }

    private fun getTagsAsList(): MutableList<String> = state.value.query
        .replace(" ", "")
        .let {
            when (it.isEmpty()) {
                true -> emptyList()
                else -> it.split(",")
            }
        }.toMutableList()

    private fun String.isAdded(): Boolean =
        getTagsAsList().any { element -> element == this }

    private fun String.criteriaFulfilled(): Boolean = this.length > 2

    sealed class Action {
        data object Retry : Action()
        data class Search(val query: String) : Action()
        data class TagSelected(val tag: String) : Action()
        data class ToggleFavorite(val feedItem: FeedItem) : Action()
        data object ToggleTagMode : Action()
    }

    data class State(
        val query: String = "",
        val tagModeIsAll: Boolean = true,
        val result: List<FeedItem> = emptyList(),
        val dataState: DataState = DataState.SUCCESS
    )

    private fun Boolean.toTagMode(): TagMode = when (this) {
        true -> TagMode.ALL
        false -> TagMode.ANY
    }
}