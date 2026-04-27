package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 13/7/2013
 * Time: 23:34
  */
public class FileDownloader {
  private final int MAX_CONCURRENCY_THREAD = 1;
  private final PriorityQueue<DownloadFileTaskBase> mDownloadingTaskQueue;
  private final ArrayList<String> mDownloadingUrlList;
  private final ExecutorService executorService;
  private final Handler mainHandler;
  private DownloadFileTaskBase activeTask;

  public interface OnFileDownloadListener {
    public void onTotalFileSizeChange(FileDownloader downloader,
                                      String url, String path, int fileId, long size);

    public void onProgressUpdate(FileDownloader downloader, String url,
                                 String path, int fileId, int progress);

    public void onDownloadingFinish(FileDownloader downloader, int fileId,
                                    boolean success);

    public void onDownloadingFinishWithError(FileDownloader downloader,
                                             int fileId, int errorResMessage);
  }

  private OnFileDownloadListener onFileDownloadListener;

  public void setOnFileDownloadListener(
      OnFileDownloadListener onFileDownloadListener) {
    this.onFileDownloadListener = onFileDownloadListener;
  }

  private void notifyTotalFileSizeChange(String url, String path, int fileId,
                                         long size) {
    if (this.onFileDownloadListener != null) {
      mainHandler.post(() -> this.onFileDownloadListener.onTotalFileSizeChange(this, url, path,
          fileId, size));
    }
  }

  private void notifyProgressUpdate(String url, String path, int fileId,
                                    int progress) {
    if (this.onFileDownloadListener != null) {
      mainHandler.post(() -> this.onFileDownloadListener.onProgressUpdate(this, url, path,
          fileId, progress));
    }
  }

  private void notifyDownloadingFinish(int fileId, boolean success) {
    if (this.onFileDownloadListener != null) {
      mainHandler.post(() -> this.onFileDownloadListener.onDownloadingFinish(this, fileId,
          success));
    }
  }

  public FileDownloader() {
    mDownloadingTaskQueue = new PriorityQueue<DownloadFileTaskBase>();
    mDownloadingUrlList = new ArrayList<String>();
    executorService = Executors.newSingleThreadExecutor();
    mainHandler = new Handler(Looper.getMainLooper());
  }

  public void startDownload(Context context, String url, String path, int fileId) {
    Log.d("startDownload", url);

    mDownloadingTaskQueue.add(new DownloadingFileTask(context,
        url, path, fileId));

    Log.d("FileDownloader", "queue size: " + mDownloadingTaskQueue.size()
        + "");
    mDownloadingUrlList.add(url);
    if (mDownloadingTaskQueue.size() > 0
        && mDownloadingUrlList.size() <= MAX_CONCURRENCY_THREAD) {
      startNextDownload();
    }
  }

  public void stopDownload(int fileId) {
    for (DownloadFileTaskBase task : mDownloadingTaskQueue) {
      if (task.getId() == fileId) {
        task.cancel();
      }
    }
  }

  private synchronized void startNextDownload() {
    if (activeTask != null || mDownloadingTaskQueue.isEmpty()) {
      return;
    }

    activeTask = mDownloadingTaskQueue.peek();
    activeTask.execute();
  }

