package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.watnapp.etipitaka.plus.Constants;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 15/7/2013
 * Time: 13:41
  */

@Singleton
public class PaliDictDatabaseHelper extends SQLiteAssetHelper {

  private static final String DATABASE_NAME = "p2t_dict";
  private static final int DATABASE_VERSION = 1;

  private SQLiteDatabase db;

  private Context mContext;

  @Inject
  public PaliDictDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    mContext = context;
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
    return db.query("p2t", new String[] { "_id", "headword" }, selection, selectionArgs, null, null, "headword");
  }

  public String getContentById(int id) {
    openDatabase();
    Cursor cursor = db.query("p2t", new String[]{ "content" }, "_id = ?",
        new String[]{ String.valueOf(id) }, null, null, null);
    cursor.moveToFirst();
    String content = cursor.getString(0);
    cursor.close();
    return content;
  }
}
