package com.watnapp.etipitaka.plus.fragment

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
internal fun MenuTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf(
        R.string.volume,
        R.string.search,
        R.string.history,
        R.string.favorite,
    )

    // SlidingMenu's CustomViewBehind can measure this ComposeView with an
    // UNSPECIFIED width during RelativeLayout's pre-measure pass (behind
    // width not resolved yet -> negative child dimension -> UNSPECIFIED).
    // M3 TabRow divides an unbounded maxWidth by the tab count and feeds the
    // result to maxIntrinsicHeight, which cannot be encoded in Constraints
    // (Play Console: IllegalArgumentException at invalidConstraint). Give
    // the strip a real width for that throwaway pass; the follow-up pass is
    // bounded and behaves as before.
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val tabRowModifier =
            if (constraints.hasBoundedWidth) Modifier.fillMaxWidth()
            else Modifier.requiredWidth(screenWidth)
        MenuTabRow(tabs, selectedIndex, onTabSelected, tabRowModifier)
    }
}

@Composable
private fun MenuTabRow(
    tabs: List<Int>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier,
) {
    TabRow(
        selectedTabIndex = selectedIndex.coerceIn(tabs.indices),
        modifier = modifier,
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