  public abstract class DownloadFileTaskBase implements
      Comparable<DownloadFileTaskBase>, Runnable {
    protected String mPath;
    protected URL mUrl;
    protected int mId;
    volatile protected boolean mPleaseStop;
    protected Context mContext;
    protected long mTotalFileSize;
    protected boolean mFileNotFound;
    private Future<?> future;

    public DownloadFileTaskBase(Context context, String url,
                                String path, int id) {
      try {
        mUrl = new URL(url);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
      mContext = context;
      mPath = path;
      mId = id;
      mPleaseStop = false;
      mTotalFileSize = 0;
    }

    public DownloadFileTaskBase(Context context, String url,
                                String path) {
      this(context, url, path, 0);
    }

    public int getId() {
      return mId;
    }

    protected long fileSize(String path) {
      if (mContext != null) {
        return (path == null) ? 0 : mContext.getFileStreamPath(path)
            .length();
      } else {
        return (path == null) ? 0 : new File(path).length();
      }
    }

    public void execute() {
      mPleaseStop = false;
      future = executorService.submit(this);
    }

    public void cancel() {
      mPleaseStop = true;
      if (future != null) {
        future.cancel(true);
      }
    }

    @Override
    public void run() {
      try {
        download();
      } finally {
        Log.d("FileDownloader", "download finished");
        Log.d("FileDownloader", "before queue size: "
            + mDownloadingTaskQueue.size() + "");
        finishCurrentDownloading();
        Log.d("FileDownloader", "after queue size: "
            + mDownloadingTaskQueue.size() + "");
      }
    }

    protected void publishProgress(Integer value) {
      notifyProgressUpdate(mUrl.toString(), mPath, mId, value);
    }

    protected abstract void download();

    @Override
    public int compareTo(DownloadFileTaskBase another) {
      if (this.getId() > another.getId())
        return 1;
      if (this.getId() < another.getId())
        return -1;
      return 0;
    }

    private synchronized void finishCurrentDownloading() {
      mDownloadingTaskQueue.remove(this);
      if (mUrl != null) {
        mDownloadingUrlList.remove(mUrl.toString());
      }
      activeTask = null;

      if (mFileNotFound || mPleaseStop || fileSize(mPath) == 0
          || fileSize(mPath) != mTotalFileSize) {
        notifyDownloadingFinish(mId, false);
      } else {
        notifyDownloadingFinish(mId, true);
      }

      startNextDownload();
    }

  }

  public class DownloadingFileTask extends DownloadFileTaskBase {
    public DownloadingFileTask(Context context, String url,
                               String path, int id) {
      super(context, url, path, id);
    }

    public DownloadingFileTask(Context context, String url, String path) {
      super(context, url, path);
    }

    @Override
    protected void download() {
      BufferedOutputStream bOut = null;
      BufferedInputStream bIn = null;
      HttpURLConnection connection = null;
      FileOutputStream fos;

      try {
        Log.d("doInBackground", "start: " + mUrl.toString());
        connection = (HttpURLConnection) mUrl.openConnection();
        connection.setConnectTimeout(5000);
        new File(mPath).delete();

        connection.setDoInput(true);

        // file was downloaded completely or authentication failed.
        if (connection.getContentLength() == -1) {
          Log.d("doInBackground",
              "file was downloaded completely or authentication failed.");
          connection.disconnect();
          return;
        }

        mTotalFileSize = connection.getContentLength()
            + fileSize(mPath);

        notifyTotalFileSizeChange(mUrl.toString(), mPath, mId,
            connection.getContentLength() + fileSize(mPath));
        bIn = new BufferedInputStream(connection.getInputStream(), 8192);

        if (mContext != null) {
          fos = (fileSize(mPath) == 0) ? mContext.openFileOutput(
              mPath, Context.MODE_PRIVATE) : mContext
              .openFileOutput(mPath, Context.MODE_APPEND);
        } else {
          fos = (fileSize(mPath) == 0) ? new FileOutputStream(mPath)
              : new FileOutputStream(mPath, true);
        }

        bOut = new BufferedOutputStream(fos, 8192);
        byte[] data = new byte[1024];
        int dataSize = 0;
        long downloaded = fileSize(mPath);
        while (!mPleaseStop && !Thread.currentThread().isInterrupted()
            && (dataSize = bIn.read(data, 0, 1024)) >= 0) {
          bOut.write(data, 0, dataSize);
          downloaded += dataSize;
          publishProgress(Integer
              .valueOf((int) (100 * downloaded / mTotalFileSize)));
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        mFileNotFound = true;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          if (bOut != null) {
            bOut.close();
          }
          if (bIn != null) {
            bIn.close();
          }
          if (connection != null) {
            connection.disconnect();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

}
