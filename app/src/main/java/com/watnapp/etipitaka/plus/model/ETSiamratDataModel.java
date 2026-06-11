package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.util.Log;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;

/**
 * Created by sutee on 4/2/14.
 */
abstract public class ETSiamratDataModel extends ETDataModel {

  protected static final String TAG = "ETSiamratDataModel";

  public ETSiamratDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.THAI);
  }

  protected int getPageId(int volume, int page) {
    openDatabase();
    if (db == null) {
      return -1;
    }
    int pageId = -1;
    Cursor cursor = null;
    try {
      cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ? AND number = ?",
          new String[] {String.valueOf(getLanguage().getCode()), String.valueOf(volume), String.valueOf(page)},
          null, null, null);
      if (cursor.moveToFirst()) {
        pageId = cursor.getInt(0);
      }
    } catch (Exception e) {
      Log.e(TAG, "getPageId failed for volume=" + volume + ", page=" + page, e);
    } finally {
      if (cursor != null) cursor.close();
    }
    return pageId;
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
        ArrayList<Integer> items = new ArrayList<Integer>();
        ArrayList<Integer> sections = new ArrayList<Integer>();
        Cursor cursor = null;
        try {
          int pageId = getPageId(volume, page);
          cursor = db.query("item", new String[] {"number", "section"},
              "page_id = ?", new String[] {String.valueOf(pageId)}, null, null, null);
          if (!cursor.moveToFirst()) {
            listener.onGetItemsFinish(null, null);
            return;
          }
          while (!cursor.isAfterLast()) {
            int number = cursor.getInt(0);
            int section = cursor.getInt(1);
            if (!items.contains(number)) {
              items.add(number);
              sections.add(section);
            }
            cursor.moveToNext();
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
      // DB file missing — see ETDataModel.openDatabase. Return empty cursor so
      // callers see getCount() == 0 instead of NPEing on db.query.
      return new MatrixCursor(new String[] { getVolumeColumn() });
    }
    Cursor cursor = null;
    try {
      cursor = db.query("page", null, "language = ? AND volume = ?",
          new String[] {String.valueOf(getLanguage().getCode()), String.valueOf(volume)}, null, null, null);
      // moveToFirst fills the cursor window — a corrupt DB file throws
      // SQLiteDatabaseCorruptException here even though openDatabase
      // succeeded (only the header is checked at open).
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
  public int getMaximumPageNumber(int volume) {
    openDatabase();
    if (db == null) {
      return 0;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("page", new String[] {"number"}, "language = ? AND volume = ?",
          new String[] { String.valueOf(getLanguage().getCode()), String.valueOf(volume)},
          null, null, "number");
      if (!cursor.moveToLast()) {
        return 0;
      }
      return cursor.getInt(0);
    } catch (Exception e) {
      Log.e(TAG, "getMaximumPageNumber failed for volume=" + volume, e);
      return 0;
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  private int getFirstPageId(int volume) {
    openDatabase();
    if (db == null) {
      return -1;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
          new String[] { String.valueOf(getLanguage().getCode()), String.valueOf(volume)},
          null, null, null);
      if (!cursor.moveToFirst()) {
        return -1;
      }
      return cursor.getInt(0);
    } catch (Exception e) {
      Log.e(TAG, "getFirstPageId failed for volume=" + volume, e);
      return -1;
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  private int getLastPageId(int volume) {
    openDatabase();
    if (db == null) {
      return -1;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
          new String[] { String.valueOf(getLanguage().getCode()), String.valueOf(volume)},
          null, null, null);
      if (!cursor.moveToLast()) {
        return -1;
      }
      return cursor.getInt(0);
    } catch (Exception e) {
      Log.e(TAG, "getLastPageId failed for volume=" + volume, e);
      return -1;
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  @Override
  public int getMaximumItemNumber(int volume) {
    openDatabase();
    if (db == null) {
      return 1;
    }
    int pageId = getLastPageId(volume);
    if (pageId < 0) {
      return 1;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("item", new String[] {"number"}, "page_id = ?",
          new String[] {String.valueOf(pageId)},
          null, null, null);
      if (!cursor.moveToFirst()) {
        return 1;
      }
      return cursor.getInt(0);
    } catch (Exception e) {
      Log.e(TAG, "getMaximumItemNumber failed for volume=" + volume, e);
      return 1;
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
    int pageId = getFirstPageId(volume);
    if (pageId < 0) {
      return 1;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("item", new String[] {"number"}, "page_id = ?",
          new String[] {String.valueOf(pageId)},
          null, null, null);
      if (!cursor.moveToFirst()) {
        return 1;
      }
      return cursor.getInt(0);
    } catch (Exception e) {
      Log.e(TAG, "getMinimumItemNumber failed for volume=" + volume, e);
      return 1;
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  private String getPagesTuple(int volume) {
    openDatabase();
    if (db == null) {
      return "()";
    }
    Cursor cursor = null;
    try {
      cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
          new String[] { String.valueOf(getLanguage().getCode()), String.valueOf(volume)},
          null, null, null);
      StringBuilder sb = new StringBuilder();
      sb.append('(');
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          sb.append(cursor.getInt(0));
          if (!cursor.isLast()) {
            sb.append(',');
          }
          cursor.moveToNext();
        }
      }
      sb.append(')');
      return sb.toString();
    } catch (Exception e) {
      Log.e(TAG, "getPagesTuple failed for volume=" + volume, e);
      return "()";
    } finally {
      if (cursor != null) cursor.close();
    }
  }

  private Integer[] getPageIdsByItem(int volume, int item) {
    openDatabase();
    if (db == null) {
      return new Integer[0];
    }
    ArrayList<Integer> pageIds = new ArrayList<Integer>();
    Cursor cursor = null;
    try {
      cursor = db.query(true, "item", new String[] {"page_id"},
          "page_id IN " + getPagesTuple(volume) + " AND start = 1 AND number = ?",
          new String[] { String.valueOf(item) }, null, null, "page_id", null);
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          pageIds.add(cursor.getInt(0));
          cursor.moveToNext();
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "getPageIdsByItem failed for volume=" + volume + ", item=" + item, e);
    } finally {
      if (cursor != null) cursor.close();
    }
    return pageIds.toArray(new Integer[pageIds.size()]);
  }

  @Override
  public int getPageIdByItem(int volume, int item, int section) {
    openDatabase();
    if (db == null) {
      return -1;
    }
    Cursor cursor = null;
    try {
      cursor = db.query(true, "item", new String[] {"page_id"},
          "page_id IN " + getPagesTuple(volume) + " AND start = 1 AND number = ? AND section = ?",
          new String[] { String.valueOf(item), String.valueOf(section) }, null, null, "page_id", null);
      if (cursor.moveToFirst()) {
        return cursor.getInt(0);
      }
    } catch (Exception e) {
      Log.e(TAG, "getPageIdByItem failed for volume=" + volume + ", item=" + item, e);
    } finally {
      if (cursor != null) cursor.close();
    }
    return -1;
  }

  @Override
  public int getPageById(int pageId) {
    openDatabase();
    if (db == null) {
      return -1;
    }
    Cursor cursor = null;
    try {
      cursor = db.query("page", new String[] {"number"}, "_id = ?",
          new String[] { String.valueOf(pageId) }, null, null, null);
      if (cursor.moveToFirst()) {
        return cursor.getInt(0);
      }
    } catch (Exception e) {
      Log.e(TAG, "getPageById failed for pageId=" + pageId, e);
    } finally {
      if (cursor != null) cursor.close();
    }
    return -1;
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    openDatabase();
    if (db == null) {
      return new Integer[0];
    }
    Integer[] pageIds = getPageIdsByItem(volume, item);
    ArrayList<Integer> pages = new ArrayList<Integer>();
    for (int pageId : pageIds) {
      Cursor cursor = null;
      try {
        cursor = db.query("page", new String[] {"number"}, "_id = ?",
            new String[] {String.valueOf(pageId)}, null, null, "number");
        if (cursor.moveToFirst()) {
          pages.add(cursor.getInt(0));
        }
      } catch (Exception e) {
        Log.e(TAG, "getPagesByItem failed for volume=" + volume + ", item=" + item + ", pageId=" + pageId, e);
      } finally {
        if (cursor != null) cursor.close();
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
          String selection = "language = ? AND volume = ?";
          ArrayList<String> selectionArgs = new ArrayList<String>();
          selectionArgs.add(String.valueOf(getLanguage().getCode()));
          selectionArgs.add(String.valueOf(volume));

          for (String keyword : keywords.split("\\s+")) {
            selection += " AND content LIKE ?";
            selectionArgs.add("%" + keyword.replace('+', ' ') + "%");
          }

          Cursor cursor = db.query("page", null, selection, selectionArgs.toArray(new String[selectionArgs.size()]),
              null, null, null);

          if (listener != null) {
            listener.onSearchProgress(keywords, volume, i+1, cursor);
          }

          if (volume >= 1 && volume <= 8) {
            totalPages[0] += cursor.getCount();
          } else if (volume >=9 && volume <= 33) {
            totalPages[1] += cursor.getCount();
          } else {
            totalPages[2] += cursor.getCount();
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

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener,
        new Integer[] {
            1,2,3,4,5,6,7,8,9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45
        }
    );
  }

  @Override
  public String getContentColumn() {
    return "content";
  }

  @Override
  public String getPageNumberColumn() {
    return "number";
  }

  @Override
  public int getSectionBoundary(int index) {
    if (index == 0) {
      return 8;
    }
    if (index == 1) {
      return 33;
    }
    return 45;
  }

  @Override
  public int getTotalVolumes() {
    return 45;
  }
}
