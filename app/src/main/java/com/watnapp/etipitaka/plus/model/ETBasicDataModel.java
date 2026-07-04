package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.util.Log;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sutee on 19/2/14.
 */
public abstract class ETBasicDataModel extends ETDataModel {

  protected static final String TAG = "ETBasicDataModel";

  public ETBasicDataModel(Context context) {
    super(context);
  }

  public String pageFormat(int page) {
    return String.format("%04d", page);
  }

  public String volumeFormat(int volume) {
    return String.format("%02d", volume);
  }

  @Override
  public void getItemsAtPage(final int volume, final int page, final BookDatabaseHelper.OnGetItemsListener listener) {
    openDatabase();
    new Thread(() -> {
      Cursor cursor = null;
      try {
        cursor = db.query("main", null, "volume=? AND page=?",
            new String[]{volumeFormat(volume), pageFormat(page)}, null, null, null);
        int itemsCol = cursor.getColumnIndex("items");
        // No matching row (out-of-range page/volume, stale saved position, etc.)
        // or missing column => report "no items" instead of crashing on getString.
        if (!cursor.moveToFirst() || itemsCol < 0) {
          listener.onGetItemsFinish(null, null);
          return;
        }
        String s = cursor.getString(itemsCol);
        String[] tokens = ((s == null || s.isEmpty()) ? "0" : s).split("\\s+");
        ArrayList<Integer> items = new ArrayList<Integer>();
        for (String token : tokens) {
          if (token.isEmpty()) {
            continue;
          }
          try {
            items.add(Integer.parseInt(token));
          } catch (NumberFormatException ignored) {
            // Skip malformed item; protects against bad rows in older DBs.
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
        if (cursor != null) {
          cursor.close();
        }
      }
    }).start();
  }

  @Override
  public Cursor read(int volume, int page) {
    openDatabase();
    if (db == null) {
      // DB file missing (see ETDataModel.openDatabase). Return an empty cursor
      // so callers can detect via getCount() == 0 instead of NPEing on query.
      return new MatrixCursor(new String[] { getVolumeColumn() });
    }
    Cursor cursor = null;
    try {
      cursor = db.query("main", null, "volume=?",
          new String[] { volumeFormat(volume) }, null, null, null);
      // moveToFirst fills the cursor window — a corrupt DB file (partial
      // download, flaky external storage) throws SQLiteDatabaseCorruptException
      // here even though openDatabase succeeded (only the header is checked).
      cursor.moveToFirst();
      if (page > 0 && page <= cursor.getCount()) {
        cursor.moveToPosition(page-1);
      }
      return cursor;
    } catch (Exception e) {
      Log.e(TAG, "read failed for volume=" + volume, e);
      if (cursor != null) cursor.close();
      return new MatrixCursor(new String[] { getVolumeColumn() });
    }
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
  public int getMaximumPageNumber(int volume) {
    openDatabase();
    if (db == null) {
      return 0;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("main", null, "volume = ?",
         new String[] { volumeFormat(volume) }, null, null, "page");
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
        new String[] { volumeFormat(volume) }, null, null, "page");
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
        new String[] { volumeFormat(volume) }, null, null, "page");
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

  abstract protected Map<String,Map<String,Map<String,ArrayList<Integer>>>> getBookItems();

  @Override
  public int getPageIdByItem(int volume, int item, int section) {
    openDatabase();
    if (db == null) {
      return 0;
    }
    Map<String, Map<String, Map<String, ArrayList<Integer>>>> bookItems = getBookItems();
    if (bookItems == null
        || bookItems.get(volume + "") == null
        || bookItems.get(volume + "").get(section + "") == null
        || bookItems.get(volume + "").get(section + "").get(item + "") == null
        || bookItems.get(volume + "").get(section + "").get(item + "").isEmpty()) {
      return 0;
    }
    int page = bookItems.get(volume + "").get(section + "").get(item + "").get(0);
    Cursor cursor = null;
    try {
      cursor = db.query("main", null, "volume=? AND page=?",
          new String[] { volumeFormat(volume), pageFormat(page) }, null, null, null);
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
      String raw = cursor.getString(pageCol);
      if (raw == null) {
        return 0;
      }
      try {
        return Integer.parseInt(raw.replaceAll("^0+", ""));
      } catch (NumberFormatException e) {
        return 0;
      }
    } catch (Exception e) {
      Log.e(TAG, "getPageById failed for pageId=" + pageId, e);
      return 0;
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    ArrayList<Integer> pages = new ArrayList<Integer>();
    Map<String, Map<String, Map<String, ArrayList<Integer>>>> bookItems = getBookItems();
    if (bookItems == null || bookItems.get(volume + "") == null) {
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
  public void search(final String keywords, final BookDatabaseHelper.OnSearchListener listener, final Integer[] volumes) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (db == null) {
          if (listener != null) {
            MatrixCursor empty = new MatrixCursor(new String[] { "_id", "total" });
            listener.onSearchFinish(keywords, empty, new int[3]);
          }
          return;
        }
        Cursor[] cursors = new Cursor[volumes.length+1];
        int totalPages[] = new int[3];
        for (int i=0; i < volumes.length; ++i) {
          int volume = volumes[i];
          String selection = "volume = ?";
          ArrayList<String> selectionArgs = new ArrayList<String>();
          selectionArgs.add(volumeFormat(volume));

          for (String keyword : keywords.split("\\s+")) {
            selection += " AND content LIKE ?";
            selectionArgs.add("%" + keyword.replace('+', ' ') + "%");
          }

          Cursor cursor;
          try {
            cursor = db.query("main", null, selection, selectionArgs.toArray(new String[selectionArgs.size()]),
                null, null, null);
            // Fill the cursor window now — a corrupt DB throws here, not at
            // query(). Uncaught on this raw Thread it would kill the process.
            cursor.getCount();
          } catch (Exception e) {
            Log.e(TAG, "search failed for volume=" + volume, e);
            cursor = new MatrixCursor(new String[] { "_id" });
          }

          if (listener != null) {
            listener.onSearchProgress(keywords, volume, i+1, cursor);
          }

          if (volume >= 1 && volume <= getSectionBoundary(0)) {
            totalPages[0] += cursor.getCount();
            Log.d(TAG, "1:" + volume + ":" + cursor.getCount());
          } else if (volume >= getSectionBoundary(0)+1 && volume <= getSectionBoundary(1)) {
            totalPages[1] += cursor.getCount();
            Log.d(TAG, "2:" + volume + ":" + cursor.getCount());
          } else {
            totalPages[2] += cursor.getCount();
            Log.d(TAG, "3:" + volume + ":" + cursor.getCount());
          }

          cursors[i+1] = cursor;

        }

        MatrixCursor headerCursor = new MatrixCursor(new String[] { "_id", "total" });
        headerCursor.addRow(new Object[] {10001, totalPages[0]});
        headerCursor.addRow(new Object[] {10002, totalPages[1]});
        headerCursor.addRow(new Object[] {10003, totalPages[2]});
        cursors[0] = headerCursor;
        if (listener != null) {
          listener.onSearchFinish(keywords, new MergeCursor(cursors), totalPages);
        }
      }
    }).start();

  }
}
