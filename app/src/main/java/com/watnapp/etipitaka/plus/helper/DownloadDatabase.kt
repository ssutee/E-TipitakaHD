package com.watnapp.etipitaka.plus.helper

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import com.koushikdutta.ion.Ion
import com.watnapp.etipitaka.plus.Constants
import com.watnapp.etipitaka.plus.R
import com.watnapp.etipitaka.plus.UnzipUtility
import com.watnapp.etipitaka.plus.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun getLocalDatabaseVersion(context: Context, language: BookDatabaseHelper.Language)
        : Int = suspendCoroutine { cont ->
    // Callers check File.exists() on the main thread, but this runs later on a
    // worker coroutine — the file can vanish in between (concurrent re-download
    // replacing it, external storage unmounted), and openDatabase then throws
    // SQLiteCantOpenDatabaseException which would kill the process from
    // GlobalScope. Treat any failure as version 0 ("unknown / not installed").
    val path = Utils.getDatabasePath(context, language)
    val version = try {
        if (!File(path).isFile) {
            0
        } else {
            SQLiteDatabase.openDatabase(path, null, 0).use { db ->
                db.rawQuery("pragma user_version", null).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0)?.toIntOrNull() ?: 0 else 0
                }
            }
        }
    } catch (e: Exception) {
        Log.w("UPDATE", "getLocalDatabaseVersion: cannot read $path", e)
        0
    }
    cont.resume(version)
}

suspend fun getCurrentDatabase(activity: Activity, language: BookDatabaseHelper.Language)
        : Int = suspendCoroutine{ cont ->
    val version = activity.getSharedPreferences("update", Context.MODE_PRIVATE).getInt(language.stringCode, 0)
    cont.resume(version)
}

suspend fun unzipDatabase(context: Context, path: String): Boolean = suspendCoroutine {cont ->
    try {
        UnzipUtility.unzip(path, Utils.getDatabaseDirectory(context))
        val zipFile = File(path)
        if (zipFile.exists()) {
            zipFile.delete()
        }
        cont.resume(true)
    } catch (e: IOException) {
        cont.resume(false)
    }
}

suspend fun isThaiClient(context: Context): Boolean
        = suspendCoroutine { cont ->
    Ion.with(context).load(Constants.GEO_API)
            .asJsonObject()
            .setCallback { e, result ->
                if (e != null || result == null
                        || result.get("geoplugin_countryCode") == null
                        || result.get("geoplugin_countryCode").asString == "TH") {
                    cont.resume(true)
                } else {
                    cont.resume(false)
                }
            }
}

suspend fun downloadDatabaseZipFile(activity: Activity, url: String, path: String, progressBar: ProgressBar)
        : Boolean = suspendCoroutine { cont ->
    val zipFile = File(path)
    if (zipFile.exists()) {
        zipFile.delete()
    }
    val fileDownloader = FileDownloader()
    fileDownloader.setOnFileDownloadListener(object : FileDownloader.OnFileDownloadListener {
        override fun onProgressUpdate(downloader: FileDownloader?, url: String?, path: String?, fileId: Int, progress: Int) {
            activity.runOnUiThread {
                progressBar.progress = progress
            }
        }

        override fun onDownloadingFinish(downloader: FileDownloader?, fileId: Int, success: Boolean) {
            cont.resume(true)
        }

        override fun onDownloadingFinishWithError(downloader: FileDownloader?, fileId: Int, errorResMessage: Int) {
            cont.resume(false)
        }

        override fun onTotalFileSizeChange(downloader: FileDownloader?, url: String?, path: String?, fileId: Int, size: Long) {
        }

    })
    fileDownloader.startDownload(null, url, path, 1)
}

@OptIn(DelicateCoroutinesApi::class)
fun update(activity: Activity,
           language: BookDatabaseHelper.Language,
           onCheckUpdateFinish : (needUpdate: Boolean) -> Unit) {
    GlobalScope.launch {
        // An exception escaping GlobalScope.launch has no parent scope to
        // handle it and crashes the process — resolve to "no update" instead.
        try {
            if (Utils.isNetworkConnected(activity)) {
                val localVersion = getLocalDatabaseVersion(activity, language)
                val currentVersion = getCurrentDatabase(activity, language)
                Log.d("UPDATE", language.stringCode)
                Log.d("UPDATE", "$localVersion : $currentVersion" )
                val ret = localVersion != currentVersion
                activity.runOnUiThread { onCheckUpdateFinish(ret) }
            } else {
                activity.runOnUiThread { onCheckUpdateFinish(false) }
            }
        } catch (e: Exception) {
            Log.e("UPDATE", "update check failed for ${language.stringCode}", e)
            activity.runOnUiThread { onCheckUpdateFinish(false) }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun download(activity: Activity,
             language: BookDatabaseHelper.Language,
             progressBar: ProgressBar,
             onDownloadFinish: (success: Boolean) -> Unit) {
    GlobalScope.launch {
        val host = if (isThaiClient(activity)) Constants.THAI_HOST else Constants.S3_HOST
        activity.runOnUiThread {
            val toast = Toast.makeText(activity, R.string.downloading, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0 ,0)
            toast.show()
            progressBar.isIndeterminate = false
            progressBar.visibility = View.VISIBLE
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        val filename = "${language.stringCode}.zip"
        val url = "$host/$filename"
        val path = Utils.getDatabaseDirectory(activity) + "/" + filename

        var result = downloadDatabaseZipFile(activity, url, path, progressBar)
        var messageId = if (result) R.string.download_complete else R.string.download_error

        activity.runOnUiThread {
            progressBar.isIndeterminate = true
        }

        if (result) {
            result = unzipDatabase(activity, path)
            messageId = if (result) R.string.download_complete else R.string.space_error
        }

        activity.runOnUiThread {
            progressBar.visibility = View.GONE
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            val toast = Toast.makeText(activity, messageId, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0,0)
            toast.show()
            onDownloadFinish(result)
        }
    }
}