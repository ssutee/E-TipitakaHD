package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by sutee on 20/3/58.
 */
abstract public class DictDatabaseHelper extends SQLiteAssetHelper {

  protected SQLiteDatabase db;

  protected Context mContext;


  public abstract Cursor doQueryHeadWords(String selection, String[] selectionArgs);
  public abstract Cursor doQueryContentById(int id);


  public DictDatabaseHelper(Context context, String name, int version) {
    super(context, name, null, version);
  }

  public void openDatabase() {
    if ((db == null || !db.isOpen())) {
      db = getReadableDatabase();
    }
  }

  public void closeDatabase() {
    if (db != null && db.isOpen()) {
      db.close();
    }
  }

  public Cursor queryHeadWords(String selection, String[] selectionArgs) {
    openDatabase();
    return doQueryHeadWords(selection, selectionArgs);
  }

  public String getContentById(int id) {
    openDatabase();
    Cursor cursor = doQueryContentById(id);
    cursor.moveToFirst();
    String content = cursor.getString(0);
    cursor.close();
    return content;
  }

}
