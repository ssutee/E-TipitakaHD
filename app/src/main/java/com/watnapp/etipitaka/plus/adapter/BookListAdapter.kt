package com.watnapp.etipitaka.plus.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaThemeTokens
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter

class BookListAdapter(
    private val context: Context,
    private var language: BookDatabaseHelper.Language,
    private val dataSource: BookListAdapterDataSource,
) : BaseAdapter(), StickyListHeadersAdapter {

    interface BookListAdapterDataSource {
        fun getTitlesArrayId(language: BookDatabaseHelper.Language): Int
        fun getSectionsArrayId(language: BookDatabaseHelper.Language): Int
        fun getSectionBoundary(index: Int): Int
    }

    fun setLanguage(language: BookDatabaseHelper.Language) {
        this.language = language
    }

    private fun getTitles(): Array<String> =
        context.resources.getStringArray(dataSource.getTitlesArrayId(language))

    private fun getSections(): Array<String>? {
        val sectionsArrayId = dataSource.getSectionsArrayId(language)
        return if (sectionsArrayId > 0) {
            context.resources.getStringArray(sectionsArrayId)
        } else {
            null
        }
    }

    override fun getHeaderId(position: Int): Long =
        when {
            position >= 0 && position < dataSource.getSectionBoundary(0) -> 1
            position >= dataSource.getSectionBoundary(0) && position < dataSource.getSectionBoundary(1) -> 2
            else -> 3
        }

    override fun getCount(): Int = getTitles().size

    override fun getItem(position: Int): Any = getTitles()[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = (convertView as? ComposeView) ?: createComposeView(parent)
        val title = getTitles()[position]
        row.setOnClickListener {
            (parent as? AdapterView<*>)?.performItemClick(row, position, getItemId(position))
        }
        row.setContent {
            BookListRow(title = title)
        }
        return row
    }

    override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = (convertView as? ComposeView) ?: createComposeView(parent)
        val sections = getSections()
        val title = when {
            sections == null -> ""
            getHeaderId(position) == 1L -> sections[0]
            getHeaderId(position) == 2L -> sections[1]
            else -> sections[2]
        }
        row.setContent {
            BookListHeader(title = title)
        }
        return row
    }

    private fun createComposeView(parent: ViewGroup): ComposeView =
        ComposeView(parent.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        }
}

@Composable
private fun BookListRow(title: String) {
    ETipitakaTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BookListHeader(title: String) {
    ETipitakaTheme {
        val colors = ETipitakaThemeTokens.colors
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.subtitleBackground)
                .padding(5.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                color = colors.subtitleText,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
