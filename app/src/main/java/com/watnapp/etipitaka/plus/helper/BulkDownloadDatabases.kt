package com.watnapp.etipitaka.plus.helper

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class BulkDownloadResult(
    val total: Int,
    val succeeded: Int,
    val failed: List<Language>
)

/**
 * Sequentially download each missing-database language by reusing the
 * existing per-language [download] helper. Each per-file download awaits
 * the next via [suspendCoroutine] so one progress bar can be reused.
 *
 * Failures do not stop the loop — they are accumulated in
 * [BulkDownloadResult.failed] and the user sees a final summary toast.
 */
suspend fun bulkDownload(
    activity: Activity,
    languages: List<Language>,
    progressBar: ProgressBar,
    progressLabel: TextView
): BulkDownloadResult {
    var succeeded = 0
    val failed = mutableListOf<Language>()
    val total = languages.size

    for ((index, language) in languages.withIndex()) {
        if (activity.isFinishing || activity.isDestroyed) {
            // Activity went away mid-batch (back-press, system kill). Stop
            // dispatching further work into a dead Activity.
            break
        }

        activity.runOnUiThread {
            progressLabel.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            progressLabel.text = activity.getString(
                R.string.bulk_download_progress,
                index + 1,
                total,
                language.getFullName(activity)
            )
        }

        val success = suspendCoroutine<Boolean> { cont ->
            download(activity, language, progressBar) { result ->
                cont.resume(result)
            }
        }

        if (success) succeeded++ else failed.add(language)
    }

    activity.runOnUiThread {
        progressLabel.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    return BulkDownloadResult(total = total, succeeded = succeeded, failed = failed)
}

/**
 * Java-friendly entry point. Spawns the suspending [bulkDownload] on
 * [GlobalScope] and dispatches [onFinish] back to the main thread when
 * complete. StartupActivity stays in plain Java + bolts.Task land.
 */
@OptIn(DelicateCoroutinesApi::class)
fun runBulkDownload(
    activity: Activity,
    languages: List<Language>,
    progressBar: ProgressBar,
    progressLabel: TextView,
    onFinish: (BulkDownloadResult) -> Unit
) {
    GlobalScope.launch {
        val result = bulkDownload(activity, languages, progressBar, progressLabel)
        activity.runOnUiThread { onFinish(result) }
    }
}

