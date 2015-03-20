package com.watnapp.etipitaka.plus.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.UnzipUtility;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.FileDownloader;
import roboguice.inject.ContentView;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 14/7/2013
 * Time: 8:14
 */

@ContentView(R.layout.activity_startup)
public class StartupActivity extends RoboSherlockFragmentActivity implements FileDownloader.OnFileDownloadListener {
  private static final String TAG = "StartupActivity";
  private Handler mHandler = new Handler();
  private ProgressDialog mProgressDialog;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setCancelable(false);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    mProgressDialog.setMax(100);
    mProgressDialog.setMessage(getString(R.string.downloading_database));

    checkSumDatabase(new Runnable() {
      @Override
      public void run() {
        downloadDatabase();
      }
    });
  }

  private int getThaiMCDatabaseVersion() {
    SQLiteDatabase db = SQLiteDatabase.openDatabase(Constants.MC_DATABASE_PATH, null, 0);
    Cursor cursor = db.rawQuery("pragma user_version", null);
    cursor.moveToFirst();
    int version = Integer.parseInt(cursor.getString(0));
    cursor.close();
    db.close();
    return version;
  }

  private int getRomanCTDatabaseVersion() {
    SQLiteDatabase db = SQLiteDatabase.openDatabase(Constants.CT_DATABASE_PATH, null, 0);
    Cursor cursor = db.rawQuery("pragma user_version", null);
    cursor.moveToFirst();
    int version = Integer.parseInt(cursor.getString(0));
    cursor.close();
    db.close();
    return version;
  }

  private int getThaiMMDatabaseVersion() {
    SQLiteDatabase db = SQLiteDatabase.openDatabase(Constants.MM_DATABASE_PATH, null, 0);
    Cursor cursor = db.rawQuery("pragma user_version", null);
    cursor.moveToFirst();
    int version = Integer.parseInt(cursor.getString(0));
    cursor.close();
    db.close();
    return version;
  }

  private void checkSumDatabase(final Runnable runnableOnSuccess, final Runnable runnableOnFail) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (new File(Constants.DATABASE_PATH).length()==Constants.DATABASE_SIZE
            && new File(Constants.MC_DATABASE_PATH).exists()
            && new File(Constants.MM_DATABASE_PATH).exists()
            && new File(Constants.BT_DATABASE_PATH).exists()
            && new File(Constants.WN_DATABASE_PATH).exists()
            && new File(Constants.PB_DATABASE_PATH).exists()
            && new File(Constants.CT_DATABASE_PATH).exists()
            && getThaiMCDatabaseVersion() == 4
            && getThaiMMDatabaseVersion() == 3
            && getRomanCTDatabaseVersion() == 2) {
          mHandler.post(runnableOnSuccess);
        } else {
          mHandler.post(runnableOnFail);
        }
    }
    }).start();
  }

  private void errorExit() {
    Toast.makeText(StartupActivity.this, R.string.database_error, Toast.LENGTH_SHORT).show();
    finish();
  }

  private void checkSumDatabase(final Runnable runnableOnFail) {
    checkSumDatabase(new Runnable() {
      @Override
      public void run() {
        startActivity(new Intent(StartupActivity.this, MainActivity.class));
        finish();
      }
    }, runnableOnFail);
  }

  private boolean isNetworkConnected() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    return !(ni == null);
  }

  private void downloadDatabase() {
    if (!new File(Constants.DATABASE_FOLDER).exists()) {
      new File(Constants.DATABASE_FOLDER).mkdirs();
    }

    if (isNetworkConnected()) {
      FileDownloader fileDownloader = new FileDownloader();
      fileDownloader.setOnFileDownloadListener(this);
      fileDownloader.startDownload(null, Constants.DATABASE_URL, Constants.DATABASE_ZIP_PATH, 1);
      mProgressDialog.setProgress(0);
      mProgressDialog.show();
    } else {
      Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void unzipDatabase() {
    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setMessage(getString(R.string.uncompressing_database));
    dialog.setCancelable(false);
    dialog.show();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          UnzipUtility.unzip(Constants.DATABASE_ZIP_PATH, Constants.DATABASE_FOLDER);
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              dialog.dismiss();
            }
          });
          checkSumDatabase(new Runnable() {
            @Override
            public void run() {
              dialog.dismiss();
              errorExit();
            }
          });
        } catch (IOException e) {
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              dialog.dismiss();
              errorExit();
            }
          });
          e.printStackTrace();
        }
      }
    }).start();
  }

  @Override
  public void onTotalFileSizeChange(FileDownloader downloader, String url, String path, int fileId, long size) {
    Log.d(TAG, size + "");
  }

  @Override
  public void onProgressUpdate(FileDownloader downloader, String url, String path, int fileId, int progress) {
    mProgressDialog.setProgress(progress);
  }

  @Override
  public void onDownloadingFinish(FileDownloader downloader, int fileId, boolean success) {
    mProgressDialog.dismiss();
    unzipDatabase();
  }

  @Override
  public void onDownloadingFinishWithError(FileDownloader downloader, int fileId, int errorResMessage) {
    errorExit();
  }
}