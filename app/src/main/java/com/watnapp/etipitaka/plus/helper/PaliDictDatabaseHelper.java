package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.database.Cursor;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 15/7/2013
 * Time: 13:41
  */

public class PaliDictDatabaseHelper extends DictDatabaseHelper {

  private static final String DATABASE_NAME = "p2t_dict";
  private static final int DATABASE_VERSION = 1;

  public PaliDictDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, DATABASE_VERSION);
  }

  @Override
  public Cursor doQueryHeadWords(String selection, String[] selectionArgs) {
    return db.query("p2t", new String[] { "_id", "headword" }, selection, selectionArgs, null, null, "headword");
  }

  @Override
  public Cursor doQueryContentById(int id) {
    return db.query("p2t", new String[]{ "content" }, "_id = ?",
        new String[]{ String.valueOf(id) }, null, null, null);
  }

  @Override
  public String prepareQueryString(String query) {
    return query.replace("\u0e0d", "\uf70f").replace("\u0e4d", "\uf711")
        .replace("\u0e10", "\uf700").replace("\u0e0d", "\uf70f").replace("\u0e4d", "\uf711");
  }
}
