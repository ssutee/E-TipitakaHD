package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 13/7/2013
 * Time: 23:34
  */
public class FileDownloader {
  private final int MAX_CONCURRENCY_THREAD = 1;
  private final PriorityQueue<DownloadFileAsyncTaskBase> mDownloadingTaskQueue;
  private final ArrayList<String> mDownloadingUrlList;

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
      this.onFileDownloadListener.onTotalFileSizeChange(this, url, path,
          fileId, size);
    }
  }

  private void notifyProgressUpdate(String url, String path, int fileId,
                                    int progress) {
    if (this.onFileDownloadListener != null) {
      this.onFileDownloadListener.onProgressUpdate(this, url, path,
          fileId, progress);
    }
  }

  private void notifyDownloadingFinish(int fileId, boolean success) {
    if (this.onFileDownloadListener != null) {
      this.onFileDownloadListener.onDownloadingFinish(this, fileId,
          success);
    }
  }

  public FileDownloader() {
    mDownloadingTaskQueue = new PriorityQueue<DownloadFileAsyncTaskBase>();
    mDownloadingUrlList = new ArrayList<String>();
  }

  public void startDownload(Context context, String url, String path, int fileId) {
    Log.d("startDownload", url);

    mDownloadingTaskQueue.add(new DownloadingFileAsyncTask(context,
        url, path, fileId));

    Log.d("FileDownloader", "queue size: " + mDownloadingTaskQueue.size()
        + "");
    mDownloadingUrlList.add(url);
    if (mDownloadingTaskQueue.size() > 0
        && mDownloadingUrlList.size() <= MAX_CONCURRENCY_THREAD) {
      mDownloadingTaskQueue.peek().execute();
    }
  }

  public void stopDownload(int fileId) {
    for (DownloadFileAsyncTaskBase task : mDownloadingTaskQueue) {
      if (task.getId() == fileId) {
        task.cancel(true);
      }
    }
  }

  public abstract class DownloadFileAsyncTaskBase extends
      AsyncTask<Void, Integer, Void> implements
      Comparable<DownloadFileAsyncTaskBase> {
    protected String mPath;
    protected URL mUrl;
    protected int mId;
    volatile protected boolean mPleaseStop;
    protected Context mContext;
    protected long mTotalFileSize;
    protected boolean mFileNotFound;

    public DownloadFileAsyncTaskBase(Context context, String url,
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

    public DownloadFileAsyncTaskBase(Context context, String url,
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

    @Override
    protected void onCancelled() {
      super.onCancelled();
      mPleaseStop = true;
      Log.d("FileDownloader", "before queue size: "
          + mDownloadingTaskQueue.size() + "");
      finishCurrentDownloading();
      Log.d("FileDownloader", "after queue size: "
          + mDownloadingTaskQueue.size() + "");
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mPleaseStop = false;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      Log.d("FileDownloader", "onPostExecute");
      Log.d("FileDownloader", "before queue size: "
          + mDownloadingTaskQueue.size() + "");
      finishCurrentDownloading();
      Log.d("FileDownloader", "after queue size: "
          + mDownloadingTaskQueue.size() + "");
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      notifyProgressUpdate(mUrl.toString(), mPath, mId, values[0]);
    }

    @Override
    public int compareTo(DownloadFileAsyncTaskBase another) {
      if (this.getId() > another.getId())
        return 1;
      if (this.getId() < another.getId())
        return -1;
      return 0;
    }

    private void finishCurrentDownloading() {
      mDownloadingTaskQueue.remove();
      mDownloadingUrlList.remove(mUrl.toString());

      if (mDownloadingTaskQueue.size() > 0
          && mDownloadingTaskQueue.peek().getStatus() == Status.PENDING) {
        mDownloadingTaskQueue.peek().execute();
      }

      if (mFileNotFound || fileSize(mPath) == 0
          || fileSize(mPath) != mTotalFileSize) {
        notifyDownloadingFinish(mId, false);
      } else {
        notifyDownloadingFinish(mId, true);
      }
    }

  }

  public class DownloadingFileAsyncTask extends DownloadFileAsyncTaskBase {
    public DownloadingFileAsyncTask(Context context, String url,
                                    String path, int id) {
      super(context, url, path, id);
    }

    public DownloadingFileAsyncTask(Context context, String url, String path) {
      super(context, url, path);
    }

    @Override
    protected Void doInBackground(Void... params) {
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
          return null;
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
        while (!mPleaseStop && !isCancelled()
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
      return null;
    }
  }

}

