package com.watnapp.etipitaka.plus.fragment

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaThemeTokens

fun interface MenuTabSelectedListener {
    fun onMenuTabSelected(index: Int)
}

object MenuTabsBridge {
    @JvmStatic
    fun render(
        composeView: ComposeView,
        selectedIndex: Int,
        listener: MenuTabSelectedListener,
    ) {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            ETipitakaTheme {
                MenuTabs(
                    selectedIndex = selectedIndex,
                    onTabSelected = listener::onMenuTabSelected,
                )
            }
        }
    }
}

@Composable
private fun MenuTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf(
        R.string.volume,
        R.string.search,
        R.string.history,
        R.string.favorite,
    )

    TabRow(
        selectedTabIndex = selectedIndex.coerceIn(tabs.indices),
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = ETipitakaThemeTokens.colors.tabStripLine,
    ) {
        tabs.forEachIndexed { index, titleRes ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = stringResource(titleRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}
