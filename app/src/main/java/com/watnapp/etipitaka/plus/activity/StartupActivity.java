package com.watnapp.etipitaka.plus.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    implements  ActivityCompat.OnRequestPermissionsResultCallback {
  private static final String TAG = "StartupActivity";
  private Handler mHandler = new Handler();
  private ActivityStartupBinding binding;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WebView.setWebContentsDebuggingEnabled(true);
    binding = ActivityStartupBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);
//    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
//      requestStorageAccessPermission();
//    } else {
      startApp();
//    }
  }

  private void startApp() {
    final ProgressDialog unzipDialog = new ProgressDialog(this);
    unzipDialog.setMessage(getString(R.string.uncompressing_database));
    unzipDialog.setCancelable(false);

    moveOldDataFiles().continueWithTask(task -> {
      runOnUiThread(unzipDialog::show);
      return unzipBundleDatabase();
    }).continueWithTask(task -> {
      runOnUiThread(unzipDialog::dismiss);
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
    super.onDestroy();
  }

  private bolts.Task<Void> moveOldDataFiles() {
    final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
    AsyncTask.execute(() -> {
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
            e.printStackTrace();
            source.setError(e);
          }
        });
    return source.getTask();
  }

  private bolts.Task<String> unzipBundleDatabase() {
    final TaskCompletionSource<String> source = new TaskCompletionSource<>();
    AsyncTask.execute(() -> {
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

  private static final int REQUEST_CODE_ACCESS_EXTERNAL_STORAGE_PERMISSION = 1;

  /**
   * Requests the {@link Manifest.permission#CAMERA} permission.
   * If an additional rationale should be displayed, the user has to launch the request from
   * a SnackBar that includes additional information.
   */
  private void requestStorageAccessPermission() {
    // Permission has not been granted and must be requested
    int writeExternalStoragePermission =
            ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
    int readExternalStoragePermission =
            ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
    if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED ||
            readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(StartupActivity.this,
          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  Manifest.permission.READ_EXTERNAL_STORAGE},
              REQUEST_CODE_ACCESS_EXTERNAL_STORAGE_PERMISSION);
    } else {
      startApp();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_CODE_ACCESS_EXTERNAL_STORAGE_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        startApp();
      } else {
        Toast.makeText(this,
                "The app was not allowed to write to your storage. " +
                        "Hence, it cannot function properly. Please consider granting it this permission",
                Toast.LENGTH_LONG).show();
        finish();
      }
    }
  }
}