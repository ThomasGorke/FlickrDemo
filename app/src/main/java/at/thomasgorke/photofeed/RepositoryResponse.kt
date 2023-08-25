package at.thomasgorke.photofeed

import at.thomasgorke.photofeed.data.model.RepositoryException

sealed class RepositoryResponse<out T> {
    data class Success<T>(val data: T): RepositoryResponse<T>()
    data class Error(val repositoryException: RepositoryException): RepositoryResponse<Nothing>()
}
