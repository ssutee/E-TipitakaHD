package com.watnapp.etipitaka.plus.activity

import android.graphics.Typeface
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme

data class DictHeadword(
    val id: Int,
    val headword: String,
)

fun interface DictEntryClickListener {
    fun onEntryClick(index: Int)
}

object DictScreenBridge {
    @JvmStatic
    fun render(
        composeView: ComposeView,
        entries: List<DictHeadword>,
        fontSize: Int,
        typeface: Typeface?,
        itemClickListener: DictEntryClickListener,
    ) {
        composeView.setContent {
            DictScreen(
                entries = entries,
                fontSize = fontSize,
                typeface = typeface,
                onEntryClick = itemClickListener::onEntryClick,
            )
        }
    }
}

@Composable
private fun DictScreen(
    entries: List<DictHeadword>,
    fontSize: Int,
    typeface: Typeface?,
    onEntryClick: (Int) -> Unit,
) {
    ETipitakaTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp),
        ) {
            itemsIndexed(entries) { index, entry ->
                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            textSize = fontSize.toFloat()
                            if (typeface != null) {
                                setTypeface(typeface)
                            }
                            setSingleLine(true)
                            setPadding(16, 10, 16, 10)
                        }
                    },
                    update = { textView ->
                        textView.text = entry.headword
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEntryClick(index) }
                        .padding(vertical = 1.dp),
                )
            }
        }
    }
}
