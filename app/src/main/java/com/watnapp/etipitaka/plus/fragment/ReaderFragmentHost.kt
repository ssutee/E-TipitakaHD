package com.watnapp.etipitaka.plus.fragment

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.compose.ui.platform.ComposeView
import androidx.viewpager2.widget.ViewPager2
import com.watnapp.etipitaka.plus.R

class ReaderFragmentViews(
    @JvmField val root: View,
    @JvmField val txtSubtitle: ComposeView,
    @JvmField val viewpager: ViewPager2,
    @JvmField val seekbar: ComposeView,
    @JvmField val layoutButtons: ComposeView,
)

object ReaderFragmentContentBridge {
    @JvmStatic
    fun create(context: Context): ReaderFragmentViews {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        val subtitle = ComposeView(context).apply {
            id = R.id.txt_subtitle
        }
        val contentArea = RelativeLayout(context)
        val pagerLayer = FrameLayout(context)
        val viewPager = ViewPager2(context).apply {
            id = R.id.viewpager
        }
        val seekBar = ComposeView(context).apply {
            id = R.id.seekbar
            visibility = View.GONE
        }
        val bottomControls = ComposeView(context).apply {
            id = R.id.layout_buttons
        }

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ),
        )
        root.addView(
            contentArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            ),
        )

        contentArea.addView(
            pagerLayer,
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT,
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            },
        )
        pagerLayer.addView(
            viewPager,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        pagerLayer.addView(
            seekBar,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ),
        )
        contentArea.addView(
            bottomControls,
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                addRule(RelativeLayout.CENTER_HORIZONTAL)
            },
        )

        return ReaderFragmentViews(
            root = root,
            txtSubtitle = subtitle,
            viewpager = viewPager,
            seekbar = seekBar,
            layoutButtons = bottomControls,
        )
    }
}
