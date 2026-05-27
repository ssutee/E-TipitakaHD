package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.util.Log;

import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;

/**
 * Created by sutee on 29/10/18.
 */
public abstract class ETHandbookDataModel extends ETDataModel {

  private static final String TAG = "ETHandbookDataModel";

  public ETHandbookDataModel(Context context) {
    super(context);
  }

  @Override
  public void search(final String keywords, final BookDatabaseHelper.OnSearchListener listener,
                     final Integer[] volumes, final BookDatabaseHelper.SearchType searchType) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (db == null) {
          if (listener != null) {
            MatrixCursor empty = new MatrixCursor(new String[] { "_id" });
            listener.onSearchFinish(keywords, empty, new int[1]);
          }
          return;
        }
        Cursor[] cursors = new Cursor[volumes.length];
        int totalPages[] = new int[1];

        for (int i=0; i < volumes.length; ++i) {
          int volume = volumes[i];

          String selection = "volume = ?";
          ArrayList<String> selectionArgs = new ArrayList<String>();
          selectionArgs.add(String.valueOf(volume));

          for (String keyword : keywords.split("\\s+")) {
            selection += String.format(" AND %s LIKE ?",
                searchType == BookDatabaseHelper.SearchType.ALL ? "content" : "buddhawaj");
            selectionArgs.add("%" + keyword.replace('+', ' ') + "%");
          }

          Cursor cursor = db.query("main", null, selection, selectionArgs.toArray(new String[selectionArgs.size()]),
              null, null, null);

          if (listener != null) {
            listener.onSearchProgress(keywords, volume, i+1, cursor);
          }
          totalPages[0] += cursor.getCount();
          cursors[i] = cursor;
        }

        if (listener != null) {
          listener.onSearchFinish(keywords, new MergeCursor(cursors), totalPages);
        }

      }
    }).start();
  }

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener, Integer[] volumes) {
    search(keywords, listener, volumes, BookDatabaseHelper.SearchType.ALL);
  }

  @Override
  public Cursor read(int volume, int page) {
    openDatabase();
    if (db == null) {
      // Handbook DB file missing (e.g. user never downloaded it, or app data
      // was cleared after the language was last selected). Return empty so
      // ReaderFragment.openBook hits the count==0 branch instead of NPEing.
      return new MatrixCursor(new String[] { getVolumeColumn() });
    }
    Cursor cursor = db.query("main", null, "volume=?",
        new String[] { String.valueOf(volume) }, null, null, null);
    cursor.moveToFirst();
    if (page > 0 && page <= cursor.getCount()) {
      cursor.moveToPosition(page-1);
    }
    return cursor;
  }

  @Override
  public int getMaximumPageNumber(int volume) {
    openDatabase();
    if (db == null) {
      return 0;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("main", null, "volume = ?",
          new String[] { String.valueOf(volume) }, null, null, "page");
      return cursor.getCount();
    } catch (Exception e) {
      Log.e(TAG, "getMaximumPageNumber failed for volume=" + volume, e);
      return 0;
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  @Override
  public int getPageById(int pageId) {
    openDatabase();
    if (db == null) {
      return 0;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("main", null, "_id = ?",
          new String[] {String.valueOf(pageId)}, null, null, null);
      int pageCol = cursor.getColumnIndex("page");
      if (pageCol < 0 || !cursor.moveToFirst()) {
        return 0;
      }
      return cursor.getInt(pageCol);
    } catch (Exception e) {
      Log.e(TAG, "getPageById failed for pageId=" + pageId, e);
      return 0;
    } finally {
      if (cursor != null) cursor.close();
    }
  }


}
