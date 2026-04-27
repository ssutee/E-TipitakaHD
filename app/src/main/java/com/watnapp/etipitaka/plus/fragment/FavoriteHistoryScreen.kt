package com.watnapp.etipitaka.plus.fragment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.Utils
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.model.Favorite
import com.watnapp.etipitaka.plus.model.History
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme

fun interface FavoriteActionListener {
    fun onFavoriteAction(favorite: Favorite)
}

fun interface HistoryActionListener {
    fun onHistoryAction(history: History)
}

object FavoriteHistoryScreenBridge {
    @JvmStatic
    fun renderFavorites(
        composeView: ComposeView,
        favorites: List<Favorite>,
        openClick: FavoriteActionListener,
        editClick: FavoriteActionListener,
        deleteClick: FavoriteActionListener,
        markClick: FavoriteActionListener,
        sortClick: Runnable,
    ) {
        composeView.setContent {
            FavoriteScreen(
                favorites = favorites,
                onOpen = openClick::onFavoriteAction,
                onEdit = editClick::onFavoriteAction,
                onDelete = deleteClick::onFavoriteAction,
                onMark = markClick::onFavoriteAction,
                onSort = sortClick::run,
            )
        }
    }

    @JvmStatic
    fun renderHistory(
        composeView: ComposeView,
        histories: List<History>,
        language: BookDatabaseHelper.Language,
        openClick: HistoryActionListener,
        deleteClick: HistoryActionListener,
        markClick: HistoryActionListener,
        sortClick: Runnable,
    ) {
        composeView.setContent {
            HistoryScreen(
                histories = histories,
                language = language,
                onOpen = openClick::onHistoryAction,
                onDelete = deleteClick::onHistoryAction,
                onMark = markClick::onHistoryAction,
                onSort = sortClick::run,
            )
        }
    }
}

@Composable
private fun FavoriteScreen(
    favorites: List<Favorite>,
    onOpen: (Favorite) -> Unit,
    onEdit: (Favorite) -> Unit,
    onDelete: (Favorite) -> Unit,
    onMark: (Favorite) -> Unit,
    onSort: () -> Unit,
) {
    ETipitakaTheme {
        var selectedFavorite by remember { mutableStateOf<Favorite?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp),
        ) {
            items(favorites, key = { it.getId() }) { favorite ->
                FavoriteRow(
                    favorite = favorite,
                    onClick = { onOpen(favorite) },
                    onLongClick = { selectedFavorite = favorite },
                )
            }
        }

        selectedFavorite?.let { favorite ->
            FavoriteActionDialog(
                favorite = favorite,
                onDismiss = { selectedFavorite = null },
                onOpen = {
                    selectedFavorite = null
                    onOpen(favorite)
                },
                onEdit = {
                    selectedFavorite = null
                    onEdit(favorite)
                },
                onDelete = {
                    selectedFavorite = null
                    onDelete(favorite)
                },
                onMark = {
                    selectedFavorite = null
                    onMark(favorite)
                },
                onSort = {
                    selectedFavorite = null
                    onSort()
                },
            )
        }
    }
}

@Composable
private fun HistoryScreen(
    histories: List<History>,
    language: BookDatabaseHelper.Language,
    onOpen: (History) -> Unit,
    onDelete: (History) -> Unit,
    onMark: (History) -> Unit,
    onSort: () -> Unit,
) {
    ETipitakaTheme {
        var selectedHistory by remember { mutableStateOf<History?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp),
        ) {
            items(histories, key = { it.getId() }) { history ->
                HistoryRow(
                    history = history,
                    language = language,
                    onClick = { onOpen(history) },
                    onLongClick = { selectedHistory = history },
                )
            }
        }

        selectedHistory?.let { history ->
            HistoryActionDialog(
                history = history,
                onDismiss = { selectedHistory = null },
                onDelete = {
                    selectedHistory = null
                    onDelete(history)
                },
                onMark = {
                    selectedHistory = null
                    onMark(history)
                },
                onSort = {
                    selectedHistory = null
                    onSort()
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteRow(
    favorite: Favorite,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val title = if (favorite.getItem() > 0) {
        context.getString(
            R.string.favorite_template,
            Utils.convertToThaiNumber(context, favorite.getVolume()),
            Utils.convertToThaiNumber(context, favorite.getPage()),
            Utils.convertToThaiNumber(context, favorite.getItem()),
        )
    } else {
        context.getString(
            R.string.favorite_no_item_template,
            Utils.convertToThaiNumber(context, favorite.getVolume()),
            Utils.convertToThaiNumber(context, favorite.getPage()),
        )
    }

    SavedItemRow(
        title = title,
        subtitle = favorite.getNote().orEmpty(),
        isMarked = favorite.getScore() > 0,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(
    history: History,
    language: BookDatabaseHelper.Language,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    SavedItemRow(
        title = history.getKeywords().orEmpty(),
        subtitle = historySubtitle(history, language),
        isMarked = history.getScore() > 0,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SavedItemRow(
    title: String,
    subtitle: String,
    isMarked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            )
            Text(
                text = subtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            )
        }
        if (isMarked) {
            Image(
                painter = painterResource(android.R.drawable.btn_star_big_on),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun FavoriteActionDialog(
    favorite: Favorite,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMark: () -> Unit,
    onSort: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(favorite.getNote().orEmpty()) },
        text = {
            Column {
                DialogAction(text = stringResource(R.string.open_note), onClick = onOpen)
                DialogAction(text = stringResource(R.string.edit_note), onClick = onEdit)
                DialogAction(text = stringResource(R.string.delete), onClick = onDelete)
                DialogAction(text = stringResource(R.string.mark), onClick = onMark)
                DialogAction(text = stringResource(R.string.sorting), onClick = onSort)
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun HistoryActionDialog(
    history: History,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onMark: () -> Unit,
    onSort: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(history.getKeywords().orEmpty()) },
        text = {
            Column {
                DialogAction(text = stringResource(R.string.delete), onClick = onDelete)
                DialogAction(text = stringResource(R.string.mark), onClick = onMark)
                DialogAction(text = stringResource(R.string.sorting), onClick = onSort)
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun DialogAction(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = text)
    }
}

@Composable
private fun historySubtitle(
    history: History,
    language: BookDatabaseHelper.Language,
): String {
    val context = LocalContext.current
    var subtitle = ""
    if (language == BookDatabaseHelper.Language.THAIBT) {
        subtitle = context.getString(
            R.string.found_n_pages,
            Utils.convertToThaiNumber(context, history.getResult1()),
        )
    } else if (language == BookDatabaseHelper.Language.THAIWN) {
        subtitle = context.getString(
            R.string.found_n_pages,
            Utils.convertToThaiNumber(context, history.getResult1()),
        )
        if (history.isBuddhawaj()) {
            subtitle += " (${context.getString(R.string.buddhawaj)})"
        }
    } else {
        if (history.getResult1() > 0) {
            subtitle += " " + context.getString(
                R.string.abbr_section1,
                Utils.convertToThaiNumber(context, history.getResult1()),
            )
        }
        if (history.getResult2() > 0) {
            subtitle += " " + context.getString(
                R.string.abbr_section2,
                Utils.convertToThaiNumber(context, history.getResult2()),
            )
        }
        if (history.getResult3() > 0) {
            subtitle += " " + context.getString(
                R.string.abbr_section3,
                Utils.convertToThaiNumber(context, history.getResult3()),
            )
        }
    }
    return subtitle.trim().ifEmpty { context.getString(R.string.not_found) }
}
