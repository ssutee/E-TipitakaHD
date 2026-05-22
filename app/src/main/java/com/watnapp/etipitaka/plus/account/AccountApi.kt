package com.watnapp.etipitaka.plus.account

import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

/**
 * Talks to the E-Tipitaka backend. All methods are blocking and MUST be called
 * off the main thread (e.g. from Dispatchers.IO).
 */
class AccountApi(private val baseUrl: String) {

    private val client = OkHttpClient()

    /** POST /rest-auth/login/ — returns the auth token on success. */
    fun login(username: String, password: String): ApiResult<String> = try {
        val form = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url("$baseUrl/rest-auth/login/")
            .post(form)
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            val token = if (response.isSuccessful) parseLoginResponse(body) else null
            if (token != null) ApiResult.Success(token)
            else ApiResult.ServerError("login failed")
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** POST /rest-auth/logout/ — invalidates the token server-side. */
    fun logout(token: String): ApiResult<Unit> = try {
        val request = authedBuilder(token, "/rest-auth/logout/")
            .post(FormBody.Builder().build())
            .build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { Unit }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /**
     * POST /upload/ — multipart upload of the export JSON.
     * [filename] MUST end in `.js` so the backend tags the platform `android`.
     */
    fun uploadBackup(
        token: String,
        filename: String,
        json: String,
    ): ApiResult<UploadOutcome> = try {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", filename)
            .addFormDataPart(
                "file",
                filename,
                json.toRequestBody("application/json".toMediaType()),
            )
            .build()
        val request = authedBuilder(token, "/upload/").post(body).build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { parseUploadResponse(it) }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** GET /user_data_list/ — the caller's own non-deleted backups. */
    fun listBackups(token: String): ApiResult<List<ServerBackup>> = try {
        val request = authedBuilder(token, "/user_data_list/").get().build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { parseUserDataList(it) }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** GET /user_data/{pk}/ — returns the backup file content as text. */
    fun downloadBackup(token: String, pk: Int): ApiResult<String> = try {
        val request = authedBuilder(token, "/user_data/$pk/").get().build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { it }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** DELETE /user_data/{pk}/ — soft-deletes the backup server-side. */
    fun deleteBackup(token: String, pk: Int): ApiResult<Unit> = try {
        val request = authedBuilder(token, "/user_data/$pk/").delete().build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { Unit }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    private fun authedBuilder(token: String, path: String): Request.Builder =
        Request.Builder()
            .url("$baseUrl$path")
            .header("Authorization", "Token $token")

    private fun <T> mapStatus(
        response: Response,
        onSuccess: (String) -> T,
    ): ApiResult<T> {
        val body = response.body?.string().orEmpty()
        return when {
            response.code == 401 -> ApiResult.AuthError
            response.isSuccessful -> ApiResult.Success(onSuccess(body))
            else -> ApiResult.ServerError("HTTP ${response.code}")
        }
    }
}
