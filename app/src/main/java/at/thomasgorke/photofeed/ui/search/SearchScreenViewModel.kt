package at.thomasgorke.photofeed.ui.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchScreenViewModel : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

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
        val result: List<String> = emptyList()
    )
}