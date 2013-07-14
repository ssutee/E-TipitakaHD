package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 0:31
 */

@Singleton
public class BookDatabaseHelper {

  protected static final String TAG = "DatabaseHelper";
  private SQLiteDatabase db;

  @Inject
  private Context mContext;

  public void openDatabase() {
    if ((db == null || !db.isOpen()) && (new File(Constants.DATABASE_PATH)).exists()) {
      db = SQLiteDatabase.openDatabase(Constants.DATABASE_PATH, null, 0);
    }
  }

  public void closeDatabase() {
    if (db != null && db.isOpen()) {
      db.close();
    }
  }

  private int getPageId(Language language, int volume, int page) {
    openDatabase();
    int pageId = -1;
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ? AND number = ?",
        new String[] {String.valueOf(language.getCode()), String.valueOf(volume), String.valueOf(page)},
        null, null, null);
    if (cursor.getCount() > 0) {
      cursor.moveToFirst();
      pageId = cursor.getInt(0);
    }
    cursor.close();
    return pageId;
  }

  public void getItemsAtPage(final Language language, final int volume, final int page,
                             final OnGetItemsListener listener) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        ArrayList<Integer> items = new ArrayList<Integer>();
        ArrayList<Integer> sections = new ArrayList<Integer>();
        int pageId = getPageId(language, volume, page);
        Cursor cursor = db.query("item", new String[] {"number", "section"},
            "page_id = ?", new String[] {String.valueOf(pageId)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
          int number = cursor.getInt(0);
          int section = cursor.getInt(1);
          if (!items.contains(number)) {
            items.add(number);
            sections.add(section);
          }
          cursor.moveToNext();
        }
        cursor.close();
        listener.onGetItemsFinish(items.toArray(new Integer[items.size()]),
            sections.toArray(new Integer[sections.size()]));
      }
    }).start();
  }

  public Cursor read(Language language, int volume, int page) {
    openDatabase();
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

  public int getMinimumPageNumber(Language language, int volume) {
    return 1;
  }

  public int getMaximumPageNumber(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"number"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, "number");
    cursor.moveToLast();
    int page = cursor.getInt(0);
    cursor.close();
    return page;
  }

  private int getFirstPageId(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, null);
    cursor.moveToFirst();
    int id = cursor.getInt(0);
    cursor.close();
    return id;
  }

  private int getLastPageId(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, null);
    cursor.moveToLast();
    int id = cursor.getInt(0);
    cursor.close();
    return id;
  }

  public int getMinimumItemNumber(Language language, int volume) {
    openDatabase();
    int pageId = getFirstPageId(language, volume);
    Cursor cursor = db.query("item", new String[] {"number"}, "page_id = ?",
        new String[] {String.valueOf(pageId)},
        null, null, null);
    cursor.moveToFirst();
    int item = cursor.getInt(0);
    cursor.close();
    return item;
  }

  public int getMaximumItemNumber(Language language, int volume) {
    openDatabase();
    int pageId = getLastPageId(language, volume);
    Cursor cursor = db.query("item", new String[] {"number"}, "page_id = ?",
        new String[] {String.valueOf(pageId)},
        null, null, null);
    cursor.moveToFirst();
    int item = cursor.getInt(0);
    cursor.close();
    return item;
  }

  private String getPagesTuple(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, null);
    StringBuilder sb = new StringBuilder();
    cursor.moveToFirst();
    sb.append('(');
    while(!cursor.isAfterLast()) {
      sb.append(cursor.getInt(0));
      if (!cursor.isLast()) {
        sb.append(',');
      }
      cursor.moveToNext();
    }
    sb.append(')');
    cursor.close();
    return sb.toString();
  }

  private Integer[] getPageIdsByItem(Language language, int volume, int item) {
    openDatabase();
    Cursor cursor = db.query(true, "item", new String[] {"page_id"},
        "page_id IN " + getPagesTuple(language, volume) + " AND start = 1 AND number = ?",
        new String[] { String.valueOf(item) }, null, null, "page_id", null);
    ArrayList<Integer> pageIds = new ArrayList<Integer>();
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      pageIds.add(cursor.getInt(0));
      cursor.moveToNext();
    }
    cursor.close();
    return pageIds.toArray(new Integer[pageIds.size()]);
  }

  public int getPageIdByItem(Language language, int volume, int item, int section) {
    openDatabase();
    Cursor cursor = db.query(true, "item", new String[] {"page_id"},
        "page_id IN " + getPagesTuple(language, volume) + " AND start = 1 AND number = ? AND section = ?",
        new String[] { String.valueOf(item), String.valueOf(section) }, null, null, "page_id", null);
    int pageId = -1;
    if (cursor.getCount() > 0) {
      cursor.moveToFirst();
      pageId = cursor.getInt(0);
    }
    cursor.close();
    return pageId;
  }

  public int getPageById(int pageId) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"number"}, "_id = ?",
        new String[] { String.valueOf(pageId) }, null, null, null);
    int page = -1;
    if (cursor.getCount() > 0) {
      cursor.moveToFirst();
      page = cursor.getInt(0);
    }
    cursor.close();
    return page;
  }

  public Integer[] getPagesByItem(Language language, int volume, int item) {
    openDatabase();
    Integer[] pageIds = getPageIdsByItem(language, volume, item);
    ArrayList<Integer> pages = new ArrayList<Integer>();
    for (int pageId : pageIds) {
      Cursor cursor = db.query("page", new String[] {"number"}, "_id = ?",
          new String[] {String.valueOf(pageId)}, null, null, "number");
      cursor.moveToFirst();
      pages.add(cursor.getInt(0));
      cursor.close();
    }
    return pages.toArray(new Integer[pages.size()]);
  }

  public void search(final Language language, final String keywords, final OnSearchListener listener,
                     final Integer[] volumes) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        Cursor[] cursors = new Cursor[volumes.length+1];
        int totalPages[] = new int[3];
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

  public void search(Language language, String keywords, OnSearchListener listener) {
    search(language, keywords, listener,
        new Integer[] {
            1,2,3,4,5,6,7,8,9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45
        }
    );
  }

  public interface OnGetItemsListener {
    public void onGetItemsFinish(Integer[] items, Integer[] sections);
  }

  public interface OnSearchListener {

    public void onSearchProgress(String keywords, int volume, int progress, Cursor cursor);
    public void onSearchFinish(String keywords, Cursor cursor, int[] totalPages);

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

    public String getFullName(Context context) {
      return context.getString(code == 0 ? R.string.thai_full_name : R.string.pali_full_name);
    }
  }

}
