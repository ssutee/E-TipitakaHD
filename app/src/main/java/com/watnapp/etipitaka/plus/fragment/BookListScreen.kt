package com.watnapp.etipitaka.plus.fragment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaThemeTokens

fun interface BookVolumeClickListener {
    fun onBookVolumeClick(position: Int)
}

object BookListScreenBridge {
    @JvmStatic
    fun render(
        composeView: ComposeView,
        titles: Array<String>,
        sections: Array<String>?,
        sectionBoundaries: IntArray,
        clickListener: BookVolumeClickListener,
    ) {
        composeView.setContent {
            BookListScreen(
                titles = titles.toList(),
                sections = sections?.toList(),
                sectionBoundaries = sectionBoundaries,
                onVolumeClick = clickListener::onBookVolumeClick,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookListScreen(
    titles: List<String>,
    sections: List<String>?,
    sectionBoundaries: IntArray,
    onVolumeClick: (Int) -> Unit,
) {
    ETipitakaTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp),
        ) {
            if (sections.isNullOrEmpty()) {
                itemsIndexed(titles) { index, title ->
                    BookListRow(
                        title = title,
                        onClick = { onVolumeClick(index) },
                    )
                }
            } else {
                val firstBoundary = sectionBoundaries.getOrElse(0) { titles.size }.coerceIn(0, titles.size)
                val secondBoundary = sectionBoundaries.getOrElse(1) { titles.size }.coerceIn(firstBoundary, titles.size)
                val sectionRanges = listOf(
                    0 until firstBoundary,
                    firstBoundary until secondBoundary,
                    secondBoundary until titles.size,
                )

                sectionRanges.forEachIndexed { sectionIndex, range ->
                    if (!range.isEmpty()) {
                        stickyHeader {
                            BookListHeader(title = sections.getOrNull(sectionIndex).orEmpty())
                        }
                        items(range.count()) { offset ->
                            val index = range.first + offset
                            BookListRow(
                                title = titles[index],
                                onClick = { onVolumeClick(index) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookListRow(
    title: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
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

@Composable
private fun BookListHeader(title: String) {
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
