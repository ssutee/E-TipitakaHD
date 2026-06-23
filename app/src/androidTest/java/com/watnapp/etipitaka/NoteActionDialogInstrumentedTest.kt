package com.watnapp.etipitaka

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.fragment.FavoriteActionDialog
import com.watnapp.etipitaka.plus.model.Favorite
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression test for the note action dialog clipping its action items when
 * the note is long.
 *
 * The dialog put the (user-entered, unbounded) note text in the Material3
 * AlertDialog *title* slot. The title is not weighted/scrollable, so a long
 * note consumed the dialog height and squeezed the weighted text slot (the
 * action Column) until only the first action ("เปิดบันทึก") was visible —
 * "แก้ไขบันทึก" / "ลบ" / "สำคัญ" / "เรียงลำดับ" were clipped off-screen.
 *
 * With the fix (title bounded with maxLines + ellipsis) every action stays
 * visible regardless of note length.
 */
@RunWith(AndroidJUnit4::class)
class NoteActionDialogInstrumentedTest {

  @get:Rule
  val composeRule = createComposeRule()

  private fun str(id: Int): String =
      InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

  @Test
  fun longNote_allActionsRemainVisible() {
    // A note far taller than any dialog so the bug clips the action list.
    val longNote = (1..200).joinToString("\n") { "บันทึกบรรทัดที่ $it" }
    val favorite = Favorite().apply { setNote(longNote) }

    composeRule.setContent {
      FavoriteActionDialog(
          favorite = favorite,
          onDismiss = {},
          onOpen = {},
          onEdit = {},
          onDelete = {},
          onMark = {},
          onSort = {},
      )
    }

    // The last action is the one that gets clipped first. Assert every
    // action item is actually on screen.
    composeRule.onNodeWithText(str(R.string.open_note)).assertIsDisplayed()
    composeRule.onNodeWithText(str(R.string.edit_note)).assertIsDisplayed()
    composeRule.onNodeWithText(str(R.string.delete)).assertIsDisplayed()
    composeRule.onNodeWithText(str(R.string.mark)).assertIsDisplayed()
    composeRule.onNodeWithText(str(R.string.sorting)).assertIsDisplayed()

    // The note header itself must be visible (full text shown, not hidden).
    composeRule.onNodeWithText("บันทึกบรรทัดที่ 1", substring = true).assertIsDisplayed()
  }
}
