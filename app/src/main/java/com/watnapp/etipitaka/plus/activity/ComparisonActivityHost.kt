package com.watnapp.etipitaka.plus.activity

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme

object ComparisonActivityContentBridge {
    @JvmStatic
    fun render(activity: ComponentActivity) {
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setBaselineAligned(false)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        val leftHost = FrameLayout(activity).apply {
            id = R.id.left_reader_fragment
        }
        val divider = ComposeView(activity).apply {
            setContent {
                ComparisonDivider()
            }
        }
        val rightHost = FrameLayout(activity).apply {
            id = R.id.right_reader_fragment
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        root.addView(
            leftHost,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f),
        )
        root.addView(
            divider,
            LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT),
        )
        root.addView(
            rightHost,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f),
        )

        activity.setContentView(root)
        ViewCompat.requestApplyInsets(root)
    }
}

@Composable
private fun ComparisonDivider() {
    ETipitakaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}
