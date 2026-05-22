package com.watnapp.etipitaka.plus.account

/** Result of one backend call. */
sealed class ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>()

    /** HTTP 401 — token rejected; caller should force-logout. */
    object AuthError : ApiResult<Nothing>()

    /** Could not reach the server (IO failure). */
    object NetworkError : ApiResult<Nothing>()

    /** Reached the server but it returned an error. */
    data class ServerError(val message: String) : ApiResult<Nothing>()
}

/** Domain outcome of a POST /upload/ call (HTTP itself is 200 in all three cases). */
enum class UploadOutcome {
    SUCCESS,
    /** A non-deleted backup with the same name already exists on the server. */
    FILE_EXISTS,
    FAILED,
}

/** One backup file listed by GET /user_data_list/. */
data class ServerBackup(
    val pk: Int,
    val filename: String,
    val platform: String,
    val createdAt: String,
)
