package com.watnapp.etipitaka

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.watnapp.etipitaka.plus.Utils
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.helper.confirmAndDownloadDatabase
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The only deterministic slice of the compare→download→continue flow: when
 * offline, confirmAndDownloadDatabase must never reach the success
 * continuation. The full download path hits the live network and is
 * manual-QA'd (see the plan's manual-QA checklist).
 *
 * Runs against a real (empty) Activity via ActivityScenario, because the
 * helper needs an Activity context. Self-skips when the device is online or
 * the target DB is already present.
 */
@RunWith(AndroidJUnit4::class)
class CompareDownloadInstrumentedTest {

  private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun offline_doesNotInvokeContinuation() {
    // Only meaningful when actually offline AND the target DB is absent.
    assumeFalse("device is online — skipping offline test",
        Utils.isNetworkConnected(context))
    val dbPath = Utils.getDatabasePath(context, BookDatabaseHelper.Language.ROMANCT)
    assumeFalse("romanct.db present — skipping", File(dbPath).exists())

    val continued = AtomicBoolean(false)
    ActivityScenario.launch(TestHostActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        // onActivity runs on the main thread; the offline branch toasts and
        // returns synchronously without invoking the continuation.
        confirmAndDownloadDatabase(activity, BookDatabaseHelper.Language.ROMANCT) {
          continued.set(true)
        }
      }
    }
    assertFalse("continuation must not run while offline", continued.get())
  }
}
