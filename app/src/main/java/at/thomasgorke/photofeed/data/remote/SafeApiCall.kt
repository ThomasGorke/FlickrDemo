package at.thomasgorke.photofeed.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResponse<T> {
    return try {
        val result = withContext(Dispatchers.IO) {
            apiCall.invoke()
        }
        NetworkResponse.Success(result)
    } catch (e: Exception) {
        NetworkResponse.Error(e)
    }
}


sealed class NetworkResponse<out T> {
    data class Success<T>(val data: T) : NetworkResponse<T>()
    data class Error(val throwable: Throwable) : NetworkResponse<Nothing>()
}
