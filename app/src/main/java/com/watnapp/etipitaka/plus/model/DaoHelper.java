package com.watnapp.etipitaka.plus.model;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;


public abstract class DaoHelper<T extends ModelBase> {

  protected Context mContext;

  protected abstract Dao<T> getDao();

  public DaoHelper(Context context) {
    mContext = context;
  }

  public int getCount() {
    return getDao().size();
  }

  public T get(int position) {
    return getDao().get(position);
  }

  public int insert(T object) {
    Uri uri = getDao().insert(object);
    try {
      if (uri.getLastPathSegment() != null) {
        object.setId(Integer.valueOf(uri.getLastPathSegment()));
        return Integer.valueOf(uri.getLastPathSegment());
      }
    } catch (NumberFormatException e) {
    }
    return -1;
  }

  public void delete(String selection, String[] selectionArgs) {
    getDao().delete(selection, selectionArgs);
  }

  public void delete(T object) {
    getDao().delete(object);
  }

  public void update(ContentValues values, String selection, String[] selectionArgs) {
    getDao().update(values, selection, selectionArgs);
  }

  public void update(T object) {
    getDao().update(object);
  }

  public T getById(int id) {
    return getDao().getById(id);
  }

  public List<T> get(String selection, String[] selectionArgs) {
    return getDao().get(selection, selectionArgs);
  }
}
