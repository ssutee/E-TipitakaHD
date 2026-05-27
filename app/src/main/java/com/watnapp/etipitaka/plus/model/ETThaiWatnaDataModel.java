package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.util.Log;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sutee on 31/8/14.
 */
public class ETThaiWatnaDataModel extends ETDataModel {

  private static final String TAG = "ETThaiWatnaDataModel";

  public ETThaiWatnaDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.THAIWN);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAIWN;
  }

  @Override
  public String getContentColumn() {
    return "content";
  }

  @Override
  public String getPageNumberColumn() {
    return "page";
  }

  @Override
  public void getItemsAtPage(final int volume, final int page, final BookDatabaseHelper.OnGetItemsListener listener) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (db == null) {
          listener.onGetItemsFinish(null, null);
          return;
        }
        Cursor cursor = null;
        try {
          cursor = db.query("main", null, "volume=? AND page=?",
              new String[]{String.valueOf(volume), String.valueOf(page)}, null, null, null);
          int itemsCol = cursor.getColumnIndex("items");
          if (!cursor.moveToFirst() || itemsCol < 0) {
            listener.onGetItemsFinish(null, null);
            return;
          }
          String itemsColumn = cursor.getString(itemsCol);
          if (itemsColumn == null || itemsColumn.trim().length() == 0) {
            listener.onGetItemsFinish(null, null);
            return;
          }
          String[] tokens = itemsColumn.split("\\s+");
          ArrayList<Integer> items = new ArrayList<Integer>();
          for (String token : tokens) {
            if (token.isEmpty()) continue;
            try {
              items.add(Integer.parseInt(token));
            } catch (NumberFormatException ignored) {
            }
          }
          if (items.isEmpty()) {
            listener.onGetItemsFinish(null, null);
            return;
          }
          int section = BookDatabaseHelper.getSubItem(mContext, getLanguage(), volume, page, items.get(0));
          ArrayList<Integer> sections = new ArrayList<Integer>();
          for (int i = 0; i < items.size(); ++i) {
            sections.add(section);
          }
          listener.onGetItemsFinish(items.toArray(new Integer[items.size()]),
              sections.toArray(new Integer[sections.size()]));
        } catch (Exception e) {
          Log.e(TAG, "getItemsAtPage failed for volume=" + volume + ", page=" + page, e);
          listener.onGetItemsFinish(null, null);
        } finally {
          if (cursor != null) cursor.close();
        }
      }
    }).start();

  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    getItemsAtPage(volume, page, listener);
  }

  @Override
  public Cursor read(int volume, int page) {
    openDatabase();
    if (db == null) {
      // DB file missing — return empty cursor so callers see getCount() == 0
      // instead of NPEing on db.query (see ETDataModel.openDatabase).
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
  public int getMinimumItemNumber(int volume) {
    openDatabase();
    if (db == null) {
      return 1;
    }
    Cursor cursor = db.query("main", null, "volume = ?",
        new String[] { String.valueOf(volume) }, null, null, "page");
    try {
      int itemsCol = cursor.getColumnIndex("items");
      if (itemsCol < 0) {
        return 1;
      }
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        String raw = cursor.getString(itemsCol);
        if (raw == null) continue;
        for (String token : raw.trim().split("\\s+")) {
          if (token.isEmpty()) continue;
          try {
            return Integer.parseInt(token);
          } catch (NumberFormatException ignore) {
          }
        }
      }
      return 1;
    } finally {
      cursor.close();
    }
  }

  @Override
  public int getMaximumItemNumber(int volume) {
    openDatabase();
    if (db == null) {
      return 1;
    }
    Cursor cursor = db.query("main", null, "volume = ?",
        new String[] { String.valueOf(volume) }, null, null, "page");
    try {
      int itemsCol = cursor.getColumnIndex("items");
      if (itemsCol < 0) {
        return 1;
      }
      int maxItem = 0;
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        String raw = cursor.getString(itemsCol);
        if (raw == null) continue;
        for (String token : raw.trim().split("\\s+")) {
          if (token.isEmpty()) continue;
          try {
            maxItem = Math.max(maxItem, Integer.parseInt(token));
          } catch (NumberFormatException ignore) {
          }
        }
      }
      return maxItem > 0 ? maxItem : 1;
    } finally {
      cursor.close();
    }
  }

  @Override
  public int getPageIdByItem(int volume, int item, int section) {
    openDatabase();
    if (db == null) {
      return 0;
    }
    Map<String, Map<String, Map<String, ArrayList<Integer>>>> bookItems = BookDatabaseHelper.getThaiWNBookItems(mContext);
    if (bookItems.get(volume + "") == null
        || bookItems.get(volume + "").get(section + "") == null
        || bookItems.get(volume + "").get(section + "").get(item + "") == null
        || bookItems.get(volume + "").get(section + "").get(item + "").isEmpty()) {
      return 0;
    }
    int page = bookItems.get(volume + "").get(section + "").get(item + "").get(0);
    Cursor cursor = null;
    try {
      cursor = db.query("main", null, "volume=? AND page=?",
          new String[] {String.valueOf(volume), String.valueOf(page) }, null, null, null);
      int idCol = cursor.getColumnIndex("_id");
      if (idCol < 0 || !cursor.moveToFirst()) {
        return 0;
      }
      return cursor.getInt(idCol);
    } catch (Exception e) {
      Log.e(TAG, "getPageIdByItem failed for volume=" + volume + ", item=" + item, e);
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
      cursor = db.query("main", null, "_id = ?", new String[] {String.valueOf(pageId)}, null, null, null);
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

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    Map<String, Map<String, Map<String, ArrayList<Integer>>>> bookItems = BookDatabaseHelper.getThaiWNBookItems(mContext);
    ArrayList<Integer> pages = new ArrayList<Integer>();
    if (bookItems.get(volume + "") == null) {
      return new Integer[0];
    }
    for (String section : bookItems.get(volume + "").keySet()) {
      Map<String, ArrayList<Integer>> sectionMap = bookItems.get(volume + "").get(section);
      if (sectionMap != null && sectionMap.containsKey(item + "")
          && sectionMap.get(item + "") != null
          && !sectionMap.get(item + "").isEmpty()) {
        pages.add(sectionMap.get(item + "").get(0));
      }
    }
    return pages.toArray(new Integer[pages.size()]);
  }

  @Override
  public boolean hasHtmlContent() {
    return true;
  }

  @Override
  public int getSectionBoundary(int index) {
    return 33;
  }

  @Override
  public int getTotalVolumes() {
    return 33;
  }

  @Override
  public int convertVolume(int volume, int section, int item) {
    if (volume <= 8) {
      return volume + 25;
    }
    return volume - 8;
  }

  @Override
  public int getComparingVolume(int volume, int page) {
    if (volume <= 25) {
      return volume + 8;
    }
    return volume - 25;
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
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener, new Integer[] {
         1, 2, 3, 4, 5, 6, 7, 8, 9,10,
        11,12,13,14,15,16,17,18,19,20,
        21,22,23,24,25,26,27,28,29,30,
        31,32,33});
  }

  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.thaiwn_short_name);
  }

}
