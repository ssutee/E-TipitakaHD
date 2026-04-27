package com.watnapp.etipitaka.plus.activity

import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme

object MainActivityContentBridge {
    @JvmStatic
    fun render(activity: ComponentActivity) {
        val root = FrameLayout(activity)
        val composeShell = ComposeView(activity).apply {
            setContent {
                MainActivityHost()
            }
        }
        val fragmentHost = FrameLayout(activity).apply {
            id = R.id.reader_fragment
        }

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        root.addView(composeShell, layoutParams)
        root.addView(
            fragmentHost,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        activity.setContentView(root)
    }
}

@Composable
fun MainActivityHost() {
    ETipitakaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )
    }
}
