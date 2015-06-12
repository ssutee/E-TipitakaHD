package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
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

  public ETThaiWatnaDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(BookDatabaseHelper.Language.THAIWN);
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
        Cursor cursor = db.query("main", null, "volume=? AND page=?",
            new String[]{String.valueOf(volume), String.valueOf(page)}, null, null, null);
        if (cursor.getCount() == 0) {
          listener.onGetItemsFinish(null, null);
        } else {
          cursor.moveToFirst();
          String itemsColumn = cursor.getString(cursor.getColumnIndex("items"));
          if (itemsColumn.trim().length() == 0) {
            listener.onGetItemsFinish(null, null);
          } else {
            String[] tokens = itemsColumn.split("\\s+");
            ArrayList<Integer> items = new ArrayList<Integer>();
            for (int i=0; i<tokens.length; ++i) {
              items.add(Integer.parseInt(tokens[i]));
            }
            int section = BookDatabaseHelper.getSubItem(mContext, getLanguage(), volume, page, Integer.parseInt(tokens[0]));
            ArrayList<Integer> sections = new ArrayList<Integer>();
            for (int i=0; i<tokens.length; ++i) {
              sections.add(section);
            }
            listener.onGetItemsFinish(items.toArray(new Integer[items.size()]), sections.toArray(new Integer[sections.size()]));
          }
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
    if (BookDatabaseHelper.getThaiWNBookItems(mContext).get(volume + "") == null) {
      return 0;
    }
    int page = BookDatabaseHelper.getThaiWNBookItems(mContext).get(volume + "").get(section+"").get(item+"").get(0);
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
    Map<String, Map<String, Map<String, ArrayList<Integer>>>> bookItems = BookDatabaseHelper.getThaiWNBookItems(mContext);
    ArrayList<Integer> pages = new ArrayList<Integer>();
    for (String section : bookItems.get(volume + "").keySet()) {
      if (bookItems.get(volume + "").get(section).containsKey(item+"")) {
        pages.add(bookItems.get(volume + "").get(section).get(item + "").get(0));
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
