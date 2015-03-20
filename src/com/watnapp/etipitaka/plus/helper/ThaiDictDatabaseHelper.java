package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.database.Cursor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by sutee on 20/3/58.
 */

@Singleton
public class ThaiDictDatabaseHelper extends DictDatabaseHelper {

  private static final String DATABASE_NAME = "thaidict";
  private static final int DATABASE_VERSION = 1;

  @Inject
  public ThaiDictDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, DATABASE_VERSION);
    mContext = context;
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
