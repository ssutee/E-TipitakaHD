package com.watnapp.etipitaka

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.fragment.MenuTabs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression test for the Play Console crash:
 *
 *   IllegalArgumentException at ConstraintsKt.invalidConstraint
 *   ... at TabRowKt$TabRowWithSubcomposeImpl (TabRow.kt:959)
 *   ... at CustomViewBehind.onMeasure (CustomViewBehind.java:143)
 *
 * SlidingMenu's CustomViewBehind measures the menu ComposeView with an
 * UNSPECIFIED width during RelativeLayout's pre-measure pass (its own width
 * is still 0, so width - behindOffset goes negative and falls through
 * ViewGroup.getChildMeasureSpec to UNSPECIFIED). Compose turns that into
 * maxWidth = Infinity, and Material3 TabRow divides Infinity by the tab
 * count and feeds ~536M px into maxIntrinsicHeight, which cannot be encoded
 * in Constraints -> crash during measure.
 *
 * horizontalScroll reproduces the same unbounded-width measurement here:
 * without the fix, setContent throws IllegalArgumentException.
 *
 * NOTE: must run on API <= 33 (Compose test lib incompatible with 34+).
 */
@RunWith(AndroidJUnit4::class)
class MenuTabsInstrumentedTest {

  @get:Rule
  val composeRule = createComposeRule()

  private fun str(id: Int): String =
      InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

  @Test
  fun menuTabs_unboundedWidth_rendersAllTabs() {
    composeRule.setContent {
      Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        MenuTabs(selectedIndex = 0, onTabSelected = {})
      }
    }

    composeRule.onNodeWithText(str(R.string.volume)).assertExists()
    composeRule.onNodeWithText(str(R.string.search)).assertExists()
    composeRule.onNodeWithText(str(R.string.history)).assertExists()
    composeRule.onNodeWithText(str(R.string.favorite)).assertExists()
  }

  @Test
  fun menuTabs_boundedWidth_rendersAllTabs() {
    composeRule.setContent {
      MenuTabs(selectedIndex = 0, onTabSelected = {})
    }

    composeRule.onNodeWithText(str(R.string.volume)).assertExists()
    composeRule.onNodeWithText(str(R.string.search)).assertExists()
    composeRule.onNodeWithText(str(R.string.history)).assertExists()
    composeRule.onNodeWithText(str(R.string.favorite)).assertExists()
  }
}
