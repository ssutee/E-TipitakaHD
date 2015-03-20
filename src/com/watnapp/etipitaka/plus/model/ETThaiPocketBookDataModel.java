package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;

/**
 * Created by sutee on 12/12/14.
 */
public class ETThaiPocketBookDataModel extends ETDataModel {

  public ETThaiPocketBookDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Constants.PB_DATABASE_PATH;
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAIPB;
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
    listener.onGetItemsFinish(null, null);
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    listener.onGetItemsFinish(null, null);
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
    return 0;
  }

  @Override
  public int getMaximumItemNumber(int volume) {
    return 0;
  }

  @Override
  public int getPageIdByItem(int volume, int item, int section) {
    return 0;
  }

  @Override
  public int getPageById(int pageId) {
    openDatabase();
    Cursor cursor = db.query("main", null, "_id = ?",
        new String[] {String.valueOf(pageId)}, null, null, null);
    cursor.moveToFirst();
    int page = cursor.getInt(cursor.getColumnIndex("page"));
    cursor.close();
    return page;
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    return null;
  }

  @Override
  public boolean hasHtmlContent() {
    return true;
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
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13
    });
  }

  @Override
  public int getSectionBoundary(int index) {
    return 13;
  }

  @Override
  public int getTotalVolumes() {
    return 13;
  }


  @Override
  public void convertToPivot(int volume, int page, int item, BookDatabaseHelper.OnConvertToPivotListener listener) {
    listener.onConvertToPivotFinish(1,1,1);
  }

  @Override
  public void convertFromPivot(int volume, int item, int section, BookDatabaseHelper.OnConvertFromPivotListener listener) {
    listener.onConvertFromPivotFinish(1,1);
  }

  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.thaipb_short_name);
  }

}
