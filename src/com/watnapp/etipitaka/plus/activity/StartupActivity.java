package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import bolts.Continuation;
import bolts.Task;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.UnzipUtility;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.helper.FileDownloader;
import roboguice.inject.ContentView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 14/7/2013
 * Time: 8:14
 */

@ContentView(R.layout.activity_startup)
public class StartupActivity extends RoboSherlockFragmentActivity {
  private static final String TAG = "StartupActivity";
  private Handler mHandler = new Handler();

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final long minimumSpace = 800000000l;

    Log.d(TAG, Utils.getDatabaseDirectory());

    long freeMemory = new File(this.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
    Log.d(TAG, String.format("free = %d", freeMemory));

    final ProgressDialog unzipDialog = new ProgressDialog(this);
    unzipDialog.setMessage(getString(R.string.uncompressing_database));
    unzipDialog.setCancelable(false);

    final ProgressDialog downloadDialog = new ProgressDialog(this);
    downloadDialog.setCancelable(false);
    downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    downloadDialog.setMax(100);
    downloadDialog.setMessage(getString(R.string.downloading_database));

    final bolts.Capture<String> host = new bolts.Capture<String>(Constants.THAI_HOST);
    if (freeMemory < minimumSpace && !new File(Utils.getDatabasePath(Language.THAI)).exists()) {
      new AlertDialog.Builder(this)
          .setTitle(R.string.no_space_left)
          .setMessage(getString(R.string.space_error,
              Utils.bytesToHuman(minimumSpace), Utils.bytesToHuman(freeMemory)))
          .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              finish();
            }
          }).create().show();
    } else {
      moveOldDataFiles().continueWithTask(new Continuation<Void, Task<Void>>() {
        @Override
        public Task<Void> then(Task<Void> task) throws Exception {
          Log.d(TAG, "moveOldDataFiles finished");
          return updateDatabasesInfo();
        }
      }).continueWithTask(new Continuation<Void, Task<Boolean>>() {
        @Override
        public Task<Boolean> then(Task<Void> task) throws Exception {
          Log.d(TAG, "updateDatabasesInfo finished");
          return isThaiClient();
        }
      }).continueWithTask(new Continuation<Boolean, Task<Boolean>>() {
        @Override
        public Task<Boolean> then(Task<Boolean> task) throws Exception {
          if (!task.isFaulted() && !task.getResult()) {
            host.set(Constants.S3_HOST);
          }
          Log.d(TAG, "isThaiClient finished");
          return downloadDatabases(host.get(), downloadDialog, unzipDialog);
        }
      }).continueWithTask(new Continuation<Boolean, Task<Boolean>>() {
        @Override
        public Task<Boolean> then(Task<Boolean> result) throws Exception {
          Task<Boolean> task = Task.forResult(null);
          for (final Language code : new Language[]
              {Language.THAI, Language.PALI, Language.THAIMM, Language.THAIMC,
                  Language.THAIWN, Language.THAIBT, Language.THAIPB,
                  Language.ROMANCT, Language.THAIVN, Language.THAIMC2,
                  Language.PALINEW} ) {
            task = task.continueWithTask(new Continuation<Boolean, Task<Boolean>>() {
              @Override
              public Task<Boolean> then(Task<Boolean> ignored) throws Exception {
                return updateDatabase(code, host.get(), downloadDialog, unzipDialog);
              }
            });
          }
          return task;
        }
      }).continueWith(new Continuation<Boolean, Object>() {
        @Override
        public Object then(Task<Boolean> task) throws Exception {
          checkDatabases(new Runnable() {
            @Override
            public void run() {
              errorExit(null);
            }
          });
          dismissDialog(downloadDialog);
          dismissDialog(unzipDialog);
          return null;
        }
      });

    }
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

  private void deleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
      for (File child : fileOrDirectory.listFiles()) {
        deleteRecursive(child);
      }
    }
    fileOrDirectory.delete();
  }

  private bolts.Task<Void> moveOldDataFiles() {
    final Task<Void>.TaskCompletionSource source = Task.create();

    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        File oldFilesDir = new File(Environment.getExternalStorageDirectory().getPath() + "/ETPK");
        if (oldFilesDir.exists()) {
          for (File file : oldFilesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
              return filename.endsWith(".db");
            }
          })) {
            File newFile = new File(Utils.getDatabaseDirectory() + "/" + file.getName());
            if (!newFile.exists()) {
              try {
                Files.move(file, newFile);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
          deleteRecursive(oldFilesDir);
        }
        source.setResult(null);
      }
    });

    return source.getTask();
  }

  private bolts.Task<Void> updateDatabasesInfo() {
    final Task<Void>.TaskCompletionSource source = Task.create();

    if (!isNetworkConnected()) {
      source.setResult(null);
      return source.getTask();
    }

    Ion.with(this)
        .load(Constants.UPDATE_URL)
        .asJsonObject()
        .setCallback(new FutureCallback<JsonObject>() {
          @Override
          public void onCompleted(Exception e, JsonObject jsonObject) {
            if (jsonObject != null) {
              Log.d(TAG, jsonObject.toString());
              SharedPreferences prefs = getSharedPreferences("update", Context.MODE_PRIVATE);
              SharedPreferences.Editor editor = prefs.edit();
              editor.putString("file", jsonObject.get("file").getAsString());
              editor.putInt("thai", jsonObject.get("thai").getAsInt());
              editor.putInt("pali", jsonObject.get("pali").getAsInt());
              editor.putInt("thaimc", jsonObject.get("thaimc").getAsInt());
              editor.putInt("thaimm", jsonObject.get("thaimm").getAsInt());
              editor.putInt("thaibt", jsonObject.get("thaibt").getAsInt());
              editor.putInt("thaiwn", jsonObject.get("thaiwn").getAsInt());
              editor.putInt("thaipb", jsonObject.get("thaipb").getAsInt());
              editor.putInt("romanct", jsonObject.get("romanct").getAsInt());
              editor.putInt("thaivn", jsonObject.get("thaivn").getAsInt());
              editor.putInt("palinew", jsonObject.get("palinew").getAsInt());
              editor.putInt("thaimc2", jsonObject.get("thaimc2").getAsInt());
              editor.commit();
              source.setResult(null);
            } else {
              e.printStackTrace();
              source.setError(e);
            }
          }
        });
    return source.getTask();
  }

  private int getLocalDatabaseVersion(BookDatabaseHelper.Language language) {
    if (!new File(Utils.getDatabasePath(language)).exists()) {
      return 0;
    }
    SQLiteDatabase db = SQLiteDatabase.openDatabase(Utils.getDatabasePath(language), null, 0);
    Cursor cursor = db.rawQuery("pragma user_version", null);
    cursor.moveToFirst();
    int version = Integer.parseInt(cursor.getString(0));
    cursor.close();
    db.close();
    return version == 0 ? 1 : version;
  }

  private bolts.Task<Integer> getLocalDatabaseVersionTask(final Language language) {
    Log.d(TAG, "getLocalDatabaseVersionTask");
    final Task<Integer>.TaskCompletionSource source = Task.create();
    new Thread(new Runnable() {
      @Override
      public void run() {
        source.setResult(getLocalDatabaseVersion(language));
      }
    }).start();
    return source.getTask();
  }

  private void checkDatabases(final Runnable runnableOnSuccess, final Runnable runnableOnFail) {
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        if (new File(Utils.getDatabasePath(Language.THAI)).exists()
            && new File(Utils.getDatabasePath(Language.PALI)).exists()
            && new File(Utils.getDatabasePath(Language.PALINEW)).exists()
            && new File(Utils.getDatabasePath(Language.THAIMC)).exists()
            && new File(Utils.getDatabasePath(Language.THAIMC2)).exists()
            && new File(Utils.getDatabasePath(Language.THAIMM)).exists()
            && new File(Utils.getDatabasePath(Language.THAIBT)).exists()
            && new File(Utils.getDatabasePath(Language.THAIWN)).exists()
            && new File(Utils.getDatabasePath(Language.THAIPB)).exists()
            && new File(Utils.getDatabasePath(Language.ROMANCT)).exists()
            && new File(Utils.getDatabasePath(Language.THAIVN)).exists()
            && getLocalDatabaseVersion(Language.THAI) == getRemoteDatabaseVersion("thai")
            && getLocalDatabaseVersion(Language.PALI) == getRemoteDatabaseVersion("pali")
            && getLocalDatabaseVersion(Language.PALINEW) == getRemoteDatabaseVersion("palinew")
            && getLocalDatabaseVersion(Language.THAIMC) == getRemoteDatabaseVersion("thaimc")
            && getLocalDatabaseVersion(Language.THAIMC2) == getRemoteDatabaseVersion("thaimc2")
            && getLocalDatabaseVersion(Language.THAIMM) == getRemoteDatabaseVersion("thaimm")
            && getLocalDatabaseVersion(Language.ROMANCT) == getRemoteDatabaseVersion("romanct")
            && getLocalDatabaseVersion(Language.THAIBT) == getRemoteDatabaseVersion("thaibt")
            && getLocalDatabaseVersion(Language.THAIWN) == getRemoteDatabaseVersion("thaiwn")
            && getLocalDatabaseVersion(Language.THAIPB) == getRemoteDatabaseVersion("thaipb")
            && getLocalDatabaseVersion(Language.THAIVN) == getRemoteDatabaseVersion("thaivn")) {
          mHandler.post(runnableOnSuccess);
        } else {
          mHandler.post(runnableOnFail);
        }
      }
    });
  }

  private void errorExit(Exception e) {
    Toast.makeText(StartupActivity.this, R.string.database_error, Toast.LENGTH_SHORT).show();
    if (e != null) {
      Toast.makeText(StartupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
    finish();
  }

  private void checkDatabases(final Runnable runnableOnFail) {
    checkDatabases(new Runnable() {
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

  private String getDatabaseFile() {
    return getSharedPreferences("update", Context.MODE_PRIVATE).getString("file", "etipitaka_plus.zip");
  }

  private int getRemoteDatabaseVersion(String code) {
    return getSharedPreferences("update", Context.MODE_PRIVATE).getInt(code, 0);
  }

  private bolts.Task<Boolean> isThaiClient() {
    final Task<Boolean>.TaskCompletionSource source = Task.create();

    if (!isNetworkConnected()) {
      source.setResult(true);
      return source.getTask();
    }

    Ion.with(this)
        .load(Constants.GEO_API)
        .asJsonObject()
        .setCallback(new FutureCallback<JsonObject>() {
          @Override
          public void onCompleted(Exception e, JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.get("geoplugin_countryCode") == null ||
                jsonObject.get("geoplugin_countryCode").getAsString().equals("TH")) {
              source.setResult(true);
            } else {
              source.setResult(false);
            }
          }
        });
    return source.getTask();
  }

  private bolts.Task<Boolean> downloadDatabases(final String host,
                                                final ProgressDialog downloadDialog,
                                                final ProgressDialog unzipDialog) {
    final Task<Boolean>.TaskCompletionSource source = Task.create();
    if (isNetworkConnected() && !new File(Utils.getDatabasePath(Language.THAI)).exists()) {
      Log.d(TAG, "downloadDatabases");
      showDialog(downloadDialog);
      downloadDatabase(getDatabaseFile(), host, new OnDownloadProgressListener() {
        @Override
        public void onDownloadProgress(int progress) {
          setDialogProgress(downloadDialog, progress);
        }
      }).continueWithTask(new Continuation<String, Task<String>>() {
            @Override
            public Task<String> then(Task<String> task) throws Exception {
              hideDialog(downloadDialog);
              showDialog(unzipDialog);
              return unzipDatabase(task.getResult());
            }
          }).continueWith(new Continuation<String, Void>() {
        @Override
        public Void then(Task<String> task) throws Exception {
          hideDialog(unzipDialog);
          source.setResult(true);
          return null;
        }
      });
    } else {
      source.setResult(false);
    }
    return source.getTask();
  }

  private void showDialog(final ProgressDialog dialog) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        dialog.show();
      }
    });
  }

  private void hideDialog(final ProgressDialog dialog) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        dialog.hide();
      }
    });
  }

  private void dismissDialog(final ProgressDialog dialog) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        dialog.dismiss();
      }
    });
  }

  private void setDialogProgress(final ProgressDialog dialog, final int progress) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        dialog.setProgress(progress);
      }
    });
  }

  private bolts.Task<Boolean> updateDatabase(final Language language, final String host,
                                             final ProgressDialog downloadDialog,
                                             final ProgressDialog unzipDialog) {
    final Task<Boolean>.TaskCompletionSource source = Task.create();

    if (!isNetworkConnected()) {
      source.setResult(false);
      return source.getTask();
    }

    final int remoteVersion = getRemoteDatabaseVersion(language.getStringCode());

    Log.d(TAG, "completed = " + source.getTask().isCompleted());
    Log.d(TAG, "update: " + language.getStringCode());

    getLocalDatabaseVersionTask(language)
        .continueWithTask(new Continuation<Integer, Task<String>>() {
          @Override
          public Task<String> then(Task<Integer> task) throws Exception {
            Log.d(TAG, "version = " + task.getResult());
            if (new File(Utils.getDatabasePath(language)).exists() &&
                task.getResult() == remoteVersion) {
              return null;
            } else {
              setDialogProgress(downloadDialog, 0);
              showDialog(downloadDialog);
              return downloadDatabase(language, host, new OnDownloadProgressListener() {
                @Override
                public void onDownloadProgress(int progress) {
                  setDialogProgress(downloadDialog, progress);
                }
              });
            }
          }
        })
        .continueWithTask(new Continuation<String, Task<String>>() {
          @Override
          public Task<String> then(Task<String> task) throws Exception {
            hideDialog(downloadDialog);
            if (task.isFaulted()) {
              errorExit(task.getError());
              return null;
            } else {
              Log.d(TAG, "start unzip");
              Log.d(TAG, task.getResult());
              showDialog(unzipDialog);
              return unzipDatabase(task.getResult());
            }
          }
        })
        .continueWith(new Continuation<String, Void>() {
          @Override
          public Void then(Task<String> task) throws Exception {
            Log.d(TAG, "update: finish");
            source.setResult(true);
            hideDialog(unzipDialog);
            return null;
          }
        });

    return source.getTask();
  }

  private bolts.Task<String> unzipDatabase(final String path) {
    final Task<String>.TaskCompletionSource source = Task.create();
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        try {
          UnzipUtility.unzip(path, Utils.getDatabaseDirectory());
          File zipFile = new File(path);
          if (zipFile.exists()) {
            zipFile.delete();
          }
          source.setResult(path);
        } catch (final IOException e) {
          source.setError(e);
        }
      }
    });

    return source.getTask();
  }

  private interface OnDownloadProgressListener {
    void onDownloadProgress(int progress);
  }

  private bolts.Task<String> downloadDatabase(final String filename, final String host,
                                              final OnDownloadProgressListener listener) {
    final Task<String>.TaskCompletionSource source = Task.create();
    String url = host + "/" + filename;
    final String path = Utils.getDatabaseDirectory() + "/" + filename;
    File zipFile = new File(path);
    if (zipFile.exists()) {
      zipFile.delete();
    }
    Log.d(TAG, "download url : " + url);
    FileDownloader fileDownloader = new FileDownloader();
    fileDownloader.setOnFileDownloadListener(new FileDownloader.OnFileDownloadListener() {
      @Override
      public void onTotalFileSizeChange(FileDownloader downloader, String url, String path, int fileId, long size) {

      }

      @Override
      public void onProgressUpdate(FileDownloader downloader, String url, String path, int fileId, int progress) {
        listener.onDownloadProgress(progress);
      }

      @Override
      public void onDownloadingFinish(FileDownloader downloader, int fileId, boolean success) {
        Log.d(TAG, "set result = " + path);
        source.setResult(path);
      }

      @Override
      public void onDownloadingFinishWithError(FileDownloader downloader, int fileId, int errorResMessage) {
        source.setError(new Exception("response: " + errorResMessage));
      }
    });
    fileDownloader.startDownload(null, url, path, 1);

    return source.getTask();
  }

  private bolts.Task<String> downloadDatabase(Language language, final String host,
                                              final OnDownloadProgressListener listener) {
    String filename = String.format("%s.zip", language.getStringCode());
    Log.d(TAG, filename);
    return downloadDatabase(filename, host, listener);
  }

}