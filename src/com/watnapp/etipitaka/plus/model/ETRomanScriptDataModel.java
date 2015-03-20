package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.util.Log;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sutee on 16/12/14.
 */
public class ETRomanScriptDataModel extends ETDataModel {

  protected static final String TAG = "ETRomanScriptDataModel";

  public ETRomanScriptDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Constants.CT_DATABASE_PATH;
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.ROMANCT;
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
    Map<String, Map<String, ArrayList<ArrayList<Integer>>>> romanItems = BookDatabaseHelper.getRomanItems(mContext);

    ArrayList<Integer> items = new ArrayList<Integer>();
    ArrayList<Integer> sections = new ArrayList<Integer>();

    if (romanItems.get(volume+"") != null && romanItems.get(volume+"").get(page+"") != null) {
      for (ArrayList<Integer> pair : romanItems.get(volume+"").get(page+"")) {
        items.add(pair.get(0));
        sections.add(pair.get(1));
      }
    }

    listener.onGetItemsFinish(items.toArray(new Integer[items.size()]), sections.toArray(new Integer[sections.size()]));
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    getItemsAtPage(volume, page, listener);
  }

  @Override
  public Cursor read(int volume, int page) {
    openDatabase();
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
    Cursor cursor = db.query("main", null, "volume = ?",
        new String[] { String.valueOf(volume) }, null, null, "page");
    int page = cursor.getCount();
    cursor.close();
    return page;

  }

  @Override
  public int getMinimumItemNumber(int volume) {
    openDatabase();
    Cursor cursor = db.query("main", null, "volume = ?",
        new String[] { String.valueOf(volume) }, null, null, "page");
    cursor.moveToFirst();
    String[] items = cursor.getString(cursor.getColumnIndex("items")).split("\\s+");
    cursor.close();
    return Integer.parseInt(items[0]);
  }

  @Override
  public int getMaximumItemNumber(int volume) {
    openDatabase();
    Cursor cursor = db.query("main", null, "volume = ?",
        new String[] { String.valueOf(volume) }, null, null, "page");
    cursor.moveToFirst();
    int maxItem = 0;
    while (!cursor.isAfterLast()) {
      String[] items = cursor.getString(cursor.getColumnIndex("items")).split("\\s+");
      for (int i=0; i<items.length; ++i) {
        if (items[i].length() > 0) {
          maxItem = Math.max(maxItem, Integer.parseInt(items[i]));
        }
      }
      cursor.moveToNext();
    }
    cursor.close();
    return maxItem;
  }

  @Override
  public int getPageIdByItem(int volume, int item, int section) {
    openDatabase();
    if (BookDatabaseHelper.getRomanPageIndex(mContext).get(volume + "") == null) {
      return 0;
    }
    int page = BookDatabaseHelper.getRomanPageIndex(mContext).get(volume + "").get(item+"").get(section+"");
    Cursor cursor = db.query("main", null, "volume=? AND page=?",
        new String[] {String.valueOf(volume), String.valueOf(page) }, null, null, null);
    cursor.moveToFirst();
    int pageId = cursor.getInt(cursor.getColumnIndex("_id"));
    cursor.close();
    return pageId;
  }

  @Override
  public int getPageById(int pageId) {
    openDatabase();
    Cursor cursor = db.query("main", null, "_id = ?", new String[] {String.valueOf(pageId)}, null, null, null);
    if (cursor.getCount() == 0) {
      cursor.close();
      return 0;
    }
    cursor.moveToFirst();
    int page = cursor.getInt(cursor.getColumnIndex("page"));
    cursor.close();
    return page;
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    Map<String, Map<String, Map<String, Integer>>> pageIndex = BookDatabaseHelper.getRomanPageIndex(mContext);
    ArrayList<Integer> pages = new ArrayList<Integer>();
    if (pageIndex.get(volume+"") != null && pageIndex.get(volume+"").get(item+"") != null) {
      for (String section : pageIndex.get(volume+"").get(item+"").keySet()) {
        int page = pageIndex.get(volume+"").get(item+"").get(section);
        pages.add(page);
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
        Cursor[] cursors = new Cursor[volumes.length+1];
        int totalPages[] = new int[3];
        for (int i=0; i < volumes.length; ++i) {
          int volume = volumes[i];
          String selection = "volume = ?";
          ArrayList<String> selectionArgs = new ArrayList<String>();
          selectionArgs.add(String.format("%d", volume));

          for (String keyword : keywords.split("\\s+")) {
            selection += " AND content LIKE ?";
            selectionArgs.add("%" + keyword.replace('+', ' ') + "%");
          }

          Cursor cursor = db.query("main", null, selection, selectionArgs.toArray(new String[selectionArgs.size()]),
              null, null, null);

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

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener,
        new Integer[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45,46,47,48,49,50,
            51,52,53,54,55,56,57,58,59,60,
            61
        }
    );

  }

  @Override
  public int getSectionBoundary(int index) {
    if (index == 0) {
      return 5;
    }
    if (index == 1) {
      return 48;
    }
    return 61;
  }

  @Override
  public int getTotalVolumes() {
    return 61;
  }

  @Override
  public void convertToPivot(int volume, int page, int item, BookDatabaseHelper.OnConvertToPivotListener listener) {
    Map<String, Map<String, ArrayList<ArrayList<Integer>>>> table = BookDatabaseHelper.getRomanMappingTable(mContext);
    ArrayList<ArrayList<Integer>> results = table.get(volume + "").get(page + "");
    if (table.get(volume+"") == null || table.get(volume+"").get(page+"") == null) {
      listener.onConvertToPivotFinish(volume, 1, 1);
    } else {
      int rVolume = results.get(0).get(0);
      int rItem = results.get(0).get(1);
      int rSection = results.get(0).get(2);
      listener.onConvertToPivotFinish(rVolume, rItem, rSection);
    }
  }

  @Override
  public void convertFromPivot(int volume, int item, int section, BookDatabaseHelper.OnConvertFromPivotListener listener) {
    Map<String, Map<String, Map<String, ArrayList<Integer>>>> table = BookDatabaseHelper.getRomanReverseMappingTable(mContext);
    if (table.get(volume+"") == null || table.get(volume+"").get(item+"") == null
        || table.get(volume+"").get(item+"").get(section+"") == null) {
      listener.onConvertFromPivotFinish(volume, 1);
    } else {
      ArrayList<Integer> result = table.get(volume + "").get(item + "").get(section + "");
      listener.onConvertFromPivotFinish(result.get(0), result.get(1));
    }
  }

  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.romanct_short_name);
  }

}
