package com.watnapp.etipitaka.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 0:31
 */

@Singleton
public class DatabaseHelper {

  protected static final String TAG = "DatabaseHelper";
  private SQLiteDatabase db;
  private static final String DATABASE_PATH = Environment.getExternalStorageDirectory().getPath()
      + "/etipitaka_plus.db";

  @Inject
  private Context mContext;

  public void openDatabase() {
    if ((db == null || !db.isOpen()) && (new File(DATABASE_PATH)).exists()) {
      db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, 0);
    }
  }

  public void closeDatabase() {
    if (db != null && db.isOpen()) {
      db.close();
    }
  }

  public Cursor read(Language language, int volume, int page) {
    Cursor cursor = db.query("page", null, "language = ? AND volume = ?",
        new String[] {String.valueOf(language.getCode()), String.valueOf(volume)}, null, null, null);
    cursor.moveToFirst();
    if (page >= 0 && page <= cursor.getCount()) {
      cursor.moveToPosition(page-1);
    }
    return cursor;
  }

  public Cursor read(Language language, int volume) {
    return read(language, volume, 0);
  }

  public void search(final Language language, final String keywords, final OnSearchListener listener,
                     final int[] volumes) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Cursor[] cursors = new Cursor[volumes.length];
        for (int i=0; i < volumes.length; ++i) {
          int volume = volumes[i];
          String selection = "language = ? AND volume = ?";
          ArrayList<String> selectionArgs = new ArrayList<String>();
          selectionArgs.add(String.valueOf(language.getCode()));
          selectionArgs.add(String.valueOf(volume));

          for (String keyword : keywords.split("\\s+")) {
            selection += " AND content LIKE ?";
            selectionArgs.add("%" + keyword.replace('+', ' ') + "%");
          }

          Log.d(TAG, selection);
          Cursor cursor = db.query("page", null, selection, selectionArgs.toArray(new String[selectionArgs.size()]),
              null, null, null);
          if (listener != null) {
            listener.onSearchProgress(keywords, volume, 1.0f * (i+1) / volumes.length, cursor);
          }
          cursors[i] = cursor;
        }
        if (listener != null) {
          listener.onSearchFinish(keywords, new MergeCursor(cursors));
        }
        for (Cursor cursor : cursors) {
          if (cursor != null && !cursor.isClosed()) {
            cursor.close();
          }
        }
      }
    }).start();
  }

  public void search(Language language, String keywords, OnSearchListener listener) {
    search(language, keywords, listener,
        new int[] {
            1,2,3,4,5,6,7,8,9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45
        }
    );
  }

  public interface OnSearchListener {

    public void onSearchProgress(String keywords, int volume, float progress, Cursor cursor);
    public void onSearchFinish(String keywords, Cursor cursor);

  }

  public enum Language {
    THAI(0), PALI(1);

    private int code;

    private Language(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

}
