package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sutee on 4/2/14.
 */

public abstract class ETDataModel {

  private static final String TAG = "ETDataModel";

  protected SQLiteDatabase db;
  protected Context mContext;

  abstract protected String getDatabasePath();
  abstract public BookDatabaseHelper.Language getLanguage();
  abstract public String getContentColumn();
  abstract public String getPageNumberColumn();
  abstract public String getShortTitle();

  abstract public void getItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener);
  abstract public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener);
  abstract public Cursor read(int volume, int page);
  abstract public int getMaximumPageNumber(int volume);
  abstract public int getMinimumItemNumber(int volume);
  abstract public int getMaximumItemNumber(int volume);
  abstract public int getPageIdByItem(int volume, int item, int section);
  abstract public int getPageById(int pageId);
  abstract public Integer[] getPagesByItem(int volume, int item);
  abstract public void search(String keywords, BookDatabaseHelper.OnSearchListener listener, Integer[] volumes);
  abstract public void search(String keywords, BookDatabaseHelper.OnSearchListener listener);
  abstract public int getSectionBoundary(int index);
  abstract public int getTotalVolumes();

  public ETDataModel(Context context) {
    this.mContext = context;
  }

  public void openDatabase() {
    if (db != null && db.isOpen()) {
      return;
    }
    String path = getDatabasePath();
    File file = new File(path);
    if (!file.exists()) {
      // DB file missing (never downloaded, cleared data, storage migration,
      // unmounted SD card). Leave db = null and let callers handle it.
      // Without this log, db.query NPEs further down the stack hid the real
      // cause — see ETHandbookDataModel.read crash report.
      Log.w(TAG, "openDatabase: database file does not exist at " + path);
      return;
    }
    try {
      db = SQLiteDatabase.openDatabase(path, null, 0);
    } catch (SQLiteException e) {
      // File exists but SQLite cannot open it. Causes seen in Play
      // Console crashes:
      //   - corrupt / partial download (zip extraction failed mid-write)
      //   - wrong format (placeholder file, 0-byte stub)
      //   - path is a directory rather than a file
      //   - external storage unmounted between exists() and openDatabase()
      // Leave db = null so callers' null-guards (see ETBasicDataModel.read
      // etc.) return an empty MatrixCursor instead of propagating the
      // SQLiteCantOpenDatabaseException up to FragmentManager and
      // crashing the activity on start.
      Log.e(TAG, "openDatabase: failed to open " + path, e);
      db = null;
    }
  }

  /**
   * @return {@code true} if the underlying database file exists on disk
   *         and looks like a plausible SQLite file (regular non-empty
   *         file). Callers should check this before invoking
   *         {@link #read} / {@link #search} etc. to avoid downstream
   *         NPEs / SQLiteCantOpenDatabaseException when the DB has not
   *         been downloaded, was partially downloaded, or was removed.
   */
  public boolean isAvailable() {
    File file = new File(getDatabasePath());
    return file.exists() && file.isFile() && file.length() > 0;
  }

  public void closeDatabase() {
    if (db != null && db.isOpen()) {
      db.close();
    }
  }

  public Cursor read(int volume) {
    return read(volume, 0);
  }

  public int getMinimumPageNumber(int volume) {
    return 1;
  }

  public String getVolumeColumn() {
    return "volume";
  }

  public int getVolume(Cursor cursor) {
    return Integer.parseInt(cursor.getString(cursor.getColumnIndex(getVolumeColumn())));
  }

  public int getPageNumber(Cursor cursor) {
    return Integer.parseInt(cursor.getString(cursor.getColumnIndex(getPageNumberColumn())));
  }

  public int convertVolume(int volume, int section, int item) {
    return volume;
  }

  public int getComparingVolume(int volume, int page) {
    return volume;
  }

  public boolean hasFooter() {
    return false;
  }

  public String getFooterColumn() {
    return null;
  }

  public Integer[] getPagesByItem(int volume, int item, boolean needConvertToSiamrat) {
    return getPagesByItem(volume, item);
  }

  public int getPageByItem(int volume, int item, int section, boolean needConvertToSiamrat) {
    return getPageById(getPageIdByItem(volume, item, section));
  }

  public boolean hasHtmlContent() {
    return false;
  }

  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener, Integer[] volumes, BookDatabaseHelper.SearchType searchType) {
    search(keywords, listener, volumes);
  }

  public void convertFromPivot(int volume, int item, int section, BookDatabaseHelper.OnConvertFromPivotListener listener) {
    int aVolume = convertVolume(volume, section, item);
    int page = getPageByItem(aVolume, item, section, true);
    listener.onConvertFromPivotFinish(aVolume, page);
  }

  public void convertToPivot(final int volume, final int page, final int item, final BookDatabaseHelper.OnConvertToPivotListener listener) {
    getComparingItemsAtPage(volume, page, new BookDatabaseHelper.OnGetItemsListener() {
      @Override
      public void onGetItemsFinish(Integer[] items, Integer[] sections) {
        int section = 1;
        boolean found = false;
        for (int i = 0; items != null && i < items.length; ++i) {
          if (items[i] == item) {
            section = sections[i];
            found = true;
            break;
          }
        }
        listener.onConvertToPivotFinish(getComparingVolume(volume, page), item, found ? section : 1);
      }
    });
  }

}
