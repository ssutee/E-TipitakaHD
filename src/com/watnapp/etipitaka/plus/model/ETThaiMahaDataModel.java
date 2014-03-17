package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sutee on 19/2/14.
 */
public abstract class ETThaiMahaDataModel extends ETDataModel {
  public ETThaiMahaDataModel(Context context) {
    super(context);
  }

  @Override
  public void getItemsAtPage(final int volume, final int page, final BookDatabaseHelper.OnGetItemsListener listener) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        Cursor cursor = db.query(getLanguage().getStringCode(), null, "volume=? AND page=?",
            new String[]{String.format("%02d", volume), String.format("%04d", page)}, null, null, null);
        cursor.moveToFirst();
        String[] tokens = cursor.getString(cursor.getColumnIndex("items")).split("\\s+");
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
    }).start();
  }

  @Override
  public Cursor read(int volume, int page) {
    openDatabase();
    Cursor cursor = db.query(getLanguage().getStringCode(), null, "volume=?",
        new String[] { String.format("%02d", volume) }, null, null, null);
    cursor.moveToFirst();
    if (page > 0 && page <= cursor.getCount()) {
      cursor.moveToPosition(page-1);
    }
    return cursor;
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
    Cursor cursor = db.query(getLanguage().getStringCode(), null, "volume = ?",
       new String[] { String.format("%02d", volume) }, null, null, "page");
    int page = cursor.getCount();
    cursor.close();
    return page;
  }

  @Override
  public int getMinimumItemNumber(int volume) {
    openDatabase();
    Cursor cursor = db.query(getLanguage().getStringCode(), null, "volume = ?",
        new String[] { String.format("%02d", volume) }, null, null, "page");
    cursor.moveToFirst();
    String[] items = cursor.getString(cursor.getColumnIndex("items")).split("\\s+");
    cursor.close();
    return Integer.parseInt(items[0]);
  }

  @Override
  public int getMaximumItemNumber(int volume) {
    openDatabase();
    Cursor cursor = db.query(getLanguage().getStringCode(), null, "volume = ?",
        new String[] { String.format("%02d", volume) }, null, null, "page");
    cursor.moveToFirst();
    int maxItem = 0;
    while (!cursor.isAfterLast()) {
      String[] items = cursor.getString(cursor.getColumnIndex("items")).split("\\s+");
      for (int i=0; i<items.length; ++i) {
        maxItem = Math.max(maxItem, Integer.parseInt(items[i]));
      }
      cursor.moveToNext();
    }
    cursor.close();
    return maxItem;
  }

  abstract protected Map<String,Map<String,Map<String,ArrayList<Integer>>>> getBookItems();

  @Override
  public int getPageIdByItem(int volume, int item, int section) {
    openDatabase();
    int page = getBookItems().get(volume + "").get(section+"").get(item+"").get(0);
    Cursor cursor = db.query(getLanguage().getStringCode(), null, "volume=? AND page=?",
        new String[] {String.format("%02d", volume), String.format("%04d", page) }, null, null, null);
    cursor.moveToFirst();
    int pageId = cursor.getInt(cursor.getColumnIndex("_id"));
    cursor.close();
    return pageId;
  }

  @Override
  public int getPageById(int pageId) {
    openDatabase();
    Cursor cursor = db.query(getLanguage().getStringCode(), null, "_id = ?", new String[] {String.valueOf(pageId)}, null, null, null);
    cursor.moveToFirst();
    int page = cursor.getInt(cursor.getColumnIndex("page"));
    cursor.close();
    return page;
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    ArrayList<Integer> pages = new ArrayList<Integer>();
    for (String section : getBookItems().get(volume + "").keySet()) {
      if (getBookItems().get(volume + "").get(section).containsKey(item+"")) {
        pages.add(getBookItems().get(volume + "").get(section).get(item + "").get(0));
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
          selectionArgs.add(String.format("%02d", volume));

          for (String keyword : keywords.split("\\s+")) {
            selection += " AND content LIKE ?";
            selectionArgs.add("%" + keyword.replace('+', ' ') + "%");
          }

          Cursor cursor = db.query(getLanguage().getStringCode(), null, selection, selectionArgs.toArray(new String[selectionArgs.size()]),
              null, null, null);

          if (listener != null) {
            listener.onSearchProgress(keywords, volume, i+1, cursor);
          }

          if (volume >= 1 && volume <= getSectionBoundary(0)) {
            totalPages[0] += cursor.getCount();
          } else if (volume >= getSectionBoundary(0)+1 && volume <= getSectionBoundary(1)) {
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
}
