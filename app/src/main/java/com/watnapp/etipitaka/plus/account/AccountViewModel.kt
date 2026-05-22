package com.watnapp.etipitaka.plus.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watnapp.etipitaka.plus.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** UI state for the Account screen. */
sealed interface AccountUiState {
    data class LoggedOut(
        val loggingIn: Boolean = false,
        val errorRes: Int? = null,
    ) : AccountUiState

    data class LoggedIn(
        val username: String,
        val backups: List<ServerBackup> = emptyList(),
        val busy: Boolean = false,
        val messageRes: Int? = null,
    ) : AccountUiState
}

class AccountViewModel(
    private val api: AccountApi,
    private val session: SessionManager,
    private val exporter: UserDataExporter,
    private val importer: UserDataImporter,
) : ViewModel() {

    var uiState by mutableStateOf<AccountUiState>(initialState())
        private set

    private fun initialState(): AccountUiState {
        val username = session.username
        return if (session.isLoggedIn && username != null) {
            AccountUiState.LoggedIn(username)
        } else {
            AccountUiState.LoggedOut()
        }
    }

    init {
        if (session.isLoggedIn) refreshBackups()
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) return
        uiState = AccountUiState.LoggedOut(loggingIn = true)
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { api.login(username, password) }) {
                is ApiResult.Success -> {
                    session.save(username, result.value)
                    uiState = AccountUiState.LoggedIn(username)
                    refreshBackups()
                }
                ApiResult.NetworkError ->
                    uiState = AccountUiState.LoggedOut(errorRes = R.string.account_network_error)
                else ->
                    uiState = AccountUiState.LoggedOut(errorRes = R.string.account_login_failed)
            }
        }
    }

    fun logout() {
        val token = session.token ?: return forceLoggedOut()
        viewModelScope.launch {
            withContext(Dispatchers.IO) { api.logout(token) }
            forceLoggedOut()
        }
    }

    fun refreshBackups() {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { api.listBackups(token) }) {
                is ApiResult.Success -> {
                    val live = uiState as? AccountUiState.LoggedIn ?: return@launch
                    uiState = live.copy(backups = result.value, busy = false)
                }
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> {
                    val live = uiState as? AccountUiState.LoggedIn ?: return@launch
                    uiState = live.copy(busy = false, messageRes = R.string.account_network_error)
                }
            }
        }
    }

    fun upload() {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { exporter.buildExportJson() }
                    .fold(
                        onSuccess = { json -> api.uploadBackup(token, uploadFilename(), json) },
                        onFailure = { ApiResult.ServerError("export failed") },
                    )
            }
            when (result) {
                is ApiResult.Success -> when (result.value) {
                    UploadOutcome.SUCCESS -> {
                        setMessage(R.string.account_upload_success)
                        refreshBackups()
                    }
                    UploadOutcome.FILE_EXISTS -> setMessage(R.string.account_file_exists)
                    UploadOutcome.FAILED -> setMessage(R.string.account_upload_failed)
                }
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> setMessage(R.string.account_upload_failed)
            }
        }
    }

    fun download(backup: ServerBackup) {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                when (val dl = api.downloadBackup(token, backup.pk)) {
                    is ApiResult.Success -> {
                        runCatching { importer.importAndroidJson(dl.value) }
                            .fold({ ApiResult.Success(Unit) },
                                  { ApiResult.ServerError("import failed") })
                    }
                    ApiResult.AuthError -> ApiResult.AuthError
                    ApiResult.NetworkError -> ApiResult.NetworkError
                    is ApiResult.ServerError -> dl
                }
            }
            when (result) {
                is ApiResult.Success -> setMessage(R.string.account_download_success)
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> setMessage(R.string.account_download_failed)
            }
        }
    }

    fun delete(backup: ServerBackup) {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { api.deleteBackup(token, backup.pk) }) {
                is ApiResult.Success -> refreshBackups()
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> setMessage(R.string.account_delete_failed)
            }
        }
    }

    /** Clears a transient message after the UI has shown it. */
    fun messageShown() {
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(messageRes = null)
    }

    private fun setMessage(res: Int) {
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = false, messageRes = res)
    }

    private fun forceLoggedOut(errorRes: Int? = null) {
        session.clear()
        uiState = AccountUiState.LoggedOut(errorRes = errorRes)
    }

    private fun uploadFilename(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "edata-$date.js"
    }
}
