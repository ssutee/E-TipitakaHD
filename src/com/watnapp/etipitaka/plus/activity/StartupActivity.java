package com.watnapp.etipitaka.plus.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import bolts.Continuation;
import bolts.Task;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.android.vending.expansion.downloader.*;
import com.google.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.UnzipUtility;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.expansion.DownloaderService;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.helper.FileDownloader;
import roboguice.inject.ContentView;

import java.io.*;
import java.util.zip.CRC32;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 14/7/2013
 * Time: 8:14
 */

@ContentView(R.layout.activity_startup)
public class StartupActivity extends RoboSherlockFragmentActivity
    implements IDownloaderClient, ActivityCompat.OnRequestPermissionsResultCallback {
  private static final String TAG = "StartupActivity";
  private Handler mHandler = new Handler();

  private ProgressBar mPB;

  private TextView mStatusText;
  private TextView mProgressFraction;
  private TextView mProgressPercent;
  private TextView mAverageSpeed;
  private TextView mTimeRemaining;

  private View mDashboard;
  private View mCellMessage;

  private Button mPauseButton;
  private Button mWiFiSettingsButton;

  private boolean mStatePaused;
  private int mState;

  private IDownloaderService mRemoteService;

  private IStub mDownloaderClientStub;

  // region Expansion Downloader
  private static class XAPKFile {
    public final boolean mIsMain;
    public final int mFileVersion;
    public final long mFileSize;

    XAPKFile(boolean isMain, int fileVersion, long fileSize) {
      mIsMain = isMain;
      mFileVersion = fileVersion;
      mFileSize = fileSize;
    }
  }

  private void setState(int newState) {
    if (mState != newState) {
      mState = newState;
      mStatusText.setText(Helpers.getDownloaderStringResourceIDFromState(newState));
    }
  }

  private void setButtonPausedState(boolean paused) {
    mStatePaused = paused;
    int stringResourceID = paused ? R.string.text_button_resume :
        R.string.text_button_pause;
    mPauseButton.setText(stringResourceID);
  }

  private static final XAPKFile[] xAPKS = {
      new XAPKFile(
          true, // true signifies a main file
          3001, // the version of the APK that the file was uploaded against
          100913209L // the length of the file in bytes
      )
  };
  static private final float SMOOTHING_FACTOR = 0.005f;

  private boolean mCancelValidation;

  void validateXAPKZipFiles() {
    Log.d(TAG, "validateXAPKZipFiles");
    AsyncTask<Object, DownloadProgressInfo, Boolean> validationTask = new
        AsyncTask<Object, DownloadProgressInfo, Boolean>() {

          @Override
          protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            mDashboard.setVisibility(View.VISIBLE);
            mCellMessage.setVisibility(View.GONE);
            mStatusText.setText(R.string.text_verifying_download);
            mPauseButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                mCancelValidation = true;
              }
            });
            mPauseButton.setText(R.string.text_button_cancel_verify);
            super.onPreExecute();
          }

          @Override
          protected Boolean doInBackground(Object... params) {
            Log.d(TAG, "doInBackground");
            for (XAPKFile xf : xAPKS) {
              String fileName = Helpers.getExpansionAPKFileName(
                  StartupActivity.this,
                  xf.mIsMain, xf.mFileVersion);
              Log.d(TAG, "check file = " + fileName);
              if (!Helpers.doesFileExist(StartupActivity.this, fileName,
                  xf.mFileSize, false))
                return false;
              fileName = Helpers
                  .generateSaveFileName(StartupActivity.this, fileName);
              ZipResourceFile zrf;
              byte[] buf = new byte[1024 * 256];
              try {
                zrf = new ZipResourceFile(fileName);
                ZipResourceFile.ZipEntryRO[] entries = zrf.getAllEntries();
                /**
                 * First calculate the total compressed length
                 */
                long totalCompressedLength = 0;
                for (ZipResourceFile.ZipEntryRO entry : entries) {
                  totalCompressedLength += entry.mCompressedLength;
                }
                float averageVerifySpeed = 0;
                long totalBytesRemaining = totalCompressedLength;
                long timeRemaining;
                /**
                 * Then calculate a CRC for every file in the
                 * Zip file, comparing it to what is stored in
                 * the Zip directory. Note that for compressed
                 * Zip files we must extract the contents to do
                 * this comparison.
                 */
                for (ZipResourceFile.ZipEntryRO entry : entries) {
                  if (-1 != entry.mCRC32) {
                    long length = entry.mUncompressedLength;
                    CRC32 crc = new CRC32();
                    DataInputStream dis = null;
                    try {
                      dis = new DataInputStream(
                          zrf.getInputStream(entry.mFileName));

                      long startTime = SystemClock.uptimeMillis();
                      while (length > 0) {
                        int seek = (int) (length > buf.length ? buf.length
                            : length);
                        dis.readFully(buf, 0, seek);
                        crc.update(buf, 0, seek);
                        length -= seek;
                        long currentTime = SystemClock.uptimeMillis();
                        long timePassed = currentTime - startTime;
                        if (timePassed > 0) {
                          float currentSpeedSample = (float) seek
                              / (float) timePassed;
                          if (0 != averageVerifySpeed) {
                            averageVerifySpeed = SMOOTHING_FACTOR
                                * currentSpeedSample
                                + (1 - SMOOTHING_FACTOR)
                                * averageVerifySpeed;
                          } else {
                            averageVerifySpeed = currentSpeedSample;
                          }
                          totalBytesRemaining -= seek;
                          timeRemaining = (long) (totalBytesRemaining / averageVerifySpeed);
                          this.publishProgress(
                              new DownloadProgressInfo(
                                  totalCompressedLength,
                                  totalCompressedLength
                                      - totalBytesRemaining,
                                  timeRemaining,
                                  averageVerifySpeed)
                          );
                        }
                        startTime = currentTime;
                        if (mCancelValidation)
                          return true;
                      }
                      if (crc.getValue() != entry.mCRC32) {
                        Log.e(TAG, "CRC does not match for entry: " + entry.mFileName);
                        Log.e(TAG, "In file: " + entry.getZipFileName());
                        return false;
                      }
                    } finally {
                      if (null != dis) {
                        dis.close();
                      }
                    }
                  }
                }
              } catch (IOException e) {
                e.printStackTrace();
                return false;
              }
            }
            return true;
          }

          @Override
          protected void onProgressUpdate(DownloadProgressInfo... values) {
            onDownloadProgress(values[0]);
            super.onProgressUpdate(values);
          }

          @Override
          protected void onPostExecute(Boolean result) {
            if (result) {
              mDashboard.setVisibility(View.VISIBLE);
              mCellMessage.setVisibility(View.GONE);
              mStatusText.setText(R.string.text_validation_complete);
              mPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  Log.d(TAG, "Obb file path = " + Helpers.getSaveFilePath(StartupActivity.this));
                }
              });
              mPauseButton.setText(android.R.string.ok);
            } else {
              mDashboard.setVisibility(View.VISIBLE);
              mCellMessage.setVisibility(View.GONE);
              mStatusText.setText(R.string.text_validation_failed);
              mPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  finish();
                }
              });
              mPauseButton.setText(android.R.string.cancel);
            }
            super.onPostExecute(result);
          }

        };
    validationTask.execute(new Object());
  }

  /**
   * If the download isn't present, we initialize the download UI. This ties
   * all of the controls into the remote service calls.
   */
  private void initializeDownloadUI() {
    mDownloaderClientStub = DownloaderClientMarshaller.CreateStub
        (this, DownloaderService.class);
    setContentView(R.layout.activity_startup_expansion);

    Log.d(TAG, "initializeDownloadUI");

    mPB = (ProgressBar) findViewById(R.id.progressBar);
    mStatusText = (TextView) findViewById(R.id.statusText);
    mProgressFraction = (TextView) findViewById(R.id.progressAsFraction);
    mProgressPercent = (TextView) findViewById(R.id.progressAsPercentage);
    mAverageSpeed = (TextView) findViewById(R.id.progressAverageSpeed);
    mTimeRemaining = (TextView) findViewById(R.id.progressTimeRemaining);
    mDashboard = findViewById(R.id.downloaderDashboard);
    mCellMessage = findViewById(R.id.approveCellular);
    mPauseButton = (Button) findViewById(R.id.pauseButton);
    mWiFiSettingsButton = (Button) findViewById(R.id.wifiSettingsButton);

    mPauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mStatePaused) {
          mRemoteService.requestContinueDownload();
        } else {
          mRemoteService.requestPauseDownload();
        }
        setButtonPausedState(!mStatePaused);
      }
    });

    mWiFiSettingsButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
      }
    });

    Button resumeOnCell = (Button) findViewById(R.id.resumeOverCellular);
    resumeOnCell.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mRemoteService.setDownloadFlags(IDownloaderService.FLAGS_DOWNLOAD_OVER_CELLULAR);
        mRemoteService.requestContinueDownload();
        mCellMessage.setVisibility(View.GONE);
      }
    });

  }

  private void launchDownloader() {
    try {
      Intent launchIntent = StartupActivity.this.getIntent();
      Intent intentToLaunchThisActivityFromNotification = new Intent(
          StartupActivity.this, StartupActivity.this.getClass());
      intentToLaunchThisActivityFromNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
          |
          Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intentToLaunchThisActivityFromNotification.setAction(launchIntent.getAction());

      if (launchIntent.getCategories() != null) {
        for (String category : launchIntent.getCategories()) {
          intentToLaunchThisActivityFromNotification.addCategory(category);
        }
      }

      PendingIntent pendingIntent = PendingIntent.getActivity(StartupActivity.this, 0, intentToLaunchThisActivityFromNotification, PendingIntent.FLAG_UPDATE_CURRENT);
      int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this, pendingIntent,
          DownloaderService.class);
      Log.d(TAG, "startResult = " + startResult);
      if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
        initializeDownloadUI();
        return;
      }
      // otherwise we fall through to starting the movie
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Cannot find own package! MAYDAY!");
      e.printStackTrace();
    }
  }

  /**
   * Connect the stub to our service on start.
   */
  @Override
  protected void onStart() {
    Log.d(TAG,"onStart");
    if (null != mDownloaderClientStub) {
      Log.d(TAG, "try to connect");
      mDownloaderClientStub.connect(this);
    }
    super.onStart();
  }

  /**
   * Disconnect the stub from our service on stop
   */
  @Override
  protected void onStop() {
    if (null != mDownloaderClientStub) {
      mDownloaderClientStub.disconnect(this);
    }
    super.onStop();
  }

  @Override
  public void onServiceConnected(Messenger m) {
    Log.d(TAG, "onServiceConnected");
    mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
    mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
  }

  @Override
  public void onDownloadStateChanged(int newState) {
    Log.d(TAG, "onDownloadStateChanged = " + newState);
    setState(newState);
    boolean showDashboard = true;
    boolean showCellMessage = false;
    boolean paused;
    boolean indeterminate;
    switch (newState) {
      case IDownloaderClient.STATE_IDLE:
        // STATE_IDLE means the service is listening, so it's
        // safe to start making calls via mRemoteService.
        paused = false;
        indeterminate = true;
        break;
      case IDownloaderClient.STATE_CONNECTING:
      case IDownloaderClient.STATE_FETCHING_URL:
        showDashboard = true;
        paused = false;
        indeterminate = true;
        break;
      case IDownloaderClient.STATE_DOWNLOADING:
        paused = false;
        showDashboard = true;
        indeterminate = false;
        break;

      case IDownloaderClient.STATE_FAILED_CANCELED:
      case IDownloaderClient.STATE_FAILED:
      case IDownloaderClient.STATE_FAILED_FETCHING_URL:
      case IDownloaderClient.STATE_FAILED_UNLICENSED:
        paused = true;
        showDashboard = false;
        indeterminate = false;
        break;
      case IDownloaderClient.STATE_PAUSED_NEED_CELLULAR_PERMISSION:
      case IDownloaderClient.STATE_PAUSED_WIFI_DISABLED_NEED_CELLULAR_PERMISSION:
        showDashboard = false;
        paused = true;
        indeterminate = false;
        showCellMessage = true;
        break;
      case IDownloaderClient.STATE_PAUSED_WIFI_DISABLED:
      case IDownloaderClient.STATE_PAUSED_NEED_WIFI:
        showDashboard = false;
        paused = true;
        indeterminate = false;
        showCellMessage = true;
        break;

      case IDownloaderClient.STATE_PAUSED_BY_REQUEST:
        paused = true;
        indeterminate = false;
        break;
      case IDownloaderClient.STATE_PAUSED_ROAMING:
      case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE:
        paused = true;
        indeterminate = false;
        break;
      case IDownloaderClient.STATE_COMPLETED:
        showDashboard = false;
        paused = false;
        indeterminate = false;
        validateXAPKZipFiles();
        return;
      default:
        paused = true;
        indeterminate = true;
        showDashboard = true;
    }
    int newDashboardVisibility = showDashboard ? View.VISIBLE : View.GONE;
    if (mDashboard.getVisibility() != newDashboardVisibility) {
      mDashboard.setVisibility(newDashboardVisibility);
    }
    int cellMessageVisibility = showCellMessage ? View.VISIBLE : View.GONE;
    if (mCellMessage.getVisibility() != cellMessageVisibility) {
      mCellMessage.setVisibility(cellMessageVisibility);
    }

    mPB.setIndeterminate(indeterminate);
    setButtonPausedState(paused);
  }

  @Override
  public void onDownloadProgress(DownloadProgressInfo progress) {
    Log.d(TAG, "onDownloadProgress");
    mAverageSpeed.setText(getString(R.string.kilobytes_per_second,
        Helpers.getSpeedString(progress.mCurrentSpeed)));
    mTimeRemaining.setText(getString(R.string.time_remaining,
        Helpers.getTimeRemaining(progress.mTimeRemaining)));

    mPB.setMax((int) (progress.mOverallTotal >> 8));
    mPB.setProgress((int) (progress.mOverallProgress >> 8));
    mProgressPercent.setText(Long.toString(progress.mOverallProgress
        * 100 /
        progress.mOverallTotal) + "%");
    mProgressFraction.setText(Helpers.getDownloadProgressString
        (progress.mOverallProgress,
            progress.mOverallTotal));
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    initializeDownloadUI();
//
//    /**
//     * Before we do anything, are the files we expect already here and
//     * delivered (presumably by Market) For free titles, this is probably
//     * worth doing. (so no Market request is necessary)
//     */
//    if (!xAPKFilesDelivered()) {
//      // does our OBB directory exist?
//      if (Helpers.canWriteOBBFile(this)) {
//        Log.d(TAG, "launchDownloader");
//        launchDownloader();
//      }
//      else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        // we need the write permission first
//        Log.d(TAG, "requestStorageWritePermission");
//        requestStorageWritePermission();
//      }
//    } else if (!xAPKFilesReadable()){
//      Log.e(TAG, "Cannot read APKx File.  Permission Perhaps?");
//      if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//          != PackageManager.PERMISSION_GRANTED) {
//        Log.e(TAG, "Need Permission!");
//        requestStorageReadPermission();
//      }
//    } else {
//      validateXAPKZipFiles();
//    }


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
      moveOldDataFiles().continueWithTask(new Continuation<Void, Task<String>>() {
        @Override
        public Task<String> then(Task<Void> task) throws Exception {
          Log.d(TAG, "moveOldDataFiles finished");
          showDialog(unzipDialog);
          return unzipBundleDatabase();
        }
      }).continueWithTask(new Continuation<String, Task<Void>>() {
        @Override
        public Task<Void> then(Task<String> task) throws Exception {
          dismissDialog(unzipDialog);
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
              alertDatabaseError();
            }
          });
          dismissDialog(downloadDialog);
          dismissDialog(unzipDialog);
          return null;
        }
      });
    }

  }

  private void alertDatabaseError() {
    new AlertDialog.Builder(this)
        .setTitle(R.string.database_error)
        .setMessage(getString(R.string.database_version_mismatched))
        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        }).create().show();
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
    this.mCancelValidation = true;
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

  private bolts.Task<String> unzipBundleDatabase() {
    final Task<String>.TaskCompletionSource source = Task.create();
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        try {
          String outFileName = new File(Utils.getDatabaseDirectory(),
              Constants.DATABASE_ZIP_FILE).toString();
          File thaiDbFile = new File(Utils.getDatabasePath(Language.THAI));
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

            UnzipUtility.unzip(outFileName, Utils.getDatabaseDirectory());

            source.setResult(outFileName);
          } else {
            source.setResult(null);
          }

        } catch (IOException e) {
          source.setError(e);
          e.printStackTrace();
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

  boolean xAPKFilesDelivered() {
    for (XAPKFile xf : xAPKS) {
      String fileName = Helpers.getExpansionAPKFileName(this, xf.mIsMain, xf.mFileVersion);
      if (!Helpers.doesFileExist(this, fileName, xf.mFileSize, false))
        return false;
    }
    return true;
  }

  boolean xAPKFilesReadable() {
    for (XAPKFile xf : xAPKS) {
      String fileName = Helpers.getExpansionAPKFileName(this, xf.mIsMain, xf.mFileVersion);
      if ( Helpers.getFileStatus(this, fileName) == Helpers.FS_CANNOT_READ ) {
        return false;
      }
    }
    return true;
  }

  private static final int PERMISSION_STORAGE_READ_REQUEST_CODE = 1;
  private static final int PERMISSION_STORAGE_WRITE_REQUEST_CODE = 2;


  /**
   * Requests the {@link android.Manifest.permission#CAMERA} permission.
   * If an additional rationale should be displayed, the user has to launch the request from
   * a SnackBar that includes additional information.
   */
  private void requestStorageReadPermission() {
    // Permission has not been granted and must be requested.
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.READ_EXTERNAL_STORAGE)) {
      ActivityCompat.requestPermissions(StartupActivity.this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
          PERMISSION_STORAGE_READ_REQUEST_CODE);
    } else {
      ActivityCompat.requestPermissions(StartupActivity.this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
          PERMISSION_STORAGE_READ_REQUEST_CODE);
    }
  }

  /**
   * Requests the {@link android.Manifest.permission#CAMERA} permission.
   * If an additional rationale should be displayed, the user has to launch the request from
   * a SnackBar that includes additional information.
   */
  private void requestStorageWritePermission() {
    // Permission has not been granted and must be requested
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      ActivityCompat.requestPermissions(StartupActivity.this,
          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
          PERMISSION_STORAGE_WRITE_REQUEST_CODE);
    } else {
      ActivityCompat.requestPermissions(StartupActivity.this,
          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
          PERMISSION_STORAGE_WRITE_REQUEST_CODE);
    }
  }
}