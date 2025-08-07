package ca.ilianokokoro.umihi.music.core

sealed class ApiResult<out T> {
    data object Loading : ApiResult<Nothing>()
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception, val errorMessage: String = "") : ApiResult<Nothing>()
}