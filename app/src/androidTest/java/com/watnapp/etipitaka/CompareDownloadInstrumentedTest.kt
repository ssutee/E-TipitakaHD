package com.watnapp.etipitaka

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.watnapp.etipitaka.plus.Utils
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.helper.confirmAndDownloadDatabase
import org.junit.Assume.assumeFalse
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The only deterministic slice of the compare→download→continue flow: when
 * offline, confirmAndDownloadDatabase must never reach the success
 * continuation. The full download path hits the live network and is
 * manual-QA'd (see the plan's manual-QA checklist).
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
    val latch = CountDownLatch(1)
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      confirmAndDownloadDatabase(
          InstrumentationRegistry.getInstrumentation().context as android.app.Activity,
          BookDatabaseHelper.Language.ROMANCT
      ) {
        continued.set(true)
      }
      latch.countDown()
    }
    latch.await(2, TimeUnit.SECONDS)
    assertFalse("continuation must not run while offline", continued.get())
  }
}
