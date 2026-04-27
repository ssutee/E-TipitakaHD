package com.watnapp.etipitaka.plus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.koushikdutta.ion.Ion;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.UnzipUtility;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.databinding.ActivityStartupBinding;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    }).continueWithTask((Continuation<Void, Task<Boolean>>) task -> {
      startActivity(new Intent(StartupActivity.this, MainActivity.class));
      finish();
      return null;
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
