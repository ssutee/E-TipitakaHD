package com.watnapp.etipitaka.plus.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaThemeTokens
import kotlin.math.roundToInt

fun interface ReaderSeekBarListener {
    fun onSeekProgressChanged(progress: Int, fromUser: Boolean)
}

object ReaderChromeBridge {
    @JvmStatic
    fun renderSubtitle(composeView: ComposeView, subtitle: String) {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            ETipitakaTheme {
                ReaderSubtitle(subtitle = subtitle)
            }
        }
    }

    @JvmStatic
    fun renderSeekBar(
        composeView: ComposeView,
        progress: Int,
        max: Int,
        listener: ReaderSeekBarListener,
    ) {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            ETipitakaTheme {
                ReaderSeekBar(
                    progress = progress,
                    max = max,
                    onProgressChanged = { listener.onSeekProgressChanged(it, true) },
                    onProgressSelected = { listener.onSeekProgressChanged(it, false) },
                )
            }
        }
    }

    @JvmStatic
    fun renderBottomControls(
        composeView: ComposeView,
        compareClick: Runnable,
        returnClick: Runnable,
    ) {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            ETipitakaTheme {
                ReaderBottomControls(
                    onCompareClick = compareClick::run,
                    onReturnClick = returnClick::run,
                )
            }
        }
    }
}

@Composable
private fun ReaderSubtitle(subtitle: String) {
    Text(
        text = subtitle,
        modifier = Modifier
            .fillMaxWidth()
            .background(ETipitakaThemeTokens.colors.subtitleBackground)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        color = ETipitakaThemeTokens.colors.subtitleText,
        fontWeight = FontWeight.Bold,
        minLines = 2,
        maxLines = 2,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun ReaderSeekBar(
    progress: Int,
    max: Int,
    onProgressChanged: (Int) -> Unit,
    onProgressSelected: (Int) -> Unit,
) {
    val coercedMax = max.coerceAtLeast(1)
    var currentProgress by remember(progress, max) {
        mutableFloatStateOf(progress.coerceIn(0, max.coerceAtLeast(0)).toFloat())
    }

    Slider(
        value = currentProgress,
        onValueChange = { value ->
            currentProgress = value
            onProgressChanged(value.roundToInt().coerceIn(0, max.coerceAtLeast(0)))
        },
        onValueChangeFinished = {
            onProgressSelected(currentProgress.roundToInt().coerceIn(0, max.coerceAtLeast(0)))
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(ETipitakaThemeTokens.colors.translucentControl)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        valueRange = 0f..coercedMax.toFloat(),
        enabled = max > 0,
    )
}

@Composable
private fun ReaderBottomControls(
    onCompareClick: () -> Unit,
    onReturnClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(ETipitakaThemeTokens.colors.translucentControl),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onCompareClick,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu_refresh),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
        IconButton(
            onClick = onReturnClick,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_revert),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
}
