package com.watnapp.etipitaka.plus.activity

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaThemeTokens
import java.io.File

fun interface FileExplorerPathChangedListener {
    fun onPathChanged(path: String)
}

fun interface FileExplorerItemClickListener {
    fun onItemClick(index: Int)
}

object FileExplorerScreenBridge {
    @JvmStatic
    fun render(
        activity: Activity,
        title: String,
        path: String,
        files: Array<File>,
        pathChangedListener: FileExplorerPathChangedListener,
        selectClick: Runnable,
        itemClickListener: FileExplorerItemClickListener,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setContent {
                    FileExplorerScreen(
                        title = title,
                        path = path,
                        files = files.toList(),
                        onPathChanged = pathChangedListener::onPathChanged,
                        onSelectClick = selectClick::run,
                        onItemClick = itemClickListener::onItemClick,
                    )
                }
            },
        )
    }
}

@Composable
private fun FileExplorerScreen(
    title: String,
    path: String,
    files: List<File>,
    onPathChanged: (String) -> Unit,
    onSelectClick: () -> Unit,
    onItemClick: (Int) -> Unit,
) {
    ETipitakaTheme {
        var pathText by remember(path) { mutableStateOf(path) }

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ETipitakaThemeTokens.colors.subtitleBackground)
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                color = ETipitakaThemeTokens.colors.subtitleText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = pathText,
                    onValueChange = {
                        pathText = it
                        onPathChanged(it)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(onClick = onSelectClick) {
                    Text(text = stringResource(R.string.select))
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(files) { index, file ->
                    FileExplorerRow(
                        file = file,
                        onClick = { onItemClick(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FileExplorerRow(
    file: File,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(if (file.isDirectory) R.drawable.folder else R.drawable.file),
            contentDescription = null,
        )
        Text(
            text = file.name,
            modifier = Modifier.weight(1f),
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
