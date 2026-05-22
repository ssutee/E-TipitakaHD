package com.watnapp.etipitaka.plus.account

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.watnapp.etipitaka.plus.Constants
import com.watnapp.etipitaka.plus.R

@Composable
fun AccountScreen(viewModel: AccountViewModel) {
    when (val state = viewModel.uiState) {
        is AccountUiState.LoggedOut -> LoggedOutContent(state, viewModel::login)
        is AccountUiState.LoggedIn -> LoggedInContent(
            state = state,
            onUpload = viewModel::upload,
            onRefresh = viewModel::refreshBackups,
            onLogout = viewModel::logout,
            onDownload = viewModel::download,
            onDelete = viewModel::delete,
            onMessageShown = viewModel::messageShown,
        )
    }
}

@Composable
private fun LoggedOutContent(
    state: AccountUiState.LoggedOut,
    onLogin: (String, String) -> Unit,
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.account_username)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.account_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.errorRes != null) {
            Text(stringResource(state.errorRes))
        }
        Button(
            onClick = { onLogin(username, password) },
            enabled = !state.loggingIn,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.account_login))
        }
        if (state.loggingIn) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        TextButton(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SIGNUP_URL))
                )
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.account_create))
        }
    }
}

@Composable
private fun LoggedInContent(
    state: AccountUiState.LoggedIn,
    onUpload: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onDownload: (ServerBackup) -> Unit,
    onDelete: (ServerBackup) -> Unit,
    onMessageShown: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(state.messageRes) {
        val res = state.messageRes
        if (res != null) {
            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
            onMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.account_logged_in_as, state.username))
            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.account_logout))
            }
        }
        Button(
            onClick = onUpload,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.account_upload))
        }
        OutlinedButton(
            onClick = onRefresh,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.account_refresh))
        }
        if (state.busy) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        Divider()
        if (state.backups.isEmpty()) {
            Text(stringResource(R.string.account_no_backups))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.backups, key = { it.pk }) { backup ->
                    BackupRow(
                        backup = backup,
                        enabled = !state.busy,
                        onDownload = { onDownload(backup) },
                        onDelete = { onDelete(backup) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupRow(
    backup: ServerBackup,
    enabled: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(platformLabel(backup.platform) + "  •  " + backup.filename)
        Text(backup.createdAt)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDownload, enabled = enabled) {
                Text(stringResource(R.string.account_download))
            }
            OutlinedButton(onClick = onDelete, enabled = enabled) {
                Text(stringResource(R.string.account_delete))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Divider()
    }
}

@Composable
private fun platformLabel(platform: String): String = when (platform) {
    "ios" -> stringResource(R.string.account_platform_ios)
    "android" -> stringResource(R.string.account_platform_android)
    "pc" -> stringResource(R.string.account_platform_pc)
    else -> platform
}
