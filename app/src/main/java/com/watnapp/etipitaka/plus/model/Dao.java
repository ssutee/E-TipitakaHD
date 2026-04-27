package com.watnapp.etipitaka.plus.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Dao<T extends ModelBase> {

  private final Context mContext;
  private final Uri mContentUri;
  private final Class<T> mClazz;
  private final String mSelection;
  private final String mSortOrder;

  public Dao(Class<T> clazz, Context context, Uri contentUri,
             String selection, String sortOrder) {
    mContext = context;
    mContentUri = contentUri;
    mClazz = clazz;
    mSelection = selection;
    mSortOrder = sortOrder;
  }

  public Dao(Class<T> clazz, Context context, Uri contentUri, String selection) {
    this(clazz, context, contentUri, selection, null);
  }

  public Dao(Class<T> clazz, Context context, Uri contentUri) {
    this(clazz, context, contentUri, null);
  }

  public T getById(int id) {
    try (Cursor cursor = mContext.getContentResolver().query(
        Uri.withAppendedPath(mContentUri, String.valueOf(id)), null,
        mSelection, null, mSortOrder)) {
      if (cursor == null || cursor.getCount() == 0) {
        return null;
      }

      cursor.moveToFirst();
      return createFromCursor(cursor);
    }
  }

  public T get(int position) {
    try (Cursor cursor = mContext.getContentResolver().query(mContentUri, null,
        mSelection, null, mSortOrder)) {
      if (cursor == null || !cursor.moveToPosition(position)) {
        return null;
      }
      return createFromCursor(cursor);
    }
  }

  public List<T> get(String selection, String[] selectionArgs) {
    ArrayList<T> results = new ArrayList<T>();
    String mergedSelection = mergeSelection(selection);
    try (Cursor cursor = mContext.getContentResolver().query(mContentUri, null,
        mergedSelection, selectionArgs, mSortOrder)) {
      if (cursor == null || !cursor.moveToFirst()) {
        return results;
      }

      do {
        results.add(createFromCursor(cursor));
      } while (cursor.moveToNext());
    }
    return results;
  }

  public int size() {
    try (Cursor cursor = mContext.getContentResolver().query(mContentUri, null,
        mSelection, null, null)) {
      return cursor == null ? 0 : cursor.getCount();
    }
  }

  public Uri insert(T object) {
    return mContext.getContentResolver().insert(mContentUri,
        object.toContentValues());
  }

  public int update(T object) {
    return mContext.getContentResolver().update(
        Uri.withAppendedPath(mContentUri, String.valueOf(object.getId())),
        object.toContentValues(), null, null);
  }

  public int update(ContentValues values, String selection, String[] selectionArgs) {
    return mContext.getContentResolver().update(mContentUri, values,
        selection, selectionArgs);
  }

  public int delete(T object) {
    return mContext.getContentResolver().delete(
        Uri.withAppendedPath(mContentUri, String.valueOf(object.getId())), null, null);
  }

  public void delete(String selection, String[] selectionArgs) {
    mContext.getContentResolver().delete(mContentUri, selection, selectionArgs);
  }

  public void destroy() {
    mContext.getContentResolver().delete(mContentUri, null, null);
  }

  private String mergeSelection(String selection) {
    if (selection != null && mSelection == null) {
      return selection;
    } else if (selection == null && mSelection != null) {
      return mSelection;
    } else if (selection != null) {
      return mSelection + " AND " + selection;
    }
    return null;
  }

  private T createFromCursor(Cursor cursor) {
    try {
      T object = mClazz.getDeclaredConstructor().newInstance();
      object.fromCursor(cursor, mContext);
      return object;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Unable to create model " + mClazz.getName(), e);
    }
  }
}
