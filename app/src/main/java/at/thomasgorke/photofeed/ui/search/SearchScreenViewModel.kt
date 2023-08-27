package at.thomasgorke.photofeed.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.data.FlickrDataSource
import at.thomasgorke.photofeed.data.model.FeedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            state.map { it.query }
                .distinctUntilChanged()
                .filter { it.isBlank() || it.length > 2 }
                .onEach { query ->
                    when (query.isEmpty()) {
                        true -> _state.update { it.copy(result = emptyList()) }
                        false -> loadData(query)
                    }
                }
                .launchIn(this)
        }
    }

    private suspend fun loadData(query: String) {
        val response = flickrDataSource.fetchFeedByTags(query)
        when (response) {
            is RepositoryResponse.Error -> { println("errorororororo") }
            is RepositoryResponse.Success -> _state.update { it.copy(result = response.data) }
        }
    }

    fun execute(action: Action) {
        when (action) {
            Action.Retry -> {}
            is Action.Search -> _state.update { it.copy(query = action.query) }
            is Action.TagSelected -> tagSelected(action.tag)
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

    sealed class Action {
        data object Retry : Action()
        data class Search(val query: String) : Action()
        data class TagSelected(val tag: String) : Action()
    }

    data class State(
        val query: String = "",
        val result: List<FeedItem> = emptyList()
    )
}