package com.watnapp.etipitaka

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.watnapp.etipitaka.plus.TestFragmentHostActivity
import com.watnapp.etipitaka.plus.fragment.FavoriteFragment
import com.watnapp.etipitaka.plus.fragment.HistoryFragment
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.vm.SharedViewModel
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression test for the Play Console crash:
 *
 *   IllegalStateException: Can't access ViewModels from detached fragment
 *   at LoaderManager.getInstance
 *   at HistoryFragment.lambda$onViewCreated$0 (HistoryFragment.java:92)
 *   at android.os.Handler.handleCallback
 *
 * The SharedViewModel language observer posts a restartLoader runnable via
 * composeView.post. The runnable lands on the main-thread queue, so it can
 * run a frame AFTER the fragment was detached (tab switched / menu closed),
 * where LoaderManager.getInstance(fragment) throws.
 *
 * Repro: attach the fragment, fire the language observer (queues the posted
 * runnable), detach the fragment in the same main-thread block, then drain
 * the main queue. Without the isAdded() guard the runnable throws and kills
 * the process; with it the runnable is a no-op.
 */
@RunWith(AndroidJUnit4::class)
class DetachedFragmentLoaderInstrumentedTest {

  private fun fireLanguageObserverThenDetach(fragment: Fragment) {
    ActivityScenario.launch(TestFragmentHostActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()
        // Observer fires synchronously on the main thread and posts the
        // restartLoader runnable onto the main queue.
        ViewModelProvider(activity)[SharedViewModel::class.java]
            .select(BookDatabaseHelper.Language.THAI)
        // Detach before that runnable gets to run.
        activity.supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commitNow()
      }
      // Drain the main queue — the posted runnable executes here, against
      // the now-detached fragment. Pre-fix: process crash.
      InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
  }

  @Test
  fun historyFragment_languageChangeAfterDetach_doesNotCrash() {
    fireLanguageObserverThenDetach(HistoryFragment())
  }

  @Test
  fun favoriteFragment_languageChangeAfterDetach_doesNotCrash() {
    fireLanguageObserverThenDetach(FavoriteFragment())
  }
}
