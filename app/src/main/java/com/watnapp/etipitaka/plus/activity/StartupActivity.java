package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.koushikdutta.ion.Ion;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.UnzipUtility;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.databinding.ActivityStartupBinding;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.helper.BulkDownloadDatabasesKt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 14/7/2013
 * Time: 8:14
 */

public class StartupActivity extends AppCompatActivity
{
  private static final String TAG = "StartupActivity";
  private static final String STARTUP_PREFS = "startup";
  private static final String KEY_BULK_DOWNLOAD_CHECK_DONE = "bulk_download_check_done";
  private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
  private ActivityStartupBinding binding;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WebView.setWebContentsDebuggingEnabled(true);
    binding = ActivityStartupBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);
    startApp();
  }

  private void startApp() {
    moveOldDataFiles().continueWithTask(task -> {
      return unzipBundleDatabase();
    }).continueWithTask(task -> {
      return updateDatabasesInfo();
    }).continueWithTask(task -> {
      return offerBulkDownload();
    }).continueWithTask((Continuation<Void, Task<Boolean>>) task -> {
      startActivity(new Intent(StartupActivity.this, MainActivity.class));
      finish();
      return null;
    });
  }

  /**
   * Languages whose database is shipped on demand (everything except the
   * bundled THAI + PALI). On first launch this set is also the candidate
   * pool for {@link #offerBulkDownload()}.
   */
  private List<Language> getDownloadableLanguages() {
    List<Language> result = new ArrayList<>();
    for (Language language : Language.values()) {
      if (language == Language.THAI || language == Language.PALI) {
        continue;
      }
      result.add(language);
    }
    return result;
  }

  private List<Language> getMissingDatabases() {
    List<Language> missing = new ArrayList<>();
    for (Language language : getDownloadableLanguages()) {
      File f = new File(Utils.getDatabasePath(this, language));
      if (!f.exists()) {
        missing.add(language);
      }
    }
    return missing;
  }

  /**
   * On the first online launch where at least one downloadable database
   * is missing, prompt the user to bulk-download them. The user's answer
   * (yes or no) is persisted so the prompt never reappears. Offline
   * first-launches skip silently so the prompt re-evaluates on the next
   * online launch.
   */
  private Task<Void> offerBulkDownload() {
    final TaskCompletionSource<Void> source = new TaskCompletionSource<>();

    SharedPreferences prefs = getSharedPreferences(STARTUP_PREFS, Context.MODE_PRIVATE);
    if (prefs.getBoolean(KEY_BULK_DOWNLOAD_CHECK_DONE, false)) {
      source.setResult(null);
      return source.getTask();
    }

    final List<Language> missing = getMissingDatabases();
    if (missing.isEmpty()) {
      // Nothing to ask. Don't even mark the flag — if the user later
      // deletes data and re-launches with missing DBs, this prompt
      // should still fire.
      source.setResult(null);
      return source.getTask();
    }

    if (!Utils.isNetworkConnected(this)) {
      // Offline: skip silently without burning the flag, so we re-ask
      // next time the user is online.
      source.setResult(null);
      return source.getTask();
    }

    StringBuilder list = new StringBuilder();
    for (int i = 0; i < missing.size(); ++i) {
      list.append("• ").append(missing.get(i).getFullName(this));
      if (i < missing.size() - 1) {
        list.append('\n');
      }
    }
    String message = getString(R.string.bulk_download_message, list.toString());

    new AlertDialog.Builder(this)
        .setTitle(R.string.bulk_download_title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(R.string.bulk_download_yes, (dialog, which) -> {
          prefs.edit().putBoolean(KEY_BULK_DOWNLOAD_CHECK_DONE, true).apply();
          runBulkDownload(missing, source);
        })
        .setNegativeButton(R.string.bulk_download_no, (dialog, which) -> {
          prefs.edit().putBoolean(KEY_BULK_DOWNLOAD_CHECK_DONE, true).apply();
          source.setResult(null);
        })
        .show();

    return source.getTask();
  }

  private void runBulkDownload(List<Language> languages, TaskCompletionSource<Void> source) {
    binding.progressBar.setVisibility(View.GONE);
    BulkDownloadDatabasesKt.runBulkDownload(
        this,
        languages,
        binding.downloadProgressBar,
        binding.downloadProgressLabel,
        result -> {
          binding.progressBar.setVisibility(View.VISIBLE);
          if (!isFinishing() && !isDestroyed()) {
            Toast toast = Toast.makeText(
                this,
                getString(R.string.bulk_download_summary,
                    result.getSucceeded(), result.getTotal()),
                Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
          }
          source.setResult(null);
          return kotlin.Unit.INSTANCE;
        });
  }

  private void clearCache() {
    try {
      File dir = this.getCacheDir();
      if (dir != null && dir.isDirectory()) {
        deleteRecursive(dir);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {
    clearCache();
    backgroundExecutor.shutdownNow();
    super.onDestroy();
  }

  private bolts.Task<Void> moveOldDataFiles() {
    final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
    backgroundExecutor.execute(() -> {
      source.setResult(null);
    });
    return source.getTask();
  }

  private void deleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      for (File child : fileOrDirectory.listFiles()) {
        deleteRecursive(child);
      }
    }
    fileOrDirectory.delete();
  }

  private bolts.Task<Void> updateDatabasesInfo() {
    final TaskCompletionSource<Void> source = new TaskCompletionSource<>();

    if (!Utils.isNetworkConnected(this)) {
      source.setResult(null);
      return source.getTask();
    }

    Ion.with(this)
        .load(Constants.UPDATE_URL)
        .asJsonObject()
        .setCallback((e, jsonObject) -> {
          if (jsonObject != null) {
            Log.d(TAG, jsonObject.toString());
            SharedPreferences prefs = getSharedPreferences("update", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("file", jsonObject.get("file").getAsString());
            for (Language language : Language.values()) {
              String code = language.getStringCode();
              editor.putInt(code, jsonObject.get(code).getAsInt());
            }
            editor.apply();
            source.setResult(null);
          } else {
            if (e != null) {
              Log.e(TAG, "Unable to update database metadata", e);
              source.setError(e);
            } else {
              source.setResult(null);
            }
          }
        });
    return source.getTask();
  }

  private bolts.Task<String> unzipBundleDatabase() {
    final TaskCompletionSource<String> source = new TaskCompletionSource<>();
    backgroundExecutor.execute(() -> {
      try {
        String outFileName = new File(Utils.getDatabaseDirectory(StartupActivity.this),
            Constants.DATABASE_ZIP_FILE).toString();
        File thaiDbFile = new File(Utils.getDatabasePath(StartupActivity.this, Language.THAI));
        if (!thaiDbFile.exists()) {
          InputStream myInput = getAssets().open(Constants.DATABASE_ASSETS_PATH);
          OutputStream myOutput = new FileOutputStream(outFileName);

          //transfer bytes from the inputfile to the outputfile
          byte[] buffer = new byte[1024];
          int length;
          while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
          }

          //Close the streams
          myOutput.flush();
          myOutput.close();
          myInput.close();

          UnzipUtility.unzip(outFileName, Utils.getDatabaseDirectory(StartupActivity.this));

          source.setResult(outFileName);
        } else {
          source.setResult(null);
        }

      } catch (IOException e) {
        source.setError(e);
        e.printStackTrace();
      }
    });

    return source.getTask();
  }
}
