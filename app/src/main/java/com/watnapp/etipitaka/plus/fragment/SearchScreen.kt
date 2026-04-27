package com.watnapp.etipitaka.plus.fragment

import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import se.emilsjolander.stickylistheaders.StickyListHeadersListView

fun interface SearchQueryChangedListener {
    fun onSearchQueryChanged(query: String)
}

object SearchScreenBridge {
    @JvmStatic
    fun render(
        composeView: ComposeView,
        query: String,
        listView: StickyListHeadersListView,
        queryChangedListener: SearchQueryChangedListener,
        searchClick: Runnable,
    ) {
        composeView.setContent {
            SearchScreen(
                query = query,
                listView = listView,
                onQueryChanged = queryChangedListener::onSearchQueryChanged,
                onSearch = searchClick::run,
            )
        }
    }
}

@Composable
private fun SearchScreen(
    query: String,
    listView: StickyListHeadersListView,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
) {
    ETipitakaTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
            var queryText by remember(query) { mutableStateOf(query) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onSearch) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_search),
                        contentDescription = stringResource(R.string.search),
                    )
                }
                OutlinedTextField(
                    value = queryText,
                    onValueChange = {
                        queryText = it
                        onQueryChanged(it)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { androidx.compose.material3.Text(stringResource(R.string.query_hint)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { onSearch() },
                    ),
                )
            }
            AndroidView(
                factory = {
                    (listView.parent as? ViewGroup)?.removeView(listView)
                    listView
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
