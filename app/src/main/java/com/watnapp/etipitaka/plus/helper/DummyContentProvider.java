package com.watnapp.etipitaka.plus.helper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by sutee on 31/10/18.
 */
public class DummyContentProvider extends ContentProvider {

  public static final String AUTHORITY = "com.watnapp.etipitaka.plus.helper.dummyprovider";

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return null;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  public static Uri getLanguageChangeUri() {
    return Uri.parse("content://" + AUTHORITY + "/language_change");
  }

  public static Uri getResetPageUri() {
    return Uri.parse("content://" + AUTHORITY + "/reset_page");
  }

}
