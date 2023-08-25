package at.thomasgorke.photofeed.data.model

sealed class RepositoryException(msg: String, throwable: Throwable): Exception(msg, throwable) {
    data class LocalException(val throwable: Throwable) : RepositoryException("Error occurred in local data repo", throwable)
    data class RemoteException(val throwable: Throwable) : RepositoryException("Error occurred in remote data repo", throwable)
}
