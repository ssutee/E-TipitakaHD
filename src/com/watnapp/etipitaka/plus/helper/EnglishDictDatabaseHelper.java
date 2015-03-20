package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.database.Cursor;
import com.google.inject.Inject;

/**
 * Created by sutee on 20/3/58.
 */
public class EnglishDictDatabaseHelper extends DictDatabaseHelper {
  private static final String DATABASE_NAME = "engdict";
  private static final int DATABASE_VERSION = 1;

  @Inject
  public EnglishDictDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, DATABASE_VERSION);
  }

  @Override
  public Cursor doQueryHeadWords(String selection, String[] selectionArgs) {
    return db.query("english", new String[] { "_id", "head" }, selection, selectionArgs, null, null, "head");
  }

  @Override
  public Cursor doQueryContentById(int id) {
    return db.query("english", new String[]{ "translation" }, "_id = ?",
        new String[]{ String.valueOf(id) }, null, null, null);
  }
}
